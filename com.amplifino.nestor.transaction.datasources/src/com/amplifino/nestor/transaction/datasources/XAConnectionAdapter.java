package com.amplifino.nestor.transaction.datasources;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEvent;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

public class XAConnectionAdapter implements XAConnection , ConnectionEventListener, StatementEventListener {

	private final PooledConnection pooledConnection;
	private final List<ConnectionEventListener> connectionEventListeners = new ArrayList<>();
	private final List<StatementEventListener> statementEventListeners = new ArrayList<>();
	private final NoXaResource xaResource;
	
	public XAConnectionAdapter(PooledConnection pooledConnection) {
		this.pooledConnection = pooledConnection;
		pooledConnection.addConnectionEventListener(this);
		pooledConnection.addStatementEventListener(this);
		xaResource = new NoXaResource();
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = pooledConnection.getConnection();
		xaResource.setConnection(connection);
		return connection;
	}

	@Override
	public void close() throws SQLException {
		pooledConnection.close();
	}

	@Override
	public void addConnectionEventListener(ConnectionEventListener listener) {
		connectionEventListeners.add(listener);
	}

	@Override
	public void removeConnectionEventListener(ConnectionEventListener listener) {
		connectionEventListeners.remove(listener);

	}

	@Override
	public void addStatementEventListener(StatementEventListener listener) {
		statementEventListeners.add(listener);

	}

	@Override
	public void removeStatementEventListener(StatementEventListener listener) {
		statementEventListeners.remove(listener);

	}

	@Override
	public XAResource getXAResource() throws SQLException {
		return xaResource;
	}

	@Override
	public void statementClosed(StatementEvent event) {
		StatementEvent wrappedEvent = new StatementEvent(this, event.getStatement(), event.getSQLException());
		statementEventListeners.forEach(listener -> listener.statementClosed(wrappedEvent));
	}

	@Override
	public void statementErrorOccurred(StatementEvent event) {
		StatementEvent wrappedEvent = new StatementEvent(this, event.getStatement(), event.getSQLException());
		statementEventListeners.forEach(listener -> listener.statementClosed(wrappedEvent));
		
	}

	@Override
	public void connectionClosed(ConnectionEvent event) {
		ConnectionEvent wrappedEvent = new ConnectionEvent(this, event.getSQLException());
		connectionEventListeners.forEach(listener -> listener.connectionClosed(wrappedEvent));
	}

	@Override
	public void connectionErrorOccurred(ConnectionEvent event) {
		ConnectionEvent wrappedEvent = new ConnectionEvent(this, event.getSQLException());
		connectionEventListeners.forEach(listener -> listener.connectionClosed(wrappedEvent));
	}

}
