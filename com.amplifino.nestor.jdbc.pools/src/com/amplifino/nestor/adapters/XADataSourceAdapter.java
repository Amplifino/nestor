package com.amplifino.nestor.adapters;

import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import com.amplifino.nestor.jdbc.wrappers.CommonDataSourceWrapper;

public final class XADataSourceAdapter extends CommonDataSourceWrapper implements XADataSource {

	private final ConnectionPoolDataSource connectionPoolDataSource;
	
	private XADataSourceAdapter(ConnectionPoolDataSource connectionPoolDataSource) {
		super(connectionPoolDataSource);
		this.connectionPoolDataSource = connectionPoolDataSource;
	}

	@Override
	public XAConnection getXAConnection() throws SQLException {
		return XAConnectionAdapter.on(connectionPoolDataSource.getPooledConnection());
	}

	@Override
	public XAConnection getXAConnection(String user, String password) throws SQLException {
		return XAConnectionAdapter.on(connectionPoolDataSource.getPooledConnection(user, password));
	}
	
	public static XADataSource on (ConnectionPoolDataSource connectionPoolDataSource) {
		return new XADataSourceAdapter(connectionPoolDataSource);
	}

}
