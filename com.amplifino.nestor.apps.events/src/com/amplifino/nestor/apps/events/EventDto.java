package com.amplifino.nestor.apps.events;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.event.Event;

public class EventDto {
	
	public String topic;
	public Map<String, Object> properties;
	
	EventDto(Event event) {
		this.topic = event.getTopic();
		this.properties = new HashMap<>();
		for (String name : event.getPropertyNames()) {
			properties.put(name, event.getProperty(name).toString());
		}
	}
}
