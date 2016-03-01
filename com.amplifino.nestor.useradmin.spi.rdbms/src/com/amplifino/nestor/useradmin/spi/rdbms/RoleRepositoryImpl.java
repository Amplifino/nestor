package com.amplifino.nestor.useradmin.spi.rdbms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.useradmin.Role;

import com.amplifino.nestor.jdbc.api.Query;
import com.amplifino.nestor.rdbms.schema.Schema;
import com.amplifino.nestor.rdbms.schema.SchemaService;
import com.amplifino.nestor.rdbms.schema.Table;
import com.amplifino.nestor.useradmin.spi.GroupEntity;
import com.amplifino.nestor.useradmin.spi.RoleEntity;
import com.amplifino.nestor.useradmin.spi.RoleRepository;
import com.amplifino.nestor.useradmin.spi.UserEntity;

@Component(property={"osgi.command.scope=useradmin", "osgi.command.function=createTables" })
public class RoleRepositoryImpl implements RoleRepository {
	
	@Reference(target="(application=useradmin)")
	private DataSource dataSource;
	
	@Reference
	private SchemaService schemaService;
	
	private Schema schema;

	@Activate
	public void activate() { 
		Schema.Builder schemaBuilder = schemaService.builder("useradmin");
		for (UserAdminSchema tableSpec : UserAdminSchema.values()) {
			Table.Builder tableBuilder = schemaBuilder.builder(tableSpec.name());
			tableSpec.complete(tableBuilder);
			tableBuilder.build();
		}
		this.schema = schemaBuilder.build();
	}
	
	@Deactivate
	public void deactivate() {
		schema.unregister();
	}
	
	private Table table(UserAdminSchema tableSpec) {
		return schema.table(tableSpec.name());
	}
	
	@Override
	public Optional<RoleEntity> getRole(String name) {
		return 
			Query.on(dataSource)
				.text(table(UserAdminSchema.USERADMIN_ROLES).selectSql())
				.text(" where name = ?")
				.parameters(name)
				.findFirst(this::parseRole);
	}

