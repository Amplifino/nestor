package com.amplifino.nestor.transaction.datasources.configuration;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

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
import com.amplifino.nestor.adapters.DataSourceAdapter;
import com.amplifino.nestor.adapters.XADataSourceAdapter;
import com.amplifino.nestor.transaction.datasources.TransactionalDataSource;

@Component(name="com.amplifino.nestor.transaction.datasources", configurationPolicy=ConfigurationPolicy.REQUIRE)
@Designate(ocd=TransactionalDataSourceConfiguration.class, factory=true)
public class TransactionalDataSourceProvider {

	@Reference
	private TransactionManager transactionManager;
	@Reference
	private TransactionSynchronizationRegistry synchronization;
	@Reference
	private DataSourceFactory dataSourceFactory;
	private TransactionalDataSource dataSource;
	private ServiceRegistration<DataSource> registration;
	
	@Activate
	public void activate(BundleContext context, TransactionalDataSourceConfiguration configuration) throws SQLException {
		Properties props = new Properties();
		props.put(DataSourceFactory.JDBC_URL, configuration.url());
		props.put(DataSourceFactory.JDBC_USER, configuration.user());
		props.put(DataSourceFactory.JDBC_PASSWORD, configuration._password());
		XADataSource xaDataSource = createXADataSource(configuration);
		TransactionalDataSource.Builder builder = TransactionalDataSource.builder(xaDataSource, transactionManager, synchronization)
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
		dataSource = builder.build();
		Dictionary<String, Object> dictionary = new Hashtable<>();
		dictionary.put(DataSourceFactory.JDBC_DATABASE_NAME, configuration.dataSourceName());	
		dictionary.put("application", configuration.application());
		registration = context.registerService(DataSource.class, dataSource,  dictionary);
	}
	
	@Deactivate 
	public void deactivate() {
		dataSource.close();
		registration.unregister();
	}
	
	private XADataSource createXADataSource(TransactionalDataSourceConfiguration configuration) throws SQLException {
		Properties props = new Properties();
		props.put(DataSourceFactory.JDBC_URL, configuration.url());
		props.put(DataSourceFactory.JDBC_USER, configuration.user());
		props.put(DataSourceFactory.JDBC_PASSWORD, configuration._password());
		switch (configuration.factoryMethod()) {
			case DATASOURCE:
				return XADataSourceAdapter.on(ConnectionPoolDataSourceAdapter.on(dataSourceFactory.createDataSource(props)));
			case CONNECTIONPOOLDATASOURCE:
				return XADataSourceAdapter.on(dataSourceFactory.createConnectionPoolDataSource(props));
			case XADATASOURCE:
				return dataSourceFactory.createXADataSource(props);
			case DRIVER:
				DataSource dataSource = DataSourceAdapter.on(dataSourceFactory.createDriver(new Properties()), configuration.url(), configuration.user(), configuration._password());
				return XADataSourceAdapter.on(ConnectionPoolDataSourceAdapter.on(dataSource));
			default:
				throw new IllegalArgumentException();
		}
	}
}
