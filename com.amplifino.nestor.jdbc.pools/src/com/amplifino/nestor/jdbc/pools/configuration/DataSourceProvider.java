package com.amplifino.nestor.jdbc.pools.configuration;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.metatype.annotations.Designate;

import com.amplifino.nestor.adapters.ConnectionPoolDataSourceAdapter;
import com.amplifino.nestor.adapters.ConnectionPoolDataSourceXaAdapter;
import com.amplifino.nestor.adapters.DataSourceAdapter;
import com.amplifino.nestor.jdbc.pools.DataSourceWrapper;
import com.amplifino.nestor.jdbc.pools.PoolDataSource;

@Component(configurationPolicy=ConfigurationPolicy.REQUIRE)
@Designate(ocd=DataSourceConfiguration.class, factory=true)
public class DataSourceProvider {

	@Reference
	private DataSourceFactory dataSourceFactory;
	private final AtomicReference<DataSourceWrapper> wrapperReference = new AtomicReference<>();;
	private PoolDataSource dataSource;
	private ServiceRegistration<DataSource> registration;
	private DataSourceConfiguration configuration;
	private BundleContext context;
	
	@Activate
	public synchronized void activate(BundleContext context, DataSourceConfiguration configuration) throws SQLException {
		this.context = context;
		this.configuration = configuration;
		ConnectionPoolDataSource connectionPoolDataSource = createConnectionPoolDataSource(configuration);
		PoolDataSource.Builder builder = PoolDataSource.builder(connectionPoolDataSource)
			.name(configuration.dataSourceName())
			.initialSize(configuration.initialPoolSize())
			.validationIdleTime(configuration.validationIdleTime(), TimeUnit.SECONDS);
		if (configuration.isValidTimeout() < 0) {
			builder.skipIsValid();
		} else {
			builder.isValidTimeout(configuration.isValidTimeout());
		}
		if (configuration.validationQuery() != null && !configuration.validationQuery().trim().isEmpty()) {
			builder.validationQuery(configuration.validationQuery().trim());
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
		if (configuration.propertyCycle() > 0) {
			builder.cycleTime(configuration.propertyCycle(), TimeUnit.SECONDS);
		}
 		if (configuration.fifo()) {
			builder.fifo();
		} else {
			builder.lifo();
		}
		dataSource = builder.build();
		register();
	}
	
	private void register() {
		Dictionary<String, Object> dictionary = new Hashtable<>();
		dictionary.put(DataSourceFactory.JDBC_DATABASE_NAME, configuration.dataSourceName());	
		dictionary.put("application", configuration.application());
		registration = context.registerService(DataSource.class, wrap(dataSource),  dictionary);
	}
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC) 
	public synchronized void setWrapper(DataSourceWrapper wrapper) {
		wrapperReference.set(wrapper);
		refresh();
	}
	
	public synchronized void unsetWrapper(DataSourceWrapper wrapper) {
		if (wrapperReference.compareAndSet(wrapper, null)) {
			refresh();
		}
	}
	
	private void refresh() {
		if (registration != null && configuration.trace()) {			
			registration.unregister();
			register();
		}
	}
	
	private DataSource wrap(DataSource dataSource) {
		DataSourceWrapper wrapper = wrapperReference.get();
		return wrapper == null || !configuration.trace() ? dataSource : wrapper.wrap(dataSource);				
	}
	
	private ConnectionPoolDataSource createConnectionPoolDataSource(DataSourceConfiguration configuration) throws SQLException {
		Properties props = new Properties();
		props.put(DataSourceFactory.JDBC_URL, configuration.url());
		props.put(DataSourceFactory.JDBC_USER, configuration.user());
		props.put(DataSourceFactory.JDBC_PASSWORD, configuration._password());
		for (String extraProperty : configuration.additionalProperties()) {
			if (extraProperty != null && !extraProperty.trim().isEmpty()) {
				String[] entry = parseProperty(extraProperty);
				props.put(entry[0], entry[1]);
			}
		}
		switch(configuration.factoryMethod()) {
			case DATASOURCE:
				return ConnectionPoolDataSourceAdapter.on(dataSourceFactory.createDataSource(props));
			case CONNECTIONPOOLDATASOURCE:
				return dataSourceFactory.createConnectionPoolDataSource(props);
			case XADATASOURCE:
				return ConnectionPoolDataSourceXaAdapter.on(dataSourceFactory.createXADataSource(props));
			case DRIVER:
				props.remove(DataSourceFactory.JDBC_URL);
				return ConnectionPoolDataSourceAdapter.on(DataSourceAdapter.on(dataSourceFactory.createDriver(new Properties()), 
					configuration.url(), props));
			default:
				throw new IllegalArgumentException();
		}
	}
	
	private String[] parseProperty(String property) {
		String[] parts = property.split("=", 2);
		if (parts.length != 2) {
			throw new IllegalArgumentException(property);
		}
		return parts;
	}
	
	@Deactivate 
	public synchronized void deactivate() {
		dataSource.close();
		registration.unregister();
		registration = null;
	}
}
