package com.amplifino.useradmin.spi.obelix;

import java.util.Collections;
import java.util.Map;

import org.osgi.service.useradmin.Role;

import com.amplifino.nestor.useradmin.spi.RoleEntity;

public class RoleEntityImpl implements RoleEntity {
	
	private final RoleDto dto;
	
	RoleEntityImpl(RoleDto dto) {
		this.dto = dto;
	}

	RoleEntityImpl(String name) {
		this(new RoleDto(Role.ROLE, name));
	}
	
	public RoleDto dto() {
		return dto;
	}
	
	@Override
	public String name() {
		return dto.name;
	}

	@Override
	public Map<String, ?> properties() {
		return Collections.unmodifiableMap(dto.properties());
	}

	@Override
	public Object putProperty(String key, Object value) {
		return dto.putProperty(key, value);
	}

	@Override
	public Object removeProperty(Object key) {
		return dto.removeProperty(key);
	}

	@Override
	public final boolean isUser() {
		return dto.type == Role.USER;
	}

	@Override
	public final boolean isGroup() {
		return dto.type == Role.GROUP;
	}
}
