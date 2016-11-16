package com.amplifino.nestor.jaxrs.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

@Component(service=SingletonSample.class)
@Designate(ocd=SingletonConfiguration.class, factory=true)
@Path("/")
public class SingletonSample {

	private final long start;
	private SingletonConfiguration configuration;
	@Context
	private HttpHeaders headers;
	
	public SingletonSample() {
		this.start = System.currentTimeMillis();
	}
	
	@Activate
	public void activate(SingletonConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@GET
	@Path("singleton")
	@Produces(MediaType.APPLICATION_JSON)
	public SampleDto getResource() {
		SampleDto dto = new SampleDto();
		dto.name = configuration.name();
		dto.base = configuration.osgi_jaxrs_resource_base();
		dto.headers = headers.getClass().getName();
		dto.age = System.currentTimeMillis() - start;
		return dto;
	}
	
}
