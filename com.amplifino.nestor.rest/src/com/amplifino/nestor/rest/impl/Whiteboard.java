/*
 * Copyright (c) Amplifino (2015). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amplifino.nestor.rest.impl;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Whiteboard {
	private static final Logger LOGGER = Logger.getLogger("com.amplifino.jersey.whiteboard");
	private static final String LOGHEADER = "Jersey Whiteboard:";
	private static final String ALIAS = "alias";
	private static final String RAW = "raw";

	@Reference
	private HttpService httpService;
	@Reference
	private WhiteboardConfigurationProvider configurationProvider;

	// use name sequence to ensure static references are set first
	// only server Applications with alias property starting with a slash, and not ending with a slash
	@Reference(name="zApplication", cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC,
		target="(&(" + ALIAS + "=/*)(!(" + ALIAS + "=*/)))")
    public void addApplication(Application application, Map<String,Object> properties) throws Exception {
		String alias = getAlias(properties);
    	try {
    		ResourceConfig resourceConfig = ResourceConfig.forApplication(Objects.requireNonNull(application));
    		if (!isRaw(properties)) {
    			resourceConfig.register(JacksonFeature.class);
    			resourceConfig.register(RolesAllowedDynamicFeature.class);
    			resourceConfig.register(ObjectMapperProvider.class);
    			EncodingFilter.enableFor(resourceConfig, GZipEncoder.class);
    		}
    		HttpServlet servlet = new ServletContainer(resourceConfig);
    		httpService.registerServlet(alias, servlet, null, null);
    		LOGGER.info(String.join(" ", LOGHEADER, "Installed", application.toString(), "on", alias));
        } catch (Exception e) {
            LOGGER.log(
            	Level.SEVERE,
            	String.join(" ",
            		LOGHEADER, "Error while installing", application.toString(), "on", alias, ":", e.getMessage()) ,
            	e);
            throw e;
        }
    }

    public void removeApplication(Application application,Map<String,Object> properties) {
    	String alias = getAlias(properties);
    	httpService.unregister(alias);
    	LOGGER.info(String.join(" ", LOGHEADER, "Uninstalled", application.toString(), "alias:", alias));
	}

    private String getAlias(Map<String,Object> properties) {
    	String webMountPoint = configurationProvider.configuration().webMountPoint();
    	return ("/".equals(webMountPoint) ? "" : webMountPoint) + properties.get(ALIAS);
    }

    private boolean isRaw(Map<String, Object> properties) {
    	return Boolean.TRUE.equals(properties.get(RAW));
    }

}
