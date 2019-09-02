package com.viewstar.dualauth.jpa.api;

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ActionLog")
public class ActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(length = 32)
    private String UserID;
    @CreatedDate
    private Date Optime;
    @Column(length = 2)
    private int ActionID;
    @Column(length = 32)
    private String result;
    @Column(length = 32)
    private String Operator;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public Date getOptime() {
        return Optime;
    }

    public void setOptime(Date optime) {
        Optime = optime;
    }

    public int getActionID() {
        return ActionID;
    }

    public void setActionID(int actionID) {
        ActionID = actionID;
    }

    public String getOperator() {
        return Operator;
    }

    public void setOperator(String operator) {
        Operator = operator;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
