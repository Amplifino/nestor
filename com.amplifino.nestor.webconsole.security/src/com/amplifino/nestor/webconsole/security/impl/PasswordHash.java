package com.amplifino.nestor.webconsole.security.impl;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

class PasswordHash {

	private static final String algorithm = "PBKDF2WithHmacSHA512";
	
	private int iterations;
	private byte[] salt;
	private byte[] hash; 
	
	private PasswordHash(String hash) {
		String[] parts = Objects.requireNonNull(hash).split(":");
		if (parts.length != 3) {
			throw new IllegalArgumentException(hash);
		}
		iterations = Integer.parseInt(parts[0]);
		salt = Base64.getDecoder().decode(parts[1]);
		this.hash = Base64.getDecoder().decode(parts[2]);
	}
	
	static PasswordHash of(String hash) {
		return new PasswordHash(hash);
	}

	boolean matches(String password)  {
		return Arrays.equals(
			hash, 
			PasswordHash.calculateHash(password.toCharArray(), salt, iterations, hash.length * Byte.SIZE));
	}
	
	static byte[] calculateHash( char[] password, byte[] salt, int iterations, int length) {
		try {
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, length);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
			return skf.generateSecret(spec).getEncoded();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	static String createHash(int iterations, String password) {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[32];
		random.nextBytes(salt);
		byte[] hash = calculateHash(password.toCharArray(), salt, iterations, 32 * Byte.SIZE);
		return String.join(":", 
	    	Integer.toString(iterations), 
	    	Base64.getEncoder().encodeToString(salt),
	    	Base64.getEncoder().encodeToString(hash));
	}
}
