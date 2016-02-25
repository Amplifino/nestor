package com.amplifino.nestor.rdbms.schema.impl;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import com.amplifino.nestor.rdbms.schema.Column;
import com.amplifino.nestor.rdbms.schema.DeleteRule;
import com.amplifino.nestor.rdbms.schema.ForeignKey;
import com.amplifino.nestor.rdbms.schema.Table;

class ForeignKeyImpl extends TableConstraintImpl implements ForeignKey {

	private Table referencedTable;
	private DeleteRule deleteRule = DeleteRule.RESTRICT;
	private boolean noDdl = false;
	
	ForeignKeyImpl(TableImpl table, String name) {
		super(table, name);
	}
	
	@Override
	public boolean isForeignKey() {
		return true;
	}

	@Override
	public Table referencedTable() {
		return referencedTable;
	}

	@Override
	public DeleteRule deleteRule() {
		return deleteRule;
	}
	
	@Override
	String constraintType() {
		return "FOREIGN KEY";
	}

	@Override
	public String ddl() {
		return super.ddl()
			.concat(
				referencedTable.primaryKeyColumns().stream()
					.map(Column::name)
					.collect(Collectors.joining(", ", " references " + referencedTable().qualifiedName() + " (", ") ")))
			.concat(deleteRule.getDdl());		
	}
	
	@Override
	public boolean noDdl() {
		return noDdl;
	}
	
	static class Builder implements ForeignKey.Builder {

		private ForeignKeyImpl foreignKey;
		
		Builder(TableImpl table, String name) {
			this.foreignKey = new ForeignKeyImpl(table, name);
		}
		
		@Override
		public ForeignKey.Builder on(Column column, Column... columns) {
			foreignKey.add((ColumnImpl) column);
			Arrays.stream(columns).map(ColumnImpl.class::cast).forEach(foreignKey::add);
			return this;
			
		}

		@Override
		public ForeignKey.Builder onDelete(DeleteRule rule) {
			foreignKey.deleteRule = Objects.requireNonNull(rule);
			return this;
		}

		@Override
		public ForeignKey.Builder references(Table table) {
			foreignKey.referencedTable = table;
			return this;
		}

		@Override
		public ForeignKey.Builder references(String tableName) {
			if (tableName.equals(foreignKey.table().name())) {
				return references(foreignKey.table());
			} else {
				return references(foreignKey.table().schema().table(tableName));
			}
		}

		@Override
		public ForeignKey.Builder references(String bundleName, String tableName) {
			if (bundleName.equals(foreignKey.table().schema().name()) && tableName.equals(foreignKey.table().name())) {
				return references(foreignKey.table());
			} else {
				return references(foreignKey.table().schema().schemaService().schema(bundleName).table(tableName));
			}
		}

		@Override
		public ForeignKey.Builder noDdl() {
			foreignKey.noDdl = true;
			return this;
		}

		@Override
		public ForeignKey add() {
			foreignKey.table().add(foreignKey);
			return foreignKey;
		}
	
	}

}
