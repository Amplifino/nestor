package com.amplifino.nestor.rdbms.schema;

public interface PrimaryKey extends TableConstraint {
		
	interface Builder {
		Builder on(Column column, Column ... columns);
		PrimaryKey add();
	}
}


