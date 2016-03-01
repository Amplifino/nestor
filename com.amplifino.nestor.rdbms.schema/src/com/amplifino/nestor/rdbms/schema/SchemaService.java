package com.amplifino.nestor.rdbms.schema;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface SchemaService {

	Schema.Builder builder(String name);
	Schema schema(String name);
	List<? extends Schema> schemas();
}
