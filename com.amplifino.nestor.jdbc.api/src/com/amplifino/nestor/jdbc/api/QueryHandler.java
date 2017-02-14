package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

final class QueryHandler  {
		
		private static final Logger logger = Logger.getLogger("com.amplifino.nestor.jdbc.api");
		private static int traceMask = 0;
		
		private StringBuilder sqlBuilder = new StringBuilder();
		private final List<Object> parameters = new ArrayList<>();
		private long limit = Long.MAX_VALUE;
		private int fetchSize = 0;
		
		QueryHandler text(String sql) {
			sqlBuilder.append(sql);
			return this;
		}

		QueryHandler parameters(Object parameter, Object[] parameters) {
			this.parameters.add(parameter);
			this.parameters.addAll(Arrays.asList(parameters));
			return this;
		}

		void limit(int limit) {
			this.limit = limit;
		}
		
		void fetchSize(int fetchSize) {
			this.fetchSize = fetchSize;
		}
		
		<T> Optional<T> findFirst(Connection connection, TupleParser<T> parser) throws SQLException {			
			try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
				bind(statement);
				logExecution("findFirst: ");
				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						logCount(1);
						return Optional.of(parser.parse(resultSet));
					} else {
						logCount(0);
						return Optional.empty();
					}
				}
			}
		}
		
		<T> Optional<T> selectOne(Connection connection, TupleParser<T> parser) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
				bind(statement);
				logExecution("selectOne: ");
				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						T singleton = parser.parse(resultSet);
						if (resultSet.next()) {
							logCount(2);
							throw new IllegalStateException("Query returned more than one row");
						} else {
							logCount(1);
							return Optional.of(singleton);
						}
					} else {
						logCount(0);
						return Optional.empty();
					}
				}
			}
		}

		int executeUpdate(Connection connection) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
				bind(statement);
				logExecution("executeUpdate: ");
				return statement.executeUpdate();
			}
		}
		
		private void bind(PreparedStatement statement) throws SQLException {
			for (int i = 0 ; i < parameters.size() ; i++) {
				set(statement, i+1, parameters.get(i));
			}
		}
		
		private void set(PreparedStatement statement, int offset, Object value) throws SQLException {
			if (value == null) {
				statement.setObject(offset, value);
			} else if (value instanceof Instant) {
				statement.setTimestamp(offset, Timestamp.from((Instant) value));
			} else if (value instanceof LocalDateTime) {
				statement.setTimestamp(offset, Timestamp.valueOf((LocalDateTime) value));
			} else if (value instanceof LocalDate) {
				statement.setDate(offset, Date.valueOf((LocalDate) value));
			} else if (value instanceof LocalTime) {
				statement.setTime(offset, Time.valueOf((LocalTime) value));
			} else {
				statement.setObject(offset, value);
			}
		}

		<T> long select(Connection connection, TupleParser<T> parser, Consumer<T> consumer) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
				if (fetchSize > 0) {
					statement.setFetchSize(fetchSize);
				}
				bind(statement);
				logExecution("select: ");
				try (ResultSet resultSet = statement.executeQuery()) {
					long i = 0;
					while(resultSet.next() && i++ < limit) {
						consumer.accept(parser.parse(resultSet));
					}
					logCount(i);
					return i;
				}
			}
		}

		<T> Optional<T> collect(Connection connection, TupleParser<T> supplier, TupleAccumulator<T> accumulator) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
				if (fetchSize > 0) {
					statement.setFetchSize(fetchSize);
				}
				bind(statement);
				logExecution("collect: ");
				try (ResultSet resultSet = statement.executeQuery()) {
					boolean isEmpty = !resultSet.next();
					if (isEmpty) {
						logCount(0);
						return Optional.empty();
					}
					T t = supplier.parse(resultSet);
					long i = 1;
					do {
						accumulator.accept(t, resultSet);
					} while (resultSet.next() && i++ < limit);
					logCount(i);
					return Optional.of(t);
				}
			}
		}
		
		<T> T generatedKey(Connection connection, TupleParser<T> generatedKeyParser) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString(), Statement.RETURN_GENERATED_KEYS)) {
				bind(statement);
				logExecution("generatedKey:" );
				statement.executeUpdate();
				try (ResultSet resultSet = statement.getGeneratedKeys()) {
					resultSet.next();
					return generatedKeyParser.parse(resultSet);
				}
			}
		}
		
		<T> int[] executeBatch(Connection connection, Iterable<T> values, Binder<? super T> binder) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
				int i = 0;
				for (T value : values) {
					binder.bind(statement, value);
					statement.addBatch();
					i++;
				}
				logBatch(i);
				return statement.executeBatch();
			}
		}

		String text() {
			return sqlBuilder.toString();
		}
		
		List<Object> parameters() {
			return new ArrayList<>(parameters);
		}
		
		void addAll(List<Object> parameters) {
			this.parameters.addAll(parameters);
		}
		
		private boolean loggingText() {
			return (traceMask & 1) != 0;
		}
		
		private boolean loggingParameters() {
			return (traceMask & 2) != 0 && parameters.size() > 0;
		}
		
		private void logExecution(String header) {
			if (loggingText()) {
				logger.info(header + sqlBuilder.toString());
			}
			if (loggingParameters()) {
				logger.info("Parameters " + parameters);
			}
		}
		
		private void logBatch(int count) {
			if (loggingText()) {
				logger.info("Batch size: " + count + " sql: " + sqlBuilder.toString());
			}
		}
		
		private void logCount(long count) {
			if (loggingCount()) {
				logger.info("Fetched " + count + " row" + (count == 1 ? "" : "s"));
			}
		}
		
		private boolean loggingCount() {
			return (traceMask & 4) != 0;
		}
		
		static void setTraceMask(int value) {
			traceMask = value;
		}
}
