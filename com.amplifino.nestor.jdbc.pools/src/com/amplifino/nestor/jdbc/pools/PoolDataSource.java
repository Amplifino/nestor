package com.amplifino.nestor.jdbc.pools;

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
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import com.amplifino.counters.Counts;
import com.amplifino.counters.CountsSupplier;
import com.amplifino.nestor.jdbc.wrappers.CommonDataSourceWrapper;
import com.amplifino.pools.Pool;
import com.amplifino.pools.PoolEntry;

/**
 * Implements a JDBC Connection pool.
 * Instances are normally created using OSGI Configuration Admin,
 * but can also be created by API using PoolDataSource.builder
 * If using the API it is important to call close before disposing the PoolDataSource to release its pooled connections
 * 
 * This is a type 2 DataSource implementation according to the DataSource javadoc.
 *
 */
public final class PoolDataSource extends CommonDataSourceWrapper implements DataSource, CountsSupplier, AutoCloseable, ConnectionEventListener {

	private final ConnectionPoolDataSource connectionPoolDataSource;
	private Pool<PooledConnection> pool;
	private OptionalInt isValidTimeout = OptionalInt.of(0);
	private final Set<PooledConnection> failedConnections = ConcurrentHashMap.newKeySet();
	private Optional<String> validationQuery = Optional.empty();
	private long validationIdleTime = 0;
	
	private PoolDataSource(ConnectionPoolDataSource connectionPoolDataSource) {
		super(connectionPoolDataSource);
		this.connectionPoolDataSource = connectionPoolDataSource;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return Optional.of(this)
			.filter(iface::isInstance)
			.map(iface::cast)
			.orElseThrow(SQLException::new);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this);
	}

	@Override
	public Connection getConnection() throws SQLException {
		PoolEntry<PooledConnection> poolEntry = pool.borrowEntry();
		try {
			return getConnection(poolEntry);
		} catch (Throwable e) {
			pool.evict(poolEntry.get());
			if (poolEntry.isFresh()) {
				throw e;
			}
		}
		return getConnection();
	}
	
	private Connection getConnection(PoolEntry<PooledConnection> poolEntry) throws SQLException {
		Connection connection = poolEntry.get().getConnection();
		try {
			checkValid(connection, poolEntry.age());
			return connection;
		} catch (Throwable e) {
			connection.close();
			throw e;
		}
	}
	
	private void checkValid(Connection connection, long age) throws SQLException {
		if (connection.isClosed()) {
			throw new SQLException("connection is closed");
		}
		if (age < validationIdleTime) {
			return;
		}
		if (isValidTimeout.isPresent()) {
			if (!connection.isValid(isValidTimeout.getAsInt())) {
				throw new SQLException("connection not valid");
			}
		}
		if (validationQuery.isPresent()) {
			try (PreparedStatement statement = connection.prepareStatement(validationQuery.get())) {
				statement.execute();
			}
		}
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void connectionClosed(ConnectionEvent event) {
		PooledConnection connection = (PooledConnection) event.getSource();
		if (failedConnections.remove(connection)) {
			pool.evict(connection);
		} else {
			pool.release(connection);
		}
	}

	@Override
	public void connectionErrorOccurred(ConnectionEvent event) {
		failedConnections.add((PooledConnection) event.getSource());
	}
	
	@Override
	public void close() {
		pool.close();
	}
	
	private PooledConnection supply() {
		try {
			PooledConnection connection = connectionPoolDataSource.getPooledConnection();
			connection.addConnectionEventListener(this);
			return connection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void destroy(PooledConnection connection) {
		connection.removeConnectionEventListener(this);
		try {
			connection.close();
		} catch (SQLException e) {			
		}
	}
	
	@Override
	public Counts counts() {
		return pool.counts();
	}
	
	/**
	 * return a PoolDataSource builder
	 * @param connectionPoolDataSource factory object for obtaining pooled connections
	 * @return
	 */
	public static Builder builder(ConnectionPoolDataSource connectionPoolDataSource) {
		return new Builder(connectionPoolDataSource);
	}

	/**
	 * PoolDataSource builder
	 *
	 */
	public static final class Builder {
		private final PoolDataSource poolDataSource;
		private final Pool.Builder<PooledConnection> poolBuilder;
		
		private Builder(ConnectionPoolDataSource connectionPoolDataSource) {
			this.poolDataSource = new PoolDataSource(connectionPoolDataSource);
			this.poolBuilder = Pool.builder(poolDataSource::supply).destroy(poolDataSource::destroy);
		}
		
		/**
		 * sets the maximum number of connections
		 * @param maxSize
		 * @return this
		 */
		public Builder maxSize(int maxSize) {
			poolBuilder.maxSize(maxSize);
			return this;
		}
		
		/**
		 * sets the maximum number of idle connections
		 * @param maxIdle
		 * @return this
		 */
		public Builder maxIdle(int maxIdle) {
			poolBuilder.maxIdle(maxIdle);
			return this;
		}
		
		/**
		 * sets the amount of time a connection can remain idle in the pool
		 * @param amount
		 * @param unit
		 * @return this
		 */
		public Builder maxIdleTime(long amount, TimeUnit unit) {
			poolBuilder.maxIdleTime(amount, unit);
			return this;
		}
		
		public Builder maxWait(long amount, TimeUnit unit) {
			poolBuilder.maxWait(amount, unit);
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
		 * sets the pool is valid timeout (in seconds)
		 * a value of 0 means the driver's default
		 * @param timeOut
		 * @return this
		 */
		public Builder isValidTimeout(int timeOut) {
			poolDataSource.isValidTimeout = OptionalInt.of(timeOut);
			return this;
		}
		
		/**
		 * configure the pool not to perform isValid testing,
		 * typically because the drivers does not support it. 
		 * @return this
		 */
		public Builder skipIsValid() {
			poolDataSource.isValidTimeout = OptionalInt.empty();
			return this;
		}
		
		public Builder validationQuery(String sql) {
			poolDataSource.validationQuery = Optional.of(sql);
			return this;
		}
		
		public Builder validationIdleTime(long timeOut, TimeUnit unit) {
			poolDataSource.validationIdleTime = unit.toMillis(timeOut);
			return this;
		}
		
		public Builder cycleTime(long amount, TimeUnit unit) {
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
		 * build a PoolDataSource
		 * @return the new pool
		 */
		public PoolDataSource build() {
			poolDataSource.pool = poolBuilder.build();
			return poolDataSource;
		}
	}
}
