package com.amplifino.nestor.rdbms.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.amplifino.nestor.rdbms.schema.AssociationType;
import com.amplifino.nestor.rdbms.schema.Column;
import com.amplifino.nestor.rdbms.schema.ForeignKey;
import com.amplifino.nestor.rdbms.schema.Index;
import com.amplifino.nestor.rdbms.schema.PrimaryKey;
import com.amplifino.nestor.rdbms.schema.Table;
import com.amplifino.nestor.rdbms.schema.TableConstraint;
import com.amplifino.nestor.rdbms.schema.Unique;

class TableImpl implements Table {
	
	private final SchemaImpl schema;
	private final String name;
	private final List<ColumnImpl> columns = new ArrayList<>();
	private final List<TableConstraintImpl> constraints = new ArrayList<>();
	private final List<IndexImpl> indexes = new ArrayList<>();
	
	TableImpl(SchemaImpl bundle, String name) {
		this.schema = bundle;
		this.name = name;
	}

	@Override
	public List<String> ddl() {
		return new TableDdlBuilder(this).getDdl();
	}

	@Override
	public SchemaImpl schema() {
		return schema;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String qualifiedName() {
		return schema.schemaOwner().map(schema -> schema+".").orElse("") + name;
	}

	@Override
	public List<ColumnImpl> columns() {
		return Collections.unmodifiableList(columns);
	}

	@Override
	public ColumnImpl getColumn(String name) {
		return columns.stream().filter(column -> column.name().equals(name)).findFirst().orElseThrow(IllegalArgumentException::new);
	}

	@Override
	public List<TableConstraintImpl> constraints() {
		return Collections.unmodifiableList(constraints);
	}

	@Override
	public PrimaryKeyImpl primaryKey() {
		return constraints.stream()
			.filter(TableConstraint::isPrimaryKey)
			.map(PrimaryKeyImpl.class::cast)
			.findFirst()
			.get();
	}

	@Override
	public List<ForeignKeyImpl> foreignKeys() {
		return constraints.stream()
			.filter(TableConstraint::isForeignKey)
			.map(ForeignKeyImpl.class::cast)
			.collect(Collectors.toList());
	}

	@Override
	public List<ColumnImpl> primaryKeyColumns() {
		return primaryKey().columns();
	}

	@Override
	public List<IndexImpl> indexes() {
		return Collections.unmodifiableList(indexes);
	}
	
	void add(ColumnImpl column) {
		columns.add(column);
	}
	
	void add(TableConstraintImpl constraint) {
		constraints.add(constraint);
	}
	
	void add(IndexImpl index) {
		indexes.add(index);
	}

	@Override
	public String insertSql() {
		return 
			columns.stream()
				.map(Column::name)
				.collect(Collectors.joining(",","insert into " + qualifiedName() + " (",") "))
				.concat(columns.stream().map(c -> "?").collect(Collectors.joining(",","values (",")")));
	}

	@Override
	public String updateSql() {
		return 
			columns.stream()
				.skip(primaryKeyColumns().size())
				.map(Column::name)
				.collect(Collectors.joining(" = ?, ", "update " + qualifiedName() + " set ", " = ? "))
				.concat(primaryKeyColumns().stream()
						.map(Column::name)
						.collect(Collectors.joining(" = ? and " , " where ", " = ?")));
	}

	@Override
	public String deleteSql() {
		return primaryKeyColumns().stream()
			.map(Column::name)
			.collect(Collectors.joining(" = ? and ", "delete from " + qualifiedName() + " where ", " = ?"));
	}
	
	@Override
	public String selectSql() {
		return columns.stream()
			.map(Column::name)
			.collect(Collectors.joining(", ", "select ", " from " + qualifiedName()));		
	}
	
	@Override
	public AssociationType associationType(Table other) {
		Optional<? extends ForeignKey> foreignKey = foreignKeys().stream()
			.filter(f -> f.referencedTable().equals(other))
			.findFirst();
		if (foreignKey.isPresent()) {
			if (foreignKey.get().isNotNull()) {
				return AssociationType.EXACTLYONE;
			} else {
				return AssociationType.ZEROORONE;
			}
		}
		foreignKey = other.foreignKeys().stream()
			.filter(f -> f.referencedTable().equals(this))
			.findFirst();
		if (foreignKey.isPresent()) {
			return AssociationType.ZEROORMORE;
		} else {
			return AssociationType.NONE;
		}
	}
	
	static class BuilderImpl implements Table.Builder {
		private TableImpl table;
		
		BuilderImpl(SchemaImpl tableBundle, String name) {
			this.table = new TableImpl(tableBundle, name);
		}
		
		@Override
		public Column.Builder column(String name) {
			return new ColumnImpl.Builder(table,name);
		}

		@Override
		public PrimaryKey.Builder primaryKey(String name) {
			return new PrimaryKeyImpl.Builder(table, name);
		}

		@Override
		public Unique.Builder unique(String name) {
			return new UniqueImpl.Builder(table, name);
		}

		@Override
		public ForeignKey.Builder foreignKey(String name) {
			return new ForeignKeyImpl.Builder(table, name);
		}

		@Override
		public Index.Builder index(String name) {
			 return new IndexImpl.Builder(table, name);
		}
		
		@Override
		public TableImpl build() {
			table.schema.add(table);
			return table;
		}

	}

}
