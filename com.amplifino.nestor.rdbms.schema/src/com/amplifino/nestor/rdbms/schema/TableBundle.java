package com.amplifino.nestor.rdbms.schema;

import java.util.List;
import java.util.Optional;

public interface TableBundle {
	
	String name();
	Optional<String> schema();
	List<? extends Table> tables();
	Table table(String name);
	
	public interface Builder {
		Builder schema(String schema);
		Table.Builder builder(String name);
		TableBundle build();
	}
}
