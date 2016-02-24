package com.amplifino.nestor.rdbms.schema;

import java.util.List;

public interface SchemaService {

	TableBundle.Builder builder(String name);
	TableBundle bundle(String name);
	List<? extends TableBundle> bundles();
}
