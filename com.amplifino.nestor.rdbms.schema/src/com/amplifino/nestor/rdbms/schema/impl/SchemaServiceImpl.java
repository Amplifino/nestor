package com.amplifino.nestor.rdbms.schema.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.annotations.Component;

import com.amplifino.nestor.rdbms.schema.SchemaService;
import com.amplifino.nestor.rdbms.schema.Schema;

@Component
public class SchemaServiceImpl implements SchemaService {

	private List<SchemaImpl> schemas = new CopyOnWriteArrayList<>();
	
	public SchemaServiceImpl() {
	}

	@Override
	public SchemaImpl.BuilderImpl builder(String name) {
		return new SchemaImpl.BuilderImpl(this, Objects.requireNonNull(name));
	}

	@Override
	public Schema schema(String name) {
		return schemas.stream().filter(bundle -> bundle.name().equals(name)).findFirst().orElseThrow(IllegalArgumentException::new);
	}

	@Override
	public List<? extends Schema> schemas() {
		return Collections.unmodifiableList(schemas);
	}
	
	void add(SchemaImpl bundle) {
		schemas.add(bundle);
	}

}
