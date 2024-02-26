package com.amplifino.nestor.datasources.rest;

import java.sql.Timestamp;

public class HistoryInfo {

	public final Long timestamp;
	public final String query;

	public HistoryInfo(Timestamp timestamp, String query) {
		this.timestamp = timestamp.toInstant().toEpochMilli();
		this.query = query;
	}

}
