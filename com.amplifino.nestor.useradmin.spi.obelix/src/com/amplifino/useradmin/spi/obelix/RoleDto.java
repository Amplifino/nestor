package com.amplifino.useradmin.spi.obelix;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.service.useradmin.Role;

import com.google.common.base.MoreObjects;
 
class RoleDto  {

	public int type;
	public String name;
	public Map<String, ValueDecorator> properties;
	public Map<String, ValueDecorator> credentials;
	public List<String> members;
	public List<String> requiredMembers;

	RoleDto() {
	}
	
	RoleDto(int type, String name) {
		this.type = type;
		this.name = name;
		this.properties = new HashMap<>();
		if (type != Role.ROLE) {
			this.credentials = new HashMap<>();
		}
		if (type == Role.GROUP) {
			this.members = new ArrayList<>();
			this.requiredMembers = new ArrayList<>();
		}
	}
	
	Object putCredential(String key, Object value) {
		return credentials.put(key, ValueDecorator.of(value));
	}

	Object removeCredential(Object key) {
		ValueDecorator removed = credentials.remove(key);
		return removed == null ? null : removed.value();
	}

	Map<String, ?> properties() {
		return properties.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().value()));
	}

	Map<String, ?> credentials() {
		return credentials.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().value()));
	}
	
	Object putProperty(String key, Object value) {
		return properties.put(key, ValueDecorator.of(value));
	}

	Object removeProperty(Object key) {
		ValueDecorator removed = properties.remove(key);
		return removed == null ? null : removed.value();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.omitNullValues()
			.add("name", name)
			.add("properties", properties)
			.add("credentials", credentials)
			.add("members", members)
			.add("requiredMembers", requiredMembers)
			.toString();
	}
	
	static enum ValueType {
		STRING {
			@Override
			Object fromString(String in) {
				return in;
			}

			@Override
			String toString(Object in) {
				return (String) in;
			}			
		},
		BYTE {
			@Override
			Object fromString(String in) {
				return Base64.getDecoder().decode(in);
			}

			@Override
			String toString(Object in) {
				return Base64.getEncoder().encodeToString((byte[]) in);
			}			
		};
		
		abstract Object fromString(String in);
		abstract String toString(Object in);
		
		static ValueType of(Object in) {
			if (in instanceof String) {
				return STRING;
			}
			if (in instanceof byte[]) {
				return BYTE;
			}
			throw new IllegalArgumentException();
		}
	}
	
	static class ValueDecorator {
		public ValueType type;
		public String value;
		
		ValueDecorator() {			
		}
		
		private ValueDecorator(Object value) {
			this.type = ValueType.of(value);
			this.value = type.toString(value);
		}
		
		static ValueDecorator of(Object value) {
			return new ValueDecorator(value);
		}
		
		Object value() {
			return type.fromString(value);
		}
		
		
		@Override 
		public String toString() {
			return type.name() + ":" + value;
		}
	}
}
