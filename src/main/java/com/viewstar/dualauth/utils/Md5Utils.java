package com.viewstar.dualauth.utils;

import org.springframework.util.DigestUtils;

/**
 * (用户名md5+密码 )md5
 * @author zhangwei
 *
 */
public class Md5Utils {
	public static String encode(String account,String passwd) {
		passwd += new String(DigestUtils.md5DigestAsHex(account.getBytes()));
		return new String(DigestUtils.md5DigestAsHex(passwd.getBytes()));
	}
	public static void main(String[] args) {
		System.out.println(encode("zhansan","123456"));
	}
}
