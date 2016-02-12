package com.amplifino.nestor.useradmin;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.service.useradmin.UserAdminEvent;
import org.osgi.service.useradmin.UserAdminListener;

import com.amplifino.nestor.useradmin.spi.GroupEntity;
import com.amplifino.nestor.useradmin.spi.RoleEntity;
import com.amplifino.nestor.useradmin.spi.RoleRepository;
import com.amplifino.nestor.useradmin.spi.UserEntity;


@Component(name="com.amplifino.useradmin")
public class UserAdminImpl implements UserAdmin {
	
	@Reference
	private RoleRepository repository;
	private final List<UserAdminListener> listeners = new CopyOnWriteArrayList<>();	
	private volatile ComponentContext context;
	private volatile EventPublisher publisher;

	@Reference(cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC)
	public void addListener(UserAdminListener listener) {
		listeners.add(listener);
	}
	
	@Activate
	public void activate(ComponentContext context) {
		this.context = context;
		this.publisher = new EventPublisher(100);
	}
	
	@Deactivate
	public void deactivate() {
		publisher.deactivate();
	}
	
	public void removeListener(UserAdminListener listener) {
		listeners.remove(listener);
	}
			
	@Override
	public Role createRole(String name, int type) {
		Optional<Role> role = doCreateRole(name, type);
		role.ifPresent(r -> publish(new UserAdminEvent(context.getServiceReference(), UserAdminEvent.ROLE_CREATED, r)));		
		return role.orElse(null);
	}
	
	private Optional<Role> doCreateRole(String name, int type) {	
		synchronized(repository) {
			Optional<? extends RoleEntity> role = repository.getRole(name);
			if (role.isPresent()) {
				return Optional.empty();
			} 
			switch (type) {
				case Role.USER:
					role = repository.createUser(name);
					break;
				case Role.GROUP:
					role = repository.createGroup(name);
					break;
				default:
					throw new IllegalArgumentException("Invalid type: " + type);
			}
			return role.map(this::wrap);
		}			
	}

	@Override
	public boolean removeRole(String name) {
		if (Role.USER_ANYONE.equals(Objects.requireNonNull(name))) {
			return false;
		}
		Optional<RoleEntity> role = repository.removeRole(name);
		role
			.map(this::wrap)
			.ifPresent(r -> publish(new UserAdminEvent(context.getServiceReference(), UserAdminEvent.ROLE_REMOVED, r)));
		return role.isPresent();
	}

	@Override
	public Role getRole(String name) {
		return repository.getRole(Objects.requireNonNull(name)).map(this::wrap).orElse(null);
	}

	@Override
	public Role[] getRoles(String filter) throws InvalidSyntaxException {
		Collection<? extends RoleEntity> roles = repository.getRoles(filter);
		return roles.isEmpty() ? null : roles.stream().map(this::wrap).toArray(Role[]::new);
	}

	@Override
	public User getUser(String key, String value) {
		return repository.getUser(key, value).map(this::wrap).map(User.class::cast).orElse(null);
	}

	@Override
	public Authorization getAuthorization(User user) {
		return new AuthorizationImpl(user, this);
	}

	void roleChanged(Role role) {
		repository.merge(((RoleMixin) role).entity());
		publish(new UserAdminEvent(context.getServiceReference(), UserAdminEvent.ROLE_CHANGED, role));
	}
	
	private void publish(UserAdminEvent event) {
		publisher.publish(event, listeners.iterator());		
	}
	
	Role wrap(RoleEntity role) {
		if (Role.USER_ANYONE.equals(role.name())) {
			return new PredefinedRole(role, this);
		}
		if (role.isUser()) {
			return new UserImpl((UserEntity) role, this);
		}
		if (role.isGroup()) {
			return new GroupImpl((GroupEntity) role, this);
		}
		throw new IllegalArgumentException();
	}

}
