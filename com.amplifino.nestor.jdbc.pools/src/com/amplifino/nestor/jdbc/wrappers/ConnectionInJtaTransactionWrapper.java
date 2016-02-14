package com.amplifino.nestor.jdbc.wrappers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

public final class ConnectionInJtaTransactionWrapper extends ConnectionWrapper {

	private ConnectionInJtaTransactionWrapper(Connection connection) {
		super(connection);
	}
	
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if (autoCommit) {
			throw new SQLFeatureNotSupportedException("Connection is participating in XA Transaction");
		} else {
			super.setAutoCommit(autoCommit);
		}
	};
	
	@Override
	public void commit() throws SQLException {
		throw new SQLFeatureNotSupportedException("Connection is participating in XA Transaction");
	}
	
	@Override
	public void rollback() throws SQLException {
		throw new SQLFeatureNotSupportedException("Connection is participating in XA Transaction");
	};
	
	@Override
	public void close() {
		// do nothing
	}

	public static Connection on(Connection connection) {
		return new ConnectionInJtaTransactionWrapper(connection);
	}
}
