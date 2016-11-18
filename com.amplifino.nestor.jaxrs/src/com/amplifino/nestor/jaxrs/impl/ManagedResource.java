package com.amplifino.nestor.jaxrs.impl;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.NamespaceException;

class ManagedResource {
	
	private final Whiteboard whiteboard;
	private final String base;
	private final DynamicApplication application = new DynamicApplication(this);
	private ServletContainer container;
		
	ManagedResource(Whiteboard whiteboard, String base) {
		this.whiteboard = whiteboard;
		this.base = base;
	}
	
	void unregister() {
		if (container != null) {
			whiteboard.httpService().unregister(alias(whiteboard.endpoint()));
			container = null;
		}
	}
	
	String base() {
		return base;
	}
	
	String alias(String mountPoint) {
		return mountPoint + ("/".equals(base) ? "" : base);		
	}

	void publish() {
		if (container == null) {
			if (!application.isEmpty()) {
				container = new ServletContainer(resourceConfig());
				try {
					whiteboard.httpService().registerServlet(alias(whiteboard.endpoint()), container, null, null);
				} catch (ServletException | NamespaceException e) {				
					e.printStackTrace();
				}
			}
		} else {
			if (application.isEmpty()) {
				unregister();				
			} else {
				container.reload(resourceConfig());
			}
		}
	}
	
	ResourceConfig resourceConfig() {
		return ResourceConfig.forApplication(application);
	}
	
	void add(ServiceReference<?> reference, Object service) {
		if (isPrototype(reference)) {
			application.add(service, whiteboard.bundleContext().getServiceObjects(reference));
		} else {
			application.add(service);
		}
	}

	void remove(Object service) {
		application.remove(service);		
	}

	ServiceLocator getServiceLocator() {
		return container.getApplicationHandler().getServiceLocator();
	}
	
	private boolean isPrototype(ServiceReference<?> reference) {
		return Constants.SCOPE_PROTOTYPE.equals(reference.getProperty(Constants.SERVICE_SCOPE));
	}
	
	boolean isEmpty() {
		return application.isEmpty();
	}

	public void application(Application application) {
		this.application.application(application);
		
	}

	public void removeApplication(Application application2) {
		this.application.removeApplication();
		
	}
}


