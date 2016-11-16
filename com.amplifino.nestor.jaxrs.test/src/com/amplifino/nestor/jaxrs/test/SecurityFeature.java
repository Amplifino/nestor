package com.amplifino.nestor.jaxrs.test;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxRSWhiteboardConstants;

@Component(property={JaxRSWhiteboardConstants.JAX_RS_FILTER_BASE+ "=/base"})
public class SecurityFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {
		context.register(RolesAllowedDynamicFeature.class);
		return true;
	}

}
