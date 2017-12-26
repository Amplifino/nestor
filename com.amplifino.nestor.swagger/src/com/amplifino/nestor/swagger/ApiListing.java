package com.amplifino.nestor.swagger;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.ApiOperation;
import io.swagger.jaxrs.Reader;
import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

@Path("/")
public class ApiListing {

	@Context
	private SwaggerApplication swaggerApplication;
	
	private Optional<Swagger> process(String alias) {
		return swaggerApplication.getApplication(alias)
			.map(application -> this.toSwagger(alias, application));
	}
	
	private Swagger toSwagger(String alias, Application application) {				
		return new Reader(new Swagger().basePath("api/" + alias)).read(classes(application));        
	}

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    @ApiOperation(value = "The swagger definition in either JSON or YAML", hidden = true)
    @Path("/{alias}.{type:json|yaml}")
    public Response getListing(@PathParam("alias") String alias, @PathParam("type") String type) {
        if ("yaml".equalsIgnoreCase(type)) {
            return getListingYaml(alias);
        } else {
            return getListingJson(alias);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{alias}")
    @ApiOperation(value = "The swagger definition in JSON", hidden = true)
    public Response getListingJson(@PathParam("alias") String alias) {
        return process(alias)
        	.map(swagger -> Response.ok().entity(swagger))
        	.orElseGet(() -> Response.status(Status.NOT_FOUND))
        	.build();
    }

    @GET
    @Produces("application/yaml")
    @Path("/{alias}")
    @ApiOperation(value = "The swagger definition in YAML", hidden = true)
    public Response getListingYaml(@PathParam("alias") String alias) {
    	return process(alias)
            .map(this::asYamlString)
            .map(s -> Response.ok().entity(s).type("application/yaml"))
            .orElseGet(() -> Response.status(Status.NOT_FOUND))
            .build();        
    }
    
    private Set<Class<?>> classes(Application application) {
    	return Stream.concat(
    			application.getClasses().stream(), 
    			Stream.concat(application.getSingletons().stream(), Stream.of(application)).map(Object::getClass))
    		.collect(Collectors.toSet());
    }

    private String asYamlString(Swagger swagger)  {
		try {
			return Yaml.mapper().writeValueAsString(swagger);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
    }
}
