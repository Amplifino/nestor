package com.amplifino.nestor.transaction.datasources;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

class NoXaResource implements XAResource {
	private Connection connection;
	private Xid xid;
	
	void setConnection(Connection connection) throws SQLException {
		this.connection = connection;
		if (xid != null) {
			connection.setAutoCommit(false);
		}
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
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return 0;
	}

	@Override
	public boolean isSameRM(XAResource xares) throws XAException {
		return false;
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
		try {
			connection.rollback();
		} catch (SQLException e) {
			throw (XAException) new XAException(e.toString()).initCause(e);
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
