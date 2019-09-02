package com.viewstar.dualauth.jpa.api;

/**
 * 登录信息
 * @author zhangwei
 *
 */
public class LoginInfo {

	private Long userId;
	private String account = "";
	private String roleName = "";
	public LoginInfo(Long userId, String account, String roleName) {
		super();
		this.userId = userId;
		this.account = account;
		this.roleName = roleName;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}
