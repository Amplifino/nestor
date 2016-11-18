package com.amplifino.nestor.transaction.control.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;

import org.osgi.service.transaction.control.TransactionControl;

import com.amplifino.pools.Pool;

/**
 * Provides transparant access to connection registered with transactioncontext
 *
 */
class XAConnectionWrapper extends ConnectionWrapper {
	
	private final Pool<XAConnection> pool;
	
	XAConnectionWrapper(TransactionControl transactionControl, Pool<XAConnection> pool) {
		super(transactionControl);
		this.pool = pool;
	}
	
	@Override
    Connection newConnection(TransactionControl transactionControl) throws SQLException {
    	XAConnection xaConnection = pool.borrow();
    	Connection connection = xaConnection.getConnection();
    	transactionControl.getCurrentContext().postCompletion(status -> this.close(connection, xaConnection));
    	if (transactionControl.activeTransaction()) {
    		transactionControl.getCurrentContext().registerXAResource(xaConnection.getXAResource(), null);
    	} else {
    		connection.setAutoCommit(true);
    	}
    	return connection;
    }
    
    private void close(Connection connection, XAConnection xaConnection) {
    	try {
    		connection.close();
    	} catch (SQLException e) {
    	}    	
    	pool.release(xaConnection);
    }
    
 }