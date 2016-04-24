package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.sql.DataSource;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Query encapsulates a SQL command 
 *
 */
@ProviderType
public interface Query {
	
	/**
	 * adds the argument to the sql text
	 * @param sql
	 * @return this
	 */
	Query text(String sql);
	
	/**
	 * adds the arguments to the list of objects to bind to the sql statement
	 * @param parameter
	 * @param parameters
	 * @return this
	 */
	Query parameters(Object parameter, Object ... parameters);
	
	/**
	 * limits the result set to the argument
	 * @param limit
	 * @return this
	 */
	Query limit(int limit);
	
	/**
	 * sets the fetchSize to set on the related PreparedStatement
	 * @param fetchSize
	 * @return this
	 */
	Query fetchSize(int fetchSize);
	/**
	 * executes the sql text and return a list obtained
	 * by calling parser.parse for each row in the resultset.
	 * @param parser
	 * @return
	 * @throws UncheckedSQLException
	 */
	<T> List<T> select(TupleParser<T> parser);
	
	/**
	 * executes the sql text and execute consumer.accept(parse.parse)
	 * for each row in the resultset.
	 *  
	 * @param parser
	 * @param consumer
	 * @return the number of rows in the resultSet
	 * @throws UncheckedSQLException
	 */
	<T> long select(TupleParser<T> parser, Consumer<T> consumer);
	/**
	 * returns the result of parser.parse for the first row in the resultSet)
	 *
	 * @param parser
	 * @return an Optional containing the parsed first row, or Optional.empty() if resultSet was empty 
	 * @throws UncheckedSQLException
	 */
	<T> Optional<T> findFirst(TupleParser<T> parser);
	
	/**
	 * execute the sql text 
	 * @return return value of statement.executeUpdate();
	 * @throws UncheckedSQLException
	 */
	int executeUpdate();
	
	/**
	 * execute a batch of sql statements,
	 * calling binder.bind for each element in batch
	 * @param batch
	 * @param binder
	 * @return return value of statement.executeBatch();
	 * @throws UncheckedSQLException
	 */
	<T> int[] executeBatch(Iterable<? extends T> batch, Binder<? super T> binder);
	
	/**
	 * exectues the sql text and
	 * returns the value of parser.parse(statement.getGeneratedKey().next())
	 * 
	 * @param parser
	 * @return
	 */
	<T> T generatedKey(TupleParser<T> parser);
	
	/**
	 * executes the sql text and collect the resultSet in the object provided by the supplier
	 * by executing accumulator.accept 
	 * @param supplier
	 * @param accumulator
	 * @return an Optional containing the supplied Object, or Optional.empty() if resultSet was empty
	 * @throws UncheckedSQLException
	 */
	<T> Optional<T> collect(TupleParser<T> supplier, TupleAccumulator<T> accumulator);

	/**
	 * creates a new Query instance on the given DataSource
	 * @param dataSource
	 * @return 
	 */
	static Query on(DataSource dataSource) {
		return new DataSourceQuery(dataSource);
	}
	
	/**
	 * creates a new Query instance on the given connection
	 * @param connection
	 * @return
	 */
	static Query on(Connection connection) {
		return new ConnectionQuery(connection);
	}
	
}
