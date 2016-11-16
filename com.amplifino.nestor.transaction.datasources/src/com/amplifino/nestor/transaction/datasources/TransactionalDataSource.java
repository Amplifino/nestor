package com.amplifino.nestor.transaction.datasources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;

import com.amplifino.counters.Counts;
import com.amplifino.counters.CountsSupplier;
import com.amplifino.nestor.jdbc.wrappers.CommonDataSourceWrapper;
import com.amplifino.nestor.jdbc.wrappers.ConnectionInJtaTransactionWrapper;
import com.amplifino.pools.Pool;
import com.amplifino.pools.PoolEntry;

/**
 * A JDBC Connection Pool that is JTA transaction aware
 * 
 * Instances are normally created using Config Admin.
 * When using the builder API, be sure to close the DataSource before disposing to release the pooled Connections.
 * 
 * This is a type 3 DataSpource implementation according to the DataSource javadoc. 
 * 
 */
public final class TransactionalDataSource extends CommonDataSourceWrapper implements DataSource, ConnectionEventListener, CountsSupplier {

	private final XADataSource xaDataSource;
	private final TransactionManager transactionManager;
	private final TransactionSynchronizationRegistry synchronization;
	private Pool<XAConnection> pool;	
	private OptionalInt isValidTimeout = OptionalInt.of(0);
	private final Set<XAConnection> failedConnections = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private Optional<String> validationQuery = Optional.empty();
	private long validationIdleTime = 0;
	
	private TransactionalDataSource(XADataSource xaDataSource, TransactionManager transactionManager, TransactionSynchronizationRegistry synchronization) {
		super(xaDataSource);
		this.xaDataSource = xaDataSource;
		this.transactionManager = transactionManager;
		this.synchronization = synchronization;
	}
	
	private PoolEntry<XAConnection> xaConnection() {
		return pool.borrowEntry();
	}
	
