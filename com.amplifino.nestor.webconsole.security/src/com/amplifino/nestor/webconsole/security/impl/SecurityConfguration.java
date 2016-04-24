package com.amplifino.nestor.webconsole.security.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name="Amplifino Web Console Security Provider")
public @interface SecurityConfguration {

	String userName() default "admin";
	@AttributeDefinition(description="PBKDF2 hash information coded as iterations:base64(salt):base64(hash)")
	String passwordHash();
}
