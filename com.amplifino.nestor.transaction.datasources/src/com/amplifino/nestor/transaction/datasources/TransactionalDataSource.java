package com.amplifino.nestor.transaction.datasources;

import java.sql.Connection;
import java.sql.SQLException;
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

import com.amplifino.jdbc.wrappers.CommonDataSourceWrapper;
import com.amplifino.jdbc.wrappers.OnCloseConnectionWrapper;
import com.amplifino.pools.Pool;

public class TransactionalDataSource extends CommonDataSourceWrapper implements DataSource, ConnectionEventListener {

	private final XADataSource xaDataSource;
	private final TransactionManager transactionManager;
	private final TransactionSynchronizationRegistry synchronization;
	private Pool<XAConnection> pool;	
	private boolean useIsValid = true;
	
	private TransactionalDataSource(XADataSource xaDataSource, TransactionManager transactionManager, TransactionSynchronizationRegistry synchronization) {
		super(xaDataSource);
		this.xaDataSource = xaDataSource;
		this.transactionManager = transactionManager;
		this.synchronization = synchronization;
	}
	
	private XAConnection xaConnection() {
		return pool.borrow();
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
				XAConnection xaConnection = xaConnection();
				Connection connection = xaConnection.getConnection();
				if (useIsValid && !connection.isValid(0)) {
					try {
						connection.close();
					} catch (SQLException e) {
					}
					pool.evict(xaConnection);
					return getConnection();
				}
				return connection;
			}
			Connection connection = (Connection) synchronization.getResource(this);
			if (connection != null) {
				return new OnCloseConnectionWrapper(connection, c -> {});
			}
			try {
				XAConnection xaConnection = xaConnection();
				XAResource xaResource = xaConnection.getXAResource();
				connection = xaConnection.getConnection();
				if (useIsValid && !connection.isValid(0)) {
					try {
						connection.close();
					} catch (SQLException e) {
					}
					pool.evict(xaConnection);
					return getConnection();
				}
				transaction.enlistResource(xaResource);
				transaction.registerSynchronization(new ConnectionCloser(connection));
				synchronization.putResource(this, connection);
				return new OnCloseConnectionWrapper(connection, c -> {});
			} catch (RollbackException e) {
				throw new SQLException(e);
			} 			
		} catch (SystemException e) {
			throw new SQLException(e);
		}		
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException();		
	}

	@Override
	public void connectionClosed(ConnectionEvent event) {
		pool.release((XAConnection) event.getSource());
	}

	@Override
	public void connectionErrorOccurred(ConnectionEvent event) {
		pool.evict((XAConnection) event.getSource());
	}
	
	void close() {
		pool.close();		
	}
	
	static Builder builder(XADataSource xaDataSource, TransactionManager manager, TransactionSynchronizationRegistry synchronization) {
		return new Builder(xaDataSource, manager, synchronization);
	}
	
	public static class Builder {
		private final TransactionalDataSource transactionalDataSource;
		private final Pool.Builder<XAConnection> poolBuilder;
		
		private Builder(XADataSource xaDataSource, TransactionManager manager, TransactionSynchronizationRegistry synchronization) {
			this.transactionalDataSource = new TransactionalDataSource(xaDataSource, manager, synchronization);
			this.poolBuilder = Pool.builder(transactionalDataSource::supply).destroy(transactionalDataSource::destroy);
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
		
		public Builder useIsValid(boolean useIsValid) {
			transactionalDataSource.useIsValid = useIsValid;
			return this;
		}
		
		public TransactionalDataSource build() {
			transactionalDataSource.pool = poolBuilder.build();
			return transactionalDataSource;
		}
	}

}
