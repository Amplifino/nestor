package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;

class ConnectionQuery extends AbstractQuery {
		
		private final Connection connection;
		
		ConnectionQuery(Connection connection) {
			this.connection = connection;
		}

		@Override
		public <T> long select(TupleParser<T> parser, Consumer<T> consumer) {
			try {
				return handler().select(connection, parser, consumer);
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
		
		@Override
		public <T> Optional<T> collect(TupleParser<T> supplier, TupleAccumulator<T> accumulator) {
			try {
				return handler().collect(connection, supplier, accumulator);
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
		
		@Override
		public int executeUpdate() {
			try {
				return handler().executeUpdate(connection);
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}

		@Override
		public <T> T generatedKey(TupleParser<T> generatedKeyParser) {
			try {
				return handler().generatedKey(connection, generatedKeyParser);
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
		
		@Override
		public <T> Optional<T> findFirst(TupleParser<T> parser) {
			try {
				return handler().findFirst(connection, parser);
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
		
		
		@Override
		public <T> Optional<T> selectOne(TupleParser<T> parser) {
			try {
				return handler().selectOne(connection, parser);
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
		
		@Override
		public  <T> int[] executeBatch(Iterable<? extends T> values, Binder<? super T> binder) {
			try {
				return handler().executeBatch(connection, values, binder);
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
		}
		
}
