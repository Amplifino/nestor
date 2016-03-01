package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Query {
	
	Query text(String sql);
	Query parameters(Object parameter, Object ... parameters);
	Query limit(int limit);
	Query fetchSize(int fetchSize);
	<T> List<T> select(TupleParser<T> parser);
	<T> long select(TupleParser<T> parser, Consumer<T> consumer);
	<T> Optional<T> findFirst(TupleParser<T> parser);
	int executeUpdate();
	<T> int[] executeBatch(Iterable<? extends T> batch, Binder<? super T> binder);
	<T> T generatedKey(TupleParser<T> parser);

	static Query on(DataSource dataSource) {
		return new DataSourceQuery(dataSource);
	}
	
	static Query on(Connection connection) {
		return new ConnectionQuery(connection);
	}
}
