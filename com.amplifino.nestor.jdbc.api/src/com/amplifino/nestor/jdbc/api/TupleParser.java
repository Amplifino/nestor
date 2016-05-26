package com.amplifino.nestor.jdbc.api;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.osgi.annotation.versioning.ConsumerType;

@FunctionalInterface
@ConsumerType
public interface TupleParser<T> {

	T parse(ResultSet resultSet) throws SQLException;

}
