package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.sql.DataSource;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Query encapsulates a SQL query and provides a fluent interface, mapping checked SQLExceptions to UncheckedSQLException.
 * 
 * Sample usage:
 * <pre>
 * {@code
 * 	List<String> names = Query.on(dataSource)
 * 		.text("select name from mytable ")
 * 		.text("where id = ? and moddate > ? ")
 * 		.parameters( id, moddate)
 * 		.select( r -> r.getString(1));
 * } 
 * </pre>
 * Both text and parameters can be repeated and interleaved.
 * 
 * After a terminal operation the Query instance should be discarded.
 * 
 */
@ProviderType
public interface Query {
	
	/**
	 * adds the argument to the sql text
	 * text() can be repeated multiple times and interleaved with parameters
	 * 
	 * @param sql
	 * @return this
	 */
	Query text(String sql);
	
	/**
	 * adds the arguments to the list of objects to bind to the sql statement
	 * Most parameters will be bound using PreparedStatement.setObject, 
	 * except for instances classes in the java.time package, which are converted
	 * to the corresponding classes in java.sql and use PreparedStatement.setTimeStamp|setDate|setTime.
	 * 
	 * parameters can be repeated multiple times, interleaved with text.
	 * the parameters are bound in the same order.
	 *  
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
	 * This is a terminal operation.
	 * 
	 * @param parser
	 * @return
	 * @throws UncheckedSQLException
	 */
	<T> List<T> select(TupleParser<T> parser);
	
	/**
	 * executes the sql text, parse each row using parser and pass the result to the consumer.
	 * While logically equivalent to
	 * <pre>
	 * {@code
	 * 	   select(parser).forEach(consumer);
	 * }
	 * this avoids the need for the intermediate list and corresponding memory need for large queries.
	 * 
	 * This is a terminal operation.
	 *  
	 * @param parser
	 * @param consumer
	 * @return the number of rows in the resultSet
	 * @throws UncheckedSQLException
	 */
	<T> long select(TupleParser<T> parser, Consumer<T> consumer);
	
	/**
	 * returns the result of parser.parse for the first row in the resultSet.
	 * 
	 * This is a terminal operation.
	 *
	 * @param parser
	 * @return an Optional containing the parsed first row, or Optional.empty() if resultSet was empty 
	 * @throws UncheckedSQLException
	 */
	<T> Optional<T> findFirst(TupleParser<T> parser);

	/**
	 * returns the result of parser.parse for the first row in the resultSet.
	 * throws an error if the resultSet has more than one row
	 * 
	 * This is a terminal operation.
	 *
	 * @param parser
	 * @return an Optional containing the parsed first row, or Optional.empty() if resultSet was empty 
	 * @throws UncheckedSQLException
	 * @throws IllegalStateException
	 */
	<T> Optional<T> selectOne(TupleParser<T> parser);

	/**
	 * execute the sql text.
	 * 
	 * this is a terminal operation.
	 * 
	 * @return return value of statement.executeUpdate();
	 * @throws UncheckedSQLException
	 */
	int executeUpdate();
	
	/**
	 * execute a batch of sql statements,
	 * calling binder.bind for each element in batch
	 * 
	 * this is a terminal operation.
	 * 
	 * @param batch
	 * @param binder
	 * @return return value of statement.executeBatch();
	 * @throws UncheckedSQLException
	 */
	<T> int[] executeBatch(Iterable<? extends T> batch, Binder<? super T> binder);
	
	/**
	 * exectues the sql text and
	 * returns the value of parser.parse(statement.getGeneratedKey().next()).
	 * 
	 * this is a terminal operation.
	 * 
	 * @param parser
	 * @return
	 */
	<T> T generatedKey(TupleParser<T> parser);
	
	/**
	 * executes the sql text and collect the resultSet in the object provided by the supplier
	 * by executing accumulator.accept 
	 * The supplier will be invoked with the first row as argument.
	 * 
	 * this is a terminal operation.
	 * 
	 * @param supplier
	 * @param accumulator
	 * @return an Optional containing the supplied Object, or Optional.empty() if resultSet was empty
	 * @throws UncheckedSQLException
	 */
	<T> Optional<T> collect(TupleParser<T> supplier, TupleAccumulator<T> accumulator);

	/**
	 * for debugging and instrumentation
	 * @return the accumulated sql text
	 */
	String text();
	
	/**
	 * for debugging and instrumentation
	 * @return a list of accumulated bind parameters
	 */
	List<Object> parameters();
	
	/**
	 * adds a  subquery. Both text and parameters are copied
	 * @param subQuery
	 * @return this Query
	 */
	Query add(Query subQuery);
	
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
	
	static void startTrace(TraceOption option, TraceOption ... options) {
		int traceMask = 1 << option.ordinal();
		for (TraceOption o : options) {
			traceMask |= 1 << o.ordinal();
		}
		QueryHandler.setTraceMask(traceMask);
	}
	
	static void stopTrace() {
		QueryHandler.setTraceMask(0);
	}
	
	enum TraceOption {
		SQLTEXT,
		PARAMETERS,
		FETCHCOUNT;
	}
	
}
