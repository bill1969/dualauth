package com.viewstar.dualauth.jpa.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Table(name = "Employee")
public class Employee implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String name;//姓名
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String account;//账号
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String passwd;//密码
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String mobile;//手机号
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String email;//邮箱
    private Integer status;//状态 1开启  0 关闭
    private Integer loginType;//登录状态 1未登录 2账号登录 3手机登录
    private  String inviteCode;
    private Long roleId;//角色id
    @Column(length = 128)
    @Length(min = 0,max=128)
    private String photoUrl;//头像url
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//注册时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;//最后登录时间
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String lastLoginIp;//登录IP
}
