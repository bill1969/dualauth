package com.viewstar.dualauth.web;

import com.alibaba.fastjson.JSONObject;

import com.viewstar.dualauth.jpa.api.LoginInfo;
import com.viewstar.dualauth.utils.RedisUtils;
import io.swagger.annotations.*;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import com.viewstar.dualauth.entity.ResponseMessage;
import com.viewstar.dualauth.entity.ResultEnum;
import com.viewstar.dualauth.entity.ResultUtils;
import com.viewstar.dualauth.jpa.api.ActionLog;
import com.viewstar.dualauth.jpa.api.User;
import com.viewstar.dualauth.jpa.core.UserService;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Api(tags="用户相关管理",value="...")
@RequestMapping("/users")
@RestController
public class UserController extends BaseController{

    /*@Autowired
    private userService userService;*/
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtils redisUtils;

    private boolean GolableSwitch = true;   //进行认证，close--不认证，返回成功
    private boolean noactive = true;
    private boolean suspend = true;
    private boolean terminated = true;
    private Date lastAuthDate = new Date();
    @Value("${saveInDB}")
    private boolean saveInDB;

    @Autowired
    public void UserController() {
        redisUtils.set("GolableSwitch","true");
        redisUtils.set("noactive","true");
        redisUtils.set("suspend","true");
        redisUtils.set("terminated","true");
        if (redisUtils.get("authOfToday")==null)
            redisUtils.set("authOfToday",0);
        if (redisUtils.get("unauthOfToday")==null)
            redisUtils.set("unauthOfToday",0);
        if (redisUtils.get("unAuthTotal")==null)
            redisUtils.set("unAuthTotal",0);

        System.out.println("Init success!");
    }

