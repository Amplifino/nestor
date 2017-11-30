package com.amplifino.nestor.web;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service=Object.class, configurationPolicy=ConfigurationPolicy.REQUIRE, property= {
		HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT + "=(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=webdir)"})
@Designate(ocd=WebDirectory.WebDirectoryConfig.class, factory=true)
public class WebDirectory {
	
	@ObjectClassDefinition(name="Web Directories")
	@interface WebDirectoryConfig {
		@AttributeDefinition(name="url pattern", description="should start with a / and end with /*")
		String osgi_http_whiteboard_resource_pattern();
		@AttributeDefinition(name="document root")
		String osgi_http_whiteboard_resource_prefix();
		String webconsole_configurationFactory_nameHint() 
			default "Mapping url pattern {osgi.http.whiteboard.resource.pattern} to document root {osgi.http.whiteboard.resource.prefix}";
	}
}
