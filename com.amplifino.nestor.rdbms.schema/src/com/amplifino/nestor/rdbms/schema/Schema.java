package com.amplifino.nestor.rdbms.schema;

import java.util.List;
import java.util.Optional;

public interface Schema {
	
	String name();
	Optional<String> schemaOwner();
	List<? extends Table> tables();
	Table table(String name);
	
	public interface Builder {
		Builder schemaOwner(String schema);
		Table.Builder builder(String name);
		Schema build();
	}
}
