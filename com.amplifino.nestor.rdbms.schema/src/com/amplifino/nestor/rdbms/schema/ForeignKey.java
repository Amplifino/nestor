package com.amplifino.nestor.rdbms.schema;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface ForeignKey extends TableConstraint {
	
	Table referencedTable();	
	DeleteRule deleteRule();
	
	@ProviderType
	interface Builder {
		Builder on(Column column, Column ... columns);
		Builder onDelete(DeleteRule rule);
		Builder references(Table table);
		Builder references(String tableName);
		Builder references(String componentName , String tableName);
		Builder noDdl();
		ForeignKey add();		
	}

}


