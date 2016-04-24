package com.amplifino.nestor.useradmin;

import java.util.Map;

class UserCredentialDictionary extends DictionaryAdapter {

	private final AbstractUser user;
	
	UserCredentialDictionary(AbstractUser user) {
		this.user = user;
	}
	
	@Override
	Map<String, ?> map() {
		return user.credentials();
	}
	
	@Override
	public Object put(String key, Object value) {
		return user.putCredential(key, value);		
	}
	
	@Override
	public Object remove(Object key) {
		return user.removeCredential(key);
	}
	
}
