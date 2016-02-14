package com.amplifino.nestor.soap.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;

import org.w3c.dom.Element;

public class EndPointDecorator extends Endpoint {

	private final Endpoint endpoint;
	private final HttpContextImpl httpContext;
	
	public EndPointDecorator(Endpoint endpoint, HttpContextImpl httpContext) {
		this.httpContext = httpContext;
		this.endpoint = endpoint;
	}

	@Override
	public Binding getBinding() {
		return endpoint.getBinding();
	}

	@Override
	public Object getImplementor() {
		return endpoint.getImplementor();
	}

	@Override
	public void publish(String address) {
		 endpoint.publish(address);		
	}

	@Override
	public void publish(Object serverContext) {
		endpoint.publish(serverContext);
	}

	@Override
	public void stop() {
		httpContext.dispose();
		endpoint.stop();
		Logger.getLogger("com.amplino.soap").info("WebService on " + httpContext.getPath() + " stopped");
	}

	@Override
	public boolean isPublished() {
		return endpoint.isPublished();
	}

	@Override
	public List<Source> getMetadata() {
		return endpoint.getMetadata();
	}

	@Override
	public void setMetadata(List<Source> metadata) {
		endpoint.setMetadata(metadata);
		
	}

	@Override
	public Executor getExecutor() {
		return endpoint.getExecutor();
	}

	@Override
	public void setExecutor(Executor executor) {
		endpoint.setExecutor(executor);		
	}

	@Override
	public Map<String, Object> getProperties() {
		return endpoint.getProperties();
	}

	@Override
	public void setProperties(Map<String, Object> properties) {
		endpoint.setProperties(properties);
		
	}

	@Override
	public EndpointReference getEndpointReference(Element... referenceParameters) {
		return endpoint.getEndpointReference(referenceParameters);
	}

	@Override
	public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
		return endpoint.getEndpointReference(clazz, referenceParameters);
	}
	
}
