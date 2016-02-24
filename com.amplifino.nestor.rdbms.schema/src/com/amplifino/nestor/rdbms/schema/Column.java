package com.amplifino.nestor.rdbms.schema;

import java.util.Optional;

public interface Column {
	String name();
	String type();
	Table table();
	Optional<String> formula();
	
	boolean isPrimaryKeyColumn();
	boolean isNotNull();
	boolean isVirtual();
	
	interface Builder {
		Builder type(String type);
		Builder notNull();
		Builder number();
		Builder varChar(int length);
		VirtualBuilder as(String formula);
		Column add();
	}
	
	interface VirtualBuilder {
		Column add();
	}
}
