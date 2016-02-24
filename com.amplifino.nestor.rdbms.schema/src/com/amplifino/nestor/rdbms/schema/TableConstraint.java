package com.amplifino.nestor.rdbms.schema;

import java.util.List;

public interface TableConstraint {
	String name();
    List<? extends Column> columns();
	Table table();
	
	boolean isPrimaryKey();
	boolean isUnique();
	boolean isForeignKey();
	boolean hasColumn(Column column);
	boolean isNotNull();
	boolean noDdl();

}
