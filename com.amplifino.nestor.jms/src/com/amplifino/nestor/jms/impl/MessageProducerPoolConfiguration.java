package com.amplifino.nestor.jms.impl;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface MessageProducerPoolConfiguration {

	DestinationType destinationType() default DestinationType.NONE;
	String destination();
	int maxIdle() default 0;
	int maxSize() default 0;
}
