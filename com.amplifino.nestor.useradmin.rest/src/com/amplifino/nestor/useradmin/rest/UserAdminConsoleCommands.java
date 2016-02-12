package com.amplifino.nestor.useradmin.rest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

@Component(service=UserAdminConsoleCommands.class, immediate=true, property={"osgi.command.scope=useradmin", 
	"osgi.command.function=createUser", 
	"osgi.command.function=createGroup" , 
	"osgi.command.function=createRoleProperty",
	"osgi.command.function=addMember",
	"osgi.command.function=removeMember",
	"osgi.command.function=removeRole",
	"osgi.command.function=setHa1",
	"osgi.command.function=hasRole",
	"osgi.command.function=allRoles"})
public class UserAdminConsoleCommands {

	private volatile UserAdmin admin;
	
	@Reference
	public void setUserAdmin(UserAdmin admin) {
		this.admin = admin;
	}
	
	@SuppressWarnings("unchecked")
	public void createUser(String name) {
		run(() -> {
			Role role = admin.createRole(name, Role.USER);
			if (role == null) {
				System.out.println("Failed to create user " + name);
			} else {
				System.out.println("Created user " + name);
				role.getProperties().put("byteProp", new byte[] { 1, 2, 3, 4});
			}
		});
	}
	
	public void createGroup(String name) {
		run(() -> {
			Role role = admin.createRole(name, Role.GROUP);
			if (role == null) {
				System.out.println("Failed to create group " + name);
			} else {
				System.out.println("Created group " + name);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void createRoleProperty(String name, String key, String value) {
		run(() -> {
			Role role = admin.getRole(name);
			if (role == null) {
				System.out.println("Role " + name + " not found");
			} else {
				role.getProperties().put(key, value);
			}
		});
	}
	
	public void addMember(String name, String memberName, boolean required) {
		run(() -> {
			Role role = admin.getRole(name);
			if (role == null) {
				System.out.println("Role " + name + " not found");
			} else if (role.getType() != Role.GROUP) {
				System.out.println("" + role + " is not a group");
			} else {
				Group group = (Group) role;
				Role member = admin.getRole(memberName);
				if (member == null) {
					System.out.println("Role " + name + " not found");
				} else {
					boolean result = false;
					if (required) {
						result = group.addRequiredMember(member);
					} else {
						result = group.addMember(member);
					}
					if (result) {
						System.out.println("Added " + (required ? " required " : "") + " member " + memberName + " to " + name);
					} else {
						System.out.println("Could not add " + (required ? " required " : "") + " member " + memberName + " to " + name);
					}
				}				
			}
		});
	}

	public void removeMember(String name, String memberName) {
		run(() -> {
			Role role = admin.getRole(name);
			if (role == null) {
				System.out.println("Role " + name + " not found");
			} else if (role.getType() != Role.GROUP) {
				System.out.println("" + role + " is not a group");
			} else {
				Group group = (Group) role;
				Role member = admin.getRole(memberName);
				if (member == null) {
					System.out.println("Role " + name + " not found");
				} else {
					if (group.removeMember(member)) {
						System.out.println("" + member + " removed from " + group);
					} else {
						System.out.println("" + member + " not present in " + group);
					}
				}				
			}
		});
	}

	public void removeRole(String name) {
		run(() -> {
			boolean result = admin.removeRole(name);
			if (result) {
				System.out.println("Role " + name + "removed");
			} else { 
				System.out.println("Role " + name + "not removed");
			}
		});
	}
	
	public void hasRole(String name, String roleName) {
		run(() -> {
			Role role = admin.getRole(name);
			if (role == null) {
				System.out.println("Role " + name + " not found");
			} else {
				if (role.getType() == Role.ROLE) {
					role = null;
				}
				boolean hasRole = admin.getAuthorization((User) role).hasRole(roleName);
				System.out.println("User " + name + (hasRole ? " has role " : " does not have role ") + roleName);
			}
		});
	}
	
	public void allRoles(String name) {
		run(() -> {
			Role role = admin.getRole(name);
			if (role == null) {
				System.out.println("User " + name + " not found");
			} else {
				if (role.getType() == Role.ROLE) {
					role = null;
				}
				String[] roles = admin.getAuthorization((User) role).getRoles();
				if (roles == null) {
					System.out.println("User " + name + " has no roles");
				} else {
					System.out.println(Arrays.stream(roles).collect(Collectors.joining(",")));
				}
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void setHa1(String name, String realm, String password) {
		run(() -> {
			Role role = admin.getRole(name);
			if (role == null || role.getType() != Role.USER) {
				System.out.println("Role " + name + " not found");
			} else {
				String ha1 = createHa1(realm, name, password);
				((User) role).getCredentials().put("HA1", ha1);
			}
		});
		String header = "{\"alg\": \"HS256\", \"typ\": \"JWT\"}";
		String body = "{\"name\": \"admin\"}";
		String header64 = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());
		String body64 = Base64.getUrlEncoder().withoutPadding().encodeToString(body.getBytes());
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		messageDigest.update(header64.getBytes());
		messageDigest.update(".".getBytes());
		messageDigest.update(body64.getBytes());		
		messageDigest.update("admin".getBytes());
		byte[] hash = messageDigest.digest();
		String hash64 = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
		System.out.println(new StringJoiner(".").add(header64).add(body64).add(hash64).toString());
	}
		
	private void run(Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	static String createHa1(String realm, String authenticationName, String password) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(authenticationName.getBytes());
			messageDigest.update(":".getBytes());
			messageDigest.update(realm.getBytes());
			messageDigest.update(":".getBytes());
			messageDigest.update(password.getBytes());
			byte[] md5 = messageDigest.digest();
			return DatatypeConverter.printHexBinary(md5).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
          throw new RuntimeException(e);
		}
  	}	

}
