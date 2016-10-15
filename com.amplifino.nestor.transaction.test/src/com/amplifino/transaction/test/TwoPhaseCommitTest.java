package com.amplifino.transaction.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
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


public class TwoPhaseCommitTest {

	private BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private List<DataSource> dataSources;
	private List<Connection> keepAliveConnections;
	private UserTransaction userTransaction;
	
	@Test
	public void test() throws SQLException, NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException, InterruptedException {
		publishH2();
		Thread.sleep(1000L);
		dataSources = getServices(DataSource.class);
		Assert.assertTrue(dataSources.size() > 1);
		keepAliveConnections = new ArrayList<>();
		for (DataSource source : dataSources) {
			keepAliveConnections.add(source.getConnection());
		}
		userTransaction = getService(UserTransaction.class);
		createTable();
		testRollback();
		testCommit();
	}
	
	private void createTable() throws SQLException {
		for (Connection connection : keepAliveConnections) {
			try (Statement statement = connection.createStatement()) {			
				statement.execute("create table test (name varchar(256))");
			}
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
		Assert.assertEquals( dataSources.size() * 2 , rowCount());
	}
	
	private int rowCount() throws SQLException {
		int result = 0;
		for (DataSource source : dataSources) {
			try (Connection connection = source.getConnection()) {
				Assert.assertTrue(connection.getAutoCommit());
				try (PreparedStatement statement = connection.prepareStatement("select count(*) from test")) {
					try (ResultSet resultSet = statement.executeQuery()) {
						resultSet.next();
						result += resultSet.getInt(1);
					}
				}
			}
		}	
		return result;
	}
	
	private void insertRow() throws SQLException {
		for (DataSource source : dataSources) {	
			try (Connection connection = source.getConnection()) {
				Assert.assertFalse(connection.getAutoCommit());
				try (PreparedStatement statement = connection.prepareStatement("insert into test values(?)")) {
					statement.setString(1,"Test");
					int count = statement.executeUpdate();
					Assert.assertEquals(1, count);
				}
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
	
	private <T> List<T> getServices(Class<T> clazz) {
		ServiceTracker<T,T> st = new ServiceTracker<>(context, clazz, null);
		st.open();
		return Arrays.stream(st.getServices()).map(clazz::cast).collect(Collectors.toList());
	}
	
	private void publishH2() {
		ConfigurationAdmin configurationAdmin = getService(ConfigurationAdmin.class);
		try {
			int databaseId  = 1;
			Configuration configuration = configurationAdmin.createFactoryConfiguration("com.amplifino.nestor.transaction.datasources", "?");
			configuration.update(properties(databaseId++));
			configuration = configurationAdmin.createFactoryConfiguration("com.amplifino.nestor.transaction.datasources", "?");
			configuration.update(properties(databaseId++));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Dictionary<String, Object> properties(int databaseId) {
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
		setProperty("url","jdbc:h2:mem:db" + databaseId, props, check);
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
