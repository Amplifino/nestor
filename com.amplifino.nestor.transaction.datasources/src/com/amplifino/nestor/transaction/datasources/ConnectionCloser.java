package com.amplifino.nestor.transaction.datasources;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.Synchronization;

class ConnectionCloser implements Synchronization {

	private final Connection connection;
	
	ConnectionCloser(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void afterCompletion(int status) {
		try {
			connection.close();
		} catch (SQLException e) {
			Logger.getLogger("com.amplifino.tx").log(
				Level.WARNING, 
				"Unexpected exception in close: " + e.getMessage(),
				e);
		}			
	}

	@Override
	public void beforeCompletion() {
	}
}
