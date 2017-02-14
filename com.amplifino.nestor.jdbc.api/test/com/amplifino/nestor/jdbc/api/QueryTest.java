package com.amplifino.nestor.jdbc.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class QueryTest {

	private Connection connection;
	
	@Before
	public void setup() throws SQLException {
		Query.startTrace(Query.TraceOption.SQLTEXT, Query.TraceOption.PARAMETERS, Query.TraceOption.FETCHCOUNT);
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl("jdbc:h2:mem:");
		connection = dataSource.getConnection();
		Query.on(connection)
			.text("create table test (id integer not null primary key, name varchar(255) not null)")
			.executeUpdate();
	}
	
	@Test
	public void testApi() {
		Assert.assertTrue(select().isEmpty());
		int insertCount = Query.on(connection)
			.text("insert into test (id, name) values(?,?) ")
			.parameters(1, "ONE")
			.executeUpdate();
		Assert.assertEquals(1, insertCount);
		Assert.assertEquals(1, select().size());
		int updateCount = Query.on(connection)
			.text("update test set name = ? where id = ? ")
			.parameters("ONEANDONLY", 1)
			.executeUpdate();
		Assert.assertEquals(1, updateCount);
		Assert.assertEquals(1, select().size());
		int deleteCount = Query.on(connection)
			.text("delete from test where id = ?")
			.parameters(1)
			.executeUpdate();
		Assert.assertEquals(1, deleteCount);
		Assert.assertEquals(0, select().size());		
	}
	
	@Test
	public void testBatch() {
		final int rowCount = 100;
		Query.on(connection)
			.text("insert into test (id, name) values(?,?) ")
			.executeBatch(
				IntStream.range(1, rowCount + 1)
					.mapToObj(i -> new AbstractMap.SimpleImmutableEntry<>(i , "Entry" + i))
					.collect(Collectors.toList()),
				(statement, entry) -> {
					statement.setInt(1, entry.getKey());
					statement.setString(2, entry.getValue());
				});
		Assert.assertEquals(rowCount, select().size());
		int[] count = new int[1];
		Query.on(connection)
			.text("select * from test")
			.select(r -> new AbstractMap.SimpleImmutableEntry<>(r.getInt(1), r.getString(2)) , entry -> count[0] = count[0] + 1);
		Assert.assertEquals(rowCount, count[0]);
		Map<Integer, String> rows = Query.on(connection)
			.text("select * from test")
			.collect(
				r -> new HashMap<Integer, String>(),
				(map, r) -> map.put(r.getInt(1), r.getString(2)))
			.get();	
		Assert.assertEquals(rowCount, rows.size());
	}
	
	@Test
	public void testTransaction() {		
		try {
			LocalTransaction.with(connection)
				.call( c -> {
					int result = Query.on(c)
						.text("insert into test (id, name) values(?,?)")
						.parameters(1, "ONE")
						.executeUpdate();
					if (result == 1) {
						throw new IllegalStateException();
					}
					return 0;
				});
		} catch (IllegalStateException e) {		
			Assert.assertTrue(select().isEmpty());
			LocalTransaction.with(connection)
				.call( c -> Query.on(c)
					.text("insert into test (id, name) values(?,?)")
					.parameters(1, "ONE")
					.executeUpdate());
			Assert.assertEquals(1, select().size());
			return;
		}
		Assert.fail();
	}
	
	private List<Map.Entry<Integer, String>> select() {
		return Query.on(connection)
			.text("select id, name from test")
			.select(r -> new AbstractMap.SimpleImmutableEntry<>(r.getInt(1), r.getString(2)));
	}
}
