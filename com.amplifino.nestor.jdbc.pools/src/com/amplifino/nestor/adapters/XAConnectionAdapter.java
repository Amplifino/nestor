package com.amplifino.nestor.adapters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class XAConnectionAdapter implements XAConnection , ConnectionEventListener, XAResource {

	private final PooledConnection pooledConnection;
	private final List<ConnectionEventListener> connectionEventListeners = new ArrayList<>();
	private Connection connection;
	private Xid xid;
	
	public XAConnectionAdapter(PooledConnection pooledConnection) {
		this.pooledConnection = pooledConnection;
		pooledConnection.addConnectionEventListener(this);
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		connection = pooledConnection.getConnection();
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
		throw new UnsupportedOperationException();

	}

	@Override
	public void removeStatementEventListener(StatementEventListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public XAResource getXAResource() throws SQLException {
		return this;
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

	@Override
	public void commit(Xid xid, boolean onePhase) throws XAException {
		this.xid = null;
		if (!onePhase) {
			throw new UnsupportedOperationException();
		}
		if (connection == null) {
			throw new IllegalStateException();
		}
		try {
			connection.commit();
		} catch (SQLException e) {
			throw (XAException) new XAException(e.getMessage()).initCause(e);
		}			
	}

	@Override
	public void end(Xid xid, int flags) throws XAException {
		if (!xid.equals(this.xid)) {
			throw new IllegalArgumentException();
		}
		this.xid = null;
	}

	@Override
	public void forget(Xid xid) throws XAException {
		if (xid.equals(this.xid)) {
			this.xid = null;
		}
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return 0;
	}

	@Override
	public boolean isSameRM(XAResource xaResource) throws XAException {
		return this == xaResource;
	}

	@Override
	public int prepare(Xid xid) throws XAException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Xid[] recover(int flag) throws XAException {
		return new Xid[0];
	}

	@Override
	public void rollback(Xid xid) throws XAException {
		this.xid = null;
		if (connection != null) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				throw (XAException) new XAException(e.toString()).initCause(e);
			}
		}
	}

	@Override
	public boolean setTransactionTimeout(int seconds) throws XAException {
		return false;
	}

	@Override
	public void start(Xid xid, int flags) throws XAException {	
		if (connection != null) {
			try {
				connection.setAutoCommit(false);
			} catch (SQLException e) {
				throw (XAException) new XAException(e.toString()).initCause(e);
			}
		}
		this.xid = xid;
	}
	

}
