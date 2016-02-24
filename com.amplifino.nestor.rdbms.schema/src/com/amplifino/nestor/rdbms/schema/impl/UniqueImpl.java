package com.amplifino.nestor.rdbms.schema.impl;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.amplifino.nestor.rdbms.schema.Column;
import com.amplifino.nestor.rdbms.schema.Unique;

class UniqueImpl extends TableConstraintImpl implements Unique {

	UniqueImpl(TableImpl table, String name) {
		super(table, name);
	}
	
	@Override
	public boolean isUnique() {
		return true;
	}
	
	@Override
	public String ddl() {
		return 
			 "CONSTRAINT " +
			 name() + 
			 " UNIQUE (" +
			 String.join(",", columns().stream().map(Column::name).collect(Collectors.toList())) +
			 ")";
	}
	
	static class Builder implements Unique.Builder {

		private UniqueImpl unique;
		
		Builder(TableImpl table, String name) {
			this.unique = new UniqueImpl(table, name);
		}
		
		@Override
		public Unique.Builder on(Column column , Column... columns) {
			unique.add((ColumnImpl) column);
			Arrays.stream(columns).map(ColumnImpl.class::cast).forEach(unique::add);
			return this;
			
		}

		@Override
		public Unique add() {
			unique.table().add(unique);
			return unique;
		}
		
	}

}
