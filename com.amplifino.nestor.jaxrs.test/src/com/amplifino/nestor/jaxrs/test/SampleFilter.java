package com.amplifino.nestor.jaxrs.test;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxRSWhiteboardConstants;

@Component(property={JaxRSWhiteboardConstants.JAX_RS_FILTER_BASE + "=/"})
public class SampleFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext context) throws IOException {
		System.out.println(context.getUriInfo().getAbsolutePath());
	}

	
}
