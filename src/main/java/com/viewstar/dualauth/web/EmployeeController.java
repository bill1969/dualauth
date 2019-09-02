package com.viewstar.dualauth.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.viewstar.dualauth.entity.LoginEnum;
import com.viewstar.dualauth.entity.ResponseMessage;
import com.viewstar.dualauth.entity.ResultEnum;
import com.viewstar.dualauth.entity.ResultUtils;
import com.viewstar.dualauth.jpa.api.ActionLog;
import com.viewstar.dualauth.jpa.api.Employee;
import com.viewstar.dualauth.jpa.api.LoginInfo;
import com.viewstar.dualauth.jpa.api.User;
import com.viewstar.dualauth.jpa.core.UserService;
import com.viewstar.dualauth.utils.IpUtils;
import com.viewstar.dualauth.utils.Md5Utils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

@Api(tags="操作员管理",value="...")
@RequestMapping("/employee")
@RestController
public class EmployeeController extends BaseController {

    @Autowired
    private UserService userService;
    @ApiOperation(value = "注册帐号",notes = "格式{\"username\":\"张三\",\"account\":\"zhangsan\",\"passwd\":\"123456\",\"mobile\":\"18612345666\",\"email\":\"abc@qq.com\",\"photoUrl\":\"/a/b\"}，最后一个参数选填")
    @ApiResponses(value = {@ApiResponse(code = 251, message =  "帐号已存在!"),
            @ApiResponse(code=252,message = "手机号已存在"),
            @ApiResponse(code=259,message = "错误的邀请码"),
            @ApiResponse(code = 200, message = "成功!")})
    @PostMapping(value = "register")
    public ResponseMessage register(HttpServletRequest request,HttpServletResponse response,
                                    @RequestBody Employee user
    ) throws Exception {
        System.out.println("register:"+user.toString());
        if (userService.checkInviteCode(user.getInviteCode())==null){
             response.setStatus(ResultEnum.ERR_INVITECODE.getCode());
            return ResultUtils.error(ResultEnum.ERR_INVITECODE, "", "/employee/register");
        }

        if(userService.checkMobile(user.getMobile())!=null) {
            response.setStatus(ResultEnum.MOBILE_IS_EXIST.getCode());
            return ResultUtils.error(ResultEnum.MOBILE_IS_EXIST, "", "/employee/register");
            }
        System.out.println("check Account:"+user.getAccount());
        if(userService.checkAccount(user.getAccount())!=null) {
            response.setStatus(ResultEnum.ACCOUNT_IS_EXIST.getCode());
            return  ResultUtils.error(ResultEnum.ACCOUNT_IS_EXIST,"", "/employee/register");
        }
        Employee _Employee = new Employee() ;
        _Employee.setName(user.getName());
        _Employee .setPasswd(Md5Utils.encode(user.getAccount(),user.getPasswd()));

        _Employee .setAccount(user.getAccount());
        _Employee .setMobile(user.getMobile());
        _Employee .setAccount(user.getAccount());
        _Employee .setEmail(user.getEmail());
        _Employee .setStatus(1);
        _Employee .setRoleId(3l);
        _Employee .setLoginType(LoginEnum.NO_LOGIN.getValue());//未登录
        _Employee .setPhotoUrl(user.getPhotoUrl());
        _Employee .setCreateDate(new Date());
        _Employee.setLastLoginTime(new Date());
        _Employee.setLastLoginIp(IpUtils.getIpAddr(request));
        userService.employeeSave(_Employee );
        ActionLog log = new ActionLog();
        log.setActionID(10);  //10-regist 11-update employee info 12-delete whitelist
        log.setUserID(user.getAccount());
        log.setResult("regist success");
        log.setOptime(new java.sql.Date(new Date().getTime()));
        log.setOperator(user.getAccount());
        userService.logSave(log);
        userService.updateInviteCode(user.getAccount(),user.getInviteCode());
        response.setStatus(ResultEnum.SUCCESS.getCode());
        return ResultUtils.success(JSONObject.toJSONString(_Employee), "/employee/register");
    }

