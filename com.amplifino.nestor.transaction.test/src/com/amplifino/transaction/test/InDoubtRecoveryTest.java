package com.amplifino.transaction.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;

import com.amplifino.nestor.transaction.provider.spi.RecoveryService;


public class InDoubtRecoveryTest {

	private BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private TransactionManager transactionManager;
	private DataSourceFactory factory;
	private List<Connection> keepAlives = new ArrayList<>();
	private List<XADataSource> dataSources = new ArrayList<>();
	
	@Test
	public void test() throws SQLException, SecurityException, NotSupportedException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException, XAException {
		factory = getService(DataSourceFactory.class);
		dataSources.add(factory.createXADataSource(getProperties(1)));
		dataSources.add(factory.createXADataSource(getProperties(2)));
		for ( XADataSource ds : dataSources) {
			createTable(ds);
		}
		transactionManager = getService(TransactionManager.class);
		boolean rollback = false;
		try {
			testRollbackFailure();
		} catch (RollbackException e) {
			rollback = true;
		}
		Assert.assertTrue(rollback);
		recover(0);
		testCommitFailure();
		recover(2);
	}
	
	public void recover(int expectedCount) throws SQLException, XAException {
		Assert.assertEquals(0, rowCount());
		for (XADataSource ds : dataSources) {
			XAResource resource = ds.getXAConnection().getXAResource();
			Assert.assertTrue(resource.recover(XAResource.TMSTARTRSCAN | XAResource.TMENDRSCAN).length > 0);
			RecoveryService recoveryService = getService(RecoveryService.class);
			recoveryService.recover(resource);
			Assert.assertTrue(resource.recover(XAResource.TMSTARTRSCAN | XAResource.TMENDRSCAN).length == 0);
		}
		Assert.assertEquals(expectedCount, rowCount());
	}
	
	private void createTable(XADataSource xaDataSource) throws SQLException {
		Connection connection = xaDataSource.getXAConnection().getConnection();
		keepAlives.add(connection);
		try (Statement statement = connection.createStatement()) {			
			statement.execute("create table testing (name varchar(256))");
		}
	}
	
	private Properties getProperties(int id) {
		Properties props = new Properties();
		props.put(DataSourceFactory.JDBC_URL, "jdbc:h2:mem:indoubtest" + id);
		return props;
	}
	
	private void testCommitFailure() throws SQLException, NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		transactionManager.begin();
		boolean commit = false;
		try {
			List<XAConnection> xaConnections = new ArrayList<>();
			for (XADataSource dataSource : dataSources) {
				XAConnection xaConnection = dataSource.getXAConnection();
				xaConnections.add(xaConnection);
				transactionManager.getTransaction().enlistResource(commitFailureWrap(xaConnection.getXAResource()));
			}
			insertRow(xaConnections);
			commit = true;
		} finally {
			if (commit) {
				transactionManager.commit();
			} else {
				transactionManager.rollback();
			}
		}
	}
	
	private void testRollbackFailure() throws SQLException, NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		transactionManager.begin();
		boolean commit = false;
		try {
			List<XAConnection> xaConnections = new ArrayList<>();
			boolean failPrepare = false;
			for (XADataSource dataSource : dataSources) {
				XAConnection xaConnection = dataSource.getXAConnection();
				xaConnections.add(xaConnection);
				transactionManager.getTransaction().enlistResource(rollbackFailureWrap(xaConnection.getXAResource(), failPrepare));
				failPrepare = true;
			}
			insertRow(xaConnections);
			commit = true;
		} finally {
			if (commit) {
				transactionManager.commit();
			} else {
				transactionManager.rollback();
			}
		}
	}
	
	private int rowCount() throws SQLException {
		int result = 0;
		for (XADataSource source : dataSources) {
			XAConnection xaConnection = source.getXAConnection();
			try {
				try (Connection connection = xaConnection.getConnection()) {
					Assert.assertTrue(connection.getAutoCommit());
					try (PreparedStatement statement = connection.prepareStatement("select count(*) from testing")) {
						try (ResultSet resultSet = statement.executeQuery()) {
						resultSet.next();
						result += resultSet.getInt(1);
						}
					}
				}
			} finally {
				xaConnection.close();
			}
		}	
		return result;
	}
	
	private void insertRow(List<XAConnection> xaConnections) throws SQLException {
		for (XAConnection xaConnection : xaConnections) {	
			Connection connection = xaConnection.getConnection();
			Assert.assertFalse(connection.getAutoCommit());
			try (PreparedStatement statement = connection.prepareStatement("insert into testing values(?)")) {
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
	
	private XAResource commitFailureWrap(XAResource resource) {
		return new XaResourceWrapper(resource) {
			@Override
			public void commit(Xid xid, boolean onePhase) {
				if (!onePhase) {
					throw new RuntimeException("In doubt test");
				}
			}
		};
	}
	
	private XAResource rollbackFailureWrap(XAResource resource, boolean failPrepare) {
		return new XaResourceWrapper(resource) {
			@Override
			public int prepare(Xid xid) throws XAException {
				int result = super.prepare(xid);
				if (failPrepare) {
					throw new RuntimeException("Prepare failed");
				}
				return result;
			}
			
			@Override
			public void rollback(Xid xid) {
				throw new RuntimeException("Rollback failed");
			}
		};
	}
}
