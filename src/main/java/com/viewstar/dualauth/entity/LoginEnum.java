package com.viewstar.dualauth.entity;

public enum LoginEnum {
	NO_LOGIN(1),
	ACCOUNT_LOGIN(2),
	MOBILE_LOGIN(3);
	
	private Integer value;
	 
    private LoginEnum(Integer value) {
        this.value = value;
    }
 
    public Integer getValue() {
        return value;
    }
}
