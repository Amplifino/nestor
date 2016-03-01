package com.amplifino.nestor.rdbms.schema;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Index {
	List<? extends Column> columns();
	String name();
	int compress();
	Table table();

	@ProviderType
	public interface Builder {
        Builder on(Column... columns);
        Builder compress(int number);
        Index add();
	}
}
