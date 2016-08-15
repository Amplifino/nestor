package com.amplifino.nestor.datasources.rest;

import java.util.List;
import java.util.Map;

public class SqlResult {

	public int rowCount;
	public List<Map<String, Object>> tuples;
	
	SqlResult(int rowCount) {
		this.rowCount = rowCount;
	}
	
	SqlResult(List<Map<String, Object>> tuples) {
		this.rowCount = tuples.size();
		this.tuples = tuples;
	}
}
