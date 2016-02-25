package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Query {
	
	Query text(String sql);
	Query parameters(Object parameter, Object ... parameters);
	<T> List<T> select(TupleParser<T> parser);
	<T> Optional<T> findFirst(TupleParser<T> parser);
	int executeUpdate();

	static Query on(DataSource dataSource) {
		return new DataSourceQuery(dataSource);
	}
	
	static Query on(Connection connection) {
		return new ConnectionQuery(connection);
	}
}
