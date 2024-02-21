package com.amplifino.nestor.datasources.rest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public class SqlJsonResult {

	public int rowCount;
	public String columns;
	public String tuples;

	SqlJsonResult(int rowCount) {
		this.rowCount = rowCount;
	}

	SqlJsonResult(JSONObject columns, JSONArray tuples) {
		this.rowCount = tuples.length();
		this.columns = columns.toString();
		this.tuples = tuples.toString();
	}
}
