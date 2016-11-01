package com.amplifino.nestor.transaction.control.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProvider;
import org.osgi.service.transaction.control.recovery.RecoverableXAResource;

import com.amplifino.pools.Pool;

class JDBCConnectionProviderImpl implements JDBCConnectionProvider, AutoCloseable {
	
	private final Pool<XAConnection> pool;
	private final ServiceRegistration<RecoverableXAResource> recoveryReference;
	
	public JDBCConnectionProviderImpl(Pool<XAConnection> pool, BundleContext context) {
		this.pool = pool;
		recoveryReference = context.registerService(RecoverableXAResource.class, new RecoverableXAResourceImpl(), null);
	}
		
	@Override
	public Connection getResource(TransactionControl transactionControl) throws TransactionException {		
		return new ConnectionWrapper(transactionControl, pool);
	}
	
	@Override
	public void close() {
		recoveryReference.unregister();
		pool.close();
	}
	
	private class RecoverableXAResourceImpl implements RecoverableXAResource {
		
		private Map<XAResource, XAConnection> leases = new ConcurrentHashMap<>();

		@Override
		public String getId() {
			return null;
		}

		@Override
		public XAResource getXAResource() throws Exception {
			XAConnection connection = pool.borrow();
			try {
				XAResource resource = connection.getXAResource();
				leases.put(resource, connection);
				return resource;
			} catch (SQLException e) {
				pool.evict(connection);
				throw e;
			}
		}

		@Override
		public void releaseXAResource(XAResource xaResource) {
			pool.release(leases.remove(xaResource));			
		}
		
	}

}
