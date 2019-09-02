package com.viewstar.dualauth.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ResponseMessage<T> {
    //错误码
    private int status;
    //信息描述
    private String error;
    private String message;
    private Timestamp timestamp;
    private String path;
    //具体的信息内容
}
