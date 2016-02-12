package com.amplifino.useradmin.spi.obelix;

import java.io.IOException;

import com.amplifino.obelix.sets.Injection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class RoleDtoInjection implements Injection<RoleDto, String> {
	
	private final ObjectMapper mapper;

	public RoleDtoInjection(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	public String map(RoleDto in) {
		try {
			return mapper.writeValueAsString(in);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	@Override
	public RoleDto unmap(String in) {
		try {
			return mapper.readValue(in, RoleDto.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
