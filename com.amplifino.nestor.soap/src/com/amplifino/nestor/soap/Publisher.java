package com.amplifino.nestor.soap;

import javax.xml.ws.Endpoint;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Web Service Publisher
 *
 */
@ProviderType
public interface Publisher {

	/**
	 * name of the service property to indicate that the service is a web service that must be published.
	 *   
	 */
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
