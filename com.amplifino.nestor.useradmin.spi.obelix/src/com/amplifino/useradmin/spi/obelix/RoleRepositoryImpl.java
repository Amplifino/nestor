package com.amplifino.useradmin.spi.obelix;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.useradmin.Role;

import com.amplifino.nestor.useradmin.spi.GroupEntity;
import com.amplifino.nestor.useradmin.spi.RoleEntity;
import com.amplifino.nestor.useradmin.spi.RoleRepository;
import com.amplifino.nestor.useradmin.spi.UserEntity;
import com.amplifino.obelix.injections.RawInjections;
import com.amplifino.obelix.sets.InfiniteMap;
import com.amplifino.obelix.sortedmaps.SortedMapBuilder;
import com.amplifino.obelix.sortedmaps.SortedMapTypes;
import com.amplifino.obelix.space.ByteSpace;
import com.amplifino.obelix.space.DirectorySpace;
import com.amplifino.obelix.space.FileChannelSpace;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

@Component(property={"osgi.command.scope=useradmin", "osgi.command.function=dumpRoles"})
public class RoleRepositoryImpl implements RoleRepository {

	private volatile InfiniteMap<String, RoleDto>  roles;
	private volatile ByteSpace space;
	
	@Activate
	public void activate() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
	    AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
	    AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);
		mapper.setAnnotationIntrospector(pair);
		Path dir = FileSystems.getDefault().getPath(System.getProperty("user.home"), ".nestor" , "userAdmin");
		this.space = DirectorySpace.on(dir, pageGenerator(),20);
		this.roles = SortedMapBuilder.<String, RoleDto>on(space)
			.keyInjection(RawInjections.strings())
			.valueInjection(new RoleDtoInjection(mapper).andThen(RawInjections.strings()))
			.build(SortedMapTypes.VALUESEGMENTWITHBTREEINDEX);
		if (!roles.get(Role.USER_ANYONE).isPresent()) {
			merge(new RoleEntityImpl(Role.USER_ANYONE));
		}
	}
	
	@Deactivate
	public void deActivate() {
		try {
			space.close();
		} catch (IOException e) {
			Logger.getLogger("com.amplifino.useradmin").log(Level.SEVERE, "Error while closing space", e);
		}
	}
	
	private BiFunction<Path, Long, ByteSpace> pageGenerator() {
		return (directory, page) -> {
			try {
				return FileChannelSpace.of(directory.resolve("h" + page));
			} catch (IOException e) {
				throw new RuntimeException(e);			
			}			
		};
	}
	
	@Override
	public void merge(RoleEntity role) {
		try {
			roles.put(role.name(), ((RoleEntityImpl) role).dto());
			space.force();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}		
	}
	
	@Override
	public Optional<RoleEntity> getRole(String name) {
		return roles.get(name).map(this::wrap);
	}
	
	@Override
	public Optional<UserEntity> createUser(String name) {
		if (roles.get(name).isPresent()) {
			return Optional.empty();
		}
		UserEntityImpl user = new UserEntityImpl(name);
		merge(user);
		return Optional.of(user);
	}
	
	@Override
	public Optional<GroupEntity> createGroup(String name) {
		if (roles.get(name).isPresent()) {
			return Optional.empty();
		}
		GroupEntityImpl group = new GroupEntityImpl(name,this);
		merge(group);
		return Optional.of(group);
	}
	
	@Override
	public Optional<RoleEntity> removeRole(String name) {
		Optional<RoleEntity> role = getRole(name);
		if (role.isPresent()) {
			roles.remove(name);
			roles.range()
				.map(this::wrap)
				.filter(RoleEntity::isGroup)
				.map(GroupEntity.class::cast)
				.forEach(group -> group.removeMember(role.get()));
		}
		return role;			
	}
	
	@Override
	public Collection<RoleEntity> getRoles(String filter) throws InvalidSyntaxException {
		if (filter == null) {
			return roles.range()
				.map(this::wrap)
				.collect(Collectors.toList());
		}
		Filter ldapFilter = FrameworkUtil.createFilter(filter);
		return roles.range()			
			.filter( role -> ldapFilter.matches(role.properties()))
			.map(this::wrap)
			.collect(Collectors.toList());
	}
	
	@Override
	public Optional<UserEntity> getUser(String key, String value) {
		return roles.range()			
			.map(this::wrap)
			.filter(RoleEntity::isUser)
			.filter( role -> value.equals(role.properties().get(key)))
			.findAny()			
			.map(UserEntity.class::cast);
	}
	
	public RoleEntity wrap(RoleDto dto) {
		switch (dto.type) {
			case Role.ROLE:
				return new RoleEntityImpl(dto);
			case Role.USER:
				return new UserEntityImpl(dto);
			case Role.GROUP:
				return new GroupEntityImpl(dto, this);
			default:
				throw new IllegalArgumentException();
		}
	}
	
	public void dumpRoles() {
		roles.range().forEach(System.out::println);			
	}
	
}
