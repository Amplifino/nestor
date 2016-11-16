package com.amplifino.nestor.jaxrs.test;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface SingletonConfiguration {

	String osgi_jaxrs_resource_base() default "/";
	String name();
	
}
