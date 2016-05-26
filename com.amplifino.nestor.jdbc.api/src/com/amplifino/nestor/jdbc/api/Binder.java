package com.amplifino.nestor.jdbc.api;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.osgi.annotation.versioning.ConsumerType;

@FunctionalInterface
@ConsumerType
public interface Binder<T> {

	void bind(PreparedStatement statement, T value) throws SQLException;
}
