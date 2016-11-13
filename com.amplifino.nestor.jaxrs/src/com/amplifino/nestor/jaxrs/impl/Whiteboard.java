package com.amplifino.nestor.jaxrs.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.core.Application;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.jaxrs.whiteboard.JaxRSWhiteboardConstants;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.tracker.ServiceTracker;

import com.amplifino.nestor.jaxrs.JerseyTracker;

@Component
@Designate(ocd=WhiteboardConfiguration.class)
public class Whiteboard {
	
	private BundleContext context;
	private ResourceTracker resourceTracker;
	private FilterTracker filterTracker;
	private InterceptorTracker interceptorTracker;
	private ApplicationTracker applicationTracker;
	private final Map<String, ManagedResource> resources = new HashMap<>();
	private final Map<ServiceReference<?>, Object> filters = new HashMap<>();
	private final Map<ServiceReference<?>, Object> interceptors = new HashMap<>();
	
	@Reference
	private HttpService httpService;
	@Reference
	private JerseyTracker jerseyTracker;
	private WhiteboardConfiguration config;

	@Activate
	public void activate(BundleContext context, WhiteboardConfiguration config) throws InvalidSyntaxException {
		this.config = config;
		this.context = context;
		filterTracker = new FilterTracker(context);
		interceptorTracker = new InterceptorTracker(context);
		applicationTracker = new ApplicationTracker(context);
		resourceTracker = new ResourceTracker(context);
		filterTracker.open();
		interceptorTracker.open();
		applicationTracker.open();
		resourceTracker.open();
	}
	
	@Deactivate
	public synchronized void deactivate() {
		resourceTracker.close();
		applicationTracker.close();
		interceptorTracker.close();
		filterTracker.close();
		resources.values().forEach(ManagedResource::unregister);
	}
	
	private String normalize(String alias) {
		if (alias.isEmpty() || "/".equals(alias)) {
			return "/";
		}
		if (alias.charAt(0) != '/') {
			return normalize("/" + alias);
		}
		if (alias.charAt(alias.length() - 1) == '/') {
			if (alias.charAt(alias.length() - 2) == '/') {
				throw new IllegalArgumentException(alias);
			} else {
				return alias.substring(0, alias.length() - 1);
			}
		} else {
			return alias;
		}
	}
	
	private String normalize(ServiceReference<?> reference, String key) {
		return normalize((String) reference.getProperty(key));
	}
	
	private List<String> normalizedList(ServiceReference<?> reference, String key) {
		Object stringy = reference.getProperty(key);
		if (stringy instanceof String) {
			return Arrays.asList(normalize((String) stringy));
		}
		if (stringy instanceof String[]) {
			return Arrays.stream((String[]) stringy).map(this::normalize).collect(Collectors.toList());
		}
		if (stringy instanceof Collection) {
			return ((Collection<?>) stringy).stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.collect(Collectors.toList());
		}
		throw new IllegalArgumentException(Objects.toString(stringy));
	}
	
	private ManagedResource resource(String alias) {
		return resources.computeIfAbsent(alias, this::newResource);
	}
	
	private ManagedResource newResource(String alias) {
		ManagedResource resource = new ManagedResource(this, alias);
		for (Map.Entry<ServiceReference<?>, Object> entry : filters.entrySet()) {
			String filterBase = normalize(entry.getKey(), JaxRSWhiteboardConstants.JAX_RS_FILTER_BASE);
			if (alias.startsWith(filterBase)) {
				resource.add(entry.getKey(), entry.getValue());
			}
		}
		for (Map.Entry<ServiceReference<?>, Object> entry : interceptors.entrySet()) {
			String interceptorBase = normalize(entry.getKey(), JaxRSWhiteboardConstants.JAX_RS_INTERCEPTOR_BASE);
			if (alias.startsWith(interceptorBase)) {
				resource.add(entry.getKey(), entry.getValue());
			}
		}
		return resource;
	}
	
	BundleContext bundleContext() {
		return context;
	}
	
	String endpoint() {
		return config.osgi_jaxrs_endpoint();
	}
	
	HttpService httpService() {
		return httpService;
	}
	
	private synchronized Object registerResource(ServiceReference<?> reference) {
		ManagedResource resource = resource(normalize(reference, JaxRSWhiteboardConstants.JAX_RS_RESOURCE_BASE));
		Object service = context.getService(reference);
		resource.add(reference, service);
		resource.publish();
		return service;
	}
	
	private synchronized void unregisterResource(ServiceReference<?> reference, Object service) {
		String alias = normalize(reference, JaxRSWhiteboardConstants.JAX_RS_RESOURCE_BASE);
		ManagedResource resource = resources.get(alias);
		if (resource != null) {
			resource.remove(service);
			resource.publish();
			if (resource.isEmpty()) {
				resources.remove(alias);
			}
		}
		context.ungetService(reference);
	}
		
	private synchronized Object registerFilter(ServiceReference<?> reference) {
		List<String> aliases = normalizedList(reference, JaxRSWhiteboardConstants.JAX_RS_FILTER_BASE);
		Object service = context.getService(reference);
		filters.put(reference, service);
		return registerFilterOrInterceptor(reference, service, aliases);		
	}
	
	private synchronized void unregisterFilter(ServiceReference<?> reference, Object service) {
		List<String> aliases= normalizedList(reference, JaxRSWhiteboardConstants.JAX_RS_FILTER_BASE);
		unregisterFilterOrInterceptor(reference, service, aliases);
		filters.remove(reference);
		context.ungetService(reference);
	}
	
