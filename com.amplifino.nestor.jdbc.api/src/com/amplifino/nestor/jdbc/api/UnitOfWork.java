package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.sql.SQLException;

import org.osgi.annotation.versioning.ConsumerType;

@FunctionalInterface
@ConsumerType
public interface UnitOfWork<T> {
	
	T apply(Connection connection) throws SQLException;

}
