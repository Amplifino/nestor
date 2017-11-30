package com.amplifino.nestor.web;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service=Servlet.class, 
	property= { HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "="}, 
	configurationPolicy=ConfigurationPolicy.REQUIRE)
@Designate(ocd=HomePageRedirector.HomePageRedirectorConfig.class)
public class HomePageRedirector extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String redirectURL;

	@Activate
	public void activate(HomePageRedirectorConfig config) {
		this.redirectURL = config.redirectURL();
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.sendRedirect(resp.encodeRedirectURL(redirectURL));
	}
	
	@ObjectClassDefinition(name="Home Page")
	@interface HomePageRedirectorConfig {
		String redirectURL();
	}
}
