package com.amplifino.nestor.transaction.datasources.configuration;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name="Transactional DataSource Configuration")
public @interface TransactionalDataSourceConfiguration {

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
	@AttributeDefinition(name="Use isValid", description="Use isValid() for testing the connection on borrow. Not all drivers support this")
	boolean useConnectionIsValid() default true;
	@AttributeDefinition(description="ldap filter for DataSourceFactory")
	String dataSourceFactory_target() default "(osgi.jdbc.driver.name=*)";
	@AttributeDefinition(description="For two phase commit XADATASOURCE is required")
	FactoryMethod factoryMethod() default FactoryMethod.XADATASOURCE;
	String webconsole_configurationFactory_nameHint() default "DataSource {dataSourceName} for applications {application}";
	
	enum FactoryMethod {
		DATASOURCE,
		CONNECTIONPOOLDATASOURCE,
		XADATASOURCE,
		DRIVER
	}
		
}
