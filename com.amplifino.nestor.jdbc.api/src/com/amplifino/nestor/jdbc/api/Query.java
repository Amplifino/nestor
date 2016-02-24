package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

public interface Query {
	
	Query text(String sql);
	Query parameters(Object parameter, Object ... parameters);
	<T> List<T> select(TupleParser<T> parser);
	int executeUpdate();

	static Query on(DataSource dataSource) {
		return new DataSourceQuery(dataSource);
	}
	
	static Query on(Connection connection) {
		return new ConnectionQuery(connection);
	}
}
