package com.amplifino.nestor.useradmin;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.useradmin.UserAdminEvent;
import org.osgi.service.useradmin.UserAdminListener;


@Component
public class UserAdminEventAdapter implements UserAdminListener {
	
	private static final Map<Integer, String> TOPICS = createTopics();
	@Reference
	private  EventAdmin eventAdmin;
	
	private static Map<Integer, String> createTopics() {
		Map<Integer, String> result = new HashMap<>();
		result.put(UserAdminEvent.ROLE_CREATED, "ROLE_CREATED");
		result.put(UserAdminEvent.ROLE_REMOVED, "ROLE_REMOVED");
		result.put(UserAdminEvent.ROLE_CHANGED, "ROLE_CHANGED");
		return result;
	}

	@Override
	public void roleChanged(UserAdminEvent event) {		
		this.eventAdmin.postEvent(toEvent(event));
	}

	private Event toEvent(UserAdminEvent in) {
		Map<String, Object> props = new HashMap<>();
		props.put("event", in);
		props.put("role", in.getRole());
		props.put("role.name", in.getRole().getName());
		props.put("role.type", in.getRole().getType());
		props.put("service", in.getServiceReference());
		props.put("service.id", in.getServiceReference().getProperty(Constants.SERVICE_ID));
		props.put("service.objectClass", in.getServiceReference().getProperty(Constants.OBJECTCLASS));
		Object pid = in.getServiceReference().getProperty(Constants.SERVICE_PID);
		if (pid != null) {
			props.put("service.pid", pid);
		}
		return new Event(getTopic(in), props);
	}
	
	private String getTopic(UserAdminEvent event) {
		return "org/osgi/service/useradmin/UserAdmin/" + TOPICS.get(event.getType());
	}
	
}
