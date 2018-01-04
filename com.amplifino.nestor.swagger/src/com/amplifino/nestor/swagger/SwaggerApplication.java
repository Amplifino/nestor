package com.amplifino.nestor.swagger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import io.swagger.jaxrs.listing.SwaggerSerializers;

@Component(service=Application.class, property={"alias=/doc",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PATTERN + "=/apps/swagger/*",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PREFIX + "=/resources/web"})
public class SwaggerApplication extends Application {
	
	private final Map<String, Application> applications = new ConcurrentHashMap<>();

	@Reference(cardinality=ReferenceCardinality.AT_LEAST_ONE, policy=ReferencePolicy.DYNAMIC, target="(&(alias=/*)(!(alias=/doc)))") 
	public void addApplication(Application application, Map<String, Object> properties) {
		Object alias = properties.get("alias");
		if  (alias == null || ! (alias instanceof String)) {
			return;
		}
		applications.put((String) alias, application);
	}
	
	public void removeApplication(Application application, Map<String, Object> properties) {
		applications.remove(properties.get("alias"), application);
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		SwaggerSerializers.setPrettyPrint(true);
		Set<Class<?>> resources = new HashSet<>();
		resources.add(ApiListing.class);
		resources.add(SwaggerSerializers.class);
		resources.add(CorsFilter.class);
		return resources;
	}
	
	@Override
	public Set<Object> getSingletons() {
		return Collections.singleton(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(SwaggerApplication.this).to(SwaggerApplication.class);		
			}
		});
	}

	Optional<Application> getApplication(String alias) {
		return Optional.ofNullable(applications.get("/" + alias));
	}
	
}