	@Override
	public Optional<UserEntity> createUser(String name) {
		if (createRole(name, Role.USER)) {
			return Optional.of(new UserEntityImpl(this, name));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<GroupEntity> createGroup(String name) {
		if (createRole(name, Role.GROUP)) {
			return Optional.of(new GroupEntityImpl(this, name));
		} else {
			return Optional.empty();
		}
	}

	private synchronized boolean createRole(String name, int type) {
		if (getRole(name).isPresent()) {
			return false;
		} else {
			Query.on(dataSource)
				.text(schema.table(UserAdminSchema.USERADMIN_ROLES.name()).insertSql())
				.parameters(name, type)
				.executeUpdate();
			return true;
		}
	}
	
	@Override
	public synchronized Optional<RoleEntity> removeRole(String name) {
		Optional<RoleEntity> role = getRole(name);
		if (role.isPresent()) {
			Query.on(dataSource)
				.text(schema.table(UserAdminSchema.USERADMIN_ROLES.name()).deleteSql())
				.parameters(name)
				.executeUpdate();
		}
		return role;
	}

	@Override
	public Optional<UserEntity> getUser(String key, String value) {
		List<String> candidates = Query.on(dataSource)
			.text("select a.name from ")
			.text(UserAdminSchema.USERADMIN_ROLES.name())
			.text(" a inner join ")
			.text(UserAdminSchema.USERADMIN_PROPERTIES.name())
			.text(" b on (a.name = b.rolename) ")
			.text(" where a.type = " + Role.USER + " and b.credential = 'N' and b.name = ? and b.type = 'S' and b.value = ? ")
			.parameters(key, value)
			.select (r -> r.getString(1));
		if (candidates.size() == 1) {
			return Optional.of(new UserEntityImpl(this, candidates.get(0)));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public void merge(RoleEntity role) {
		// do nothing as we update on putProperty and removePropery
	}

	@Override
	public Collection<? extends RoleEntity> getRoles(String filter) throws InvalidSyntaxException {
		List<RoleEntity> allRoles = Query.on(dataSource)
				.text(schema.table(UserAdminSchema.USERADMIN_ROLES.name()).selectSql())
				.select(this::parseRole);
		if (filter == null) {
			return allRoles;
		} else {
			Filter ldapFilter = FrameworkUtil.createFilter(filter);
			return allRoles.stream()
				.filter(role -> ldapFilter.matchCase(new Hashtable<>(role.properties())))
				.collect(Collectors.toList());
		}
	}

	private RoleEntity parseRole(ResultSet resultSet) throws SQLException {
		return createRoleEntity(resultSet.getString(1), resultSet.getInt(2));
	}
	
	private RoleEntity createRoleEntity(String name , int type) {
		switch (type) {
			case Role.ROLE:
				return new RoleEntityImpl(this, name);
			case Role.USER:
				return new UserEntityImpl(this, name);
			case Role.GROUP:
				return new GroupEntityImpl(this, name);
			default:
				throw new IllegalArgumentException("" + type);
		}
	}
	
	Map<String, Object> getProperties(RoleEntityImpl role, boolean credentials) {
		return Query.on(dataSource)
			.text(table(UserAdminSchema.USERADMIN_PROPERTIES).selectSql())
			.text(" where rolename = ? and credential = ?")
			.parameters(role.name(), credentials ? "Y" : "N")
			.select(this::parseProperty)
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	private Map.Entry<String, Object> parseProperty(ResultSet resultSet) throws SQLException {
		String type = resultSet.getString(4);
		if (type.equals("B")) {
			return new AbstractMap.SimpleEntry<>(resultSet.getString(3), Base64.getDecoder().decode(resultSet.getString(5)));
		} else if (type.equals("S")) {
			return new AbstractMap.SimpleEntry<>(resultSet.getString(3), resultSet.getString(5));
		} else {
			throw new IllegalArgumentException(type);
		}
	}
	
	private String[] typedValue(Object value) {
		if (value instanceof byte[]) {
			return new String[] {"B", Base64.getEncoder().encodeToString((byte[]) value) };
		} else if (value instanceof String) {
			return new String[] {"S" , (String) value};
		} else {
			throw new IllegalArgumentException("" + value);
		}
	}
	
	void createProperty(RoleEntityImpl role , boolean credential, String key, Object value) {
		String[] typedValue = typedValue(value);
		Query.on(dataSource)
			.text(table(UserAdminSchema.USERADMIN_PROPERTIES).insertSql())
			.parameters(credential ? "Y" : "N" , role.name() , key, typedValue[0], typedValue[1])
			.executeUpdate();
	}
	
	void updateProperty(RoleEntityImpl role , boolean credential, String key, Object value) {
		String[] typedValue = typedValue(value);
		Query.on(dataSource)
			.text(table(UserAdminSchema.USERADMIN_PROPERTIES).updateSql())
			.parameters(typedValue[0], typedValue[1])
			.parameters(credential ? "Y" : "N" , role.name() , key)
			.executeUpdate();
	}
	
	void removeProperty(RoleEntityImpl role, boolean credential, String key) {
		Query.on(dataSource)
			.text(table(UserAdminSchema.USERADMIN_PROPERTIES).deleteSql())
			.parameters(credential ? "Y" : "N" , role.name() , key)
			.executeUpdate();
	}
	
	Member addMember(GroupEntityImpl group, RoleEntity role, boolean required) {
		Query.on(dataSource)
			.text(table(UserAdminSchema.USERADMIN_MEMBERS).insertSql())
			.parameters(group.name(), role.name(), required ? "Y" : "N")
			.executeUpdate();
		return new Member(role, required);
	}
	
	void removeMember(GroupEntityImpl group, RoleEntity role) {
		Query.on(dataSource)
			.text(table(UserAdminSchema.USERADMIN_MEMBERS).deleteSql())
			.parameters(group.name(), role.name())
			.executeUpdate();
	}
	
	List<Member> members(GroupEntity group) {
		return Query.on(dataSource)
			.text(memberSql())
			.parameters(group.name())
			.select(this::parseMember);
	}
	
	private String memberSql() {
		return 
			"select a.membername, a.required , b.type from " +
			UserAdminSchema.USERADMIN_MEMBERS.name() + 
			" a inner join " +
			UserAdminSchema.USERADMIN_ROLES.name() +
			" b on (a.membername = b.name) where a.groupname = ? ";
	}
	
	private Member parseMember(ResultSet resultSet) throws SQLException {
		return new Member(createRoleEntity(resultSet.getString(1), resultSet.getInt(3)), resultSet.getString(2).equals("Y"));
	}
	
	public void createTables() {
		try {
			schema.create(dataSource);
			Query.on(dataSource)
				.text(table(UserAdminSchema.USERADMIN_ROLES).insertSql())
				.parameters(Role.USER_ANYONE, Role.ROLE)
				.executeUpdate();
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}
}

