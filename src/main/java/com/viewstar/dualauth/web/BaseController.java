package com.viewstar.dualauth.web;

import com.viewstar.dualauth.jpa.api.LoginInfo;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Controller
public class BaseController {

	public LoginInfo getLoginInfo(HttpServletRequest request) {
		String account = (String) request.getSession().getAttribute("account");
		String roleName = (String) request.getSession().getAttribute("roleName");
		Long userId = (Long) request.getSession().getAttribute("userId");
		
		return new LoginInfo(userId==null?-1:userId.longValue(), account,roleName);
	}
	
	
}
