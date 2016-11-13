package com.amplifino.nestor.transaction.spi;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface TransactionLogConfiguration {

	int formatId() default 0x416d706c;
	
}

