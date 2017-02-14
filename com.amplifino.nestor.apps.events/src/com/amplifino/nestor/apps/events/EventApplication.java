package com.amplifino.nestor.apps.events;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

@Component(service=Application.class, property= {
	"alias=/events", 
	"raw:Boolean=true",
	HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PATTERN + "=/apps/events/*",
	HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PREFIX + "=/resources/web"})
public class EventApplication extends Application implements LogListener {

    private final SseBroadcaster broadcaster = new SseBroadcaster();
    @Reference
    LogReaderService logReaderService;
	
	@Override
	public Set<Class<?>> getClasses() {
		return Collections.singleton(EventResource.class);
	}
	
	@Override
	public Set<Object> getSingletons() {
		return Collections.singleton(binder());
	}
	
	@Activate
	public void activate() {
		logReaderService.addLogListener(this);
	}
	
	@Deactivate
	public void deactivate() {
		logReaderService.removeLogListener(this);
	}
	
	private AbstractBinder binder() {
		return new AbstractBinder() {
			
			@Override
			protected void configure() {
				bind(EventApplication.this).to(EventApplication.class);
			}
		};
	}

	@Override
	public void logged(LogEntry  entry) {
		broadcaster.broadcast(outbound(entry));
	}
	
	private OutboundEvent outbound(LogEntry entry) {
		return new OutboundEvent.Builder()
			.mediaType(MediaType.APPLICATION_JSON_TYPE)
			.data(new LogEntryDto(entry))
			.build();
	}
	
	EventOutput createEventOutput() {
		final EventOutput result = new EventOutput();
		broadcaster.add(result);
		return result;
	}
}
