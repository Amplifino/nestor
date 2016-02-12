package com.amplifino.nestor.useradmin.spi.memory;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.useradmin.Role;

import com.amplifino.nestor.useradmin.spi.GroupEntity;
import com.amplifino.nestor.useradmin.spi.RoleEntity;
import com.amplifino.nestor.useradmin.spi.RoleRepository;
import com.amplifino.nestor.useradmin.spi.UserEntity;

@Component
public class RoleRepositoryImpl implements RoleRepository {
	
	private Map<String, RoleEntity> roles = new ConcurrentHashMap<>();

	public RoleRepositoryImpl() {
		this.roles.put(Role.USER_ANYONE, new RoleEntityImpl(Role.USER_ANYONE));
	}
	
	@Override
	public Optional<RoleEntity> getRole(String name) {
		return Optional.ofNullable(roles.get(name));
	}

	@Override
	public Optional<UserEntity> createUser(String name) {
		UserEntity user = new UserEntityImpl(name);
		roles.put(name, user);
		return Optional.of(user);
	}

	@Override
	public Optional<GroupEntity> createGroup(String name) {
		GroupEntity group = new GroupEntityImpl(name);
		roles.put(name, group);
		return Optional.of(group);
	}

	@Override
	public Optional<RoleEntity> removeRole(String name) {
		return Optional.ofNullable(roles.remove(name));
	}

	@Override
	public Collection<? extends RoleEntity> getRoles(String filter) throws InvalidSyntaxException {
		if (filter == null) {
			return roles.values();
		}
		Filter f = FrameworkUtil.createFilter(filter);
		return roles.values().stream()
			.filter( role -> f.match(toDictionary(role.properties())))
			.collect(Collectors.toList());
	}

	private Dictionary<String, Object> toDictionary(Map<String, ?> map) {
		return map.entrySet().stream().collect(
			() -> new Hashtable<>() , 
			(dictionary, entry) -> dictionary.put(entry.getKey(), entry.getValue()), 
			(dictionary1 , dictionary2) -> dictionary1.putAll(dictionary2));	
	}
	
	@Override
	public Optional<UserEntity> getUser(String key, String value) {
		return roles.values().stream()
			.filter(UserEntity.class::isInstance)
			.map(UserEntity.class::cast)
			.filter( user -> value.equals(user.properties().get(key))).findAny();
	}

	@Override
	public void merge(RoleEntity role) {
	}

}
