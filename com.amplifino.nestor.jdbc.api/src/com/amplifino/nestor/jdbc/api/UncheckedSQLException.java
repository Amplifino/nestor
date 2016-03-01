package com.amplifino.nestor.jdbc.api;

import java.sql.SQLException;

public class UncheckedSQLException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UncheckedSQLException(SQLException e) {
		super(e);
	}
}
