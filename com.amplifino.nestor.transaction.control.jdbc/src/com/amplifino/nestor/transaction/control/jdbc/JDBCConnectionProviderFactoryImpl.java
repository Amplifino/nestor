package com.amplifino.nestor.transaction.control.jdbc;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProvider;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProviderFactory;

import com.amplifino.nestor.adapters.ConnectionPoolDataSourceAdapter;
import com.amplifino.nestor.adapters.DataSourceAdapter;
import com.amplifino.pools.Pool;

@Component
public class JDBCConnectionProviderFactoryImpl implements JDBCConnectionProviderFactory {

	private BundleContext bundleContext;

	@Activate
	public void activate(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public JDBCConnectionProvider getProviderFor(DataSource dataSource, Map<String, Object> properties) {
		ConnectionPoolDataSource adapter = ConnectionPoolDataSourceAdapter.on(dataSource);
		Pool.Builder<PooledConnection> builder = Pool.builder(uncheck(adapter::getPooledConnection));
		configure(builder, properties);
		return new JDBCLocalConnectionProvider(builder.build());
	}

	@Override
	public JDBCConnectionProvider getProviderFor(XADataSource xaSource, Map<String, Object> properties) {
		Pool.Builder<XAConnection> builder = Pool.builder(uncheck(xaSource::getXAConnection));
		configure(builder, properties);
		return new JDBCXAConnectionProvider(builder.build(), bundleContext);
	}

	private void configure(Pool.Builder<?> pool, Map<String, Object> properties) {
		pool
			.maxIdle((int) properties.getOrDefault(JDBCConnectionProviderFactory.MIN_CONNECTIONS, 10))
			.maxSize((int) properties.getOrDefault(JDBCConnectionProviderFactory.MAX_CONNECTIONS, 10))
			.maxIdleTime((int) properties.getOrDefault(JDBCConnectionProviderFactory.IDLE_TIMEOUT, 3), TimeUnit.MINUTES)
			.maxWait((int) properties.getOrDefault(JDBCConnectionProviderFactory.CONNECTION_TIMEOUT, 30), TimeUnit.SECONDS);
	}

	@Override
	public JDBCConnectionProvider getProviderFor(DataSourceFactory dataSourceFactory, Properties props, Map<String, Object> map) {
		try {
			if (Boolean.TRUE.equals(props.getProperty("osgi.use.driver"))) {
				return getProviderFor(dataSourceFactory.createDriver(props),  props, map);
			} else {
				return getProviderFor(dataSourceFactory.createXADataSource(props), map);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JDBCConnectionProvider getProviderFor(Driver driver, Properties props, Map<String, Object> map) {
		return getProviderFor(DataSourceAdapter.on(driver, props), map);
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