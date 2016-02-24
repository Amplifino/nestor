package com.amplifino.nestor.jdbc.api;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface TupleParser<T> {

	T parse(ResultSet resultSet) throws SQLException;

}