	private synchronized Object registerInterceptor(ServiceReference<?> reference) {
		List<String> aliases = normalizedList(reference, JaxRSWhiteboardConstants.JAX_RS_INTERCEPTOR_BASE);
		Object service = context.getService(reference);
		interceptors.put(reference, service);
		return registerFilterOrInterceptor(reference, service, aliases);		
	}
	
	private synchronized void unregisterInterceptor(ServiceReference<?> reference, Object service) {
		List<String> aliases = normalizedList(reference, JaxRSWhiteboardConstants.JAX_RS_FILTER_BASE);
		unregisterFilterOrInterceptor(reference, service, aliases);
		interceptors.remove(reference);
		context.ungetService(reference);
	}
	
	private boolean startsWithAny(String base, List<String> aliases) {
		return aliases.stream().anyMatch(base::startsWith);
	}
	
	private Object registerFilterOrInterceptor(ServiceReference<?> reference, Object service, List<String> aliases) {
		for (Map.Entry<String, ManagedResource> entry : resources.entrySet()) {
			if (startsWithAny(entry.getKey(), aliases)) {
				entry.getValue().add(reference, service);
				entry.getValue().publish();				
			}
		}
		return service;		
	}
	
	private void unregisterFilterOrInterceptor(ServiceReference<?> reference, Object service, List<String> aliases) {
		for (Map.Entry<String, ManagedResource> entry : resources.entrySet()) {
			if (startsWithAny(entry.getKey(), aliases)) { 
				entry.getValue().remove(service);
				entry.getValue().publish();				
			}
		}
		context.ungetService(reference);
	}
	
	private synchronized Application registerApplication(ServiceReference<Application> reference) {
		ManagedResource resource = resource(normalize(reference, JaxRSWhiteboardConstants.JAX_RS_APPLICATION_BASE));
		Application application = context.getService(reference);
		resource.application(application);
		resource.publish();
		return application;
	}
	
	private synchronized void unregisterApplication(ServiceReference<Application> reference, Application application) {
		String alias = normalize(reference, JaxRSWhiteboardConstants.JAX_RS_APPLICATION_BASE);
		ManagedResource resource = resources.get(alias);
		if (resource != null) {
			resource.removeApplication(application);
			resource.publish();
			if (resource.isEmpty()) {
				resources.remove(alias);
			}
		}
		context.ungetService(reference);
	}
	
	private class ResourceTracker extends ServiceTracker<Object, Object> {
		
		private static final String filter = "(" + JaxRSWhiteboardConstants.JAX_RS_RESOURCE_BASE + "=*)";
		
		ResourceTracker(BundleContext context) throws InvalidSyntaxException {
			super(context, context.createFilter(filter), null);
		}
		
		@Override
		public Object addingService(ServiceReference<Object> reference) {
			return registerResource(reference);			
		}
		
		@Override
		public void modifiedService(ServiceReference<Object> reference, Object service) {
			removedService(reference, service);
			addingService(reference);
		}
		
		@Override
		public void removedService(ServiceReference<Object> reference, Object service) {
			unregisterResource(reference, service);
			context.ungetService(reference);
		}
	}
	
	private class FilterTracker extends ServiceTracker<Object, Object> {
		
		private static final String filter = "(" + JaxRSWhiteboardConstants.JAX_RS_FILTER_BASE + "=*)"; 
		
		FilterTracker(BundleContext context) throws InvalidSyntaxException {
			super(context, context.createFilter(filter), null);
		}
		
		@Override
		public Object addingService(ServiceReference<Object> reference) {
			return registerFilter(reference);			
		}
		
		@Override
		public void modifiedService(ServiceReference<Object> reference, Object service) {
			removedService(reference, service);
			addingService(reference);
		}
		
		@Override
		public void removedService(ServiceReference<Object> reference, Object service) {
			unregisterFilter(reference, service);	
		}
	}
	
	class InterceptorTracker extends ServiceTracker<Object, Object> {
		
		private static final String filter = "(" + JaxRSWhiteboardConstants.JAX_RS_INTERCEPTOR_BASE + "=*)"; 
		
		InterceptorTracker(BundleContext context) throws InvalidSyntaxException {
			super(context, context.createFilter(filter), null);
		}
		
		@Override
		public Object addingService(ServiceReference<Object> reference) {
			return registerInterceptor(reference);			
		}
		
		@Override
		public void modifiedService(ServiceReference<Object> reference, Object service) {
			removedService(reference, service);
			addingService(reference);
		}
		
		@Override
		public void removedService(ServiceReference<Object> reference, Object service) {
			unregisterInterceptor(reference, service);	
		}
	}
	
	class ApplicationTracker extends ServiceTracker<Application, Application> {
		
		private static final String filter = "(&(" + Constants.OBJECTCLASS + "=javax.ws.rs.core.Application)(" + 
		JaxRSWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=*))"; 
		
		ApplicationTracker(BundleContext context) throws InvalidSyntaxException {
			super(context, context.createFilter(filter), null);
		}
		
		@Override
		public Application addingService(ServiceReference<Application> reference) {
			return registerApplication(reference);			
		}
		
		@Override
		public void modifiedService(ServiceReference<Application> reference, Application application) {
			removedService(reference, application);
			addingService(reference);
		}
		
		@Override
		public void removedService(ServiceReference<Application> reference, Application application) {
			unregisterApplication(reference, application);			
		}
	}
	
}
