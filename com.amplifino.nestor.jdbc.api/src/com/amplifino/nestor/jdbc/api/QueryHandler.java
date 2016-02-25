package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

class QueryHandler  {
		
		private StringBuilder sqlBuilder = new StringBuilder();
		private final List<Object> parameters = new ArrayList<>();
		private int limit = Integer.MAX_VALUE;
		
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
		
		<T> List<T> select(Connection connection, TupleParser<T> parser) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
				bind(statement);
				try (ResultSet resultSet = statement.executeQuery()) {
					List<T> result = new ArrayList<>();
					int i = 0;
					while(resultSet.next() && i++ < limit) {
						result.add(parser.parse(resultSet));
					}
					return result;
				}
			}
		}
		
		<T> Optional<T> findFirst(Connection connection, TupleParser<T> parser) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
				bind(statement);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						return Optional.of(parser.parse(resultSet));
					} else {
						return Optional.empty();
					}
				}
			}
		}

		int executeUpdate(Connection connection) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
				bind(statement);
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

}
