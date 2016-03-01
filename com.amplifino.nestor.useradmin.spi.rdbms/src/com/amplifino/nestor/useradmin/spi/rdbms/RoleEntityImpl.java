package com.amplifino.nestor.useradmin.spi.rdbms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.amplifino.nestor.useradmin.spi.RoleEntity;

public class RoleEntityImpl implements RoleEntity {
	
	private final RoleRepositoryImpl repository;
	private final String name;
	private Map<String, Object> properties;

	RoleEntityImpl(RoleRepositoryImpl repository, String name) {
		this.repository = repository;
		this.name = name;
	}
	
	private Map<String, Object> fetchProperties() {
		if (properties == null) {
			properties = new HashMap<>();
			properties.putAll(repository.getProperties(this, false));
		}
		return properties;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public Map<String, ?> properties() {
		return Collections.unmodifiableMap(fetchProperties());
	}

	@Override
	public Object putProperty(String key, Object value) {
		Map<String, Object> props = fetchProperties();
		if (props.containsKey(key)) {
			repository.updateProperty(this, false, key, value);
		} else {
			repository.createProperty(this, false, key, value);
		}
		return props.put(key, value);
	}

	@Override
	public Object removeProperty(Object key) {
		Map<String, Object> props = fetchProperties();
		if (props.containsKey(key)) {
			repository.removeProperty(this, false, (String) key);
		}
		return props.remove(key);
	}

	@Override
	public boolean isUser() {
		return false;
	}

	@Override
	public boolean isGroup() {
		return false;
	}
	
	RoleRepositoryImpl repository() {
		return repository;
	}

}
