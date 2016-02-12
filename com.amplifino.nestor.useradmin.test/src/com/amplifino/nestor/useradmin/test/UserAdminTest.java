package com.amplifino.nestor.useradmin.test;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.service.useradmin.UserAdminEvent;
import org.osgi.service.useradmin.UserAdminListener;
import org.osgi.util.tracker.ServiceTracker;



public class UserAdminTest {
	
	private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private UserAdmin userAdmin;
	private final CountDownLatch latch = new CountDownLatch(1);
	
	@Before
	public void setup() {
		userAdmin = getService(UserAdmin.class);
	}

	@Test
	public void test() {
		User user = (User) userAdmin.createRole("admin", Role.USER);
		Assert.assertTrue(userAdmin.getRole("admin") != null);
		Group group = (Group) userAdmin.createRole("administrators", Role.GROUP);
		Assert.assertTrue(userAdmin.getRole("administrators") != null);
		group.addMember(user);
		Assert.assertTrue(userAdmin.getAuthorization(user).hasRole("administrators"));
		Group securityOfficer = (Group) userAdmin.createRole("Security Officer", Role.GROUP);
		securityOfficer.addMember(group);
		Assert.assertTrue(userAdmin.getAuthorization(user).hasRole("Security Officer"));	
	}
	
	@Test
	public void testRequiredMember() {
		User user = (User) userAdmin.createRole("test", Role.USER);
		Group testers = (Group) userAdmin.createRole("testers", Role.GROUP);
		Group qa = (Group) userAdmin.createRole("qa", Role.GROUP);
		testers.addMember(user);
		qa.addRequiredMember(testers);
		Assert.assertFalse(userAdmin.getAuthorization(user).hasRole("qa"));
		qa.addMember(userAdmin.getRole(Role.USER_ANYONE));
		Assert.assertTrue(userAdmin.getAuthorization(user).hasRole("qa"));
		
	}
	
	@Test
	public void testLoop() {
		User user = (User) userAdmin.createRole("loop", Role.USER);
		Group group1 = (Group) userAdmin.createRole("group1", Role.GROUP);
		Group group2 = (Group) userAdmin.createRole("group2", Role.GROUP);
		group1.addMember(group2);
		group2.addMember(group1);
		Assert.assertFalse(userAdmin.getAuthorization(user).hasRole("group1"));
		Assert.assertFalse(userAdmin.getAuthorization(user).hasRole("group2"));
		group1.addMember(user);
		Assert.assertTrue(userAdmin.getAuthorization(user).hasRole("group1"));
		Assert.assertTrue(userAdmin.getAuthorization(user).hasRole("group2"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testListener() throws InterruptedException {
		User user = (User) userAdmin.createRole("listenerUser", Role.USER);
		UserAdminListener listener = this::roleChanged;
		ServiceRegistration<UserAdminListener> registration = context.registerService(UserAdminListener.class, listener, null);
		try {
			user.getProperties().put("name", "dummy");
			Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
		} finally {
			registration.unregister();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEvent() throws InterruptedException {
		User user = (User) userAdmin.createRole("eventUser", Role.USER);
		EventHandler handler = this::roleChanged;
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(EventConstants.EVENT_TOPIC, new String[] {"org/osgi/service/useradmin/UserAdmin/*"});
		ServiceRegistration<EventHandler> registration = context.registerService(EventHandler.class, handler, props);
		try {
			user.getCredentials().put("name", "dummy");
			Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
		} finally {
			registration.unregister();
		}
	}
	
	private void roleChanged(UserAdminEvent event) {
		latch.countDown();
	}
	
	private void roleChanged(Event event) {
		latch.countDown();
	}
	
	private <T> T getService(Class<T> clazz) {
		ServiceTracker<T, T> tracker = new ServiceTracker<>(context, clazz, null);
		tracker.open();
		try {
			return Objects.requireNonNull(tracker.waitForService(1000L));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
