package com.amplifino.nestor.rest.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Amplifino Rest Whiteboard")
@interface WhiteboardConfiguration {	
		@AttributeDefinition(description="web mount point for jax-rs applications")
		String webMountPoint() default "/api";	
}
