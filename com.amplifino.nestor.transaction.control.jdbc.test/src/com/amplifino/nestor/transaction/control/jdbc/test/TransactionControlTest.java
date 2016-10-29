package com.amplifino.nestor.transaction.control.jdbc.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Objects;

import javax.sql.XADataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.transaction.control.ScopedWorkException;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProvider;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProviderFactory;
import org.osgi.util.tracker.ServiceTracker;

public class TransactionControlTest {

	private BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private TransactionControl transactionControl;
	private JDBCConnectionProviderFactory factory;
	private Connection connection;
	private Connection keepAliveConnection;
	
	@Before
	public void setup() throws SQLException {
		transactionControl = getService(TransactionControl.class);
		factory = getService(JDBCConnectionProviderFactory.class);
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:db1");
		keepAliveConnection = ds.getConnection();
		try (Statement statement = keepAliveConnection.createStatement()) {
			statement.execute("create table test (name varchar(80))");
		}
		JDBCConnectionProvider provider = factory.getProviderFor((XADataSource) ds, Collections.emptyMap());
		connection = provider.getResource(transactionControl);
	}
	
	@After
	public void tearDown() throws SQLException {
		try (Statement statement = keepAliveConnection.createStatement()) {
			statement.execute("drop table test");
		}
		keepAliveConnection.close();
	}
	
	@Test
	public void test() {
		Assert.assertNotNull(connection);
		int count = transactionControl.required(this::doWork);
		Assert.assertEquals(1, count);
	}
	
	@Test(expected=ScopedWorkException.class)
	public void testRollback() {
		transactionControl.required(this::doFailedWork);
	}
	
	@Test
	public void testRollback2() throws SQLException {
		try {
			transactionControl.required(this::doFailedWork);
		} catch (ScopedWorkException e) {
		}
		Assert.assertEquals(0, (int) transactionControl.supports(this::count));
	}
	
	@Test
	public void testNested() {
		int count = transactionControl.required(this::doNestedWork);
		Assert.assertEquals(3, count);
		Assert.assertEquals(3, count());
	}
	
	@Test(expected=ScopedWorkException.class)
	public void testNestedRollback() {
		int count = transactionControl.required(this::doNestedFailedWork);
		Assert.assertEquals(3, count);
		Assert.assertEquals(3, count());
	}
	
	@Test
	public void testNestedRollback2() {
		try {
			transactionControl.required(this::doNestedFailedWork);
		} catch (ScopedWorkException e) {			
		}
		Assert.assertEquals(0, count());
	}
	
	@Test(expected=TransactionException.class)
	public void testNestedRollbackWithCatch() {
		transactionControl.required(this::doNestedCatchedWork);
	}
	
	@Test
	public void testRequiresNew() {
		transactionControl.required(this::doNestedIsolatedWork);
		Assert.assertEquals(3, count());
	}
	
	@Test(expected=ScopedWorkException.class)
	public void testRequiresNewWithRollback() {
		transactionControl.required(this::doNestedIsolatedWorkAndFail);
	}
	
	@Test
	public void testRequiresNewWithRollback2() {
		try {
			transactionControl.required(this::doNestedIsolatedWorkAndFail);
		} catch (ScopedWorkException e) {
		}
		Assert.assertEquals(1, count());
	}
	
	private int doWork() throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("insert into test (name) values (?)")) {
			statement.setString(1, "azerty");
			int insertCount = statement.executeUpdate();
			Assert.assertEquals(1, insertCount);
			return insertCount;
		}
	}
	
	private int doFailedWork() throws SQLException {
		doWork();
		try (PreparedStatement statement = connection.prepareStatement("insert into failedtest (name) values (?)")) {
			statement.setString(1, "azerty");
			int insertCount = statement.executeUpdate();
			Assert.assertEquals(1, insertCount);
			return insertCount;
		}
	}
	
	private int doNestedWork() throws SQLException {
		return doWork() + transactionControl.required(this::doWork) + doWork();
	}
	
	private int doNestedFailedWork() throws SQLException {
		int start = doWork();
		return start + transactionControl.required(this::doFailedWork) + doWork();
	}
	
	private int doNestedCatchedWork() throws SQLException {
		int start = doWork();
		try {
			transactionControl.required(this::doFailedWork);
		} catch (ScopedWorkException e) {			
		}
		return start + doWork();
	}
	
	private int doNestedIsolatedWork() throws SQLException {
		int count = doWork();
		count  += transactionControl.requiresNew(this::doWork);
		return count + doWork();		
	}
	
	private int doNestedIsolatedWorkAndFail() throws SQLException {
		int count = doWork();
		count  += transactionControl.requiresNew(this::doWork);
		return count + doFailedWork();		
	}
	
	private int count() {
		return transactionControl.supports(this::doCount);
	}
	
	private int doCount() throws SQLException {	
		try (PreparedStatement statement = connection.prepareStatement("select count(*) from test")) {
			try (ResultSet rs = statement.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}
	
	private <T> T getService(Class<T> clazz) {
		ServiceTracker<T, T> tracker = new ServiceTracker<>(context, clazz, null);
		tracker.open();
		try {
			return Objects.requireNonNull(tracker.waitForService(1000L));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
