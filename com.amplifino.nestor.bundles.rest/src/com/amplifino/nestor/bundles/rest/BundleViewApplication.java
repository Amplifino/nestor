package com.amplifino.nestor.bundles.rest;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import com.amplifino.nestor.dot.DotService;

@Component(service=Application.class, property={
		"alias=/bundles", 
		HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PATTERN + "=/apps/bundles/*",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PREFIX + "=/resources"})

public class BundleViewApplication extends Application {

	private volatile BundleContext context;
	
	@Reference
	private DotService dotService;
	
	public BundleViewApplication()  {
	}

	@Activate
	public void activate(BundleContext context) {
		this.context = context; 
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return Stream.of(BundleResource.class).collect(Collectors.toSet());
	}
	
	@Override
	public Set<Object> getSingletons() {
		return Stream.of(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(context).to(BundleContext.class);		
				bind(dotService).to(DotService.class);
			}
		}).collect(Collectors.toSet());
	}

	@Override
	public String toString() {
		return "Osgi Application";
	}
	

}
