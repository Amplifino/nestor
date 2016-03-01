package com.amplifino.nestor.rdbms.schema;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface PrimaryKey extends TableConstraint {
		
	@ProviderType
	interface Builder {
		Builder on(Column column, Column ... columns);
		PrimaryKey add();
	}
}


