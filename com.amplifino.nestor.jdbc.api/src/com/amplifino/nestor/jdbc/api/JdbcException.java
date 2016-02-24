package com.amplifino.nestor.jdbc.api;

import java.sql.SQLException;

public class JdbcException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JdbcException(SQLException e) {
		super(e);
	}
}