	private XAConnection supply() {
		try {
			XAConnection xaConnection = xaDataSource.getXAConnection();
			xaConnection.addConnectionEventListener(this);
			return xaConnection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void destroy(XAConnection xaConnection) {
		try {
			xaConnection.close();
		} catch (SQLException e) {
			throw new RuntimeException();
		}
	}
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return iface.cast(this);
		} else {
			throw new SQLException();
		}
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this);
	}

	@Override
	public Connection getConnection() throws SQLException {
		try {
			Transaction transaction = transactionManager.getTransaction();
			if (transaction == null) {
				return getLocalConnection();
			} else {
				return getConnection(transaction);
			}
		} catch (SystemException | RollbackException e) {
			throw  new SQLException(e);
		}
	}
	
	private Connection getLocalConnection() throws SQLException {
		PoolEntry<XAConnection> poolEntry = xaConnection();
		Connection connection = poolEntry.get().getConnection();
		if (!isValid(connection, poolEntry.age())) {
			try {
				connection.close();
			} catch (SQLException e) {
			}
			pool.evict(poolEntry.get());
			return getLocalConnection();
		} else {
			return connection;
		}
	}
	
	private Connection getConnection(Transaction transaction) throws SQLException, SystemException, RollbackException {		
		Connection connection = (Connection) synchronization.getResource(this);
		if (connection != null) {
			return ConnectionInJtaTransactionWrapper.on(connection);
		}
		PoolEntry<XAConnection> poolEntry = xaConnection();
		XAResource xaResource = poolEntry.get().getXAResource();
		connection = poolEntry.get().getConnection();
		if (!isValid(connection, poolEntry.age())) {
			try {
				connection.close();					
			} catch (SQLException e) {
			}
			pool.evict(poolEntry.get());
			return getConnection(transaction);
		}
		transaction.enlistResource(xaResource);
		transaction.registerSynchronization(new ConnectionCloser(connection));
		synchronization.putResource(this, connection);
		return ConnectionInJtaTransactionWrapper.on(connection);
	}
	
	private boolean isValid(Connection connection, long age) throws SQLException {
		if (connection.isClosed()) {
			return false;
		}
		if (age < validationIdleTime) {
			return true;
		}
		if (isValidTimeout.isPresent()) {
			if (!connection.isValid(isValidTimeout.getAsInt())) {
				return false;
			}
		}
		if (validationQuery.isPresent()) {
			try {
				try (PreparedStatement statement = connection.prepareStatement(validationQuery.get())) {
					statement.execute();
				}
			} catch (SQLException e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException();		
	}

	@Override
	public void connectionClosed(ConnectionEvent event) {
		XAConnection connection = (XAConnection) event.getSource();
		if (failedConnections.remove(connection)) {
			pool.evict(connection);
		} else {
			pool.release(connection);
		}
	}

	@Override
	public void connectionErrorOccurred(ConnectionEvent event) {
		failedConnections.add((XAConnection) event.getSource());
	}
	
	@Override
	public Counts counts() {
		return pool.counts();
	}
	
	/**
	 * closes the connection pool, releasing all pooled connections 
	 */
	public void close() {
		pool.close();		
	}
	
	/**
	 * returns a TransactionalDataSource builder
	 * @param xaDataSource
	 * @param manager
	 * @param synchronization
	 * @return
	 */
	public static Builder builder(XADataSource xaDataSource, TransactionManager manager, TransactionSynchronizationRegistry synchronization) {
		return new Builder(xaDataSource, manager, synchronization);
	}
	
	/**
	 * a TransactionalDataSource builder
	 */
	public static class Builder {
		private final TransactionalDataSource transactionalDataSource;
		private final Pool.Builder<XAConnection> poolBuilder;
		
		private Builder(XADataSource xaDataSource, TransactionManager manager, TransactionSynchronizationRegistry synchronization) {
			this.transactionalDataSource = new TransactionalDataSource(xaDataSource, manager, synchronization);
			this.poolBuilder = Pool.builder(transactionalDataSource::supply).destroy(transactionalDataSource::destroy);
		}
		
		/**
		 * sets the maximum number of connections in the pool
		 * @param maxSize
		 * @return this
		 */
		public Builder maxSize(int maxSize) {
			poolBuilder.maxSize(maxSize);
			return this;
		}
		
		/**
		 * sets the maximum number of idle connections in the pool
		 * @param maxIdle
		 * @return
		 */
		public Builder maxIdle(int maxIdle) {
			poolBuilder.maxIdle(maxIdle);
			return this;
		}
		
		public Builder maxWait(long amount, TimeUnit unit) {
			poolBuilder.maxWait(amount, unit);
			return this;
		}
		
		/**
		 * sets the amount of time a connection can remain idle
		 * @param amount
		 * @param unit
		 * @return this
		 */
		public Builder maxIdleTime(long amount, TimeUnit unit) {
			poolBuilder.maxIdleTime(amount, unit);
			return this;
		}
		
		/**
		 * sets the pool's initial size
		 * @param initialSize
		 * @return this
		 */
		public Builder initialSize(int initialSize) {
			poolBuilder.initialSize(initialSize);
			return this;
		}
		
		/**
		 * sets the pool's name
		 * @param name
		 * @return this
		 */
		public Builder name(String name) {
			poolBuilder.name(name);
			return this;
		}
		
		/**
		 * set the timeout in seconds for the connection isValid test
		 * a value of 0 means the driver's default timeout
		 * @param isValidTimeout
		 * @return this
		 */
		public Builder isValidTimeout(int isValidTimeout) {
			transactionalDataSource.isValidTimeout = OptionalInt.of(isValidTimeout);
			return this;
		}
		
		/**
		 * instruct the pool to skip the isValid test,
		 * typically because the driver does not support isValid
		 * @return this
		 */
		public Builder skipIsValid() {
			transactionalDataSource.isValidTimeout = OptionalInt.empty();
			return this;
		}
		
		public Builder validationQuery(String sql) {
			transactionalDataSource.validationQuery = Optional.of(sql);
			return this;
		}
		
		public Builder propertyCycle(long amount, TimeUnit unit) {
			poolBuilder.propertyCycle(amount, unit);
			return this;
		}
		
		/**
		 * configure the pool to use first in first out scheduling
		 * @return this
		 */
		public Builder fifo() {
			poolBuilder.fifo();
			return this;
		}
		
		/**
		 * configure the pool to use last in first out scheduling
		 * @return this
		 */
		public Builder lifo() {
			poolBuilder.lifo();
			return this;
		}
		
		/**
		 * builds a new TransactionDataSource
		 * @return the new pool
		 */
		public TransactionalDataSource build() {
			transactionalDataSource.pool = poolBuilder.build();
			return transactionalDataSource;
		}
	}

}
