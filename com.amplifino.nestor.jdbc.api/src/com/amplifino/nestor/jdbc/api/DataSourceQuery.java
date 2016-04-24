package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;

import javax.sql.DataSource;

class DataSourceQuery extends AbstractQuery  {
		
		private final DataSource dataSource;
		
		DataSourceQuery(DataSource dataSource) {
			this.dataSource = dataSource;
		}
		
		@Override
		public <T> long select(TupleParser<T> parser, Consumer<T> consumer) {
			try {
				try(Connection connection = dataSource.getConnection()) {
					return handler().select(connection, parser, consumer);
				} 
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
		
		@Override
		public <T> Optional<T> collect(TupleParser<T> supplier, TupleAccumulator<T> accumulator) {
			try {
				try(Connection connection = dataSource.getConnection()) {
					return handler().collect(connection, supplier, accumulator);
				}
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}

		
		@Override
		public <T> Optional<T> findFirst(TupleParser<T> parser) {
			try {
				try (Connection connection = dataSource.getConnection()) {
					return handler().findFirst(connection, parser);
				}
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}

		@Override
		public int executeUpdate() {
			try {
				try (Connection connection = dataSource.getConnection()) {
					return handler().executeUpdate(connection);
				}
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
		
		@Override
		public <T> T generatedKey(TupleParser<T> generatedKeyParser) {
			try {
				try (Connection connection = dataSource.getConnection()) {
					return handler().generatedKey(connection, generatedKeyParser);
				}
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
		
		@Override
		public <T> int[] executeBatch(Iterable<? extends T> values, Binder<? super T> binder) {
			try {
				try (Connection connection = dataSource.getConnection()) {
					return handler().executeBatch(connection, values, binder);
				}
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
}
