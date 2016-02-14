package com.amplifino.nestor.soap.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.spi.http.HttpContext;
import javax.xml.ws.spi.http.HttpExchange;

public class HttpExchangeImpl extends HttpExchange {
	
	private final HttpContextImpl context;
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	
	public HttpExchangeImpl(HttpContextImpl context, HttpServletRequest request, HttpServletResponse response) {
		this.context = context;
		this.request = request;
		this.response = response;
	}

	@Override
	public Map<String, List<String>> getRequestHeaders() {
		return Collections.list(request.getHeaderNames()).stream()
			.collect(Collectors.toMap(Function.identity(), header -> Collections.list(request.getHeaders(header))));
		
	}
	
	@Override
	public String getRequestHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public Map<String, List<String>> getResponseHeaders() {
		return response.getHeaderNames().stream()
			.collect(Collectors.toMap(Function.identity(), header -> new ArrayList<>(response.getHeaders(header))));
	}

	@Override
	public void addResponseHeader(String name, String value) {
		response.addHeader(name, value);
	}

	@Override
	public String getRequestURI() {		
		return request.getRequestURI();
	}

	@Override
	public String getContextPath() {
		return request.getContextPath();
	}

	@Override
	public String getRequestMethod() {
		return request.getMethod();
	}

	@Override
	public HttpContext getHttpContext() {
		return context;
	}

	@Override
	public void close() throws IOException {
		response.flushBuffer();		
	}

	@Override
	public InputStream getRequestBody() throws IOException {
		return request.getInputStream();
	}

	@Override
	public OutputStream getResponseBody() throws IOException {
		return response.getOutputStream();
	}

	@Override
	public void setStatus(int status) {
		response.setStatus(status);		
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return InetSocketAddress.createUnresolved(request.getRemoteAddr(), request.getRemotePort());
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return InetSocketAddress.createUnresolved(request.getLocalAddr(), request.getLocalPort());
	}

	@Override
	public String getProtocol() {
		return request.getProtocol();
	}

	@Override
	public String getScheme() {
		return request.getScheme();
	}

	@Override
	public String getPathInfo() {
		return request.getPathInfo();
	}

	@Override
	public String getQueryString() {
		return request.getQueryString();
	}

	@Override
	public Object getAttribute(String name) {
		switch(name) {
			case MessageContext.SERVLET_CONTEXT:
				return request.getServletContext();
			case MessageContext.SERVLET_REQUEST:
				return request;
			case MessageContext.SERVLET_RESPONSE:
				return response;
			default:
				return request.getAttribute(name);
		}
	}

	@Override
	public Set<String> getAttributeNames() {
		return Stream.concat(
				Stream.of(MessageContext.SERVLET_CONTEXT, MessageContext.SERVLET_REQUEST, MessageContext.SERVLET_RESPONSE),
				Collections.list(request.getAttributeNames()).stream())
			.collect(Collectors.toSet());
	}

	@Override
	public Principal getUserPrincipal() {
		return request.getUserPrincipal();
	}

	@Override
	public boolean isUserInRole(String role) {
		return request.isUserInRole(role);
	}
	
	
}
