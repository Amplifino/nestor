package com.amplifino.nestor.jaxrs;

import javax.servlet.ServletException;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.tracker.ServiceTracker;

@Component
@Designate(ocd=WhiteboardConfiguration.class)
public class Whiteboard {
	
	private BundleContext context;
	private ResourceTracker tracker;
	private ServletContainer container;
	private final DynamicApplication application = new DynamicApplication(this);
	@Reference
	private HttpService httpService;
	@Reference
	private JerseyTracker jerseyTracker;
	private WhiteboardConfiguration config;

	@Activate
	public void activate(BundleContext context, WhiteboardConfiguration config) throws InvalidSyntaxException {
		this.config = config;
		this.context = context;
		tracker = new ResourceTracker(context);
		tracker.open();
	}
	
	@Deactivate
	public synchronized void deactivate() {
		tracker.close();
		if (container != null) {
			httpService.unregister(config.osgi_jaxrs_endpoint());
		}
	}
	
	private synchronized void publish() {
		if (container == null) {
			if (!application.isEmpty()) {
				container = new ServletContainer(createConfig());
				try {
					httpService.registerServlet(config.osgi_jaxrs_endpoint(), container, null, null);
				} catch (ServletException | NamespaceException e) {				
					e.printStackTrace();
				}
			}
		} else {
			if (application.isEmpty()) {
				httpService.unregister(config.osgi_jaxrs_endpoint());
				container = null;
			} else {
				container.reload(ResourceConfig.forApplication(application));
			}
		}
	}
	
	ServiceLocator getServiceLocator() {
		return container.getApplicationHandler().getServiceLocator();
	}
	
	private synchronized Object register(ServiceReference<?> reference) {
		Object service = context.getService(reference);
		if (Constants.SCOPE_PROTOTYPE.equals(reference.getProperty(Constants.SERVICE_SCOPE))) {
			application.add(service, context.getServiceObjects(reference));
		} else {
			application.add(service);
		}
		publish();
		return service;
	}
	
	private void unregister(Object service) {
		application.remove(service);
	}
	
	private ResourceConfig createConfig() {
		return ResourceConfig.forApplication(application).register(JacksonFeature.class);
	}
	
	class ResourceTracker extends ServiceTracker<Object, Object> {
		
		ResourceTracker(BundleContext context) throws InvalidSyntaxException {
			super(context, context.createFilter("(osgi.jaxrs.resource.base=*)"), null);
		}
		
		@Override
		public Object addingService(ServiceReference<Object> reference) {
			return register(reference);			
		}
		
		@Override
		public void removedService(ServiceReference<Object> reference, Object service) {
			unregister(service);
			context.ungetService(reference);
		}
	}
	
}
