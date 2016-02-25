package com.amplifino.nestor.rdbms.schema;

import java.util.List;

public interface Table {

    List<String> ddl();
    String selectSql();
    String insertSql();
    String updateSql();
    String deleteSql();
    Schema schema();

    String name();
    String qualifiedName();
    List<? extends Column> columns();
    Column getColumn(String name);
    List<? extends TableConstraint> constraints();
    PrimaryKey primaryKey();
    List<? extends ForeignKey> foreignKeys();
    List<? extends Column> primaryKeyColumns();
    List<? extends Index> indexes();
        
    interface Builder {	
    	Column.Builder column(String name);
        PrimaryKey.Builder primaryKey(String name);
        Unique.Builder unique(String name);
        ForeignKey.Builder foreignKey(String name);
        Index.Builder index(String name);
        Table build();
    }
}
