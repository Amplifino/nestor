package com.amplifino.nestor.transaction.control.jdbc.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

import javax.sql.XADataSource;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.transaction.control.ScopedWorkException;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProvider;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProviderFactory;
import org.osgi.util.tracker.ServiceTracker;

public class TransactionControlScenarioTest {

	private BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private TransactionControl transactionControl;
	private JDBCConnectionProviderFactory factory;
	private Connection connection;
	private Connection keepAliveConnection;
	private UserTransaction userTransaction;
	private DataSourceFactory dataSourceFactory;
	
	@Before
	public void setup() throws SQLException {
		transactionControl = getService(TransactionControl.class);
		factory = getService(JDBCConnectionProviderFactory.class);
		userTransaction = getService(UserTransaction.class);
		dataSourceFactory = getService(DataSourceFactory.class);
		Properties props = new Properties();
		props.put(DataSourceFactory.JDBC_URL, "jdbc:h2:mem:db1");
		XADataSource xaDataSource = dataSourceFactory.createXADataSource(props);
		keepAliveConnection = xaDataSource.getXAConnection().getConnection();
		try (Statement statement = keepAliveConnection.createStatement()) {
			statement.execute("create table test (name varchar(80))");
		}
		JDBCConnectionProvider provider = factory.getProviderFor(xaDataSource, Collections.emptyMap());
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
		transactionControl.required(() -> this.doWork("1"));
		Assert.assertEquals(1, count());
	}
	
	@Test
	public void testNested() {
		transactionControl.required(() -> this.doWork("1R(1R(1)1)1"));
		Assert.assertEquals(5, count());
	}
	
	@Test
	public void testNew() {
		try {
			transactionControl.required(() -> this.doWork("11N(1)0"));
		} catch (ScopedWorkException e) {			
		}
		Assert.assertEquals(1, count());
	}
	
	@Test
	public void testSupported() {
		try {
			transactionControl.required(() -> this.doWork("S(10)"));
		} catch (ScopedWorkException e) {
		}
		Assert.assertEquals(0, count());
		try {
			transactionControl.supports(() -> this.doWork("10"));
		} catch (ScopedWorkException e) {
		}
		Assert.assertEquals(2, count());	
	}
	
	@Test
	public void testNotSupported() {
		try {
			transactionControl.required(() -> this.doWork("1X(111)0"));
		} catch (ScopedWorkException e) {		
		}
		Assert.assertEquals(3, count());
	}
	
	@Test
	public void testInterrupt() {
		try {
			transactionControl.required(() -> this.doWork("1R(1I)"));
		} catch(ScopedWorkException e) {
		}
		Assert.assertTrue(Thread.interrupted());
	}
	
	@Test
	public void testUserTransaction() {
		try {
			transactionControl.supports(() -> this.doWork("U(R(1)R(0))"));
		} catch (ScopedWorkException e) {
		}
		Assert.assertEquals(0, count());
		transactionControl.supports(() -> this.doWork("U(R(1)R(1)R(1))"));
		Assert.assertEquals(3, count());
	}
	
	@Test
	public void testBuilder() {
		try {
			transactionControl.build()
				.noRollbackFor(SQLException.class)
				.required(() -> this.doWork("10"));
		} catch (ScopedWorkException e) {			
		}
		Assert.assertEquals(2, count());
	}
	
	@Test
	public void testBuilder2() {
		try {
			transactionControl.build()
				.noRollbackFor(SQLException.class)
				.rollbackFor(Exception.class)
				.required(() -> this.doWork("10"));
		} catch (ScopedWorkException e) {			
		}
		Assert.assertEquals(2, count());
	}
	

	@Test
	public void testBuilder3() {
		try {
			transactionControl.build()
				.noRollbackFor(Exception.class)
				.rollbackFor(SQLException.class)
				.required(() -> this.doWork("10"));
		} catch (ScopedWorkException e) {			
		}
		Assert.assertEquals(0, count());
	}
	
	private int doWork(String work) throws Exception {
		if (work.isEmpty()) {
			return 0;
		}
		char action = work.charAt(0);
		switch (action) {
			case '0':
				doFailedWork();
				return doWork(work.substring(1));
			case '1':
				doWork();
				return doWork(work.substring(1));
			case 'I':
				throw new InterruptedException();
			case 'R':
			case 'S':
			case 'N':
			case 'X':			
			case 'U':
				int offset = executeScope(work);
				return doWork(work.substring(offset));
			default:
				throw new IllegalArgumentException(work);
		}
	}
	
	private int executeScope(String work) throws Exception {
		int offset = 2;
		if (work.charAt(1) != '(') {
			throw new IllegalArgumentException();
		}
		int level = 1;
		for (; offset < work.length(); offset++) {
			char pos = work.charAt(offset);
			if (pos == '(') {
				level++;
			}
			if (pos == ')') {
				level--;
			}
			if (level == 0) {
				break;
			}
		}
		if (level != 0) {
			throw new IllegalArgumentException(work);
		}
		String argument = work.substring(2, offset);
		switch (work.charAt(0)) {
			case 'R':
				transactionControl.required(() -> this.doWork(argument));
				break;
			case 'S':
				transactionControl.supports(() -> this.doWork(argument));
				break;
			case 'N':
				transactionControl.requiresNew(() -> this.doWork(argument));
				break;
			case 'X':
				transactionControl.notSupported(() -> this.doWork(argument));
				break;
			case 'U':
				userTransaction.begin();
				try {
					this.doWork(argument);
					userTransaction.commit();
					break;
				} catch (Throwable e) {
					userTransaction.rollback();
					throw e;
				}
			default:
				throw new IllegalArgumentException();
		}
		return offset + 1;
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
