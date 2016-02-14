package com.amplifino.nestor.soap.impl;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name="Amplifino Web Service Publisher Configuration")
public @interface PublisherConfiguration {
	String webMountPoint() default "/soap";
}
