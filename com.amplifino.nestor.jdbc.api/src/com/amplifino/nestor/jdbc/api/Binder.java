package com.amplifino.nestor.jdbc.api;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface Binder<T> {

	void bind(PreparedStatement statement, T value) throws SQLException;
}
