package com.amplifino.nestor.jdbc.api;

import org.osgi.annotation.versioning.ProviderType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

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
	 * Adds the argument to the sql text.
	 * This call can be repeated multiple times and interleaved with {@link #parameters()}.
	 *
	 * @param sql sql text
	 * @return this
	 */
	Query text(String sql);

	default Query columns(String... names) {
		return this.text(String.join(", ", names));
	}

	default Query columns(String alias, String... names) {
		return this.text(Stream.of(names).map(each -> alias + "." + each).collect(joining(", ")));
	}

	default Query values(int columns) {
		return this.text(IntStream.range(0, columns).mapToObj(i -> "?").collect(joining(", ", "values (",") ")));
	}

	/**
     * Adds the arguments to the list of objects to bind to the sql statement.
     * Most parameters will be bound using PreparedStatement.setObject,
     * except for instances classes in the java.time package, which are converted
     * to the corresponding classes in java.sql and use PreparedStatement.setTimeStamp|setDate|setTime.
     * <br>
     * This call can be repeated multiple times, interleaved with text.
     * The parameters are bound in the same order.
     *
     * @param parameter first bind parameter
     * @param parameters additional bind parameters
     * @return this
     */
	Query parameters(Object parameter, Object ... parameters);

	/**
	 * limits the result set to the argument
	 * @param limit maximum number of rows to return
	 * @return this
	 */
	Query limit(int limit);

	/**
	 * sets the fetchSize to set on the related PreparedStatement
	 * @param fetchSize fetch size
	 * @return this
	 */
	Query fetchSize(int fetchSize);

	/**
	 * executes the sql text and return a list obtained
	 * by calling parser.parse for each row in the resultset.
	 * This is a terminal operation.
	 *
	 * @param parser converts resultset row to result type
	 * @param <T> result type
	 * @return result list
	 * @throws UncheckedSQLException if a jdbc call threw a SQLException
	 */
	<T> List<T> select(TupleParser<T> parser);

	/**
	 * executes the sql text, parse each row using parser and pass the result to the consumer.
	 * While logically equivalent to
	 * <pre>
	 * {@code
	 * 	   select(parser).forEach(consumer);
	 * }
	 * </pre>
	 * this avoids the need for the intermediate list and corresponding memory need for large queries.
	 *
	 * This is a terminal operation.
	 *
	 * @param parser converts resultset row to result type
	 * @param consumer consumes result type
	 * @param<T> result type
	 * @return the number of rows in the resultSet
	 * @throws UncheckedSQLException if a jdbc call threw a SQLException
	 */
	<T> long select(TupleParser<T> parser, Consumer<T> consumer);

	/**
	 * returns the result of parser.parse for the first row in the resultSet.
	 *
	 * This is a terminal operation.
	 *
	 * @param parser converts resultset row to result type
	 * @param<T> result type
	 * @return an Optional containing the parsed first row, or Optional.empty() if resultSet was empty
	 * @throws UncheckedSQLException if a jdbc call threw a SQLException
	 */
	<T> Optional<T> findFirst(TupleParser<T> parser);

	/**
	 * returns the result of parser.parse for the first row in the resultSet.
	 * throws an error if the resultSet has more than one row
	 *
	 * This is a terminal operation.
	 *
	 * @param parser converts resultset row to result type
	 * @param<T> result type
	 * @return an Optional containing the parsed first row, or Optional.empty() if resultSet was empty
	 * @throws UncheckedSQLException if a jdbc call threw a SQLException
	 * @throws IllegalStateException if the query returns more than one row
	 */
	<T> Optional<T> selectOne(TupleParser<T> parser);

	/**
	 * execute the sql text.
	 *
	 * this is a terminal operation.
	 *
	 * @return return value of statement.executeUpdate();
	 * @throws UncheckedSQLException if a jdbc call threw a SQLException
	 */
	int executeUpdate();

	/**
	 * execute a batch of sql statements,
	 * calling binder.bind for each element in batch
	 *
	 * this is a terminal operation.
	 *
	 * @param batch to process
	 * @param binder bind batch entry to statement
	 * @param <T> batch entry type
	 * @return return value of statement.executeBatch();
	 * @throws UncheckedSQLException if a jdbc call threw a SQLException
	 */
	<T> int[] executeBatch(Iterable<? extends T> batch, Binder<? super T> binder);

	/**
	 * exectues the sql text and
	 * returns the value of parser.parse(statement.getGeneratedKey().next()).
	 *
	 * this is a terminal operation.
	 *
	 * @param parser converts resultset to generated key
	 * @param <T> generated key type
	 * @return the generated key
	 * @throws UncheckedSQLException if a jdbc call threw a SQLException
	 */
	<T> T generatedKey(TupleParser<T> parser);

	/**
	 * executes the sql text and collect the resultSet in the object provided by the supplier
	 * by executing accumulator.accept
	 * The supplier will be invoked with the first row as argument.
	 *
	 * this is a terminal operation.
	 *
	 * @param supplier supplies the result
	 * @param accumulator add a row to the resut
	 * @param <T> the result type
	 * @return an Optional containing the supplied Object, or Optional.empty() if resultSet was empty
	 * @throws UncheckedSQLException if a jdbc call threw a SQLException
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
	 * adds a subquery. Both text and parameters are copied
	 * @param subQuery query to add
	 * @return this
	 */
	Query add(Query subQuery);

	/**
	 * adds an in clause with as many bind markers as the size of the collection and adds the collection elements as bind parameters.
	 * the callers must ensure that all previous bind parameters are set prior to this call, and that the collection size does not
	 * exceed the limits of the database. (e.g. Oracle has a limitation that an expression list can not contain more than 1000 elements).
	 *
	 * @param collection elements to bind
	 * @return this
	 */
	Query in(Collection<?> collection);

	/**
	 * creates a new Query instance on the given DataSource
	 * @param dataSource connection provider
	 * @return a new query
	 */
	static Query on(DataSource dataSource) {
		return new DataSourceQuery(dataSource);
	}

	/**
	 * creates a new Query instance on the given connection
	 * @param connection to use for the query
	 * @return a new query
	 */
	static Query on(Connection connection) {
		return new ConnectionQuery(connection);
	}

	/**
	 * starts tracing query execution
	 * @param option first trace option
	 * @param options additional trace options
	 */
	static void startTrace(TraceOption option, TraceOption ... options) {
		int traceMask = 1 << option.ordinal();
		for (TraceOption o : options) {
			traceMask |= 1 << o.ordinal();
		}
		QueryHandler.setTraceMask(traceMask);
	}

	/**
	 * stops tracing
	 */
	static void stopTrace() {
		QueryHandler.setTraceMask(0);
	}

	/**
	 * Trace options
	 *
	 */
	enum TraceOption {
		/**
		 * trace sql text
		 */
		SQLTEXT,
		/**
		 * trace sql parameters
		 */
		PARAMETERS,
		/**
		 * trace resultset size
		 */
		FETCHCOUNT;
	}

}
