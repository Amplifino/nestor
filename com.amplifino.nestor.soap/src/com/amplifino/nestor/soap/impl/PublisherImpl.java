package com.amplifino.nestor.soap.impl;

import java.util.Optional;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.xml.ws.Endpoint;
import javax.xml.ws.spi.Provider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.amplifino.nestor.soap.Publisher;


@Component(immediate=true)
@Designate(ocd=PublisherConfiguration.class)
public class PublisherImpl implements Publisher {
	
	private static Logger logger = Logger.getLogger("com.amplino.soap");
	
	@Reference
	private HttpService httpService;	
	private HttpContextServlet servlet;
	private PublisherConfiguration configuration;
	private BundleContext context;
	private ServiceTracker<Object, Endpoint> tracker;
	private Optional<Provider> provider = Optional.empty();

	@Reference(cardinality=ReferenceCardinality.OPTIONAL, policyOption=ReferencePolicyOption.GREEDY)
	public void setProvider(Provider provider) {
		this.provider = Optional.of(provider);
	}
	
	private Provider provider() {
		return provider.orElseGet(() -> Provider.provider());
	}
	
	@Override
	public Endpoint publish(String path, Object implementation) {
		if (path == null || !path.startsWith("/")) {
			logger.warning("Invalid path argument: " + path);
			throw new IllegalArgumentException(path);
		}
		HttpContextImpl context = new HttpContextImpl(servlet, path);
		Endpoint endpoint = provider().createEndpoint(null, implementation.getClass(), new InvokerImpl(implementation));
		endpoint.publish(context);
		servlet.register(path, context);
		logger.info("WebService " + implementation + " deployed on " + configuration.webMountPoint() + path);
		return new EndPointDecorator(endpoint, context);
	}
	
	@Activate
	public void activate(BundleContext context, PublisherConfiguration configuration) throws ServletException, NamespaceException, InvalidSyntaxException {
		this.context = context;
		this.configuration = configuration;
		this.servlet = new HttpContextServlet(configuration.webMountPoint());
		httpService.registerServlet(configuration.webMountPoint(), servlet, null, null);
		tracker = new ServiceTracker<>(context, context.createFilter(filter()), new WebServiceTrackerCustomizer());
		tracker.open();
	}
	
	@Deactivate
	public void deactivate() {
		tracker.close();
		httpService.unregister(configuration.webMountPoint());
	}
	
	private String filter() {
		return "(&(" + LOCAL_ENDPOINT_ADDRESS + "=/*)(!(" + LOCAL_ENDPOINT_ADDRESS + "=*/)))"; 
	}
	
	
	private class WebServiceTrackerCustomizer implements ServiceTrackerCustomizer<Object, Endpoint> {

		@Override
		public Endpoint addingService(ServiceReference<Object> reference) {			
			return publish((String) reference.getProperty(LOCAL_ENDPOINT_ADDRESS), context.getService(reference));
		}

		@Override
		public void modifiedService(ServiceReference<Object> reference, Endpoint service) {
		}

		@Override
		public void removedService(ServiceReference<Object> reference, Endpoint service) {
			service.stop();
			context.ungetService(reference);			
		}
		
		
	}
}
