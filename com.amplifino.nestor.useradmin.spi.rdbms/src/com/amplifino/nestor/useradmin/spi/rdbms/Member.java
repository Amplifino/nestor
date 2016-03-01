package com.amplifino.nestor.useradmin.spi.rdbms;

import com.amplifino.nestor.useradmin.spi.RoleEntity;

public class Member {

	private final RoleEntity roleEntity;
	private final boolean required;
	
	public Member(RoleEntity roleEntity, boolean required) {
		this.roleEntity = roleEntity;
		this.required = required;
	}
	
	RoleEntity role() {
		return roleEntity;
	}
	
	boolean isRequired() {
		return required;
	}
	
}
