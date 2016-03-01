package com.amplifino.nestor.rdbms.schema.impl;

import java.util.Optional;

import com.amplifino.nestor.rdbms.schema.Column;

class ColumnImpl implements Column {

	private final TableImpl table;
	private final String name;
	private String type;
	private boolean notNull;
	private Optional<String> formula = Optional.empty();
	
	ColumnImpl(TableImpl table, String name) {
		this.table = table;
		this.name = name;
	}

	@Override
	public TableImpl table() {
		return table;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public String type() {
		return type;
	}

	@Override
	public boolean isPrimaryKeyColumn() {
		return false;
	}

	@Override
	public boolean isNotNull() {
		return notNull;
	}

	@Override
	public boolean isVirtual() {
		return formula.isPresent();
	}
	
	@Override
	public Optional<String> formula() {
		return formula;
	}
	
	static class Builder implements Column.Builder {
		
		private final ColumnImpl column;
		
		Builder(TableImpl table, String name) {
			this.column = new ColumnImpl(table, name);
		}

		@Override
		public Column.Builder type(String type) {
			column.type = type;
			return this;
		}

		@Override
		public Column.Builder notNull() {
			column.notNull = true;
			return this;
		}

		@Override
		public Column.Builder number() {
			return type("NUMBER");
		}
		
		@Override
		public Column.Builder decimal(int precision, int scale) {
			return type("DECIMAL(" + precision + "," + scale + ")");
		}

		@Override
		public Column.Builder character(int length) {
			return type("CHARACHER(" + length + ")");
		}
		@Override
		public Column.Builder varChar(int length) {
			return type("VARCHAR(" + length + ")");
		}

		@Override
		public Column.VirtualBuilder as(String formula) {
			column.formula = Optional.of(formula);
			return new VirtualBuilder(column);
		}

		@Override
		public Column add() {
			column.table.add(column);
			return column;
		}
		
	}
	
	static class VirtualBuilder implements Column.VirtualBuilder {
		
		private final ColumnImpl column;
		
		VirtualBuilder(ColumnImpl column) {
			this.column = column;
		}

		@Override
		public Column add() {
			column.table.add(column);
			return column;
		}
		
	}
}
