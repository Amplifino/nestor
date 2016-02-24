package com.amplifino.nestor.rdbms.schema;

public interface Unique extends TableConstraint {
		
	interface Builder {
		Builder on(Column column, Column ... columns);
		Unique add();
	}
}


