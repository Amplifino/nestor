package com.amplifino.nestor.rdbms.schema;

import java.util.List;

public interface SchemaService {

	Schema.Builder builder(String name);
	Schema schema(String name);
	List<? extends Schema> schemas();
}
