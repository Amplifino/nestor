package com.amplifino.nestor.adapters;

import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import com.amplifino.nestor.jdbc.wrappers.CommonDataSourceWrapper;

/**
 * turns a ConnectinoPoolDataSource into an XADataSource.
 * The resulting XADataSource can only participate in JTA transactions involving a single XAResource.
 * ( Calling prepare on its XAResource will throw UnsupportedOperation)
 *
 */
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
