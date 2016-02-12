package com.amplifino.nestor.security.http;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

class DigestAuthentication extends  UserAdminHttpAuthentication {
	
	DigestAuthentication(String realm, UserAdmin userAdmin) {
		super(realm, userAdmin);		
	}
	
	@Override
	String authorizationMethod() {
		return "Digest";
	}
		
	@Override
	String challenge() {
		StringBuilder header = new StringBuilder(",");
		appendAttribute(header, "nonce", createNonce());
		header.append(",qop=auth");
		return header.toString();
	}	
	
	private String createNonce() {
		StringBuilder result = new StringBuilder("" + System.currentTimeMillis());
		result.append(":");
		result.append(System.nanoTime());
		return result.toString();
	}
	
	@Override
	Optional<User> getUser(HttpServletRequest request, String in) {
		return new DigestResponse(request.getMethod(), in).getUser();		
	}
		
    private class DigestResponse {
    	private final String method;
    	private final String in;
    	private final Map<String,String> attributes = new HashMap<>();
    	
    	DigestResponse(String method, String in) {
    		this.method = method;
    		this.in = in;
    		parse();
    	}
    	
    	private void parse() {
    		for (String part : in.split(",")) {
    			String[] subParts = part.split("=",2);						
    			String value = subParts[1];
    			if (value.startsWith("\"")) {
    				value = value.substring(1,value.length()-1);
    			}
    			attributes.put(subParts[0].trim(),value);
    		}    		
    	}
    	    	    	
    	boolean matchHa1(String ha1) {
    		if (ha1 == null) {
    			return false;
    		}
    		String ha2 = md5(method,attributes.get("uri"));			
    		String calculated = md5(ha1,attributes.get("nonce"),attributes.get("nc"),attributes.get("cnonce"),attributes.get("qop"),ha2);
    		return calculated.equals(attributes.get("response"));			
    	}
    	
    	Optional<User> getUser() {
    		return DigestAuthentication.this.getUser(attributes.get("username"), user -> matchHa1((String) user.getCredentials().get("HA1")));	
    	}
    		
    	private String md5(String ...strings ) {
    		try {
    			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    			String separator = null;
    			for (String each : strings) {
    				if (separator == null) {
    					separator = ":";
    				} else {
    					messageDigest.update(separator.getBytes());
    				}
    				messageDigest.update(each.getBytes());
    			}
    			return DatatypeConverter.printHexBinary(messageDigest.digest()).toLowerCase();
    		} catch (NoSuchAlgorithmException e) {
    			throw new RuntimeException(e);
    		}
    	}
    }
}
