# com.amplifino.jersey.whiteboard


OSGI Whiteboard for JAX-RS applications using Jersey as a provider and Jackson for JSON handling.  
Bundles that need to publish JAX-RS resources only need to register 
a **javax.ws.rs.core.Application** service with the **alias** property set to required path 
,starting with a / but not ending in /. (An OSGI service property, not an Application::getProperties property)  
The whiteboard will publish the JAX-RS application at /api/alias using the OSGI HTTP Service.
The default /api web mount point can be changed using Configuration Admin Service.

By default the Whiteboard performs the following configuration on the Application:

- add RolesAllowedDynamicFeature to support authorization of JAX-RS methods with @RolesAllowed annotation
- registers JacksonFeature to use Jackson as JSON library
- add an ObjectMapper provider to configure Jackon serialization/deserialization
	- do not serialize null fields
	- do not fail on empty beans
	- map dates to milliseconds since epoch
	- supports the java 8 java.time classes aka JSR310

If the applications can overrules this by adding a **raw=true** OSGI service property,
and add its filters, features and providers to the getClasses and getSingletons Sets.

## Version history 

- 0.0.0: Work in Progress 
- 1.1.0: Added JerseyTracker service that is published when Jersey initialization is complete.
Usefull for Jersey client application bundles to avoid race condition with Jersey initialization.
Not needed for Jersey server applications, as whiteboard only starts after Jersey initialization.

