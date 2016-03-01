package com.amplifino.nestor.rdbms.schema.impl;

import java.util.Arrays;

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
	String constraintType() {
		return "PRIMARY KEY";
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
			if (!primaryKey.columns().equals(primaryKey.table().columns().subList(0, primaryKey.columns().size()))) {
				throw new IllegalArgumentException("Columns specified for primary key do not match first columns in table");
			}
			return this;			
		}

		@Override
		public PrimaryKey add() {
			primaryKey.table().add(primaryKey);
			return primaryKey;
		}
		
	}

}
