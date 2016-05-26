package com.amplifino.nestor.webconsole.jdbc.pools;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

import org.apache.felix.inventory.Format;
import org.apache.felix.inventory.InventoryPrinter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jdbc.DataSourceFactory;

import com.amplifino.counters.CountsSupplier;

@Component(property={
	"felix.inventory.printer.name=DataSources",
	"felix.inventory.printer.title=DataSources",	
	"felix.inventory.printer.format=TEXT"})
public class DataSourcePrinter implements InventoryPrinter {

	@Reference(policy=ReferencePolicy.DYNAMIC)
	private final List<Map.Entry<Map<String,Object>, DataSource>> dataSources = new CopyOnWriteArrayList<>();
		
	@Override
	public void print(PrintWriter writer, Format format, boolean ignored) {
		dataSources.forEach(entry -> print(writer, entry));
	}
	
	private void print(PrintWriter writer, Map.Entry<Map<String, Object>, DataSource> entry) {
		writer.println("DataSource: " + entry.getKey().get(DataSourceFactory.JDBC_DATABASE_NAME));
			if (entry.getValue() instanceof CountsSupplier) {
				((CountsSupplier) entry.getValue()).counts().asMap()
					.forEach((key, count) -> this.print(writer,  key, count));							
			}
		writer.println();
	}
	
	private void print(PrintWriter writer, Enum<?> key, long count) {
		writer.println("\t" + key.name().toLowerCase() + ": " + count);
	}

}
