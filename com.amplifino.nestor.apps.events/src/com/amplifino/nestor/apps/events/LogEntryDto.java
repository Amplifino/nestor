package com.amplifino.nestor.apps.events;

import org.osgi.service.log.LogEntry;

public class LogEntryDto {
	
	public long timestamp;
	public String message;
	
	LogEntryDto(LogEntry entry) {
		this.timestamp = entry.getTime();
		this.message = entry.getMessage();		
	}
}
