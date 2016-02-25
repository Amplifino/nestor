package com.amplifino.nestor.rdbms.schema.test;

import com.amplifino.nestor.rdbms.schema.Column;
import com.amplifino.nestor.rdbms.schema.DeleteRule;
import com.amplifino.nestor.rdbms.schema.Table;
import com.amplifino.nestor.rdbms.schema.Table.Builder;

public enum CatalogSchema {

	TABLEBUNDLE {
		@Override
		public void complete(Table.Builder builder) {
			Column name = builder.column("name").varChar(256).notNull().add();
			builder.primaryKey("PK_" + name()).on(name).add();
		}
	},
	
	TABLETABLE {
		@Override
		public void complete(Builder builder) {
			Column bundle = builder.column("bundle").varChar(256).notNull().add();
			Column name = builder.column("name").varChar(256).notNull().add();
			builder.primaryKey("PK_" + name()).on(bundle, name).add();
			builder.foreignKey("FK_" + name()).on(bundle).references(TABLEBUNDLE.name()).onDelete(DeleteRule.CASCADE).add();
		}		
	},
	
	TABLECOLUMN {
		@Override
		public void complete(Builder builder) {
			Column bundle = builder.column("bundle").varChar(256).notNull().add();
			Column table = builder.column("tablename").varChar(256).notNull().add();
			Column name = builder.column("name").varChar(256).notNull().add();
			builder.column("type").varChar(256).notNull().add();
			builder.column("notnull").varChar(1).notNull().add();
			builder.column("formula").varChar(256).add();
			builder.primaryKey("PK_" + name()).on(bundle, table, name).add();
			builder.foreignKey("FK_" + name()).on(bundle,  table).references(TABLETABLE.name()).onDelete(DeleteRule.CASCADE).add();
		}
	},
	TABLECONSTRAINT {
		@Override
		public void complete(Builder builder) {
			Column bundle = builder.column("bundle").varChar(256).notNull().add();
			Column table = builder.column("tablename").varChar(256).notNull().add();
			Column name = builder.column("name").varChar(256).notNull().add();
			builder.column("type").varChar(1).notNull().add();
			builder.column("referencedBundle").varChar(256).add();
			builder.column("referencedTable").varChar(256).add();
			builder.column("deleteRule").varChar(1).add();
			builder.column("noddl").varChar(1).add();
			builder.primaryKey("PK_" + name()).on(bundle, table, name).add();
			builder.foreignKey("FK_" + name()).on(bundle,  table).references(TABLETABLE.name()).onDelete(DeleteRule.CASCADE).add();
		}		 	
	},
	COLUMNINCONSTRAINT {
		@Override
		public void complete(Builder builder) {
			Column bundle = builder.column("bundle").varChar(256).notNull().add();
			Column table = builder.column("tablename").varChar(256).notNull().add();
			Column constraintName = builder.column("constraintname").varChar(256).notNull().add();
			Column ordinal = builder.column("ordinal").number().notNull().add();
			Column columnName = builder.column("columnname").varChar(256).notNull().add();
			builder.primaryKey("PK_" + name()).on(bundle, table, constraintName, ordinal).add();
			builder.unique("U_" + name()).on(bundle,  table, constraintName, columnName).add();
			builder.foreignKey("FK_" + name() + "_1").on(bundle, table, constraintName).references(TABLECONSTRAINT.name()).onDelete(DeleteRule.CASCADE).add();
			builder.foreignKey("FK_" + name() + "_2").on(bundle, table, columnName).references(TABLECOLUMN.name()).onDelete(DeleteRule.RESTRICT).add();			
		}		
	};
	
	public abstract void complete(Table.Builder builder);
}
