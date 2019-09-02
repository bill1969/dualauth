package com.viewstar.dualauth.entity;

import java.util.Date;

public class ResultUtils {
    public static ResponseMessage success(String msg,String path) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(ResultEnum.SUCCESS.getCode());
        responseMessage.setError("");
        responseMessage.setMessage(msg);
        responseMessage.setTimestamp(new java.sql.Timestamp(new Date().getTime()));
        responseMessage.setPath(path);
        return responseMessage;
    }

    public static ResponseMessage created(String msg,String path) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(ResultEnum.CREATED.getCode());
        responseMessage.setError("");
        responseMessage.setMessage(msg);
        responseMessage.setTimestamp(new java.sql.Timestamp(new Date().getTime()));
        responseMessage.setPath(path);
        return responseMessage;
    }
    /**
     * 操作失败返回的消息
     * @param code
     * @param msg
     * @return
     */
    public static ResponseMessage error(int code,String msg,String Path) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(code);
        responseMessage.setError(msg);
        responseMessage.setTimestamp(new java.sql.Timestamp(new Date().getTime()));
        responseMessage.setPath(Path);
        return responseMessage;
    }

    /**
     * 操作失败返回消息，对error的重载
     * @param resultEnum
     * @return
     */
    public static ResponseMessage error(ResultEnum resultEnum,String msg,String path){
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(resultEnum.getCode());
        responseMessage.setError(resultEnum.getMsg());
        responseMessage.setMessage(msg);
        responseMessage.setTimestamp(new java.sql.Timestamp(new Date().getTime()));
        responseMessage.setPath(path);

        return responseMessage;
    }
}