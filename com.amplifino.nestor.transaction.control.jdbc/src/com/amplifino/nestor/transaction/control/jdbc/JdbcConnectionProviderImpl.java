package com.amplifino.nestor.transaction.control.jdbc;

import java.sql.Connection;

import javax.sql.DataSource;

import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProvider;

public class JdbcConnectionProviderImpl implements JDBCConnectionProvider {
	
	private final DataSource dataSource;
	
	public JdbcConnectionProviderImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}
		
	@Override
	public Connection getResource(TransactionControl transactionControl) throws TransactionException {		
		return new ConnectionWrapper(transactionControl, dataSource);
	}

}
