package com.amplifino.nestor.jdbc.wrappers;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

import javax.sql.CommonDataSource;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Wraps a CommonDataSource forwarding all methods to the wrapped object.
 *
 */
@ConsumerType
public abstract class CommonDataSourceWrapper implements CommonDataSource {

	private final CommonDataSource commonDataSource;
	
	protected CommonDataSourceWrapper(CommonDataSource commonDataSource) {
		this.commonDataSource = Objects.requireNonNull(commonDataSource);
	}
	
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return commonDataSource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		commonDataSource.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		commonDataSource.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return commonDataSource.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return commonDataSource.getParentLogger();
	}

}
