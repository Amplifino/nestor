package com.amplifino.nestor.transaction.control.jdbc;

import java.sql.Connection;

import javax.sql.PooledConnection;

import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProvider;

import com.amplifino.pools.Pool;

class JDBCLocalConnectionProvider implements JDBCConnectionProvider, AutoCloseable {
	
	private final Pool<PooledConnection> pool;
	
	public JDBCLocalConnectionProvider(Pool<PooledConnection> pool) {
		this.pool = pool;
	}
		
	@Override
	public Connection getResource(TransactionControl transactionControl) throws TransactionException {		
		return new LocalConnectionWrapper(transactionControl, pool);
	}
	
	@Override
	public void close() {
		pool.close();
	}

}