    @ApiOperation(value = "创建用户", notes = "根据User对象创建用户,userid是必填项")
    @ApiImplicitParam(name = "user", value = "用户信息", dataType = "User")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "已创建!"),
            @ApiResponse(code = 239, message = "userid为空!"),
            @ApiResponse(code = 232, message = "用户已存在!")})
    @RequestMapping(value = "createUser", method = RequestMethod.POST)
    public ResponseMessage createUser(HttpServletRequest request,HttpServletResponse response, @RequestBody User user) throws Exception {

        System.out.println("createUser:" + JSONObject.toJSONString(user));
        LoginInfo loginInfo = getLoginInfo(request);
        if (user == null) {
            saveToDB(0,"","user is null",loginInfo.getAccount());
            response.setStatus(ResultEnum.USER_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USER_IS_NULL, "", "/users/createUser");
        }
        if ((user.getUserid() == null) || (user.getUserid().isEmpty())) {
            saveToDB(0,"","userid is null",loginInfo.getAccount());
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/createUser");
        }
        if ((userService.getUserByUserId(user.getUserid()) != null) && (user.getState() < 3)) {
            saveToDB(0,user.getUserid(),"user is exist",loginInfo.getAccount());
            response.setStatus(ResultEnum.USER_IS_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_IS_EXIST, "", "/users/createUser");
        } else {
            saveToDB(0,user.getUserid(),"create userinfo success",loginInfo.getAccount());

            user.setActiveTime(new java.sql.Date(new Date().getTime()));
            user.setUpdateTime(new java.sql.Date(new Date().getTime()));
            redisUtils.set(user.getUserid().toLowerCase(),user.getState());            userService.save(user);
            response.setStatus(ResultEnum.SUCCESS.getCode());
            return ResultUtils.success(JSONObject.toJSONString(user), "/users/createUser");
        }
    }

    @ApiOperation(value = "修改用户信息", notes = "修改用户信息")
    @ApiImplicitParam(name = "user", value = "用户信息", dataType = "User")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功!"),
            @ApiResponse(code = 239, message = "userid为空!"),
            @ApiResponse(code = 233, message = "用户不存在!")})
    @RequestMapping(value = "modifyUser", method = RequestMethod.PUT)
    public ResponseMessage modifyUser(HttpServletRequest request,HttpServletResponse response, @RequestBody User user) throws Exception {
        System.out.println("modifyUser:" + JSONObject.toJSONString(user));
        LoginInfo loginInfo = getLoginInfo(request);

        if (user == null) {
            saveToDB(1,"","user is null",loginInfo.getAccount());
            response.setStatus(ResultEnum.USER_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USER_IS_NULL, "", "/users/modifyUser");
        }
        if ((user.getUserid() == null) || (user.getUserid().isEmpty())) {
            saveToDB(1,"NULL","modifyUser user is null",loginInfo.getAccount());
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/modifyUser");
        }
        User persons = (User) userService.getUserByUserId(user.getUserid());
        if (persons == null) {
            saveToDB(1,user.getUserid(),"modifyUser user is  not exist",loginInfo.getAccount());
            response.setStatus(ResultEnum.USER_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_EXIST, "", "/users/modifyUser");
        } else {
            saveToDB(1,user.getUserid(),"modifyUser success",loginInfo.getAccount());

            if (user.getAccountType() >= 0) persons.setAccountType(user.getAccountType());
            if (user.getAddress() != null) persons.setAddress(user.getAddress());
            if (user.getCity() != null) persons.setCity(user.getCity());
            if (user.getEpgGroup() != null) persons.setEpgGroup(user.getEpgGroup());
            if (user.getGender() != null) persons.setGender(user.getGender());
            if (user.getIDnumber() != null) persons.setIDnumber(user.getIDnumber());
            if (user.getProvince() != null) persons.setProvince(user.getProvince());
            if (user.getRegion() != null) persons.setRegion(user.getRegion());
            int state = user.getState();
            if ((state == 2) || (state == 3) || (state == 5) || (state == 6) || (state == 7) || (state == 8) || (state == 9))
                persons.setState(2);
            else if ((state == 4) || (state == 10)) persons.setState(3);
            else if (state > 10) {
                response.setStatus(ResultEnum.STATUS_NOT_EXIST.getCode());
                return ResultUtils.error(ResultEnum.STATUS_NOT_EXIST, "", "/users/modifyUser");
            } else
                persons.setState(state);
            user.setUpdateTime(new java.sql.Date(new Date().getTime()));
            if (user.getTelePhone() != null) persons.setTelePhone(user.getTelePhone());
            if (user.getUsername() != null) persons.setUsername(user.getUsername());
            if (user.getStbID() != null) persons.setStbID(user.getStbID());
            if (user.getPassword() != null) persons.setPassword(user.getPassword());
            if (user.getTeamID() > 0) persons.setTeamID(user.getTeamID());
            if (user.getUpdateTime() != null) persons.setUpdateTime(user.getUpdateTime());
            else {
                persons.setUpdateTime(new Date());
            }

            if (user.getCarrier() >= 0) persons.setCarrier(user.getCarrier());
            if (user.getTradeFlag() >= 0) persons.setTradeFlag(user.getTradeFlag());
            if (user.getMAC() != null) persons.setMAC(user.getMAC());
            if (user.getFee() >= 0) persons.setFee(user.getFee());
            if (user.getSPID() != null) persons.setSPID(user.getSPID());
            if (user.getProductList() != null) persons.setProductList(user.getProductList());
            if (user.getUserType() >= 0) persons.setUserType(user.getUserType());
            redisUtils.set(user.getUserid().toLowerCase(),user.getState());
            userService.save(persons);
            return ResultUtils.success(JSONObject.toJSONString(user), "/users/modifyUser");
        }
    }

    @ApiOperation(value = "修改用户状态", notes = "修改用户状态")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功!"),
            @ApiResponse(code = 239, message = "userid为空!"),
            @ApiResponse(code = 233, message = "用户不存在!")})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userid", value = "主键id", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "state", value = "状态", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "spid", value = "平台标识", required = false, paramType = "query", dataType = "String")
    })
    @PutMapping(value = "/modifyUserState")
    public ResponseMessage modifyUserState(HttpServletRequest request,HttpServletResponse response,
                                           @RequestParam(value = "userid", required = true) String userid,
                                           @RequestParam(value = "state", required = true) int state,
                                           @RequestParam(value = "spid", required = false) String spid) throws Exception {
        System.out.println("modifyUserState:userid=" + userid + " state=" + state + " spid=" + spid);
        LoginInfo loginInfo = getLoginInfo(request);
        if (userid.isEmpty() || (userid == null)) {
            saveToDB(2,"","modifyUserState userid is null",loginInfo.getAccount());
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/modifyUserState");
        }
        User user = userService.getUserByUserId(userid);
        if (user == null) {
            saveToDB(2,userid,"modifyUserState userid not exist",loginInfo.getAccount());
            response.setStatus(ResultEnum.USER_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_EXIST, "", "/users/modifyUserState");
        } else {

            if (spid != null) user.setSPID(spid);

            if ((state == 2) || (state == 3) || (state == 5) || (state == 6) || (state == 7) || (state == 8) || (state == 9)){
                user.setState(2);
            }
            else if ((state == 4) || (state == 10)) {
                    user.setState(3);
            }
            else if (state > 10) {
                saveToDB(2,userid,"modifyUserState status not exist",loginInfo.getAccount());
                response.setStatus(ResultEnum.STATUS_NOT_EXIST.getCode());
                return ResultUtils.error(ResultEnum.STATUS_NOT_EXIST, "", "/users/modifyUserState");
            } else
                user.setState(state);
            saveToDB(2,userid,"modifyUserState status success!",loginInfo.getAccount());

            user.setUpdateTime(new java.sql.Date(new Date().getTime()));
            redisUtils.set(user.getUserid().toLowerCase(),user.getState());
            userService.save(user);
            return ResultUtils.success("修改用户状态成功", "/users/modifyUserState");
        }
    }

    @ApiOperation(value = "修改用户状态,参数为json格式", notes = "修改用户状态,参数为json格式")

    @PutMapping(value = "/modifyUserStatus")
    public ResponseMessage modifyUserState(HttpServletRequest request,HttpServletResponse response,
                                           @RequestBody JSONObject params) throws Exception {

        System.out.println("modifyUserState:params=" + params);
        LoginInfo loginInfo = getLoginInfo(request);
        String userid = params.getString("userid");
        String spid = params.getString("spdi");
        int state = Integer.parseInt(params.getString("state"));
        if (userid.isEmpty() || (userid == null)) {
            saveToDB(2,"","modifyUserStatus userid is null",loginInfo.getAccount());
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/modifyUserStatus");
        }
        User user = userService.getUserByUserId(userid);
        if (user == null) {
            saveToDB(2,userid,"modifyUserStatus userid not exist",loginInfo.getAccount());
            response.setStatus(ResultEnum.USER_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_EXIST, "", "/users/modifyUserStatus");
        } else {

            if (spid != null) user.setSPID(spid);

            if ((state == 2) || (state == 3) || (state == 5) || (state == 6) || (state == 7) || (state == 8) || (state == 9))
                user.setState(2);
            else if ((state == 4) || (state == 10)) user.setState(3);
            else if (state > 10) {
                saveToDB(2,userid,"modifyUserStatus status not exist",loginInfo.getAccount());
                response.setStatus(ResultEnum.STATUS_NOT_EXIST.getCode());
                return ResultUtils.error(ResultEnum.STATUS_NOT_EXIST, "", "/users/modifyUserStatus");
            } else
                user.setState(state);
            saveToDB(2,userid,"modifyUserStatus success",loginInfo.getAccount());

            user.setUpdateTime(new java.sql.Date(new Date().getTime()));
            redisUtils.set(user.getUserid().toLowerCase(),user.getState());
            userService.save(user);
            return ResultUtils.success("修改用户状态成功", "/users/modifyUserStatus");
        }
    }

    @ApiOperation(value = "增加白名单", notes = "增加白名单")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功!"),
            @ApiResponse(code = 233, message = "用户不存在!"),
            @ApiResponse(code = 239, message = "userid为空!"),
            @ApiResponse(code = 235, message = "用户在黑名单里!"),
            @ApiResponse(code = 234, message = "用户在白名单里!"),
            @ApiResponse(code = 256, message = "未有登录信息!")})
    @RequestMapping(value = "/addWhitelist/{userid}", method = RequestMethod.POST)
    public ResponseMessage addWhiteList(HttpServletRequest request,HttpServletResponse response, @PathVariable(value = "userid") String userid) throws Exception {
        System.out.println("add Whitelist:userid=" + userid);
        LoginInfo loginInfo = getLoginInfo(request);
        if (loginInfo.getAccount() ==null){
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/users/addWhiteList");
        }

            if (userid.isEmpty() || (userid == null)) {
            ActionLog log = new ActionLog();
            log.setActionID(3);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID("");
            log.setResult("addWhiteList useris is null");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/addWhiteList");
        }
        User user = userService.getUserByUserId(userid);
        if (user == null) {
            ActionLog log = new ActionLog();
            log.setActionID(3);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("addWhiteList user is not exist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_EXIST, "", "/users/addWhiteList");
        }
        if (user.getState() >= 20) {
            ActionLog log = new ActionLog();
            log.setActionID(3);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("addWhiteList user in blacklist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_IN_BLACKLIST.getCode());
            return ResultUtils.error(ResultEnum.USER_IN_BLACKLIST, "", "/users/addWhiteList");
        } else if (user.getState() >= 10) {
            ActionLog log = new ActionLog();
            log.setActionID(3);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("addWhiteList user in whitelist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_IN_WHITELIST.getCode());
            return ResultUtils.error(ResultEnum.USER_IN_WHITELIST, "", "/users/addWhiteList");
        } else {
            ActionLog log = new ActionLog();
            log.setActionID(3);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("addWhiteList success");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            user.setState(user.getState() + 10);
            redisUtils.set(user.getUserid().toLowerCase(),user.getState());
            userService.logSave(log);
            userService.save(user);
            return ResultUtils.success("增加白名单成功", "/users/addWhiteList");
        }
    }

    @ApiOperation(value = "删除白名单", notes = "删除白名单")
    @ApiResponses(value = {@ApiResponse(code = 233, message = "用户不存在!"),
            @ApiResponse(code = 200, message = "成功!"),
            @ApiResponse(code = 239, message = "userid为空!"),
            @ApiResponse(code = 236, message = "用户没在白名单里!"),
            @ApiResponse(code = 256, message = "未有登录信息!")})
    @RequestMapping(value = "/deleteWhitelist/{userid}", method = RequestMethod.DELETE)
    //@RequestMapping(value = "user/{id}", method = RequestMethod.DELETE)
    public ResponseMessage deleteWhiteList(HttpServletRequest request,HttpServletResponse response, @PathVariable(value = "userid") String userid) throws Exception {
        System.out.println("delete whiteList:userid=" + userid);
        LoginInfo loginInfo = getLoginInfo(request);
        if (loginInfo.getAccount() ==null){
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/users/addWhiteList");
        }
        if (userid.isEmpty() || (userid == null)) {
            ActionLog log = new ActionLog();
            log.setActionID(4);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("deleteWhiteList userid is null");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/deleteWhiteList");
        }
        User user = userService.getUserByUserId(userid);
        System.out.println(user);
        if (user == null) {
            ActionLog log = new ActionLog();
            log.setActionID(4);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("deleteWhiteList user not exist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_EXIST, "", "/users/deleteWhiteList");
        }
        if (user.getState() >= 20) {
            ActionLog log = new ActionLog();
            log.setActionID(4);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("deleteWhiteList user in blacklist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_IN_BLACKLIST.getCode());
            return ResultUtils.error(ResultEnum.USER_IN_BLACKLIST, "", "/users/deleteWhiteList");
        } else if (user.getState() >= 10) {
            ActionLog log = new ActionLog();
            log.setActionID(4);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(user.getUserid());
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            log.setResult("delWhiteList success!");
            user.setState(user.getState() - 10);
            userService.logSave(log);
            redisUtils.set(user.getUserid().toLowerCase(),user.getState());
            userService.save(user);
            return ResultUtils.success("删除白名单成功", "/users/deleteWhiteList");
        } else {
            ActionLog log = new ActionLog();
            log.setActionID(4);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("deleteWhiteList user not in whitelist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_NOT_IN_WHITELIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_IN_WHITELIST, "", "/users/deleteWhiteList");
        }
    }

    @ApiOperation(value = "增加黑名单", notes = "增加黑名单")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功!"),
            @ApiResponse(code = 233, message = "用户不存在!"),
            @ApiResponse(code = 239, message = "userid为空!"),
            @ApiResponse(code = 235, message = "用户在黑名单里!"),
            @ApiResponse(code = 234, message = "用户在白名单里!"),
            @ApiResponse(code = 256, message = "未有登录信息!")})
    @RequestMapping(value = "/addBlacklist/{userid}", method = RequestMethod.POST)
    public ResponseMessage addBlackList(HttpServletRequest request,HttpServletResponse response, @PathVariable(value = "userid") String userid) throws Exception {
        System.out.println("add Black User :" + userid);
        LoginInfo loginInfo = getLoginInfo(request);
        if (loginInfo.getAccount() ==null){
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/users/addWhiteList");
        }
        if (userid == null) {
            ActionLog log = new ActionLog();
            log.setActionID(8);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID("");
            log.setResult("addBlackList userid is null");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/addBlackList");
        }
        User user = userService.getUserByUserId(userid);
        if (user == null) {
            ActionLog log = new ActionLog();
            log.setActionID(8);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("addBlackList user not exist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_EXIST, "", "/users/addBlackList");
        }
        if (user.getState() >= 20) {
            ActionLog log = new ActionLog();
            log.setActionID(8);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("addBlackList user in blacklist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_IN_BLACKLIST.getCode());
            return ResultUtils.error(ResultEnum.USER_IN_BLACKLIST, "", "/users/addBlackList");
        } else if (user.getState() >= 10) {
            ActionLog log = new ActionLog();
            log.setActionID(8);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("addBlackList user in whitelist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_IN_WHITELIST.getCode());
            return ResultUtils.error(ResultEnum.USER_IN_WHITELIST, "", "/users/addBlackList");
        } else {
            ActionLog log = new ActionLog();
            log.setActionID(8);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth 6-globalauth 7-globalstatus 8-add Black list 9-delete Black List
            log.setUserID(user.getUserid());
            log.setResult("addBlackList success");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            user.setState(user.getState() + 20);
            userService.logSave(log);
            redisUtils.set(user.getUserid().toLowerCase(),user.getState());
            userService.save(user);
            return ResultUtils.success("增加黑名单成功", "/users/addBlackList");
        }
    }

    @ApiOperation(value = "删除黑名单", notes = "删除黑名单")
    @ApiResponses(value = {@ApiResponse(code = 233, message = "用户不存在!"),
            @ApiResponse(code = 200, message = "成功!"),
            @ApiResponse(code = 239, message = "userid为空!"),
            @ApiResponse(code = 236, message = "用户没在黑名单里!"),
            @ApiResponse(code = 256, message = "未有登录信息!")})
    @RequestMapping(value = "/deleteBlacklist/{userid}", method = RequestMethod.DELETE)
    //@RequestMapping(value = "user/{id}", method = RequestMethod.DELETE)
    public ResponseMessage deleteBlackList(HttpServletRequest request,HttpServletResponse response, @PathVariable(value = "userid") String userid) throws Exception {
        System.out.println("delete Black List:" + userid);
        LoginInfo loginInfo = getLoginInfo(request);
        if (loginInfo.getAccount() ==null){
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/users/addWhiteList");
        }
        if (userid == null) {
            ActionLog log = new ActionLog();
            log.setActionID(9);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("delBlackList userid is null");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/deleteBlackList");
        }
        User user = userService.getUserByUserId(userid);
        System.out.println(user);
        if (user == null) {
            ActionLog log = new ActionLog();
            log.setActionID(9);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("delBlackList user not exist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_EXIST, "", "/users/deleteBlackList");
        }
        if ((user.getState() >= 10) && (user.getState() < 20)) {
            ActionLog log = new ActionLog();
            log.setActionID(9);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("delBlackList user in whitelist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_IN_WHITELIST.getCode());
            return ResultUtils.error(ResultEnum.USER_IN_WHITELIST, "", "/users/deleteBlackList");
        } else if (user.getState() >= 20) {
            ActionLog log = new ActionLog();
            log.setActionID(9);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth 6-globalauth 7-add Black list 8-delete Black List
            log.setUserID(user.getUserid());
            log.setResult("delBlackList success");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            user.setState(user.getState() - 20);
            userService.logSave(log);
            redisUtils.set(user.getUserid().toLowerCase(),user.getState());
            userService.save(user);
            return ResultUtils.success("删除黑名单成功", "/users/deleteBlackList");
        } else {
            ActionLog log = new ActionLog();
            log.setActionID(9);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult("user in blacklist");
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(loginInfo.getAccount());
            userService.logSave(log);
            response.setStatus(ResultEnum.USER_NOT_IN_BLACKLIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_IN_BLACKLIST, "", "/users/deleteBlackList");
        }
    }

    @CrossOrigin
    @ApiOperation(value = "认证", notes = "认证")
    @ApiResponses(value = {@ApiResponse(code = 233, message = "用户不存在!"),
            @ApiResponse(code = 200, message = "成功!"),
            @ApiResponse(code = 231, message = "未知错误!"),
            @ApiResponse(code = 235, message = "用户在黑名单里!"),
            @ApiResponse(code = 240, message = "用户未激活"),
            @ApiResponse(code = 241, message = "用户停机"),
            @ApiResponse(code = 242, message = "用户拆机")
    })
    @RequestMapping(value = "/Auth/{userid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMessage auth(HttpServletRequest request,HttpServletResponse response, @PathVariable(value = "userid") String userid) throws Exception {
        System.out.println("auth:userid=" + userid);
        Date today = new Date();

        if (("false").equals(redisUtils.get("GolableSwitch")))
            GolableSwitch = false;
        else
            GolableSwitch = true;
        if (("false").equals(redisUtils.get("noactive")))
            noactive = false;
        else
            noactive = true;

        if (("false").equals(redisUtils.get("suspend")))
            suspend = false;
        else
            suspend = true;

        if (("false").equals(redisUtils.get("terminated")))
            terminated = false;
        else
            terminated = true;
        ;
        if (today.getDate() != lastAuthDate.getDate()) {
            redisUtils.set("authOfToday", 0);
            redisUtils.set("unauthOfToday",0);
            lastAuthDate = today;
            System.out.println("Today is " + lastAuthDate);
        }
        int st = -1;
        if (userid !=null){
            if (redisUtils.get(userid)!=null){
                st= Integer.parseInt(redisUtils.get(userid).toString());
            }
            else{
                User user = userService.getUserByUserId(userid);
                if (user ==null)
                    st= -1;
                else
                    st=user.getState();
                    redisUtils.set(userid.toLowerCase(),st);
            }
        }

        JSONObject msg = new JSONObject();

        if (userid.isEmpty() || (userid == null)) {
            redisUtils.incr(" unauthOfToday", 1);
            LoginInfo loginInfo = getLoginInfo(request);
            saveToDB(5,"","AUTH USERID IS NULL",loginInfo.getAccount());
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/Auth");
        } else if (st==-1) {
            redisUtils.incr(" unauthOfToday", 1);
            LoginInfo loginInfo = getLoginInfo(request);
            saveToDB(5,userid,"AUTH USER NOT EXIST",loginInfo.getAccount());
            response.setStatus(ResultEnum.USER_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_EXIST, "", "/users/Auth");
        } else if ((!GolableSwitch)) {
            redisUtils.incr("authOfToday", 1);
            LoginInfo loginInfo = getLoginInfo(request);
            saveToDB(5,userid,"AUTH SWITCH CLOSE",loginInfo.getAccount());
            msg.put("GolableSwitch", GolableSwitch);
            msg.put("userid", userid);
            return ResultUtils.success(msg.toString(), "/users/Auth");
        } else if ((st==1)|| ((st >= 10) && (st < 20))) {
            redisUtils.incr("authOfToday", 1);
            LoginInfo loginInfo = getLoginInfo(request);
            saveToDB(5,userid,"AUTH SUCCESS",loginInfo.getAccount());

            msg.put("state", st);
            msg.put("userid", userid);
            return ResultUtils.success(msg.toString(), "/users/Auth");
        } else if (st >= 20) {
            redisUtils.incr("unauthOfToday", 1);
            LoginInfo loginInfo = getLoginInfo(request);
            saveToDB(5,userid,"AUTH USER IN BLACKLIST",loginInfo.getAccount());
            response.setStatus(ResultEnum.USER_IN_BLACKLIST.getCode());
            return ResultUtils.error(ResultEnum.USER_IN_BLACKLIST, "", "/users/Auth");
        } else if ((st == 0) && noactive) {
            redisUtils.incr("unauthOfToday", 1);
            LoginInfo loginInfo = getLoginInfo(request);
            saveToDB(5,userid,"AUTH USER IN NOACTIVE",loginInfo.getAccount());

            response.setStatus(ResultEnum.USER_IS_NOACTIVE.getCode());
            return ResultUtils.error(ResultEnum.USER_IS_NOACTIVE, "", "/users/Auth");
        } else if ((st == 2) && suspend) {
            redisUtils.incr("unauthOfToday", 1);

            LoginInfo loginInfo = getLoginInfo(request);
            saveToDB(5,userid,"AUTH USER IS SUSPEND",loginInfo.getAccount());

            response.setStatus(ResultEnum.USER_IS_SUSPEND.getCode());
            return ResultUtils.error(ResultEnum.USER_IS_SUSPEND, "", "/users/Auth");
        } else if ((st == 3) && terminated) {
            redisUtils.incr("unauthOfToday", 1);
            LoginInfo loginInfo = getLoginInfo(request);
            saveToDB(5,userid,"AUTH USER IS TERMINATED",loginInfo.getAccount());

            response.setStatus(ResultEnum.USER_IS_TERMINATED.getCode());
            return ResultUtils.error(ResultEnum.USER_IS_TERMINATED, "", "/users/Auth");
        }
        LoginInfo loginInfo = getLoginInfo(request);
        saveToDB(5,userid,"AUTH USER IS UNKNOWN_ERROR",loginInfo.getAccount());

        redisUtils.incr("unauthOfToday", 1);
        response.setStatus(ResultEnum.UNKNOWN_ERROR.getCode());
        return ResultUtils.error(ResultEnum.UNKNOWN_ERROR, userid, "/users/Auth");
    }

    @ApiOperation(value = "查询用户信息", notes = "查询用户信息")
    @ApiResponses(value = {@ApiResponse(code = 255, message = "帐号不存在!"),
            @ApiResponse(code = 200, message = "成功!")})
    @RequestMapping(value = "getUser/{userid}", method = RequestMethod.GET)
    public ResponseMessage getUser(HttpServletRequest request,HttpServletResponse response, @PathVariable(value = "userid") String userid) {
        response.setHeader("Content-Type", "application/json");
        System.out.println("getUser:userid=" + userid);
        //content-type为application/x-json;charset=UTF-8
        if (userid.isEmpty() || (userid == null)) {
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/getUser");
        }
        User user = userService.getUserByUserId(userid);
        if (user == null) {
            response.setStatus(ResultEnum.USER_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_EXIST, "", "/users/getUser");
        } else {
            response.setStatus(ResultEnum.SUCCESS.getCode());
            return ResultUtils.success(JSONObject.toJSONString(user), "/users/getUser");
        }
    }
    @ApiOperation(value = "查询一组用户信息", notes = "查询一组用户信息")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功!"),
                        @ApiResponse(code = 255, message = "帐号不存在!"),
                        @ApiResponse(code = 256, message = "未有登录信息!")})
    @RequestMapping(value = "getUserByString/{userIdString}", method = RequestMethod.GET)
    public ResponseMessage getUserByString(HttpServletRequest request,HttpServletResponse response, @PathVariable(value = "userIdString") String[] userIdString) {
        response.setHeader("Content-Type", "application/json");
        //content-type为application/x-json;charset=UTF-8
        List<String> idList  =  new ArrayList<>();
        for (int i=0;i<userIdString.length;i++) {
            ((ArrayList) idList).add(userIdString[i]);
            System.out.println("getUser:userIdString=" + userIdString[i]);
        }
        if (userIdString.length==0 || (userIdString == null)) {
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/getUser");
        }
        List<User> user = userService.getUserByUserIdString( idList);
        if (user == null) {
            response.setStatus(ResultEnum.USER_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.USER_NOT_EXIST, "", "/users/getUser");
        } else {
            response.setStatus(ResultEnum.SUCCESS.getCode());
            return ResultUtils.success(JSONObject.toJSONString(user), "/users/getUser");
        }
    }
    @CrossOrigin
    @ApiOperation(value = "全局认证开关", notes = "全局认证开关")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功!"),
            @ApiResponse(code = 256, message = "未有登录信息!")})
    @RequestMapping(value = "/GlobalAuth/{authSwitch}", method = RequestMethod.PUT)
    public ResponseMessage globalauth(HttpServletRequest request,HttpServletResponse response, @PathVariable(value = "authSwitch") boolean authSwitch) throws Exception {
        System.out.println("GlobalSwitch!");
        LoginInfo loginInfo = getLoginInfo(request);
        if (loginInfo.getAccount() ==null){
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/users/addWhiteList");
        }
        JSONObject msg = new JSONObject();

        if (authSwitch) {
            GolableSwitch = true;
            redisUtils.set("GolableSwitch","true");
        }
        else{
            redisUtils.set("GolableSwitch","false");
            GolableSwitch = false;
        }
        msg.put("GolableSwitch", GolableSwitch);
        ActionLog log = new ActionLog();
        log.setActionID(6);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth 6-globalauth
        log.setOptime(new java.sql.Date(new Date().getTime()));
        log.setOperator(loginInfo.getAccount());
        log.setResult("change GolableSwitch success!");
        userService.logSave(log);
        return ResultUtils.success(msg.toString(), "/users/GlobalAuth/");
    }
    @ApiOperation(value = "查询全局认证开关", notes = "查询全局认证开关")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功!")})
    @RequestMapping(value = "/GlobalAuth", method = RequestMethod.GET)
    public ResponseMessage getglobalauth(HttpServletRequest request,HttpServletResponse response) throws Exception {
        System.out.println("GlobalSwitch!");
        LoginInfo loginInfo = getLoginInfo(request);
        JSONObject msg = new JSONObject();
        msg.put("GolableSwitch", GolableSwitch);
        return ResultUtils.success(msg.toString(), "/users/GlobalAuth");
    }
    @CrossOrigin
    @ApiOperation(value = "全局用户状态认证开关", notes = "全局用户状态认证开关")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功!"),
            @ApiResponse(code = 256, message = "未有登录信息!")})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "noactive", value = "未激活认证开关", required = true, paramType = "query", dataType = "Boolean"),
            @ApiImplicitParam(name = "suspend", value = "停机认证开关", required = true, paramType = "query", dataType = "Boolean"),
            @ApiImplicitParam(name = "terminated", value = "拆机认证开关", required = true, paramType = "query", dataType = "Boolean")
    })
    @RequestMapping(value = "/GlobalStatus", method = RequestMethod.PUT)
    public ResponseMessage globalstatus(HttpServletRequest request,HttpServletResponse response,
                                        @RequestParam(value = "noactive", required = true) Boolean _noactive,
                                        @RequestParam(value = "suspend", required = true) Boolean _suspend,
                                        @RequestParam(value = "terminated", required = true) Boolean _terminated) throws Exception {
        System.out.println("GlobalStatus:");
        LoginInfo loginInfo = getLoginInfo(request);
        if (loginInfo.getAccount() ==null){
            response.setStatus(ResultEnum.NOT_LOGIN.getCode());
            return ResultUtils.error(ResultEnum.NOT_LOGIN, "", "/users/addWhiteList");
        }
        JSONObject msg = new JSONObject();
        if (_noactive){
            noactive = true;
            redisUtils.set("noactive","true");
        }
        else{
            noactive = false;
            redisUtils.set("noactive","false");
        }

        if (_suspend){
            suspend = true;
            redisUtils.set("suspend","true");
        }
        else{
            suspend = false;
            redisUtils.set("suspend","false");
        }
        if (_terminated){
            terminated = true;
            redisUtils.set("terminated","true");
        }
        else {
            terminated = false;
            redisUtils.set("terminated","false");
        }

        msg.put("noactive", noactive);
        msg.put("suspend", suspend);
        msg.put("terminated", terminated);

        ActionLog log = new ActionLog();
        log.setActionID(7);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth 6-globalauth 7-globalstatus 8-addblacklist 9-delete black list
        log.setResult(msg.toString());
        log.setOptime(new java.sql.Date(new Date().getTime()));
        log.setOperator(loginInfo.getAccount());
        log.setResult("change GlobalStatus success!");
        userService.logSave(log);
        return ResultUtils.success(msg.toString(), "/users/GlobalStatus/");
    }

    @ApiOperation(value = "查询全局用户状态认证开关", notes = "查询全局用户状态认证开关")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "成功!")})

    @RequestMapping(value = "/GlobalStatus", method = RequestMethod.GET)
    public ResponseMessage globalstatus(HttpServletRequest request,HttpServletResponse response) throws Exception {
        System.out.println("GlobalStatus:");
        JSONObject msg = new JSONObject();
        if ("true".equals(redisUtils.get("noactive"))) noactive = true;
        else noactive = false;
        if ("true".equals(redisUtils.get("suspend"))) suspend = true;
        else suspend = false;
        if ("true".equals(redisUtils.get("terminated"))) terminated = true;
        else terminated = false;
        msg.put("noactive", noactive);
        msg.put("suspend", suspend);
        msg.put("terminated", terminated);
        System.out.println("GlobalStatus:" + msg.toString());

        return ResultUtils.success(msg.toString(), "/users/GlobalStatus");
    }

    @ApiOperation(value = "按页返回用户信息", notes = "按页返回用户信息")
    @RequestMapping(value = "/findUserByState", method = RequestMethod.GET)
    public Page<User> findUserByState(@RequestParam(defaultValue = "0") Integer page,
                                      @RequestParam(defaultValue = "5") Integer size,@RequestParam(defaultValue = "4") Integer status) {
        if (null == page) {
            page = 0;
        }
        if (null == size) {
            size = 10;
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "userid");
        Page<User> users = userService.findUserNoCriteria(page, size,status);
        return users;
    }
    @ApiOperation(value = "查询白名单用户", notes = "查询白名单用户")
    @RequestMapping(value = "/listWhiteUser", method = RequestMethod.GET)
    public List<User> listWhiteUser(@RequestParam(defaultValue = "0") Integer page,
                              @RequestParam(defaultValue = "5") Integer size) throws Exception {
        if (null == page) {
            page = 0;
        }
        if (null == size) {
            size = 10;
        }
        List<User> list = userService.listWhiteUser(page,size);
        return list;
    }


    @ApiOperation(value = "导出白名单用户", notes = "导出白名单用户")
    @RequestMapping(value = "/exportWhiteExcel", method = RequestMethod.GET)
    public void exportWhiteExcel(HttpServletRequest request,HttpServletResponse response) throws Exception {

        //查询数据，实际可通过传过来的参数当条件去数据库查询，在此我就用空集合（数据）来替代
        List<User> list = userService.listWhiteAllUser();
        //创建poi导出数据对象
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();

        //创建sheet页
        SXSSFSheet sheet = sxssfWorkbook.createSheet("sheet_name");
        //创建表头
        SXSSFRow headRow = sheet.createRow(0);
        //设置表头信息
        headRow.createCell(0).setCellValue("no.");
        headRow.createCell(1).setCellValue("userid");
        headRow.createCell(2).setCellValue("username");
        headRow.createCell(3).setCellValue("state");
        // 遍历上面数据库查到的数据
        int x = 1;
        for (User pm : list) {
            //序号

            //填充数据
            SXSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
            //序号
            dataRow.createCell(0).setCellValue(x);
            //看你实体类在进行填充
            dataRow.createCell(1).setCellValue(pm.getUserid());
            dataRow.createCell(2).setCellValue(pm.getUsername());
            dataRow.createCell(3).setCellValue(pm.getState());
            x++;
        }

        // 下载导出
        String filename = "导出excel表格名字";
        // 设置头信息
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel");
        //一定要设置成xlsx格式
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".xlsx", "UTF-8"));
        //创建一个输出流
        ServletOutputStream outputStream = response.getOutputStream();
        //写入数据
        sxssfWorkbook.write(outputStream);

        // 关闭
        outputStream.close();
        sxssfWorkbook.close();
    }
    @ApiOperation(value = "查询黑名单用户", notes = "查询黑名单用户")
    @RequestMapping(value = "/listBlackUser", method = RequestMethod.GET)
    public List<User> listBlackUser(@RequestParam(defaultValue = "0") Integer page,
                                    @RequestParam(defaultValue = "5") Integer size) throws Exception {


        if (null == page) {
            page = 0;
        }
        if (null == size) {
            size = 10;
        }
        return userService.listBlackUser(page,size);
    }
    @ApiOperation(value = "导出黑名单用户", notes = "导出黑名单用户")
    @RequestMapping(value = "/exportBlackExcel", method = RequestMethod.GET)
    public void exportBlackExcel(HttpServletRequest request,HttpServletResponse response) throws Exception {

        //查询数据，实际可通过传过来的参数当条件去数据库查询，在此我就用空集合（数据）来替代
        List<User> list = userService.listBlackAllUser();
        //创建poi导出数据对象
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();

        //创建sheet页
        SXSSFSheet sheet = sxssfWorkbook.createSheet("sheet_name");
        //创建表头
        SXSSFRow headRow = sheet.createRow(0);
        //设置表头信息
        headRow.createCell(0).setCellValue("no.");
        headRow.createCell(1).setCellValue("userid");
        headRow.createCell(2).setCellValue("username");
        headRow.createCell(3).setCellValue("state");
        // 遍历上面数据库查到的数据
        int x = 1;
        for (User pm : list) {
            //序号

            //填充数据
            SXSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
            //序号
            dataRow.createCell(0).setCellValue(x);
            //看你实体类在进行填充
            dataRow.createCell(1).setCellValue(pm.getUserid());
            dataRow.createCell(2).setCellValue(pm.getUsername());
            dataRow.createCell(3).setCellValue(pm.getState());
            x++;
        }

        // 下载导出
        String filename = "导出excel表格名字";
        // 设置头信息
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel");
        //一定要设置成xlsx格式
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".xlsx", "UTF-8"));
        //创建一个输出流
        ServletOutputStream outputStream = response.getOutputStream();
        //写入数据
        sxssfWorkbook.write(outputStream);

        // 关闭
        outputStream.close();
        sxssfWorkbook.close();
    }

    @ApiOperation(value = "获得认证统计数据", notes = "获得认证统计数据")
    @RequestMapping(value = "/exportAuthResult", method = RequestMethod.GET)
    public JSONObject exportAuthResult(HttpServletRequest request,HttpServletResponse response) throws Exception {

        JSONObject msg = new JSONObject();
        msg.put("authOfToday",redisUtils.get("authOfToday"));
        msg.put("unauthOfToday",redisUtils.get("unauthOfToday"));
        msg.put("unAuthTotal",redisUtils.get("unAuthTotal"));
        return msg;
    }

    @CrossOrigin
    @ApiOperation(value = "获得用户日志数据", notes = "获得用户日志数据")
    @RequestMapping(value = "/listActionLog/{userid}/{size}", method = RequestMethod.GET)
    public List<ActionLog> listActionLog( @PathVariable(value = "userid") String userid,@PathVariable(value = "size") int size) throws Exception {
        return userService.listActionLog(userid,size);
    }
    @ApiOperation(value = "同步用户信息", notes = "同步用户信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功!"),
            @ApiResponse(code = 239, message = "userid为空!")})
    @RequestMapping(value = "syncUser", method = RequestMethod.POST)
    public ResponseMessage syncUser(HttpServletRequest request,HttpServletResponse response, @RequestBody User user) throws Exception {
        System.out.println("syncUser:" + JSONObject.toJSONString(user));
        LoginInfo loginInfo = getLoginInfo(request);

        if (user == null) {
            saveToDB(0,"", " user is null",loginInfo.getAccount());

            response.setStatus(ResultEnum.USER_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USER_IS_NULL, "", "/users/syncUser");
        }
        if ((user.getUserid() == null) || (user.getUserid().isEmpty())) {
            saveToDB(0,"", " user is null",loginInfo.getAccount());
            response.setStatus(ResultEnum.USERID_IS_NULL.getCode());
            return ResultUtils.error(ResultEnum.USERID_IS_NULL, "", "/users/syncUser");
        }
        User persons = (User) userService.getUserByUserId(user.getUserid());
        int actionid = 1;
        String tmp ="modify";
        if (persons == null) {
            persons = new User();
            actionid = 0;
            tmp = "create";
            persons.setUserid(user.getUserid());
        }
        saveToDB(actionid,user.getUserid(),tmp + " User success",loginInfo.getAccount());

        if (user.getAccountType() >= 0) persons.setAccountType(user.getAccountType());
        if (user.getAddress() != null) persons.setAddress(user.getAddress());
        if (user.getCity() != null) persons.setCity(user.getCity());
        if (user.getEpgGroup() != null) persons.setEpgGroup(user.getEpgGroup());
        if (user.getGender() != null) persons.setGender(user.getGender());
        if (user.getIDnumber() != null) persons.setIDnumber(user.getIDnumber());
        if (user.getProvince() != null) persons.setProvince(user.getProvince());
        if (user.getRegion() != null) persons.setRegion(user.getRegion());
        int state = user.getState();
        if ((state == 2) || (state == 3) || (state == 5) || (state == 6) || (state == 7) || (state == 8) || (state == 9))
            persons.setState(2);
        else if ((state == 4) || (state == 10)) persons.setState(3);
        else if (state > 10) {
            response.setStatus(ResultEnum.STATUS_NOT_EXIST.getCode());
            return ResultUtils.error(ResultEnum.STATUS_NOT_EXIST, "", "/users/syncUser");
        } else
            persons.setState(state);
        user.setUpdateTime(new java.sql.Date(new Date().getTime()));
        if (user.getTelePhone() != null) persons.setTelePhone(user.getTelePhone());
        if (user.getUsername() != null) persons.setUsername(user.getUsername());
        if (user.getStbID() != null) persons.setStbID(user.getStbID());
        if (user.getPassword() != null) persons.setPassword(user.getPassword());
        if (user.getTeamID() > 0) persons.setTeamID(user.getTeamID());
        if (user.getUpdateTime() != null) persons.setUpdateTime(user.getUpdateTime());
        else {
            persons.setUpdateTime(new Date());
        }
        if (user.getCarrier() >= 0) persons.setCarrier(user.getCarrier());
        if (user.getTradeFlag() >= 0) persons.setTradeFlag(user.getTradeFlag());
        if (user.getMAC() != null) persons.setMAC(user.getMAC());
        if (user.getFee() >= 0) persons.setFee(user.getFee());
        if (user.getSPID() != null) persons.setSPID(user.getSPID());
        if (user.getProductList() != null) persons.setProductList(user.getProductList());
        if (user.getUserType() >= 0) persons.setUserType(user.getUserType());
        redisUtils.set(user.getUserid().toLowerCase(),user.getState());
        userService.save(persons);
        return ResultUtils.success(JSONObject.toJSONString(user), "/users/syncUser");
    }

    void saveToDB(int actionid,String userid,String result,String operator) throws Exception {
        if (saveInDB) {
            ActionLog log = new ActionLog();
            log.setActionID(actionid);  //0-create 1-update user info 2-update user state 3-add whitelist 4-delete whitelist 5-auth
            log.setUserID(userid);
            log.setResult(result);
            log.setOptime(new java.sql.Date(new Date().getTime()));
            log.setOperator(operator);
            userService.logSave(log);
        }
    }
}