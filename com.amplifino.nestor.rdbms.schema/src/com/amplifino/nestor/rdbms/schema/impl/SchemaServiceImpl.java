package com.amplifino.nestor.rdbms.schema.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;

import com.amplifino.nestor.rdbms.schema.Schema;
import com.amplifino.nestor.rdbms.schema.SchemaService;

@Component
public class SchemaServiceImpl implements SchemaService {

	private Map<String, SchemaImpl> schemas = new ConcurrentHashMap<>();
	
	public SchemaServiceImpl() {
	}

	@Override
	public SchemaImpl.BuilderImpl builder(String name) {
		return new SchemaImpl.BuilderImpl(this, Objects.requireNonNull(name));
	}

	@Override
	public Schema schema(String name) {
		return Optional.ofNullable(schemas.get(name)).orElseThrow(IllegalArgumentException::new);
	}

	@Override
	public List<? extends Schema> schemas() {
		return new ArrayList<>(schemas.values());
	}
	
	void add(SchemaImpl schema) {
		schemas.put(schema.name(), schema);
	}

}
