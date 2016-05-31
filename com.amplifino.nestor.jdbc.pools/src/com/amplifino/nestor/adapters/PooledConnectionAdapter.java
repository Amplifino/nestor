package com.amplifino.nestor.adapters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import com.amplifino.nestor.jdbc.wrappers.ConnectionHandle;

class PooledConnectionAdapter implements PooledConnection {

	private final Connection connection;
	private final List<ConnectionEventListener> listeners = new CopyOnWriteArrayList<>();
	private volatile boolean inUse;
	
	private PooledConnectionAdapter(Connection connection) {
		this.connection = connection;
		this.inUse = false;
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (inUse) {
			throw new SQLException("Outstanding connection");
		}
		Connection handle = new ConnectionHandle(connection, this::handleClosed);
		inUse = true;
		return handle;
	}

	@Override
	public void close() throws SQLException {
		connection.close();
	}

	@Override
	public void addConnectionEventListener(ConnectionEventListener listener) {
		listeners.add(listener);		
	}

	@Override
	public void removeConnectionEventListener(ConnectionEventListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void addStatementEventListener(StatementEventListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeStatementEventListener(StatementEventListener listener) {
		throw new UnsupportedOperationException();
	}
	
	private void handleClosed(Connection connection) throws SQLException {
		inUse = false;
		ConnectionEvent event = new ConnectionEvent(this);
		listeners.forEach(listener -> listener.connectionClosed(event));
		connection.setAutoCommit(true);			
	}
	
	static PooledConnection on(Connection connection) {
		return new PooledConnectionAdapter(connection);
	}
}
