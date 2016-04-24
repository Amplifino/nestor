package com.amplifino.nestor.jdbc.api;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface TupleAccumulator<T> {

	void accept(T t, ResultSet resultSet) throws SQLException;

}
