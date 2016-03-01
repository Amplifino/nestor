package com.amplifino.nestor.rdbms.schema;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
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
