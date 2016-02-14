package com.amplifino.nestor.soap.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class HttpContextServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final Map<String, HttpContextImpl> contexts = new ConcurrentHashMap<>();
	private final String mountPath;
	
	HttpContextServlet(String mountPath) {
		this.mountPath = mountPath;
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getPathInfo();
		if (path == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		HttpContextImpl context = contexts.get(path);
		if (context == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		context.service(request, response);
	}
	
	void register(String path, HttpContextImpl context) {
		contexts.put(path, context);
	}
	
	void unregister(String path, HttpContextImpl context) {
		this.contexts.remove(path, context);
	}
	
	String getMountPath() {
		return mountPath;
	}
	
}
	

