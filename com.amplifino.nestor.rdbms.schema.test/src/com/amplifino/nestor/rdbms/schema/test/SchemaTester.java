package com.amplifino.nestor.rdbms.schema.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import com.amplifino.nestor.jdbc.api.Query;
import com.amplifino.nestor.rdbms.schema.Column;
import com.amplifino.nestor.rdbms.schema.ForeignKey;
import com.amplifino.nestor.rdbms.schema.Schema;
import com.amplifino.nestor.rdbms.schema.SchemaService;
import com.amplifino.nestor.rdbms.schema.Table;
import com.amplifino.nestor.rdbms.schema.TableConstraint;

public class SchemaTester {
	
	private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

	@Test
	public void test() throws SQLException {
		SchemaService service = getService(SchemaService.class);
		Assert.assertTrue(service != null);
		Schema.Builder schemaBuilder = service.builder("test");
		Table.Builder tableBuilder = schemaBuilder.builder("testtable");
		tableBuilder.column("name").varChar(20).add();
		tableBuilder.build();
		Schema schema = schemaBuilder.build();
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl("jdbc:h2:mem:db1");
		schema.create(dataSource);
	}
	
	@Test
	public void selfReference() throws SQLException {
		SchemaService service = getService(SchemaService.class);
		Assert.assertTrue(service != null);
		Schema.Builder schemaBuilder = service.builder("selfrefence");
		Table.Builder tableBuilder = schemaBuilder.builder("treetest");
		Column id = tableBuilder.column("id").number().notNull().add();
		Column parentId = tableBuilder.column("parentid").number().notNull().add();
		tableBuilder.primaryKey("PK_TREETEST").on(id).add();
		tableBuilder.foreignKey("FK_TREETEST").on(parentId).references("treetest").add();
		Table table = tableBuilder.build();
		Schema schema = schemaBuilder.build();
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl("jdbc:h2:mem:db1");
		try (Connection keepAlive = dataSource.getConnection()) {
			schema.create(dataSource);
			Assert.assertEquals(0,  Query.on(dataSource).text(table.selectSql()).select(rs -> "").size());
		}
	}
	
