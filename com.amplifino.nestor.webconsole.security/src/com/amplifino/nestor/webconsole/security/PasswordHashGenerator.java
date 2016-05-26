package com.amplifino.nestor.webconsole.security;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface PasswordHashGenerator {

	/**
	 * The generated string has the following structure:
	 * iterations:base64(salt):base64(hash)
	 * @param iterations
	 * @param password
	 * @return
	 */
	String generate(int iterations, String password);
}
