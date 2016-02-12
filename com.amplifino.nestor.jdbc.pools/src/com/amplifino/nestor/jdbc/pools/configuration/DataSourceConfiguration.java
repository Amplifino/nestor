package com.amplifino.nestor.jdbc.pools.configuration;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name="DataSource Configuration")
public @interface DataSourceConfiguration {

	String dataSourceName();
	String url();
	String user();
	String _password();
	int initialPoolSize() default 0;
	int maxPoolSize() default 0;
	int minPoolSize() default 0;
	@AttributeDefinition(description="maximum idle time in seconds")
	int maxIdleTime() default 0;
	String[] application();
	@AttributeDefinition(description="Use isValid() for testing the connection on borrow. Not all drivers support this")
	boolean useConnectionIsValid() default true;
	@AttributeDefinition(description="ldap filter for DataSourceFactory")
	String dataSourceFactory_target() default "(osgi.jdbc.driver.name=*)";
	@AttributeDefinition(description="When using DATASOURCE or DRIVER the configured dataSource will return wrapped Connections")
	FactoryMethod factoryMethod() default FactoryMethod.CONNECTIONPOOLDATASOURCE;
	String webconsole_configurationFactory_nameHint() default "DataSource {dataSourceName} for applications {application}";
	
	enum FactoryMethod {
		DATASOURCE,
		CONNECTIONPOOLDATASOURCE,
		XADATASOURCE,
		DRIVER
	}
	
}
