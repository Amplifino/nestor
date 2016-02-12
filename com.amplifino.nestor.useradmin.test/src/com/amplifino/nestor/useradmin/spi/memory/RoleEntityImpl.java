package com.amplifino.nestor.useradmin.spi.memory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amplifino.nestor.useradmin.spi.RoleEntity;

public class RoleEntityImpl implements RoleEntity {
	
	private final String name;
	private final Map<String, Object> properties = new ConcurrentHashMap<>();

	RoleEntityImpl(String name) {
		this.name = name;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public Map<String, ?> properties() {
		return Collections.unmodifiableMap(properties);
	}

	@Override
	public Object putProperty(String key, Object value) {
		return properties.put(key, value);
	}

	@Override
	public Object removeProperty(Object key) {
		return properties.remove(key);
	}

	@Override
	public boolean isUser() {
		return false;
	}

	@Override
	public boolean isGroup() {
		return false;
	}

}
