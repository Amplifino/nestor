package com.amplifino.nestor.activemq;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface ConnectionProviderConfiguration {

	String brokerUrl() default "tcp://localhost:61616?jms.clientID=nestor";
	String[] application() default {"whiteboard.queue" , "whiteboard.topic"};
	
}
