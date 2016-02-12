package com.amplifino.nestor.jdbc.wrappers;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public abstract class DataSourceWrapper extends CommonDataSourceWrapper implements DataSource {

	private final DataSource dataSource;
	
	protected DataSourceWrapper(DataSource dataSource) {
		super(dataSource);
		this.dataSource = dataSource;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return iface.cast(this);
		} else {
			return dataSource.unwrap(iface);
		}
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this) || dataSource.isWrapperFor(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return dataSource.getConnection(username, password);
	}
	
	
}
