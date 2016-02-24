package com.amplifino.nestor.rdbms.schema.test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

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
import com.amplifino.nestor.rdbms.schema.TableBundle;
import com.amplifino.nestor.rdbms.schema.TableConstraint;

import org.h2.jdbcx.JdbcDataSource;

public class SchemaTester {
	
	private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

	@Test
	public void test() throws SQLException {
		SchemaService service = getService(SchemaService.class);
		Assert.assertTrue(service != null);
		TableBundle.Builder bundleBuilder = service.builder("test");
		Table.Builder tableBuilder = bundleBuilder.builder("testtable");
		tableBuilder.column("name").varChar(20).add();
		tableBuilder.build();
		bundleBuilder.build().tables().forEach( table -> table.ddl().forEach(System.out::println));
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl("jdbc:h2:mem:db1");
		Statement statement = dataSource.getConnection().createStatement();
		for (Table table : service.bundle("test").tables()) {
			for (String ddl : table.ddl()) {
				statement.executeUpdate(ddl);
			}
		}
		
	}
	
	@Test
	public void catalog() throws SQLException {
		SchemaService service = getService(SchemaService.class);
		Assert.assertTrue(service != null);
		TableBundle.Builder bundleBuilder = service.builder("catalog");
		for (Schema schema : Schema.values()) {
			Table.Builder tableBuilder = bundleBuilder.builder(schema.name());
			schema.complete(tableBuilder);
			tableBuilder.build();
		}
		bundleBuilder.build();
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl("jdbc:h2:mem:db1");
		Statement statement = dataSource.getConnection().createStatement();
		for (Table table : service.bundle("catalog").tables()) {
			for (String ddl : table.ddl()) {
				statement.executeUpdate(ddl);
			}
		}
		TableBundle bundle = service.bundle("catalog");
		int insertCount = Query.on(dataSource)
			.text(bundle.table(Schema.TABLEBUNDLE.name()).insertSql())
			.parameters(bundle.name())
			.executeUpdate();
		Assert.assertEquals(1,  insertCount);
		int selectCount = Query.on(dataSource)
			.text(bundle.table(Schema.TABLEBUNDLE.name()).selectSql())
			.select(tuple -> tuple.getString(1))
			.size();
		Assert.assertEquals(1,  selectCount);
		for (Table table : bundle.tables()) {
			insertCount = Query.on(dataSource)
				.text(bundle.table(Schema.TABLETABLE.name()).insertSql())
				.parameters(bundle.name(), table.name())
				.executeUpdate();
				Assert.assertEquals(1,  insertCount);
			for (Column column : table.columns()) {
				insertCount = Query.on(dataSource)
					.text(bundle.table(Schema.TABLECOLUMN.name()).insertSql())
					.parameters(bundle.name(), table.name(), column.name(), column.type())
					.parameters(column.isNotNull() ? "Y" : "N" , column.formula().orElse(null))
					.executeUpdate();
				Assert.assertEquals(1,  insertCount);
			}
			for (TableConstraint constraint : table.constraints()) {
				insertCount = Query.on(dataSource)
					.text(bundle.table(Schema.TABLECONSTRAINT.name()).insertSql())
					.parameters(
						bundle.name(), 
						table.name(), 
						constraint.name(), 
						typeString(constraint), 
						constraint.isForeignKey() ? ((ForeignKey) constraint).referencedTable().bundle().name() : null,
						constraint.isForeignKey() ? ((ForeignKey) constraint).referencedTable().name() : null,
						constraint.isForeignKey() ? deleteRuleString((ForeignKey) constraint) :  null)
					.executeUpdate();
				Assert.assertEquals(1,  insertCount);
				int ordinal = 1;
				for (Column column : constraint.columns()) {
					insertCount = Query.on(dataSource)
						.text(bundle.table(Schema.COLUMNINCONSTRAINT.name()).insertSql())
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
			.text(bundle.table(Schema.TABLETABLE.name()).selectSql())
			.select(tuple -> tuple.getString(2))
			.size();
		Assert.assertEquals(bundle.tables().size(), selectCount);
		selectCount = Query.on(dataSource)
				.text(bundle.table(Schema.TABLECOLUMN.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
		Assert.assertEquals(bundle.tables().stream().flatMap(t -> t.columns().stream()).count(), selectCount);
		selectCount = Query.on(dataSource)
				.text(bundle.table(Schema.TABLECONSTRAINT.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
		Assert.assertEquals(bundle.tables().stream().flatMap(t -> t.constraints().stream()).count(), selectCount);
		selectCount = Query.on(dataSource)
				.text(bundle.table(Schema.COLUMNINCONSTRAINT.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
		Assert.assertEquals(bundle.tables().stream().flatMap(t -> t.constraints().stream()).flatMap(c -> c.columns().stream()).count(), selectCount);
		bundle.tables().stream().flatMap(t ->t.constraints().stream())
			.forEach(c -> Query.on(dataSource)
				.text(bundle.table(Schema.TABLECONSTRAINT.name()).deleteSql())
				.parameters(bundle.name(), c.table().name(), c.name())
				.executeUpdate());
		int deleteCount = Query.on(dataSource)
				.text(bundle.table(Schema.TABLEBUNDLE.name()).deleteSql())
				.parameters(bundle.name())
				.executeUpdate();
		Assert.assertEquals(1,  deleteCount);
		selectCount = Query.on(dataSource)
				.text(bundle.table(Schema.COLUMNINCONSTRAINT.name()).selectSql())
				.select(tuple -> tuple.getString(3))
				.size();
		Assert.assertEquals(0,  selectCount);
		selectCount = Query.on(dataSource)
				.text(bundle.table(Schema.TABLECOLUMN.name()).selectSql())
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
