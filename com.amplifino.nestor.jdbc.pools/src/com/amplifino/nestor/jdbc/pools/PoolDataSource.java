package com.amplifino.nestor.jdbc.pools;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import com.amplifino.counters.Counts;
import com.amplifino.counters.CountsSupplier;
import com.amplifino.nestor.adapters.ConnectionPoolDataSourceAdapter;
import com.amplifino.nestor.jdbc.wrappers.CommonDataSourceWrapper;
import com.amplifino.pools.Pool;

public class PoolDataSource extends CommonDataSourceWrapper implements DataSource, CountsSupplier, ConnectionEventListener {

	private final ConnectionPoolDataSource connectionPoolDataSource;
	private Pool<PooledConnection> pool;
	private OptionalInt isValidTimeout = OptionalInt.of(0);
	
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
		while(true) {
			PooledConnection pooledConnection = pool.borrow();
			Connection connection = pooledConnection.getConnection();
			if (!isValidTimeout.isPresent() || connection.isValid(isValidTimeout.getAsInt())) {
				return connection;
			} else {
				pool.evict(pooledConnection);
			}
		}
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void connectionClosed(ConnectionEvent event) {
		pool.release((PooledConnection) event.getSource());
	}

	@Override
	public void connectionErrorOccurred(ConnectionEvent event) {
	}
	
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
	
	public static Builder builder(ConnectionPoolDataSource connectionPoolDataSource) {
		return new Builder(connectionPoolDataSource);
	}
	
	public static Builder builder(DataSource dataSource) {
		return builder(ConnectionPoolDataSourceAdapter.on(dataSource));
	}

	@Override
	public Counts counts() {
		return pool.counts();
	}

	public static class Builder {
		private final PoolDataSource poolDataSource;
		private final Pool.Builder<PooledConnection> poolBuilder;
		
		private Builder(ConnectionPoolDataSource connectionPoolDataSource) {
			this.poolDataSource = new PoolDataSource(connectionPoolDataSource);
			this.poolBuilder = Pool.builder(poolDataSource::supply).destroy(poolDataSource::destroy);
		}
		
		public Builder maxSize(int maxSize) {
			poolBuilder.maxSize(maxSize);
			return this;
		}
		
		public Builder maxIdle(int maxIdle) {
			poolBuilder.maxIdle(maxIdle);
			return this;
		}
		
		public Builder maxIdleTime(long amount, TimeUnit unit) {
			poolBuilder.maxIdleTime(amount, unit);
			return this;
		}
		
		public Builder initialSize(int initialSize) {
			poolBuilder.initialSize(initialSize);
			return this;
		}
		
		public Builder name(String name) {
			poolBuilder.name(name);
			return this;
		}
		
		public Builder isValidTimeout(int timeOut) {
			poolDataSource.isValidTimeout = OptionalInt.of(timeOut);
			return this;
		}
		
		public Builder skipIsValid() {
			poolDataSource.isValidTimeout = OptionalInt.empty();
			return this;
		}
		
		public PoolDataSource build() {
			poolDataSource.pool = poolBuilder.build();
			return poolDataSource;
		}
	}
}
