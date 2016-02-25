package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ConnectionQuery extends AbstractQuery {
		
		private final Connection connection;
		
		ConnectionQuery(Connection connection) {
			this.connection = connection;
		}

		@Override
		public <T> List<T> select(TupleParser<T> parser) {
			try {
				return handler().select(connection, parser);
			} catch (SQLException e) {
				throw new JdbcException(e);
			}
		}

		@Override
		public int executeUpdate() {
			try {
				return handler().executeUpdate(connection);
			} catch (SQLException e) {
				throw new JdbcException(e);
			}
		}

		@Override
		public <T> Optional<T> findFirst(TupleParser<T> parser) {
			try {
				return handler().findFirst(connection, parser);
			} catch (SQLException e) {
				throw new JdbcException(e);
			}
		}
		
}
