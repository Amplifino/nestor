package com.amplifino.nestor.adapters;

import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import javax.sql.XADataSource;

import com.amplifino.nestor.jdbc.wrappers.CommonDataSourceWrapper;



public class ConnectionPoolDataSourceXaAdapter extends CommonDataSourceWrapper implements ConnectionPoolDataSource {
	
	private final XADataSource xaDataSource;
	
	private ConnectionPoolDataSourceXaAdapter(XADataSource xaDataSource) {
		super(xaDataSource);
		this.xaDataSource = xaDataSource;
	}

	@Override
	public PooledConnection getPooledConnection() throws SQLException {
		return xaDataSource.getXAConnection();
	}

	@Override
	public PooledConnection getPooledConnection(String user, String password) throws SQLException {
		return xaDataSource.getXAConnection(user, password);
	}

	public static ConnectionPoolDataSource on(XADataSource xaDataSource) {
		return new ConnectionPoolDataSourceXaAdapter(xaDataSource);
	}

}
