package com.amplifino.nestor.rdbms.schema;

import java.util.List;

public interface Index {
	List<? extends Column> columns();
	String name();
	int compress();
	Table table();

	public interface Builder {
        Builder on(Column... columns);
        Builder compress(int number);
        Index add();
	}
}
