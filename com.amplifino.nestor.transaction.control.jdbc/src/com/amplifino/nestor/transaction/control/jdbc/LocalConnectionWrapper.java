package com.amplifino.nestor.transaction.control.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.PooledConnection;

import org.osgi.service.transaction.control.LocalResource;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;

import com.amplifino.pools.Pool;

class LocalConnectionWrapper extends ConnectionWrapper {
	
	private final Pool<PooledConnection> pool;
	
	LocalConnectionWrapper(TransactionControl transactionControl, Pool<PooledConnection> pool) {
		super(transactionControl);
		this.pool = pool;
	}
	
	@Override
    Connection newConnection(TransactionControl transactionControl) throws SQLException {
    	PooledConnection pooledConnection = pool.borrow();
    	Connection connection = pooledConnection.getConnection();
    	transactionControl.getCurrentContext().postCompletion(status -> this.close(connection, pooledConnection));
    	if (transactionControl.activeTransaction()) {
    		connection.setAutoCommit(false);
    		transactionControl.getCurrentContext().registerLocalResource(localResource(connection));
    	} else {
    		connection.setAutoCommit(true);
    	}
    	return connection;
    }
    
    private LocalResource localResource(Connection connection) {
    	return new LocalResource() {

			@Override
			public void commit() throws TransactionException {
				try {
					connection.commit();
				} catch (SQLException e) {
					throw new TransactionException(e.toString(), e);
				}
			}

			@Override
			public void rollback() throws TransactionException {
				try {
					connection.rollback();
				} catch (SQLException e) {
					throw new TransactionException(e.toString(), e);
				}				
			}
    		
    	};
    }
    
    private void close(Connection connection, PooledConnection pooledConnection) {
    	try {
    		connection.close();
    	} catch (SQLException e) {
    	}    	
    	pool.release(pooledConnection);
    }
    
}