package com.amplifino.nestor.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(service=ServletContextHelper.class, property= {
	HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=webdir",
	HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH + "=/"})
public class WebDirectoryHelper extends ServletContextHelper {
	
	@Override
	public URL getResource(String name) {
		try {
			return Paths.get(name).toUri().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
}
