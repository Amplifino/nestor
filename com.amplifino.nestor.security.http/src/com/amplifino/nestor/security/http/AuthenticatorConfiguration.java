package com.amplifino.nestor.security.http;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Amplifino Authenticator")
@interface AuthenticatorConfiguration {	
		String realm() default "Amplifino";
		AuthenticationType authenticationType() default AuthenticationType.DIGEST;
		@AttributeDefinition(name="pattern", required=false, 
				description="Apply this servlet filter to the specified URL path patterns. The format of the patterns is specified in the servlet specification")
		String[] osgi_http_whiteboard_filter_pattern() default "/api/*";
		@AttributeDefinition(name="regex", required=false,  
				description="Apply this servlet filter to the specified URL paths. The paths are specified as regular expressions following the syntax defined in the java.util.regex.Pattern class" )
		String[] osgi_http_whiteboard_filter_regex();
		@AttributeDefinition(name="servlet", required=false,
				description="Apply this servlet filter to the referenced servlet(s) by name")
		String[] osgi_http_whiteboard_filter_servlet();
		@AttributeDefinition(name="context select", 
				description="An LDAP-style filter to select the associated ServletContextHelper service to use")
		String osgi_http_whiteboard_context_select() default "(osgi.http.whiteboard.context.name=*)";
		
		enum AuthenticationType {
			BASIC,
			DIGEST
		}
}
