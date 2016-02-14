package com.amplifino.nestor.soap.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.spi.http.HttpContext;

public class HttpContextImpl extends HttpContext {
	
	private final HttpContextServlet servlet;
	private final String path;
	
	public HttpContextImpl(HttpContextServlet servlet, String path) {
		this.servlet = servlet;
		this.path = path;
	}

	@Override
	public String getPath() {
		return servlet.getMountPath() + path;
	}

	@Override
	public Object getAttribute(String name) {
		return null;
	}

	@Override
	public Set<String> getAttributeNames() {
		return Collections.emptySet();
	}
	
	void dispose() {
		servlet.unregister(path, this);
	}
	
	void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		handler.handle(new HttpExchangeImpl(this, request, response));
	}
	
	

}
