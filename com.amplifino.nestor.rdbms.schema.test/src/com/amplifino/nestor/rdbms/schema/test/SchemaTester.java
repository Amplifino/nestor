package com.amplifino.nestor.rdbms.schema.test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import com.amplifino.nestor.jdbc.api.Query;
import com.amplifino.nestor.rdbms.schema.Column;
import com.amplifino.nestor.rdbms.schema.ForeignKey;
import com.amplifino.nestor.rdbms.schema.SchemaService;
import com.amplifino.nestor.rdbms.schema.Table;
import com.amplifino.nestor.rdbms.schema.Schema;
import com.amplifino.nestor.rdbms.schema.TableConstraint;

import org.h2.jdbcx.JdbcDataSource;

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
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl("jdbc:h2:mem:db1");
		Statement statement = dataSource.getConnection().createStatement();
		for (Table table : service.schema("test").tables()) {
			for (String ddl : table.ddl()) {
				statement.executeUpdate(ddl);
			}
		}
		
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
		schemaBuilder.build();
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl("jdbc:h2:mem:db1");
		Statement statement = dataSource.getConnection().createStatement();
		statement.executeUpdate(table.ddl().get(0));
		Assert.assertEquals(0,  Query.on(dataSource).text(table.selectSql()).select(rs -> "").size());		
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
		schemaBuilder.build();
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl("jdbc:h2:mem:db1");
		Statement statement = dataSource.getConnection().createStatement();
		for (Table table : service.schema("catalog").tables()) {
			for (String ddl : table.ddl()) {
				statement.executeUpdate(ddl);
			}
		}
		Schema bundle = service.schema("catalog");
		int insertCount = Query.on(dataSource)
			.text(bundle.table(CatalogSchema.TABLEBUNDLE.name()).insertSql())
			.parameters(bundle.name())
			.executeUpdate();
		Assert.assertEquals(1,  insertCount);
		int selectCount = Query.on(dataSource)
			.text(bundle.table(CatalogSchema.TABLEBUNDLE.name()).selectSql())
			.select(tuple -> tuple.getString(1))
			.size();
		Assert.assertEquals(1,  selectCount);
		for (Table table : bundle.tables()) {
			insertCount = Query.on(dataSource)
				.text(bundle.table(CatalogSchema.TABLETABLE.name()).insertSql())
				.parameters(table.schema().name(), table.name())
				.executeUpdate();
				Assert.assertEquals(1,  insertCount);
			for (Column column : table.columns()) {
				insertCount = Query.on(dataSource)
					.text(bundle.table(CatalogSchema.TABLECOLUMN.name()).insertSql())
					.parameters(
						bundle.name(), 
						table.name(), 
						column.name(), 
						column.type(),
						column.isNotNull() ? "Y" : "N" , 
						column.formula().orElse(null))
					.executeUpdate();
				Assert.assertEquals(1,  insertCount);
			}
			for (TableConstraint constraint : table.constraints()) {
				Optional<ForeignKey> foreignKey = Optional.of(constraint).filter(TableConstraint::isForeignKey).map(ForeignKey.class::cast);
				insertCount = Query.on(dataSource)
					.text(bundle.table(CatalogSchema.TABLECONSTRAINT.name()).insertSql())
					.parameters(
						bundle.name(), 
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
						.text(bundle.table(CatalogSchema.COLUMNINCONSTRAINT.name()).insertSql())
						.parameters(
							bundle.name(),
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
			.text(bundle.table(CatalogSchema.TABLETABLE.name()).selectSql())
			.select(tuple -> tuple.getString(2))
			.size();
		Assert.assertEquals(bundle.tables().size(), selectCount);
		selectCount = Query.on(dataSource)
				.text(bundle.table(CatalogSchema.TABLECOLUMN.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
		Assert.assertEquals(bundle.tables().stream().flatMap(t -> t.columns().stream()).count(), selectCount);
		selectCount = Query.on(dataSource)
				.text(bundle.table(CatalogSchema.TABLECONSTRAINT.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
		Assert.assertEquals(bundle.tables().stream().flatMap(t -> t.constraints().stream()).count(), selectCount);
		selectCount = Query.on(dataSource)
				.text(bundle.table(CatalogSchema.COLUMNINCONSTRAINT.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
		Assert.assertEquals(bundle.tables().stream().flatMap(t -> t.constraints().stream()).flatMap(c -> c.columns().stream()).count(), selectCount);
		bundle.tables().stream().flatMap(t ->t.constraints().stream())
			.forEach(c -> Query.on(dataSource)
				.text(bundle.table(CatalogSchema.TABLECONSTRAINT.name()).deleteSql())
				.parameters(bundle.name(), c.table().name(), c.name())
				.executeUpdate());
		int deleteCount = Query.on(dataSource)
				.text(bundle.table(CatalogSchema.TABLEBUNDLE.name()).deleteSql())
				.parameters(bundle.name())
				.executeUpdate();
		Assert.assertEquals(1,  deleteCount);
		selectCount = Query.on(dataSource)
				.text(bundle.table(CatalogSchema.COLUMNINCONSTRAINT.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
		Assert.assertEquals(0,  selectCount);
		selectCount = Query.on(dataSource)
				.text(bundle.table(CatalogSchema.TABLECOLUMN.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
		Assert.assertEquals(0,  selectCount);
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
