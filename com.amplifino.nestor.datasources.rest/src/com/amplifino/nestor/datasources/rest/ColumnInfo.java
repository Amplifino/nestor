package com.amplifino.nestor.datasources.rest;

public class ColumnInfo {

	public final String name;
	public final String table;
	public final String type;

	public ColumnInfo(String name, String table, String type) {
		this.name = name;
		this.table = table;
		this.type = type;
	}

}
