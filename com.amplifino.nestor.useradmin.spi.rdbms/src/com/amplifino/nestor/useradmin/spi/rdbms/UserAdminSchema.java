package com.amplifino.nestor.useradmin.spi.rdbms;

import com.amplifino.nestor.rdbms.schema.Column;
import com.amplifino.nestor.rdbms.schema.DeleteRule;
import com.amplifino.nestor.rdbms.schema.Table;

public enum UserAdminSchema {

	USERADMIN_ROLES {
		@Override
		void complete(Table.Builder builder) {
			Column name = builder.column("name").varChar(256).notNull().add();
			builder.column("type").number().notNull().add();
			builder.primaryKey("PK_" + name()).on(name).add();
		}
	},
	USERADMIN_PROPERTIES {
		@Override
		void complete(Table.Builder builder) {
			Column credential = builder.column("credential").varChar(1).notNull().add();
			Column roleName = builder.column("rolename").varChar(256).notNull().add();
			Column name = builder.column("name").varChar(256).notNull().add();
			builder.column("type").varChar(1).notNull().add();
			builder.column("propertyvalue").varChar(1024).notNull().add();
			builder.primaryKey("PK_" + name()).on(credential, roleName,  name).add();
			builder.foreignKey("FK_" + name()).on(roleName).references(USERADMIN_ROLES.name()).onDelete(DeleteRule.CASCADE);
		}
	},
	USERADMIN_MEMBERS {
		@Override
		void complete(Table.Builder builder) {
			Column groupName = builder.column("groupname").varChar(256).notNull().add();
			Column memberName = builder.column("membername").varChar(256).notNull().add();
			builder.column("required").varChar(1).notNull().add();
			builder.primaryKey("PK_" + name()).on(groupName, memberName).add();
			builder.foreignKey("FK_" + name()).on(groupName).references(USERADMIN_ROLES.name()).onDelete(DeleteRule.CASCADE);
			builder.foreignKey("FK_" + name()).on(memberName).references(USERADMIN_ROLES.name()).onDelete(DeleteRule.CASCADE);
		}
	};
	
	abstract void complete(Table.Builder builder); 
}
