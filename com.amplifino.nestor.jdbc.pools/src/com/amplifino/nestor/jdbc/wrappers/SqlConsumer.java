package com.amplifino.nestor.jdbc.wrappers;

import java.sql.SQLException;

import org.osgi.annotation.versioning.ConsumerType;

@FunctionalInterface
@ConsumerType
public interface SqlConsumer<T> {

	void accept(T t) throws SQLException;
}
