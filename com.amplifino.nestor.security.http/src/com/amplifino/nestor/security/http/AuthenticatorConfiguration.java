package com.amplifino.nestor.security.http;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Amplifino Authenticator")
@interface AuthenticatorConfiguration {	
		@AttributeDefinition(description="realm")
		String realm() default "Amplifino";
		@AttributeDefinition(description="authentication type")
		AuthenticationType authenticationType() default AuthenticationType.DIGEST;
		String osgi_http_whiteboard_filter_pattern() default "/api/*";
		@AttributeDefinition(required=false)
		String osgi_http_whiteboard_filter_regex() ;
		String osgi_http_whiteboard_context_select() default "(osgi.http.whiteboard.context.name=*)";
		String webconsole_configurationFactory_nameHint() 
			default "{authenticationType} Authentication Filter on pattern {osgi.http.whiteboard.filter.pattern}, regex {osgi.http.whiteboard.filter.regex}";
		
		enum AuthenticationType {
			BASIC,
			DIGEST
		}
}
