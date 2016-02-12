package com.amplifino.nestor.bundles.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Capability;

import com.amplifino.nestor.dot.DigraphBuilder;
import com.amplifino.nestor.dot.DotService;

@Path("/")
public class BundleResource {

	private final BundleContext context;
	
	@Inject
	public BundleResource(BundleContext context) {
		this.context = context;		
	}
	
	@GET
	@Path("/wheel")
	@Produces(MediaType.APPLICATION_JSON)
	public DependencyWheel wheel() {
		return Arrays.stream(context.getBundles())
			.map(bundle -> bundle.adapt(BundleWiring.class))
			.filter(Objects::nonNull)
			.map(bundleWiring -> bundleWiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE))
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.collect(
				() -> DependencyWheel.of(Arrays.stream(context.getBundles()).map(this::wheelName).collect(Collectors.toList())),
				(wheel, bundleWire) -> wheel.setDependency(
						wheelName(bundleWire.getRequirerWiring().getBundle()),
						wheelName(bundleWire.getProviderWiring().getBundle())),
				DependencyWheel::addDependencies);
	}
	
	@GET
	@Path("/image")
	@Produces("image/svg+xml")
	public String image(@Context DotService dotService) {
		DigraphBuilder builder = DigraphBuilder.name("Bundles");
		Arrays.stream(context.getBundles())
			.filter(bundle -> bundle.getBundleId() != 0)
			.forEach(bundle -> builder.node(name(bundle)).label(label(bundle)).url(url(bundle)).add());
		Arrays.stream(context.getBundles())
			.map(bundle -> bundle.adapt(BundleWiring.class))
			.filter(Objects::nonNull)
			.map(bundleWiring -> bundleWiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE))
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.map(WireAdapter::of)
			.filter(adapter -> adapter.to().getBundleId() != 0)
			.distinct()
			.forEach( adapter -> {
				builder.quote(name(adapter.from()));
				builder.append(" -> ");
				builder.quote(name(adapter.to()));
				builder.newLine();
			});			
		builder.closeCurly();
		return new String(dotService.toSvg(builder.build()));
	}
	
	@GET
	@Path("/image/{id}")
	@Produces("image/svg+xml")
	public String bundleImage(@Context DotService dotService, @PathParam("id") long bundleId) {
		DigraphBuilder builder = DigraphBuilder.name("Bundle");
		builder.append("rankdir=\"LR\";\n");
		Bundle bundle = context.getBundle(bundleId);
		if (bundle == null) {
			throw new NotFoundException();
		}
		Set<Long> bundleIds = new HashSet<>();
		bundleIds.add(bundle.getBundleId());
		builder.node(name(bundle)).label(label(bundle)).add();
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		List<BundleCapability> capabilities = Optional.ofNullable(wiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE)).orElse(Collections.emptyList());
		capabilities.forEach( capability -> builder.node(name(capability)).shape(DigraphBuilder.Shape.BOX).label(label(capability)).add());
		capabilities.forEach( capability -> {
				builder.quote(name(capability));
				builder.append(" -> ");
				builder.quote(name(bundle));
				builder.newLine();
			});
		List<BundleWire> wires = Optional.ofNullable(wiring.getProvidedWires((BundleRevision.PACKAGE_NAMESPACE))).orElse(Collections.emptyList());
		for (BundleWire wire : wires) {
			Bundle importingBundle = wire.getRequirerWiring().getBundle();
			if (!bundleIds.contains(importingBundle.getBundleId())) {
				builder.node(name(importingBundle)).label(label(importingBundle)).url(url(importingBundle)).add();
				bundleIds.add(importingBundle.getBundleId());
			}
			builder.quote(name(importingBundle));
			builder.append(" -> ");
			builder.quote(name(wire.getCapability()));
			builder.newLine();
		};
		wires = Optional.ofNullable(wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE)).orElse(Collections.emptyList());
		capabilities = wires.stream().map(BundleWire::getCapability).distinct().collect(Collectors.toList()); 
		capabilities.forEach( capability -> builder.node(name(capability)).shape(DigraphBuilder.Shape.BOX).label(label(capability)).add());
		capabilities.forEach( capability -> {
				builder.quote(name(bundle));
				builder.append(" -> ");
				builder.quote(name(capability));
				builder.newLine();
			});	
		for (BundleWire wire : wires) {
			Bundle exportingBundle = wire.getProviderWiring().getBundle();
			if (!bundleIds.contains(exportingBundle.getBundleId())) {
				builder.node(name(exportingBundle)).label(label(exportingBundle)).url(url(exportingBundle)).add();
				bundleIds.add(exportingBundle.getBundleId());
			}
			builder.quote(name(wire.getCapability()));
			builder.append(" -> ");
			builder.quote(name(exportingBundle));
			builder.newLine();
		};
		builder.closeCurly();
		return new String(dotService.toSvg(builder.build()));
	}
	
	private String name(Bundle bundle) {
		return "B" + bundle.getBundleId();
	}

	private String wheelName(Bundle bundle) {
		return String.join(":", bundle.getSymbolicName(), bundle.getVersion().toString());
	}
	
	private String label(Bundle bundle) {
		return String.join("\n", bundle.getHeaders().get("Bundle-Name") , bundle.getSymbolicName(), bundle.getVersion().toString());
	}
	
	private String name(Capability capability) {
		return String.join(":", 		
			capability.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE).toString(), 
			(String) capability.getAttributes().get("version").toString());
	}
	
	private String label(Capability capability) {
		return String.join("\n", 
			capability.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE).toString(), 
			(String) capability.getAttributes().get("version").toString());
	}
	
	private String url(Bundle bundle) {
		return "/api/bundles/image/" + bundle.getBundleId();
	}
}