	@Test
	public void catalog() throws SQLException {
		SchemaService service = getService(SchemaService.class);
		Assert.assertTrue(service != null);
		Schema.Builder schemaBuilder = service.builder("catalog");
		for (CatalogSchema schema : CatalogSchema.values()) {
			Table.Builder tableBuilder = schemaBuilder.builder(schema.name());
			schema.complete(tableBuilder);
			tableBuilder.build();
		}
		Schema schema = schemaBuilder.build();
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl("jdbc:h2:mem:db1");
		try (Connection keepAlive = dataSource.getConnection()) {
			schema.create(dataSource);
			int insertCount = Query.on(dataSource)
			.text(schema.table(CatalogSchema.TABLEBUNDLE.name()).insertSql())
			.parameters(schema.name())
			.executeUpdate();
			Assert.assertEquals(1,  insertCount);
			int selectCount = Query.on(dataSource)
				.text(schema.table(CatalogSchema.TABLEBUNDLE.name()).selectSql())
				.select(tuple -> tuple.getString(1))
				.size();
			Assert.assertEquals(1,  selectCount);
			for (Table table : schema.tables()) {
				insertCount = Query.on(dataSource)
					.text(schema.table(CatalogSchema.TABLETABLE.name()).insertSql())
					.parameters(table.schema().name(), table.name())
					.executeUpdate();
				Assert.assertEquals(1,  insertCount);
				int[] insertCounts = Query.on(dataSource)
					.text(schema.table(CatalogSchema.TABLECOLUMN.name()).insertSql())
					.executeBatch(table.columns(), this::bindColumn);
				Assert.assertEquals(table.columns().size(), insertCounts.length);
				for (TableConstraint constraint : table.constraints()) {
					Optional<ForeignKey> foreignKey = Optional.of(constraint).filter(TableConstraint::isForeignKey).map(ForeignKey.class::cast);
					insertCount = Query.on(dataSource)
						.text(schema.table(CatalogSchema.TABLECONSTRAINT.name()).insertSql())
						.parameters(
							schema.name(), 
							table.name(), 
							constraint.name(), 
							typeString(constraint), 
							foreignKey.map(k -> k.referencedTable().schema().name()).orElse(null),
							foreignKey.map(k -> k.referencedTable().name()).orElse(null),
							foreignKey.map(k -> deleteRuleString(k)).orElse(null),
							foreignKey.map(k -> k.noDdl() ? "Y" : "N").orElse(null))
						.executeUpdate();
					Assert.assertEquals(1,  insertCount);
					int ordinal = 1;
					for (Column column : constraint.columns()) {
						insertCount = Query.on(dataSource)
							.text(schema.table(CatalogSchema.COLUMNINCONSTRAINT.name()).insertSql())
							.parameters(
								schema.name(),
								table.name(),
								constraint.name(),
								ordinal++,
								column.name())
							.executeUpdate();
						Assert.assertEquals(1,  insertCount);
					}
				}
			}
			selectCount = Query.on(dataSource)
				.text(schema.table(CatalogSchema.TABLETABLE.name()).selectSql())
				.select(tuple -> tuple.getString(2))
				.size();
			Assert.assertEquals(schema.tables().size(), selectCount);
			selectCount = Query.on(dataSource)
				.text(schema.table(CatalogSchema.TABLECOLUMN.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
			Assert.assertEquals(schema.tables().stream().flatMap(t -> t.columns().stream()).count(), selectCount);
			selectCount = Query.on(dataSource)
				.text(schema.table(CatalogSchema.TABLECONSTRAINT.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
			Assert.assertEquals(schema.tables().stream().flatMap(t -> t.constraints().stream()).count(), selectCount);
			selectCount = Query.on(dataSource)
				.text(schema.table(CatalogSchema.COLUMNINCONSTRAINT.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
			Assert.assertEquals(schema.tables().stream().flatMap(t -> t.constraints().stream()).flatMap(c -> c.columns().stream()).count(), selectCount);
			schema.tables().stream().flatMap(t ->t.constraints().stream())
				.forEach(c -> Query.on(dataSource)
				.text(schema.table(CatalogSchema.TABLECONSTRAINT.name()).deleteSql())
				.parameters(schema.name(), c.table().name(), c.name())
				.executeUpdate());
			int deleteCount = Query.on(dataSource)
				.text(schema.table(CatalogSchema.TABLEBUNDLE.name()).deleteSql())
				.parameters(schema.name())
				.executeUpdate();
			Assert.assertEquals(1,  deleteCount);
			selectCount = Query.on(dataSource)
				.text(schema.table(CatalogSchema.COLUMNINCONSTRAINT.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
			Assert.assertEquals(0,  selectCount);
			selectCount = Query.on(dataSource)
				.text(schema.table(CatalogSchema.TABLECOLUMN.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
			Assert.assertEquals(0,  selectCount);
		}
	}
	
	private void bindColumn(PreparedStatement statement, Column column) throws SQLException {
		int i = 1;
		statement.setString(i++, column.table().schema().name());
		statement.setString(i++, column.table().name()); 
		statement.setString(i++, column.name()); 
		statement.setString(i++, column.type());
		statement.setString(i++, column.isNotNull() ? "Y" : "N"); 
		statement.setString(i++, column.formula().orElse(null));
	}
	
	private String typeString(TableConstraint constraint) {
		if (constraint.isPrimaryKey()) {
			return "P";
		} else if (constraint.isUnique()) {
			return "U";
		} else if (constraint.isForeignKey()) {
			return "F";
		} else {
			throw new IllegalArgumentException(constraint.toString());
		}
	}
	
	private String deleteRuleString(ForeignKey constraint) {
		switch (constraint.deleteRule()) {
			case RESTRICT:
				return "R";
			case SETNULL:
				return "N";
			case CASCADE:
				return "C";
			default:
				throw new IllegalArgumentException(constraint.deleteRule().toString());
		}
	}
	
	private <T> T getService(Class<T> clazz) {
		ServiceTracker<T,T> st = new ServiceTracker<>(context, clazz, null);
		st.open();
		try {
			return Objects.requireNonNull(st.waitForService(1000L));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
