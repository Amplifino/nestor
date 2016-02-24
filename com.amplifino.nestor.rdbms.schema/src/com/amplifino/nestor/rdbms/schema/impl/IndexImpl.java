package com.amplifino.nestor.rdbms.schema.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.amplifino.nestor.rdbms.schema.Column;
import com.amplifino.nestor.rdbms.schema.Index;

class IndexImpl implements Index {

	private final TableImpl table;
	private final String name;
	private List<ColumnImpl> columns = new ArrayList<>();
	private int compressCount = 0;
	
	IndexImpl(TableImpl table, String name) {
		this.table = table;
		this.name = name;
	}

	@Override
	public List<ColumnImpl> columns() {
		return Collections.unmodifiableList(columns);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int compress() {
		return compressCount;
	}

	@Override
	public TableImpl table() {
		return table;
	}

	static class Builder implements Index.Builder {
		private final IndexImpl index;
		
		Builder(TableImpl table, String name) {
			index = new IndexImpl(table, name);
		}

		@Override
		public Index.Builder on(Column... columns) {
			Arrays.stream(columns).map(ColumnImpl.class::cast).forEach(index.columns::add);
			return this;
		}

		@Override
		public Index.Builder compress(int number) {
			if (number <= 0 || number > index.columns.size()) {
				throw new IllegalArgumentException();
			}
			index.compressCount = number;
			return this;
		}

		@Override
		public IndexImpl add() {
			index.table.add(index);
			return index;
		}
	}
	
}