    @ApiOperation(value = "删除帐号",notes = "帐号优先,电话次之")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "帐号", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "telephone", value = "电话", required = true, paramType = "query", dataType = "String"),
    })
    @ApiResponses(value = {@ApiResponse(code = 255, message =  "帐号不存在!"),
            @ApiResponse(code = 256, message = "未有登录信息!"),
            @ApiResponse(code = 200, message = "成功!")})
    @PostMapping("delete")
    public ResponseMessage delete(HttpServletRequest request,HttpServletResponse response, @RequestParam(value = "account", required = true) String account,@RequestParam(value = "telephone", required = true) String telephone) throws Exception {
        System.out.println("employee/delete:"+account+" "+telephone);
        LoginInfo loginInfo = getLoginInfo(request);
        if (loginInfo.getAccount() ==null){
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/users/addWhiteList");
        }
        Employee emp=userService.checkAccount(account);
        if (emp==null)
            emp=userService.checkMobile(telephone);
        if (emp!=null){
            ActionLog log = new ActionLog();
            log.setActionID(12);  //10-regist 11-update employee info 12-delete employee
            log.setUserID(account+"/"+telephone);
            if (loginInfo.getRoleName().equals("1")||loginInfo.getRoleName().equals("admin")){
                userService.employeeDel(emp.getId());
                log.setResult("delete account success");
                log.setOptime(new java.sql.Date(new Date().getTime()));
                log.setOperator(loginInfo.getAccount());
                userService.logSave(log);
                response.setStatus(ResultEnum.SUCCESS.getCode());
                return ResultUtils.success("success", "/employee/delete");
            }else{
                log.setResult("delete account no right");
                log.setOptime(new java.sql.Date(new Date().getTime()));
                log.setOperator(loginInfo.getAccount());
                response.setStatus(ResultEnum.NO_RIGHT.getCode());
                return ResultUtils.error(ResultEnum.NO_RIGHT, "", "/employee/delete");
            }

        }
        else{
            ActionLog log = new ActionLog();
            log.setActionID(12);  //10-regist 11-update employee info 12-delete employee
            log.setUserID(account);
            log.setResult("del account not exist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.ACCOUNT_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.ACCOUNT_NOT_EXIST,"","/users/getUser");
        }
    }
    @ApiOperation(value = "删除帐号",notes = "帐号优先,电话次之{\n" +
            "  \"account\": \"iptv0431123\",\n" +
            "  \"telephone\": \"13303452120\"\n" +
            "}")
    @ApiResponses(value = {@ApiResponse(code = 255, message =  "帐号不存在!"),
            @ApiResponse(code = 256, message = "未有登录信息!"),
            @ApiResponse(code = 200, message = "成功!")})
    @PostMapping("Delete")
    public ResponseMessage Delete(HttpServletRequest request,HttpServletResponse response,  @RequestBody JSONObject params) throws Exception {
        String account = params.getString("account");
        String telephone = params.getString("telephone");
        System.out.println("employee/Delete:"+account+" "+telephone);
        LoginInfo loginInfo = getLoginInfo(request);
        if (loginInfo.getAccount() ==null){
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/users/addWhiteList");
        }
        Employee emp=userService.checkAccount(account);
        if (emp==null)
            emp=userService.checkMobile(telephone);
        if (emp!=null){
            ActionLog log = new ActionLog();
            log.setActionID(12);  //10-regist 11-update employee info 12-delete employee
            log.setUserID(account+"/"+telephone);

            if (loginInfo.getRoleName().equals("1")||loginInfo.getRoleName().equals("admin")){
                userService.employeeDel(emp.getId());
                log.setResult("delete account success");
                log.setOptime(new java.sql.Date(new Date().getTime()));
                log.setOperator(loginInfo.getAccount());
                userService.logSave(log);
                response.setStatus(ResultEnum.SUCCESS.getCode());
                return ResultUtils.success("success", "/employee/delete");
            }else{
                log.setResult("delete account no right");
                log.setOptime(new java.sql.Date(new Date().getTime()));
                log.setOperator(loginInfo.getAccount());
                response.setStatus(ResultEnum.NO_RIGHT.getCode());
                return ResultUtils.error(ResultEnum.NO_RIGHT, "", "/employee/delete");
            }
        }
        else{
            ActionLog log = new ActionLog();
            log.setActionID(12);  //10-regist 11-update employee info 12-delete employee
            log.setUserID(account+"/"+telephone);
            log.setResult("delete account not exist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.ACCOUNT_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.ACCOUNT_NOT_EXIST,"","/users/getUser");
        }
    }
    @ApiOperation(value = "获取当前用户")
    @ApiResponses(value = {@ApiResponse(code = 256, message =  "未有登录信息!"),
            @ApiResponse(code = 200, message = "成功!")})
    @GetMapping("/currentUser")
    public ResponseMessage getCurrentUser(HttpServletRequest request,HttpServletResponse response) {
        LoginInfo loginInfo = getLoginInfo(request);
        Optional<Employee> employee = userService.getById(loginInfo.getUserId());

        if((employee ==null)||(!employee.isPresent())) {
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/employee/currentUser");
        }
        System.out.println("currentUser:"+employee.toString());
        response.setStatus(ResultEnum.SUCCESS.getCode());
        return ResultUtils.success(JSONObject.toJSONString(employee), "/employee/currentUser");
    }


    @ApiOperation(value = "用户登录",notes = "type:1=未登录 2=账号登录 3=手机登录")
    @ApiResponses(value = {@ApiResponse(code = 254, message =  "用户名密码错误!"),
            @ApiResponse(code = 200, message = "成功!")})
    @PostMapping("/Login")
    public ResponseMessage Login(HttpServletRequest request,HttpServletResponse response, @RequestBody JSONObject params) throws Exception {

        String username = params.getString("username");
        String password = params.getString("password");
        int type = params.getInteger("type");
        System.out.println("Login:"+username+" pass:"+password+" "+type);
        LoginInfo loginInfo = getLoginInfo(request);
        Employee emp =null;
        if (type==2)
            emp =userService.checkAccount(username);
        else
            emp =userService.checkMobile(username);

        if (emp!=null){
            Employee employee=userService.checkLogin(username,Md5Utils.encode(emp.getAccount(),password),type);

            if(employee!=null) {
            employee.setLastLoginIp(IpUtils.getIpAddr(request));
            employee.setLastLoginTime(new Date());
            userService.employeeSave(employee);
            request.getSession().setAttribute("userId",employee.getId());
            request.getSession().setAttribute("account", employee.getAccount());
            request.getSession().setAttribute("roleName", ""+employee.getRoleId());

            ActionLog log = new ActionLog();
            log.setActionID(13);  //10-regist 11-update employee info 12-delete employee  13-login
            log.setUserID(employee.getAccount());
            log.setResult("login success!");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(employee.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.SUCCESS.getCode());
            return ResultUtils.success(JSONObject.toJSONString(employee), "/employee/login");
            }else {
                ActionLog log = new ActionLog();
                log.setActionID(13);  //10-regist 11-update employee info 12-delete employee  13-login
                log.setUserID(username);
                log.setResult("login  type"+type+" password error!");
                log.setOptime(new java.sql.Date(new Date().getTime()));
                log.setOperator(username);
                userService.logSave(log);
                response.setStatus(ResultEnum.USERNAME_PASS_ERROR.getCode());
                return ResultUtils.error(ResultEnum.USERNAME_PASS_ERROR, "", "/employee/login");
            }
        }else
        {
            ActionLog log = new ActionLog();
            log.setActionID(13);  //10-regist 11-update employee info 12-delete employee  13-login
            log.setUserID(username);
            log.setResult("login type"+type+" password error!");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(username);
            userService.logSave(log);
            response.setStatus(ResultEnum.USERNAME_PASS_ERROR.getCode());
            return ResultUtils.error(ResultEnum.USERNAME_PASS_ERROR, "", "/employee/login");
        }
    }

    @ApiOperation(value = "用户登录",notes = "type:1=未登录 2=账号登录 3=手机登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "名称", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "type", value = "登录类型", required = true, paramType = "query", dataType = "int")
    })

    @ApiResponses(value = {@ApiResponse(code = 254, message =  "用户名密码错误!"),
            @ApiResponse(code = 200, message = "成功!")})
    @PostMapping("/login")
    public ResponseMessage login(HttpServletRequest request,HttpServletResponse response, @RequestParam(value = "username", required = true) String username,@RequestParam(value = "password", required = true)String password,@RequestParam(value = "type", required = true) int type) throws Exception {
        System.out.println("login:"+username+" pass:"+password+" "+type);
        LoginInfo loginInfo = getLoginInfo(request);
        Employee emp =null;
        if (type==2)
            emp =userService.checkAccount(username);
        else
            emp =userService.checkMobile(username);

        if (emp!=null){
            Employee employee=userService.checkLogin(username,Md5Utils.encode(emp.getAccount(),password),type);

            if(employee!=null) {
                employee.setLastLoginIp(IpUtils.getIpAddr(request));
                employee.setLastLoginTime(new Date());
                userService.employeeSave(employee);
                request.getSession().setAttribute("userId",employee.getId());
                request.getSession().setAttribute("account",employee.getAccount());
                request.getSession().setAttribute("roleName", ""+employee.getRoleId());

                ActionLog log = new ActionLog();
                log.setActionID(13);  //10-regist 11-update employee info 12-delete employee  13-login
                log.setUserID(employee.getAccount());
                log.setResult("login success");
                log.setOptime(new java.sql.Date(new Date().getTime()));
                log.setOperator(employee.getAccount());
                userService.logSave(log);
                response.setStatus(ResultEnum.SUCCESS.getCode());
                return ResultUtils.success(JSONObject.toJSONString(employee), "/employee/login");
            }else {
                response.setStatus(ResultEnum.USERNAME_PASS_ERROR.getCode());
                return ResultUtils.error(ResultEnum.USERNAME_PASS_ERROR, "", "/employee/login");
            }
        }else
        {
            response.setStatus(ResultEnum.USERNAME_PASS_ERROR.getCode());
            return ResultUtils.error(ResultEnum.USERNAME_PASS_ERROR, "", "/employee/login");
        }
    }

    @ApiOperation(value = "用户登出",notes = "参数参照用户登录,type:1=未登录 2=账号登录 3=手机登录")
    @ApiResponses(value = {@ApiResponse(code = 254, message =  "用户名密码错误!"),
            @ApiResponse(code = 256, message = "未有登录信息!"),
            @ApiResponse(code = 200, message = "成功!")})
    @PostMapping("/Logout")
    public ResponseMessage Logout(HttpServletRequest request,HttpServletResponse response, @RequestBody JSONObject params) throws Exception {

        String username = params.getString("username");
        String password = params.getString("password");
        int type = params.getInteger("type");
        System.out.println("logout:"+username+" pass:"+password+" "+type);
        LoginInfo loginInfo = getLoginInfo(request);
        if (loginInfo.getAccount() ==null){
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/users/addWhiteList");
        }
        Employee emp =null;
        if (type==2)
            emp =userService.checkAccount(username);
        else
            emp =userService.checkMobile(username);

        if (emp!=null){
            Employee employee=userService.checkLogin(username,Md5Utils.encode(emp.getAccount(),password),type);

            if(employee!=null) {
                employee.setLastLoginIp(IpUtils.getIpAddr(request));
                employee.setLastLoginTime(new Date());
                userService.employeeSave(employee);
                request.getSession().setAttribute("userId",null);
                request.getSession().setAttribute("account", null);
                request.getSession().setAttribute("roleName", "");

                ActionLog log = new ActionLog();
                log.setActionID(14);  //10-regist 11-update employee info 12-delete employee  13-login 14-logout
                log.setUserID(employee.getAccount());
                log.setResult("logout success!");
                log.setOptime(new java.sql.Date(new Date().getTime()));
                log.setOperator(loginInfo.getAccount());
                userService.logSave(log);
                response.setStatus(ResultEnum.SUCCESS.getCode());
                return ResultUtils.success(JSONObject.toJSONString(employee), "/employee/logout");
            }else {
                ActionLog log = new ActionLog();
                log.setActionID(14);  //10-regist 11-update employee info 12-delete employee  13-login 14-logout
                log.setUserID(employee.getAccount());
                log.setResult("logout password error!");
                log.setOptime(new java.sql.Date(new Date().getTime()));
                log.setOperator(loginInfo.getAccount());
                userService.logSave(log);
                response.setStatus(ResultEnum.USERNAME_PASS_ERROR.getCode());
                return ResultUtils.error(ResultEnum.USERNAME_PASS_ERROR, "", "/employee/logout");
            }
        }else
        {
            ActionLog log = new ActionLog();
            log.setActionID(14);  //10-regist 11-update employee info 12-delete employee  13-login 14-logout
            log.setUserID(username);
            log.setResult("logout success!");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USERNAME_PASS_ERROR.getCode());
            return ResultUtils.error(ResultEnum.USERNAME_PASS_ERROR, "", "/employee/logout");
        }
    }

    @ApiOperation(value = "修改用户密码",notes = "type:1=未登录 2=账号登录 3=手机登录")
    @ApiResponses(value = {@ApiResponse(code = 255, message =  "帐号不存在!"),
            @ApiResponse(code = 256, message = "未有登录信息!"),@ApiResponse(code = 258, message =  "无权修改!"),
            @ApiResponse(code = 200, message = "成功!")})
    @PostMapping("/UpdatePwd")
    public ResponseMessage UpdatePwd(HttpServletRequest request,HttpServletResponse response, @RequestBody JSONObject params) throws Exception {

        String username = params.getString("username");
        String password = params.getString("password");
        int type = params.getInteger("type");
        System.out.println("UpdatePwd:"+username+" pass:"+password+" "+type);
        LoginInfo loginInfo = getLoginInfo(request);
        if (loginInfo.getAccount() ==null){
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/users/addWhiteList");
        }
        Employee emp =null;
        if (type==2)
            emp =userService.checkAccount(username);
        else
            emp =userService.checkMobile(username);

        if (emp!=null){
            if (loginInfo.getRoleName().equals("1")||loginInfo.getRoleName().equals("admin")||loginInfo.getAccount().equals(emp.getAccount())){
                emp.setLastLoginIp(IpUtils.getIpAddr(request));
                emp.setLastLoginTime(new Date());
                emp.setPasswd(Md5Utils.encode(emp.getAccount(),password));
                userService.employeeSave(emp);

                ActionLog log = new ActionLog();
                log.setActionID(15);  //10-regist 11-update employee info 12-delete employee  13-login 14-logout 15-update pwd
                log.setUserID(emp.getAccount());
                log.setResult("update pwd success!");
                log.setOptime(new java.sql.Date(new Date().getTime()));
                log.setOperator(loginInfo.getAccount());
                userService.logSave(log);
                response.setStatus(ResultEnum.SUCCESS.getCode());
                return ResultUtils.success(JSONObject.toJSONString(emp), "/employee/UpdatePwd");
            }else {
                ActionLog log = new ActionLog();
                log.setActionID(15);  //10-regist 11-update employee info 12-delete employee  13-login 14-logout 15-update pwd
                log.setUserID(emp.getAccount());
                log.setResult("update pwd no right!");
                log.setOptime(new java.sql.Date(new Date().getTime()));
                log.setOperator(loginInfo.getAccount());
                userService.logSave(log);
                response.setStatus(ResultEnum.NO_RIGHT.getCode());
                return ResultUtils.error(ResultEnum.NO_RIGHT, "", "/employee/UpdatePwd");
            }
        }else
        {
            ActionLog log = new ActionLog();
            log.setActionID(15);  //10-regist 11-update employee info 12-delete employee  13-login 14-logout 15-update pwd
            log.setUserID(username);
            log.setResult("update pwd account not exist!");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.ACCOUNT_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.ACCOUNT_NOT_EXIST, "", "/employee/UpdatePwd");
        }
    }

    @ApiOperation(value = "获得操作日志数据", notes = "获得操作日志数据")
    @RequestMapping(value = "/listOperationLog/{account}/{size}", method = RequestMethod.GET)
    public List<ActionLog> listOperationLog(HttpServletRequest request, @PathVariable(value = "account") String account,@PathVariable(value = "size") int size) throws Exception {
        return userService.listOperationLog(account,size);
    }
}
