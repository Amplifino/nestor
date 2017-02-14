# com.amplifino.nestor.jaxrs #

This bundle is an initial implementation of OSGI RFC 217 JAX-RS service,
using Jersey and the HTTP Service.

Main issue is to map the url mapping requirements of RFC 217 to Jersey.

Deviations from the draft spec and additional remarks: 

### 5.1.1 Resource mapping ###

Currently the whiteboard uses a different servlet for each unique value of osgi.jaxrs.resource.base.
So far this seems the only reasonable way to prefix the @Path path declared in the resource with a base value.
(unless declaring all methods using the programmatic API of Resource.Builder)

As a result some servlets may hide resources declared in other servlets e.g.
a resource with osgi.jaxrs.resource.base=/a and a method with path("b/c") will be hidden 
by a resource with osgi.jaxrs.resource.base = /a/b.


### 5.1.2 Container base context path ###

The container base path is configurable, /rest by default

### 5.2 Filter and interceptor mapping ###

Filter and interceptor are only active for resource whose base url is equal or a child of the base url of the filter.
e.g a Filter registered with osgi.jaxrs.resource.base =/a will be active for a resource with property osgi.jaxrs.resource.base=/a/b,
but will not be active for a resource with property osgi.jaxrs.resource.base=/ and a @Path("a/b"); 

A fully RFC 217 compliant implementation on Jersey may be challenging, as there seems to be no easy way to limit
a filter or interceptor to a sub path. 


### 5.4 JAX-RS applications ###

Currently the whiteboard allows only a single application with a given osgi.jaxrs.application.base.
This is not a feasibility issue, but an implementation choice to avoid conflicts between applications.

### 5.5 JAX-RS endpoint advertisement ###

not implemented

### 5.6 Error handling ###

not implemented

### 5.7 JaxRSServiceRunTime ###

not implemented

### 5.8 JAX-RS client ###

not implemented. Seems like a bad idea to publish a javax.rs.ws.client.ClientBuilder service as it is not thread safe. 
Even when using a PrototypeServiceFactory, clients can still get access to shared instance when using ServiceReference.getService().
The implementation does publish a JerseyReady service after Jersey initialization has completed,
and it safe to use ClientBuilder.newBuilder() or ClientBuilder.newClient();

### 5.9 Implementation Provided Capabilities ###

not implemented

