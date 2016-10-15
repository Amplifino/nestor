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
	@AttributeDefinition(description="Use first in first out scheduling if true, last in first out if false")
	boolean fifo() default false;
	String[] application();
	@AttributeDefinition(description="Timeout in seconds to use on connection.isValid() call. Specify -1 to skip isValid() if your JDCB driver does not support isValid")
	int isValidTimeout() default 0;
	@AttributeDefinition(description="Query to validate connection. Use if driver does not support isValid()")
	String validationQuery() default "";
	@AttributeDefinition(description="Minimum idle time in seconds to perform validation on connection ")
	int validationIdleTime();
	@AttributeDefinition(description="Time in seconds between scans for expired connections")
	int propertyCycle();
	@AttributeDefinition(description="Ldap filter for DataSourceFactory")
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
