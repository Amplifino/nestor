package com.amplifino.nestor.webconsole.security.impl;

import org.osgi.service.component.annotations.Component;

import com.amplifino.nestor.webconsole.security.PasswordHashGenerator;

@Component(property={"osgi.command.scope=security", "osgi.command.function=createHash"})
public class PasswordHashGeneratorImpl implements PasswordHashGenerator {
	
	@Override
	public String generate(int iterations, String pass) {
		return PasswordHash.createHash(iterations, pass);
	}
	
	/**
	 * Gogo console command for generating password hash
	 * @param iterations
	 * @param pass
	 */
	public void createHash(int iterations, String pass) {
		try {
			String hash = generate(iterations, pass);
			System.out.println("Hash: " + hash);
			long start = System.nanoTime();
			if (PasswordHash.of(hash).matches(pass)) {
				long elapsed = (System.nanoTime() - start) / 1_000_000;
				System.out.println("Password verification took " + elapsed + " ms");
			} else {
				System.out.println("Program error: Password validation failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
