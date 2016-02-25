package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

public class DataSourceQuery extends AbstractQuery  {
		
		private final DataSource dataSource;
		
		DataSourceQuery(DataSource dataSource) {
			this.dataSource = dataSource;
		}
		
		@Override
		public <T> List<T> select(TupleParser<T> parser) {
			try {
				try (Connection connection = dataSource.getConnection()) {
					return handler().select(connection, parser);
				}
			} catch (SQLException e) {
				throw new JdbcException(e);
			}
		}
		
		@Override
		public <T> Optional<T> findFirst(TupleParser<T> parser) {
			try {
				try (Connection connection = dataSource.getConnection()) {
					return handler().findFirst(connection, parser);
				}
			} catch (SQLException e) {
				throw new JdbcException(e);
			}
		}

		@Override
		public int executeUpdate() {
			try {
				try (Connection connection = dataSource.getConnection()) {
					return handler().executeUpdate(connection);
				}
			} catch (SQLException e) {
				throw new JdbcException(e);
			}
		}
		
}
