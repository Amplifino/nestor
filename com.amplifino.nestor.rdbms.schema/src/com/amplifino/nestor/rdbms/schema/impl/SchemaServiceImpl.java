package com.amplifino.nestor.rdbms.schema.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.annotations.Component;

import com.amplifino.nestor.rdbms.schema.SchemaService;
import com.amplifino.nestor.rdbms.schema.TableBundle;

@Component
public class SchemaServiceImpl implements SchemaService {

	private List<TableBundleImpl> bundles = new CopyOnWriteArrayList<>();
	
	public SchemaServiceImpl() {
	}

	@Override
	public TableBundleImpl.BuilderImpl builder(String name) {
		return new TableBundleImpl.BuilderImpl(this, Objects.requireNonNull(name));
	}

	@Override
	public TableBundle bundle(String name) {
		return bundles.stream().filter(bundle -> bundle.name().equals(name)).findFirst().orElseThrow(IllegalArgumentException::new);
	}

	@Override
	public List<? extends TableBundle> bundles() {
		return Collections.unmodifiableList(bundles);
	}
	
	void add(TableBundleImpl bundle) {
		bundles.add(bundle);
	}

}
