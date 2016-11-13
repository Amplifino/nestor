package com.amplifino.nestor.jaxrs.impl;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.osgi.framework.ServiceObjects;

class DynamicApplication extends Application {
	
	private final Set<Object> singletons = new HashSet<>();
	private final Map<Object, ServiceObjects<?>> prototypes = new IdentityHashMap<>();
	private final ManagedResource managedResource;
	private Optional<Application> application = Optional.empty();
	
	DynamicApplication(ManagedResource managedResource) {
		this.managedResource = managedResource;
	}
	
	void add(Object service) {
		singletons.add(service);
	}

	void add(Object service, ServiceObjects<?> serviceObjects) {
		prototypes.put(service, serviceObjects);
	}
	
	void remove(Object service) {
		if (!singletons.remove(service)) {
			prototypes.remove(service);
		}
	}

	void application(Application application) {
		this.application = Optional.of(application);
	}
	
	void removeApplication() {
		this.application = Optional.empty();
	}
	
	private Stream<Class<?>> applicationClasses() {
		return application.map(Application::getClasses).map(Set::stream).orElse(Stream.empty()); 
	}
	
	private Stream<Object> applicationSingletons() {
		return application.map(Application::getSingletons).map(Set::stream).orElse(Stream.empty());
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return Stream.of(applicationClasses(), prototypes.keySet().stream().map(Object::getClass))
			.flatMap(Function.identity())
			.collect(Collectors.toSet()); 
	}
	
	@Override
	public Set<Object> getSingletons() {
		return Stream.of(applicationSingletons(), singletons.stream(), Stream.of(getBinder()))
			.flatMap(Function.identity())
			.collect(Collectors.toSet());
	}
	
	@Override
	public Map<String, Object> getProperties() {
		return application.map(Application::getProperties).orElseGet(() -> super.getProperties());
	}
	
	private ServiceLocator locator() {
		return managedResource.getServiceLocator();
	}
	
	boolean isEmpty() {
		return prototypes.isEmpty() && singletons.isEmpty() && !application.isPresent();
	}
	
	private AbstractBinder getBinder() {
		return new AbstractBinder() {
			
			@Override
			protected void configure() {				
				prototypes.forEach(this::add);
			}
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			private void add(Object object, ServiceObjects<?> serviceObjects) {				
				bindFactory(new FactoryImpl(serviceObjects)).to(object.getClass()).in(RequestScoped.class);
			}
		};
	}
	
	private class FactoryImpl<T> implements Factory<T> {
		
		private final ServiceObjects<T> serviceObjects;
		
		FactoryImpl(ServiceObjects<T> serviceObjects) {
			this.serviceObjects = serviceObjects;
		}
		
		@Override
		@PerLookup
		public T provide() {
			T result = serviceObjects.getService();
			locator().inject(result);
			return result;				
		}

		@Override
		public void dispose(T instance) {			
			serviceObjects.ungetService(instance);
		}			
		
	}
	
	
	
}
