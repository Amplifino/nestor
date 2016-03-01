package com.amplifino.nestor.rdbms.schema;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Schema {
	
	String name();
	Optional<String> schemaOwner();
	List<? extends Table> tables();
	Table table(String name);
	void create(DataSource dataSource);
	void unregister();
	
	@ProviderType
	public interface Builder {
		Builder schemaOwner(String schema);
		Table.Builder builder(String name);
		Schema build();
	}
}
