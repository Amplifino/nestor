package com.amplifino.nestor.jaxrs;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface WhiteboardConfiguration {

	String osgi_jaxrs_endpoint() default "/rest";
}
