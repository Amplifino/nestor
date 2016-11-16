package com.amplifino.nestor.jaxrs.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jaxrs.whiteboard.JaxRSWhiteboardConstants;

@Component(service=MediaTest.class, scope=ServiceScope.PROTOTYPE, property={JaxRSWhiteboardConstants.JAX_RS_RESOURCE_BASE + "=/media/"})
@Path("/")
public class MediaTest {

	private final long start;
	
	@Context
	private HttpHeaders headers;
	
	public MediaTest() {
		this.start = System.currentTimeMillis();
	}

	@GET
	@Path("prototype.json")
	@Produces(MediaType.APPLICATION_JSON)
	public SampleDto getJsonResource() {
		SampleDto dto = new SampleDto();
		dto.name = "json";
		dto.base = "/media/";
		dto.headers = headers.getClass().getName();
		dto.age = System.currentTimeMillis() - start;
		return dto;
	}
	
	@GET
	@Path("prototype.xml")
	@Produces(MediaType.APPLICATION_XML)
	public SampleDto getXmlResource() {
		SampleDto dto = new SampleDto();
		dto.name = "xml";
		dto.base = "/media/";
		dto.headers = headers.getClass().getName();
		dto.age = System.currentTimeMillis() - start;
		return dto;
	}
}
