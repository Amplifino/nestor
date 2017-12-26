package com.amplifino.nestor.bundles.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;

@Path("/")
@Api
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
	@ApiOperation("Show image")
	public String image(@Context DotService dotService, @QueryParam("filter") String filter, @QueryParam("tred") String tred) {
		Predicate<Bundle> bundleFilter;
		if (filter != null && !filter.trim().isEmpty()) {
			Pattern pattern = Pattern.compile(filter);
			bundleFilter = bundle -> pattern.matcher(bundle.getSymbolicName()).matches();
		} else {
			bundleFilter = bundle -> bundle.getBundleId() != 0;
		}
		DigraphBuilder builder = DigraphBuilder.name("Bundles");
		Arrays.stream(context.getBundles())
			.filter(bundleFilter)
			.forEach(bundle -> builder.node(name(bundle)).label(label(bundle)).url(url(bundle)).add());
		Arrays.stream(context.getBundles())
			.filter(bundleFilter)
			.map(bundle -> bundle.adapt(BundleWiring.class))
			.filter(Objects::nonNull)
			.map(bundleWiring -> bundleWiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE))
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.map(WireAdapter::of)
			.filter(adapter -> bundleFilter.test(adapter.to()))
			.distinct()
			.forEach( adapter -> builder.edge(name(adapter.from())).to(name(adapter.to())));				
		builder.closeCurly();
		String digraph = builder.build();
		return new String(dotService.toSvg("1".equals(tred) ? dotService.tred(digraph) : digraph));
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
		builder.node(name(bundle)).label(label(bundle)).add();
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		List<BundleCapability> capabilities = Optional.ofNullable(wiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE)).orElse(Collections.emptyList());
		capabilities.forEach( capability -> builder.node(name(capability)).shape(DigraphBuilder.Shape.BOX).label(label(capability)).add());
		capabilities.forEach( capability -> builder.edge(name(capability)).to(name(bundle))); 		
		List<BundleWire> wires = Optional.ofNullable(wiring.getProvidedWires((BundleRevision.PACKAGE_NAMESPACE))).orElse(Collections.emptyList());
		for (BundleWire wire : wires) {
			Bundle importingBundle = wire.getRequirerWiring().getBundle();
			builder.node(name(importingBundle)).label(label(importingBundle)).url(url(importingBundle)).add();
			builder.edge(name(importingBundle)).to(name(wire.getCapability()));		
		};
		wires = Optional.ofNullable(wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE)).orElse(Collections.emptyList());
		capabilities = wires.stream().map(BundleWire::getCapability).distinct().collect(Collectors.toList()); 
		capabilities.forEach( capability -> builder.node(name(capability)).shape(DigraphBuilder.Shape.BOX).label(label(capability)).add());
		capabilities.forEach( capability -> builder.edge(name(bundle)).to(name(capability)));
		for (BundleWire wire : wires) {
			Bundle exportingBundle = wire.getProviderWiring().getBundle();
			builder.node(name(exportingBundle)).label(label(exportingBundle)).url(url(exportingBundle)).add();
			builder.edge(name(wire.getCapability())).to(name(exportingBundle));			
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
