package com.amplifino.nestor.soap;

import javax.xml.ws.Endpoint;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Publisher {

	public static final String LOCAL_ENDPOINT_ADDRESS = "com.amplifino.soap.endpoint";   
	
	/**
	 * Most applications should prefer the whiteboard approach.
	 * If using the API, do not forget to call Endpoint.stop when
	 * your bundle or component deactivates
	 * 
	 * @param path local path 
	 * @param server web service implementation
	 * @return a SOAP endpoint
	 */
	Endpoint publish(String path, Object server);
}
