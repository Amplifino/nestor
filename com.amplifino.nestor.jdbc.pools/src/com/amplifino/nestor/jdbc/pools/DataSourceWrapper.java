package com.amplifino.nestor.jdbc.pools;

import javax.sql.DataSource;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface DataSourceWrapper {

	public DataSource wrap(DataSource dataSource);
}
