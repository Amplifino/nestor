package com.amplifino.nestor.bundles.rest;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

@Component(service=TestComponent.class, property={"osgi.command.function=nestor", "osgi.command.scope=nestor"})
public class TestComponent {

	@Activate 
	public void activate() {
		System.out.println("in activate");
	}
	
	@Deactivate
	public void deactivate() {
		System.out.println("in deactivate");
	}
	
	@Reference(policy=ReferencePolicy.DYNAMIC)
	public void setLogService(LogService logService) {
		System.out.println("in set");
	}
	
	public void unsetLogService(LogService logService) {
		System.out.println("in unset");
	}
	
	public void nestor() {
		System.out.println("Welcome to nestor");
	}
}
