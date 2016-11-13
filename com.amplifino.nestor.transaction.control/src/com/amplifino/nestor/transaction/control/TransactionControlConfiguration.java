package com.amplifino.nestor.transaction.control;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface TransactionControlConfiguration {

	Compliance compliance() default Compliance.ACID;
}
