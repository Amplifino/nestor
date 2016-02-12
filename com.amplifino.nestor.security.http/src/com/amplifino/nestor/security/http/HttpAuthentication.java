package com.amplifino.nestor.security.http;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HttpAuthentication {
	boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
