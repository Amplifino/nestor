package com.amplifino.nestor.useradmin.spi.rdbms;

import com.amplifino.nestor.useradmin.spi.RoleEntity;

public class MemberEntity {

	private final RoleEntity roleEntity;
	private final boolean required;
	
	public MemberEntity(RoleEntity roleEntity, boolean required) {
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
