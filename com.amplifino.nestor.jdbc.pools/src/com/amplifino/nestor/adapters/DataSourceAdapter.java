package com.amplifino.nestor.adapters;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class DataSourceAdapter implements DataSource {
	
	private final Driver driver;
	private final String url;
	private final Properties properties;
	
	private DataSourceAdapter(Driver driver, String url, String user, String password) {
		this.driver = driver;
		this.url = url;
		properties = new Properties();
		properties.put("user", user);
		properties.put("password", password);
	}
	
	public static DataSourceAdapter on(Driver driver, String url, String user, String password) {
		return new DataSourceAdapter(driver, url, user, password);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return Optional.of(this).filter(iface::isInstance).map(iface::cast).orElseThrow(SQLException::new);

	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return driver.connect(url, properties);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException();
	}

}
