package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.osgi.annotation.versioning.ProviderType;

public final class LocalTransaction {

	private LocalTransaction() {		
	}
	
	public static TransactionPerformer with(DataSource dataSource) {
		return new DataSourceTransactionPerformer(dataSource);
	}
	
	public static TransactionPerformer with(Connection connection) {
		return new ConnectionTransactionPerformer(connection);
	}
	
	@ProviderType
	public interface TransactionPerformer {
		<T> T call(UnitOfWork<T> work); 
	}
	
	private static class DataSourceTransactionPerformer implements TransactionPerformer {
		
		private final DataSource dataSource;
		
		private DataSourceTransactionPerformer(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		@Override
		public <T> T call(UnitOfWork<T> work) {
			try (Connection connection = dataSource.getConnection()) {
				return new ConnectionTransactionPerformer(connection).call(work);
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
				
	}
	
	private static class ConnectionTransactionPerformer implements TransactionPerformer {
		
		private final Connection connection;
		
		private ConnectionTransactionPerformer(Connection connection) {
			this.connection = connection;
		}

		@Override
		public <T> T call(UnitOfWork<T> work) {
			try {
				return handleAutoCommit(work);
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
			
		}
			
		private <T> T handleAutoCommit(UnitOfWork<T> work) throws SQLException {			
			boolean autoCommit = connection.getAutoCommit();
			try {
				if (autoCommit) {
					connection.setAutoCommit(false);
				}
				return handleCommit(work);
			} finally {
				if (autoCommit) {
					connection.setAutoCommit(true);
				}
			}
		}
		
		private <T> T handleCommit(UnitOfWork<T> work) throws SQLException {
			boolean success = false;
			try {
				T result = work.apply(connection);
				success = true;
				return result;
			} finally  {
				if (success) {
					connection.commit();
				} else {
					connection.rollback();
				}				
			}
		}				
	}
}
