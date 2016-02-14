package com.amplifino.nestor.soap.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.spi.Invoker;

class InvokerImpl extends Invoker {
	
	private final Object implementation;
	private WebServiceContext webServiceContext;
	
	InvokerImpl(Object implementation) {
		this.implementation = implementation;
	}

	@Override
	public void inject(WebServiceContext webServiceContext) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.webServiceContext = webServiceContext;
		for (Class<?> clazz = implementation.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(Resource.class) && field.getType().isAssignableFrom(WebServiceContext.class)) {
					field.setAccessible(true);
					field.set(implementation, webServiceContext);
				}
			}
			for (Method method : clazz.getDeclaredMethods()) {
				if (method.isAnnotationPresent(Resource.class) 
						&& method.getParameterCount() == 1 
						&& method.getParameterTypes()[0].isAssignableFrom(WebServiceContext.class)) {
					method.setAccessible(true);
					method.invoke(implementation, webServiceContext);
				}
			}
		}
		for (Class<?> clazz = implementation.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			for (Method method : clazz.getDeclaredMethods()) {
				if (method.isAnnotationPresent(PostConstruct.class) && method.getParameterCount() == 0) {
					method.setAccessible(true);
					method.invoke(implementation);
				}
			}
		}
	}

	@Override
	public Object invoke(Method m, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		checkRole(m);
		return m.invoke(implementation, args);
	}
		
	private void checkRole(Method m) throws InvocationTargetException {
		if (!hasAccess(m , webServiceContext::isUserInRole)) {
			throw new InvocationTargetException(new ProtocolException("Forbidden"));
		}
	}
	
	private boolean hasAccess(Method m , Predicate<String> filter) {
		return rolesAllowed(m).map(Arrays::stream).map(stream -> stream.anyMatch(filter)).orElse(true);
	}
	
	private Optional<String[]> rolesAllowed(AnnotatedElement element) {
		if (element.isAnnotationPresent(RolesAllowed.class)) {
			return Optional.of(element.getAnnotation(RolesAllowed.class).value());
		}
		if (element.isAnnotationPresent(PermitAll.class)) {
			return Optional.empty();
		}
		if (element.isAnnotationPresent(DenyAll.class)) {
			return Optional.of(new String[0]);
		}
		if (element instanceof Method) {
			return rolesAllowed(((Method) element).getDeclaringClass());
		} else {
			return Optional.empty();
		}
	}

	
}
