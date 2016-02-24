package com.amplifino.nestor.rdbms.schema.impl;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.amplifino.nestor.rdbms.schema.Column;
import com.amplifino.nestor.rdbms.schema.PrimaryKey;

class PrimaryKeyImpl extends TableConstraintImpl implements PrimaryKey {

	PrimaryKeyImpl(TableImpl table, String name) {
		super(table, name);
	}
	
	@Override
	public boolean isPrimaryKey() {
		return true;
	}
	
	@Override
	public String ddl() {
		return 
		 "CONSTRAINT " +
		 name() + 
		 " PRIMARY KEY (" +
		String.join(",", columns().stream().map(Column::name).collect(Collectors.toList())) +
		")";
		 
	}
	
	static class Builder implements PrimaryKey.Builder {

		private PrimaryKeyImpl primaryKey;
		
		Builder(TableImpl table, String name) {
			this.primaryKey = new PrimaryKeyImpl(table, name);
		}
		
		@Override
		public PrimaryKey.Builder on(Column column, Column... columns) {
			primaryKey.add((ColumnImpl) column); 
			Arrays.stream(columns).map(ColumnImpl.class::cast).forEach(primaryKey::add);
			return this;
			
		}

		@Override
		public PrimaryKey add() {
			primaryKey.table().add(primaryKey);
			return primaryKey;
		}
		
	}

}
