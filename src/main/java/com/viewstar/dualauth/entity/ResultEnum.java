package com.viewstar.dualauth.entity;

public enum ResultEnum {
    UNKNOWN_ERROR(231, "未知错误"),
    USER_IS_EXIST(232, "该用户已存在"),
    USER_NOT_EXIST(233, "不存在该用户"),
    USER_IN_WHITELIST(234, "用户在白名单里"),
    USER_IN_BLACKLIST(235, "用户在黑名单里"),
    USER_NOT_IN_WHITELIST(236, "用户不在白名单里"),
    USER_NOT_IN_BLACKLIST(237, "用户不在黑名单里"),
    USER_IS_NULL(238, "用户类为空"),
    USERID_IS_NULL(239, "用户ID为空"),
    USER_IS_NOACTIVE(240, "用户未激活"),
    USER_IS_SUSPEND(241, "用户停机"),
    USER_IS_TERMINATED(242, "用户拆机"),
    ACCOUNT_IS_EXIST(251,"帐号已存在"),
    MOBILE_IS_EXIST(252,"手机号已存在"),
    EMAIL_IS_EXIST(253,"Email已存在"),
    USERNAME_PASS_ERROR(254,"用户名密码错误"),
    ACCOUNT_NOT_EXIST(255,"帐号不存在"),
    NOT_LOGIN(256,"未有登录信息"),
    STATUS_NOT_EXIST(257,"用户状态不存在"),
    NO_RIGHT(258,"无权修改"),
    ERR_INVITECODE(259,"错误的邀请码"),
    SUCCESS(200, "success"),
    CREATED(201,"created!"),
    SYSTEM_ERROR(500,"系统错误");

    private Integer code;

    private String msg;

    private ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
