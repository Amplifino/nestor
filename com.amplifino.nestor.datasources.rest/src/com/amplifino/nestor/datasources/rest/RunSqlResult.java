package com.amplifino.nestor.datasources.rest;

import java.util.List;
import java.util.Map;

public class RunSqlResult {

	public int rowCount;
	public List<ColumnInfo> columns;
	public List<Map<String, Map<String, Object>>> tuples;

	RunSqlResult(int rowCount) {
		this.rowCount = rowCount;
	}

	RunSqlResult(List<ColumnInfo> columns, List<Map<String, Map<String, Object>>> tuples) {
		this.rowCount = tuples.size();
		this.columns = columns;
		this.tuples = tuples;
	}
}
