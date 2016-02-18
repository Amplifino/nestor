package com.amplifino.nestor.jdbc.pools.configuration;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.metatype.annotations.Designate;

import com.amplifino.nestor.adapters.ConnectionPoolDataSourceAdapter;
import com.amplifino.nestor.adapters.ConnectionPoolDataSourceXaAdapter;
import com.amplifino.nestor.adapters.DataSourceAdapter;
import com.amplifino.nestor.jdbc.pools.PoolDataSource;

@Component(configurationPolicy=ConfigurationPolicy.REQUIRE)
@Designate(ocd=DataSourceConfiguration.class, factory=true)
public class DataSourceProvider {

	@Reference
	private DataSourceFactory dataSourceFactory;
	private PoolDataSource dataSource;
	private ServiceRegistration<DataSource> registration;
	
	@Activate
	public void activate(BundleContext context, DataSourceConfiguration configuration) throws SQLException {
		Properties props = new Properties();
		props.put(DataSourceFactory.JDBC_URL, configuration.url());
		props.put(DataSourceFactory.JDBC_USER, configuration.user());
		props.put(DataSourceFactory.JDBC_PASSWORD, configuration._password());
		ConnectionPoolDataSource connectionPoolDataSource = createConnectionPoolDataSource(configuration);
		PoolDataSource.Builder builder = PoolDataSource.builder(connectionPoolDataSource)
			.name(configuration.dataSourceName())
			.initialSize(configuration.initialPoolSize());
		if (configuration.isValidTimeout() < 0) {
			builder.skipIsValid();
		} else {
			builder.isValidTimeout(configuration.isValidTimeout());
		}
		if (configuration.maxPoolSize() > 0) {
			builder.maxSize(configuration.maxPoolSize());
		}
		if (configuration.minPoolSize() > 0) {
			builder.maxIdle(configuration.minPoolSize());
		}
		if (configuration.maxIdleTime() > 0) {
			builder.maxIdleTime(configuration.maxIdleTime(), TimeUnit.SECONDS);
		}
		if (configuration.fifo()) {
			builder.fifo();
		} else {
			builder.lifo();
		}
		dataSource = builder.build();
		Dictionary<String, Object> dictionary = new Hashtable<>();
		dictionary.put(DataSourceFactory.JDBC_DATABASE_NAME, configuration.dataSourceName());	
		dictionary.put("application", configuration.application());
		registration = context.registerService(DataSource.class, dataSource,  dictionary);
	}
	
	private ConnectionPoolDataSource createConnectionPoolDataSource(DataSourceConfiguration configuration) throws SQLException {
		Properties props = new Properties();
		props.put(DataSourceFactory.JDBC_URL, configuration.url());
		props.put(DataSourceFactory.JDBC_USER, configuration.user());
		props.put(DataSourceFactory.JDBC_PASSWORD, configuration._password());
		switch(configuration.factoryMethod()) {
			case DATASOURCE:
				return ConnectionPoolDataSourceAdapter.on(dataSourceFactory.createDataSource(props));
			case CONNECTIONPOOLDATASOURCE:
				return dataSourceFactory.createConnectionPoolDataSource(props);
			case XADATASOURCE:
				return ConnectionPoolDataSourceXaAdapter.on(dataSourceFactory.createXADataSource(props));
			case DRIVER:
				DataSource dataSource = DataSourceAdapter.on(dataSourceFactory.createDriver(new Properties()), 
					configuration.url(), configuration.user(), configuration._password());
				return ConnectionPoolDataSourceAdapter.on(dataSource);
			default:
				throw new IllegalArgumentException();
		}
	}
	
	@Deactivate 
	public void deactivate() {
		dataSource.close();
		registration.unregister();
	}
}
