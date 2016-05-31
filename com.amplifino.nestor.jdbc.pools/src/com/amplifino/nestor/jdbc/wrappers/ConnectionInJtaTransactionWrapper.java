package com.amplifino.nestor.jdbc.wrappers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * A ConnectionWrapper for a connection participating in a JTA transaction
 *
 */
public final class ConnectionInJtaTransactionWrapper extends ConnectionWrapper {

	private ConnectionInJtaTransactionWrapper(Connection connection) {
		super(connection);
	}
	
	/**
	 * Throws SQLFeatureNotSupportedException on setAutoCommit(true), 
	 * as transaction is managed by JTA Transaction Manager 
	 */
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if (autoCommit) {
			throw new SQLFeatureNotSupportedException("Connection is participating in XA Transaction");
		} else {
			super.setAutoCommit(autoCommit);
		}
	};
	
	/**
	 * Throws SQLFeatureNotSupportedException 
	 * as transaction is managed by JTA Transaction Manager 
	 */
	@Override
	public void commit() throws SQLException {
		throw new SQLFeatureNotSupportedException("Connection is participating in XA Transaction");
	}
	
	/**
	 * Throws SQLFeatureNotSupportedException  
	 * as transaction is managed by JTA Transaction Manager 
	 */
	@Override
	public void rollback() throws SQLException {
		throw new SQLFeatureNotSupportedException("Connection is participating in XA Transaction");
	};
	
	@Override
	public void close() {
		// do nothing as connection will be closed when ending tx
	}
	
	/**
	 * returns a Connection wrapper 
	 * @param connection
	 * @return
	 */
	public static Connection on(Connection connection) {
		return new ConnectionInJtaTransactionWrapper(connection);
	}
}
