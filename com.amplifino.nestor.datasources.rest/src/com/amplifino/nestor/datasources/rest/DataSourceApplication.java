package com.amplifino.nestor.datasources.rest;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;
import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.jdbc.DataSourceFactory;

@Component(service = Application.class, property = { "alias=/datasources",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PATTERN + "=/apps/datasources/*",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_RESOURCE_PREFIX + "=/resources/web"})
public class DataSourceApplication extends Application {

	private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy=ReferencePolicy.DYNAMIC, target = "(" + DataSourceFactory.JDBC_DATABASE_NAME
			+ "=*)")
	public void addDataSource(DataSource dataSource, Map<String, ?> props) {
		dataSources.put((String) props.get(DataSourceFactory.JDBC_DATABASE_NAME), dataSource);
	}

	public void removeDataSource(DataSource dataSource, Map<String, ?> props) {
		dataSources.remove(props.get(DataSourceFactory.JDBC_DATABASE_NAME), dataSource);
	}

	Optional<DataSource> dataSource(String name) {
		return Optional.ofNullable(dataSources.get(name));
	}

	Set<String> dataSourceNames() {
		return dataSources.keySet();
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return Stream.of(DataSourceResource.class).collect(Collectors.toSet());
	}
	
	@Override
	public Set<Object> getSingletons() {
		return Stream.of(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(DataSourceApplication.this).to(DataSourceApplication.class);
			}
		}).collect(Collectors.toSet());
	}

}
