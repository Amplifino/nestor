package com.amplifino.nestor.rdbms.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.amplifino.nestor.rdbms.schema.Table;
import com.amplifino.nestor.rdbms.schema.Schema;

class SchemaImpl implements Schema {

	private final SchemaServiceImpl schemaService;
	private final String name;
	private Optional<String> schemaOwner = Optional.empty();
	private final List<TableImpl> tables = new ArrayList<>();
	
	SchemaImpl(SchemaServiceImpl schemaService, String name) {
		this.schemaService = schemaService;
		this.name = name;
	}

	@Override 
	public String name() {
		return name;
	}
	
	@Override
	public Optional<String> schemaOwner() {
		return schemaOwner;
	}
	
	@Override
	public List<TableImpl> tables() {		
		return Collections.unmodifiableList(tables);
	}

	@Override
	public TableImpl table(String name) {
		return tables.stream().filter(table -> table.name().equals(name)).findAny().orElseThrow(IllegalArgumentException::new);
	}
	
	SchemaServiceImpl schemaService() {
		return schemaService;
	}
	
	void add(TableImpl table) {
		this.tables.add(table);
	}
	
	static class BuilderImpl implements Schema.Builder {
		
		private final SchemaImpl tableBundle;
		
		BuilderImpl(SchemaServiceImpl service, String name) {
			this.tableBundle = new SchemaImpl(service, name);
		}
		
		@Override
		public BuilderImpl schemaOwner(String name) {
			tableBundle.schemaOwner =  Optional.of(name);
			return this;
		}
		
		@Override
		public Table.Builder builder(String name) {
			return new TableImpl.BuilderImpl(tableBundle, name);
		}
			
		@Override
		public SchemaImpl build() {
			tableBundle.schemaService.add(tableBundle);
			return tableBundle;
		}
	}

	
}