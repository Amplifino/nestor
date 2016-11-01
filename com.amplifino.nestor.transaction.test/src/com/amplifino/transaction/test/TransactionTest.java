package com.amplifino.transaction.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.osgi.util.tracker.ServiceTracker;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;


public class TransactionTest {

	private BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private DataSource dataSource;
	private UserTransaction userTransaction;
	
	@Test
	public void test() throws SQLException, NotSupportedException, SystemException, SecurityException, RollbackException, HeuristicMixedException, HeuristicRollbackException, IOException {
		Configuration config = publishH2();		
		dataSource = getService(DataSource.class);
		userTransaction = getService(UserTransaction.class);
		try (Connection connection = dataSource.getConnection()) {
			createTable(connection);
		}
		testRollback();
		testCommit();
		config.delete();
	}
	
	private void createTable(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("create table test (name varchar(256))");
		}
	}
	
	private void testRollback() throws SQLException, NotSupportedException, SystemException {
		userTransaction.begin();
		try {
			insertRow();
			insertRow();
		} finally {
			userTransaction.rollback();
		}
		Assert.assertEquals(0, rowCount());
	}
	
	private void testCommit() throws SQLException, NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		userTransaction.begin();
		boolean commit = false;
		try {
			insertRow();
			insertRow();
			commit = true;
		} finally {
			if (commit) {
				userTransaction.commit();
			} else {
				userTransaction.rollback();
			}
		}
		Assert.assertEquals(2, rowCount());
	}
	
	private int rowCount() throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			Assert.assertTrue(connection.getAutoCommit());
			try (PreparedStatement statement = connection.prepareStatement("select count(*) from test")) {
				try (ResultSet resultSet = statement.executeQuery()) {
					resultSet.next();
					return resultSet.getInt(1);
				}
			}
		}	
	}
	
	private void insertRow() throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			Assert.assertFalse(connection.getAutoCommit());
			try (PreparedStatement statement = connection.prepareStatement("insert into test values(?)")) {
				statement.setString(1,"Test");
				int count = statement.executeUpdate();
				Assert.assertEquals(1, count);
			}
		}
	}
	
	private <T> T getService(Class<T> clazz) {
		ServiceTracker<T,T> st = new ServiceTracker<>(context, clazz, null);
		st.open();
		try {
			return Objects.requireNonNull(st.waitForService(1000L));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Configuration publishH2() {
		ConfigurationAdmin configurationAdmin = getService(ConfigurationAdmin.class);
		try {
			Configuration configuration = configurationAdmin.createFactoryConfiguration("com.amplifino.nestor.transaction.datasources", "?");
			configuration.update(properties());
			return configuration;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Dictionary<String, Object> properties() {
		Bundle txBundle = Arrays.stream(context.getBundles())
			.filter(bundle -> "com.amplifino.nestor.transaction.datasources".equals(bundle.getSymbolicName()))
			.findFirst().get();
		MetaTypeInformation metaInfo = getService(MetaTypeService.class).getMetaTypeInformation(txBundle);
		ObjectClassDefinition definition = metaInfo.getObjectClassDefinition("com.amplifino.nestor.transaction.datasources", null);
		Set<String> check = Arrays.stream(definition.getAttributeDefinitions(ObjectClassDefinition.REQUIRED))			
			.map(AttributeDefinition::getID)
			.collect(Collectors.toSet());
		Dictionary<String, Object> props = new Hashtable<>();
		setProperty("dataSourceFactory.target", "(osgi.jdbc.driver.name=*)", props, check);
		setProperty("url","jdbc:h2:mem:db1", props, check);
		setProperty("user","user", props, check);
		setProperty(".password", "dummy", props, check);
		setProperty("dataSourceName", "H2", props, check);
		setProperty("application", new String[] {"test"}, props, check);
		setProperty("factoryMethod", "XADATASOURCE", props, check);
		return props;
	}
	
	private void setProperty(String key, Object value, Dictionary<String, Object> target, Set<String> check ) {
		Assert.assertTrue(check.contains(key));
		target.put(key, value);
	}
}
