package com.amplifino.nestor.jdbc.pools.configuration;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name="DataSource Configuration")
public @interface DataSourceConfiguration {

	String dataSourceName();
	String url();
	String user();
	String _password();
	@AttributeDefinition(description="Use key=value syntax to add driver properties. Note that most drivers allow you to specify properties on the url")
	String[] additionalProperties() default {};
	int initialPoolSize() default 0;
	int maxPoolSize() default 0;
	int minPoolSize() default 0;
	@AttributeDefinition(description="Use first in first out scheduling if true, last in first out if false")
	boolean fifo() default false;
	@AttributeDefinition(description="Maximum connection idle time in seconds")
	int maxIdleTime() default 0;
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
