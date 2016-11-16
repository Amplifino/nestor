package com.amplifino.nestor.jaxrs.test;

import javax.ws.rs.ext.ContextResolver;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxRSWhiteboardConstants;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component(property={JaxRSWhiteboardConstants.JAX_RS_FILTER_BASE+"=/"})
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	private final ObjectMapper mapper;
	
	public ObjectMapperProvider() {
		this.mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
	}
	
	@Override
	public ObjectMapper getContext(Class<?> ignored) {
		return mapper;
	}

}
