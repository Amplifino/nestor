package com.amplifino.nestor.jaxrs;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Application;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.osgi.framework.ServiceObjects;

public class DynamicApplication extends Application {
	
	private final Set<Object> singletons = new HashSet<>();
	private final Map<Object, ServiceObjects<?>> prototypes = new IdentityHashMap<>();
	private final Whiteboard whiteboard;
	
	public DynamicApplication(Whiteboard whiteboard) {
		this.whiteboard = whiteboard;
	}
	
	public void add(Object service) {
		singletons.add(service);
	}

	public void add(Object service, ServiceObjects<?> serviceObjects) {
		prototypes.put(service, serviceObjects);
	}
	
	public void remove(Object service) {
		if (!singletons.remove(service)) {
			prototypes.remove(service);
		}
	}

	@Override
	public Set<Class<?>> getClasses() {
		return prototypes.keySet().stream().map(Object::getClass).collect(Collectors.toSet()); 
	}
	
	@Override
	public Set<Object> getSingletons() {
		return Stream.concat(singletons.stream(), Stream.of(getBinder())).collect(Collectors.toSet());
	}
	
	private ServiceLocator locator() {
		return whiteboard.getServiceLocator();
	}
	
	boolean isEmpty() {
		return prototypes.isEmpty() && singletons.isEmpty();
	}
	
	private AbstractBinder getBinder() {
		return new AbstractBinder() {
			
			@Override
			protected void configure() {
				bind(DynamicApplication.this).to(DynamicApplication.class);
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
