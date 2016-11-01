package com.amplifino.nestor.transaction.control.jdbc;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProvider;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProviderFactory;

import com.amplifino.nestor.adapters.ConnectionPoolDataSourceAdapter;
import com.amplifino.nestor.adapters.DataSourceAdapter;
import com.amplifino.nestor.adapters.XADataSourceAdapter;
import com.amplifino.pools.Pool;

@Component
public class JDBCConnectionProviderFactoryImpl implements JDBCConnectionProviderFactory {
	
	@Reference
	private TransactionManager transactionManager;
	@Reference
	private TransactionSynchronizationRegistry synchronization;
	private BundleContext bundleContext; 

	@Activate
	public void activate(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	@Override
	public JDBCConnectionProvider getProviderFor(DataSource dataSource, Map<String, Object> properties) {
		return getProviderFor(XADataSourceAdapter.on(ConnectionPoolDataSourceAdapter.on(dataSource)), properties);
	}

	@Override
	public JDBCConnectionProvider getProviderFor(XADataSource xaSource, Map<String, Object> properties) {
		Pool<XAConnection> pool = Pool.builder(uncheck(() -> xaSource.getXAConnection()))
			.maxIdle((int) properties.getOrDefault(JDBCConnectionProviderFactory.MIN_CONNECTIONS, 10))
			.maxSize((int) properties.getOrDefault(JDBCConnectionProviderFactory.MAX_CONNECTIONS, 10))
			.maxIdleTime((int) properties.getOrDefault(JDBCConnectionProviderFactory.IDLE_TIMEOUT, 3), TimeUnit.MINUTES)
			.maxWait((int) properties.getOrDefault(JDBCConnectionProviderFactory.CONNECTION_TIMEOUT, 30), TimeUnit.SECONDS)
			.build();
		return new JDBCConnectionProviderImpl(pool, bundleContext);		
	}

	@Override
	public JDBCConnectionProvider getProviderFor(DataSourceFactory dataSourceFactory, Properties props, Map<String, Object> map) {
		try {
			if (Boolean.TRUE.equals(props.getProperty("osgi.use.driver"))) {
				Driver driver = dataSourceFactory.createDriver(props);
				return getProviderFor(XADataSourceAdapter.on(ConnectionPoolDataSourceAdapter.on(DataSourceAdapter.on(driver, props))), map);	
			} else {
				return getProviderFor(dataSourceFactory.createXADataSource(props), map);
			}
		} catch (SQLException e) {
			throw new TransactionException(e.toString(), e);
		}
	}

	@Override
	public JDBCConnectionProvider getProviderFor(Driver driver, Properties props, Map<String, Object> map) {
		return getProviderFor(XADataSourceAdapter.on(ConnectionPoolDataSourceAdapter.on(DataSourceAdapter.on(driver, props))), map);
	}

	static <T> Supplier<T> uncheck(Callable<T> callable) {
		return () -> { try {
				return callable.call();
			} catch (RuntimeException e) {
				throw e;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new TransactionException(e.toString(), e);
			} catch (Exception e) {
				throw new TransactionException(e.toString(), e);
			}
		};
	}
}