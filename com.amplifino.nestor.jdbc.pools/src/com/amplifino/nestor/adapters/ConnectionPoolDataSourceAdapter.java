package com.amplifino.nestor.adapters;

import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import com.amplifino.nestor.jdbc.wrappers.CommonDataSourceWrapper;

public class ConnectionPoolDataSourceAdapter extends CommonDataSourceWrapper implements ConnectionPoolDataSource {

	private final DataSource dataSource;
	
	private ConnectionPoolDataSourceAdapter(DataSource dataSource) {
		super(dataSource);
		this.dataSource = dataSource;
	}

	@Override
	public PooledConnection getPooledConnection() throws SQLException {
		return PooledConnectionAdapter.on(dataSource.getConnection());
	}

	@Override
	public PooledConnection getPooledConnection(String user, String password) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	static public ConnectionPoolDataSourceAdapter on(DataSource dataSource) {
		return new ConnectionPoolDataSourceAdapter(dataSource);
	}

}
