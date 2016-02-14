package com.amplifino.nestor.soap.sample;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.osgi.service.component.annotations.Component;

import com.amplifino.nestor.soap.Publisher;

@WebService
@Component(service=SampleWebService.class, property={Publisher.LOCAL_ENDPOINT_ADDRESS + "=/sample"})
public class SampleWebService {

	@WebMethod
	public int add(int x, int y) {
		return x + y;
	}
}
