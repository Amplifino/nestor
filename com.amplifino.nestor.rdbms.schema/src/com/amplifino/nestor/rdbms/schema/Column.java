package com.amplifino.nestor.rdbms.schema;

import java.util.Optional;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Column {
	String name();
	String type();
	Table table();
	Optional<String> formula();
	
	boolean isPrimaryKeyColumn();
	boolean isNotNull();
	boolean isVirtual();
	
	@ProviderType
	interface Builder {
		Builder type(String type);
		Builder notNull();
		Builder number();
		Builder decimal(int precision, int scale);
		Builder character(int length);
		Builder varChar(int length);
		VirtualBuilder as(String formula);
		Column add();
	}
	
	interface VirtualBuilder {
		Column add();
	}
}
