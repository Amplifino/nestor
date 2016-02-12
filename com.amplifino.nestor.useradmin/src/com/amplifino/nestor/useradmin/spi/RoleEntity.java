package com.amplifino.nestor.useradmin.spi;

import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface RoleEntity {

	String name();
	Map<String, ?> properties();
	Object putProperty(String key, Object value);
	Object removeProperty(Object key);
	boolean isUser();
	boolean isGroup();
}
