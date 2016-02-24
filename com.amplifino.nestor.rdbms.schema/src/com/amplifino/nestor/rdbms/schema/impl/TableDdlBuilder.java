package com.amplifino.nestor.rdbms.schema.impl;

import java.util.ArrayList;
import java.util.List;

class TableDdlBuilder {
	
    private final TableImpl table;
    private final List<String> ddl = new ArrayList<>();
   
    public TableDdlBuilder(TableImpl table) {
		this.table = table;
	}
	

    List<String> getDdl() {
        ddl.add(getTableDdl());
        for (TableConstraintImpl constraint : table.constraints()) {
            if (constraint.needsIndex()) {
                ddl.add(getConstraintIndexDdl(constraint));
            }
        }
        for (IndexImpl index : table.indexes()) {
            ddl.add(getIndexDdl(index));
        }
        for (TableImpl created : table.bundle().tables()) {
        	if (created.equals(table)) {
        		break;
        	}
        	for (ForeignKeyImpl constraint : created.foreignKeys()) {
        		if (constraint.referencedTable().equals(table) && !constraint.noDdl()) {
        			ddl.add("alter table " + constraint.table().qualifiedName() + " add " + getConstraintFragment(constraint));
        		}
        	}
        }
        return ddl;
    }

    private String getTableDdl() {
        StringBuilder sb = new StringBuilder("create table ");
        sb.append(table.qualifiedName());
        sb.append("(");
        doAppendColumns(sb, table.columns(), true, true);
        for (TableConstraintImpl constraint : table.constraints()) {
        	if (!constraint.delayDdl() && !constraint.noDdl()) {
        		sb.append(", ");
        		sb.append(getConstraintFragment(constraint));
        	}
        }
        sb.append(")");
         return sb.toString();        
    }
    

    private String getConstraintFragment(TableConstraintImpl constraint) {
        return constraint.ddl();
    }


    private String getConstraintIndexDdl(TableConstraintImpl constraint) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE INDEX ");
        builder.append(constraint.name());
        builder.append(" ON ");
        builder.append(table.qualifiedName());
        appendColumns(builder, constraint.columns(), false, false);
        return builder.toString();
    }

    private String getIndexDdl(IndexImpl index) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE INDEX ");
        builder.append(index.name());
        builder.append(" ON ");
        builder.append(table.qualifiedName());
        appendColumns(builder, index.columns(), false, false);
        return builder.toString();
    }

    private void appendColumns(StringBuilder builder, List<ColumnImpl> columns, boolean addType, boolean addNullable) {
        builder.append(" (");
        doAppendColumns(builder, columns, addType, addNullable);
        builder.append(") ");
    }

    private void doAppendColumns(StringBuilder builder, List<ColumnImpl> columns, boolean addType, boolean addNullable) {
        String separator = "";
        for (ColumnImpl column : columns) {
            builder.append(separator);
            appendDdl(builder, column, addType, addNullable);
            separator = ", ";
        }
    }


    private void appendDdl(StringBuilder builder, ColumnImpl column, boolean addType, boolean addNullable) {
        builder.append(column.name());
        if (addType) {
            builder.append(" ");
            builder.append(column.type() == null ? "" : column.type());
            if (column.isVirtual()) {
                builder.append(" AS (");
                builder.append(column.formula());
                builder.append(")");
            }
            if (addNullable) {
                if (column.isNotNull()) {
                    builder.append(" NOT");
                }
                builder.append(" NULL");
            }
        }
    }

   
}
