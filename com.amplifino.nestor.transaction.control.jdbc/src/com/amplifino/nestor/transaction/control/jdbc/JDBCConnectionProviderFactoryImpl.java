package com.amplifino.nestor.transaction.control.jdbc;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProvider;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProviderFactory;

import com.amplifino.nestor.adapters.ConnectionPoolDataSourceAdapter;
import com.amplifino.nestor.adapters.DataSourceAdapter;
import com.amplifino.nestor.adapters.XADataSourceAdapter;
import com.amplifino.nestor.transaction.datasources.TransactionalDataSource;

@Component
public class JDBCConnectionProviderFactoryImpl implements JDBCConnectionProviderFactory {
	
	@Reference
	private TransactionManager transactionManager;
	@Reference
	private TransactionSynchronizationRegistry synchronization;

	@Override
	public JDBCConnectionProvider getProviderFor(DataSource dataSource, Map<String, Object> properties) {
		return getProviderFor(XADataSourceAdapter.on(ConnectionPoolDataSourceAdapter.on(dataSource)), properties);
	}

	@Override
	public JDBCConnectionProvider getProviderFor(XADataSource xaSource, Map<String, Object> properties) {
		DataSource dataSource = TransactionalDataSource.builder(xaSource, transactionManager, synchronization)
			.maxIdle((int) properties.getOrDefault(JDBCConnectionProviderFactory.MIN_CONNECTIONS, 10))
			.maxSize((int) properties.getOrDefault(JDBCConnectionProviderFactory.MAX_CONNECTIONS, 10))
			.maxIdleTime((int) properties.getOrDefault(JDBCConnectionProviderFactory.IDLE_TIMEOUT, 3), TimeUnit.MINUTES)
			.maxWait((int) properties.getOrDefault(JDBCConnectionProviderFactory.CONNECTION_TIMEOUT, 30), TimeUnit.SECONDS)
			.build();
		return new JdbcConnectionProviderImpl(dataSource);		
	}

	@Override
	public JDBCConnectionProvider getProviderFor(DataSourceFactory dataSourceFactory, Properties props, Map<String, Object> map) {
		try {
			return getProviderFor(dataSourceFactory.createXADataSource(props), map);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JDBCConnectionProvider getProviderFor(Driver driver, Properties props, Map<String, Object> map) {
		return getProviderFor(XADataSourceAdapter.on(ConnectionPoolDataSourceAdapter.on(DataSourceAdapter.on(driver, props))), map);
	}

}