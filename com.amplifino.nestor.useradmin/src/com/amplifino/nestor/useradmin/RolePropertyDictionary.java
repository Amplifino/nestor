package com.amplifino.nestor.useradmin;

import java.util.Map;

class RolePropertyDictionary extends DictionaryAdapter {

	private final RoleMixin role;
	
	RolePropertyDictionary(RoleMixin role) {
		this.role = role;
	}
	
	@Override
	Map<String, ?> map() {
		return role.properties();
	}
	
	@Override
	public Object put(String key, Object value) {
		return role.putProperty(key, value);		
	}
	
	@Override
	public Object remove(Object key) {
		return role.removeProperty(key);
	}
	
	
}
