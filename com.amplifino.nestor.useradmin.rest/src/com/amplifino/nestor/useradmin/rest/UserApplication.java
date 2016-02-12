package com.amplifino.nestor.useradmin.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.UserAdmin;

@Component(service=Application.class, property={ 
		"alias=/useradmin" , 
		HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PATTERN + "=/apps/useradmin/*",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PREFIX + "=/resources",
	})
public class UserApplication extends Application {

	@Reference
	private UserAdmin userAdmin;
	
	@Override
	public Set<Class<?>> getClasses() {
		return Stream.of(UserResource.class, GroupResource.class, RoleResource.class).collect(Collectors.toSet());
	}
	
	@Override
	public Set<Object> getSingletons() {
		return Stream.of(new AbstractBinder() {
			
			@Override
			protected void configure() {
				bind(userAdmin).to(UserAdmin.class);
			}
		}).collect(Collectors.toSet());
	}
	
	static List<Role> allRoles(UserAdmin userAdmin) {
		try {
			Role[] allRoles = userAdmin.getRoles(null);
			return allRoles == null ? Collections.emptyList() : Arrays.asList(allRoles);
		} catch (InvalidSyntaxException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	static Map<String, Object> toMap(Dictionary<String, Object> props) {
		Map<String, Object> map = new HashMap<>();
		Enumeration<String> enumerator = props.keys();
		while (enumerator.hasMoreElements()) {
			String key = enumerator.nextElement();
			map.put(key, props.get(key));
		}
		return map;
	}
	
	static void update(Dictionary<String, Object> oldProps, Map<String, ?> newProps) {
		Enumeration<String> enumeration = oldProps.keys();
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			if (!newProps.containsKey(key)) {
				oldProps.remove(key);
			}
		}
		newProps.entrySet().stream()
			.filter(entry -> !Objects.equals(entry.getValue(), oldProps.get(entry.getKey())))
			.forEach(entry -> oldProps.put(entry.getKey(), entry.getValue()));		
	}
} 
