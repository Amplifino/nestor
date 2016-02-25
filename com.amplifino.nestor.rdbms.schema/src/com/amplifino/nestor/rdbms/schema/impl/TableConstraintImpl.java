package com.amplifino.nestor.rdbms.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.amplifino.nestor.rdbms.schema.Column;
import com.amplifino.nestor.rdbms.schema.TableConstraint;

abstract class TableConstraintImpl implements TableConstraint {

	private final TableImpl table;
	private final String name;
	private final List<ColumnImpl> columns = new ArrayList<>();
	
	TableConstraintImpl(TableImpl table, String name) {
		this.table = table;
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public List<ColumnImpl> columns() {
		return Collections.unmodifiableList(columns);
	}

	@Override
	public TableImpl table() {
		return table;
	}

	@Override
	public boolean isPrimaryKey() {
		return false;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public boolean isForeignKey() {
		return false;
	}

	@Override
	public boolean hasColumn(Column column) {
		return columns.contains(column);
	}

	@Override
	public boolean isNotNull() {
		return columns.stream().allMatch(Column::isNotNull);
	}

	@Override
	public boolean noDdl() {
		return false;
	}
	
	boolean needsIndex() {
		return false;
	}

	void add(ColumnImpl column) {
		columns.add(Objects.requireNonNull(column));
	}
	
	public String ddl() {
		return 
			" constraint " + 
			name() +
			" " + 
			constraintType() +
			columns.stream().map(Column::name).collect(Collectors.joining(","," (",") "));
	}
	
	abstract String constraintType();
		
}
