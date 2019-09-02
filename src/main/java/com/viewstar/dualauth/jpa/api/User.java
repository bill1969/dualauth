package com.viewstar.dualauth.jpa.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "User")
public class User {
    @Id
    @Column(name = "userid",length=34)
    @NotNull @NotBlank
    private String userid;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String FatherAccount;
    private int AccountType;
    @Length(min = 0,max=32)
    @Column(length = 32)
    private String password;
    @Column(length = 8)
    @Length(min = 0,max=8)
    private String SPID;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String StbID;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String MAC;
    @Column(length = 2)
    private int Carrier;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String Province;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String City;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String Region;
    @Column(length = 2)
    private int TradeFlag;
    @Column(length = 2)
    private int TeamID;
    @Column(length = 2)
    private int UserType;
    private int Fee;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String EpgGroup;
    private int State;
    @Column(length = 128)
    @Length(min = 0,max=128)
    private String ProductList;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activeTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    @Column(length = 64)
    @Length(min = 0,max=64)
    private String Username;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String TelePhone;
    @Column(length = 128)
    @Length(min = 0,max=128)
    private String Address;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String IDnumber;
    @Column(length = 6)
    @Length(min = 0,max=6)
    private String Gender;

    public String getTelePhone() {
        return TelePhone;
    }

    public void setTelePhone(String telePhone) {
        TelePhone = telePhone;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getIDnumber() {
        return IDnumber;
    }

    public void setIDnumber(String IDnumber) {
        this.IDnumber = IDnumber;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getFatherAccount() {
        return FatherAccount;
    }

    public void setFatherAccount(String fatherAccount) {
        FatherAccount = fatherAccount;
    }

    public int getAccountType() {
        return AccountType;
    }

    public void setAccountType(int accountType) {
        AccountType = accountType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSPID() {
        return SPID;
    }

    public void setSPID(String SPID) {
        this.SPID = SPID;
    }

    public String getStbID() {
        return StbID;
    }

    public void setStbID(String stbID) {
        StbID = stbID;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public int getCarrier() {
        return Carrier;
    }

    public void setCarrier(int carrier) {
        Carrier = carrier;
    }

    public String getProvince() {
        return Province;
    }

    public void setProvince(String province) {
        Province = province;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getRegion() {
        return Region;
    }

    public void setRegion(String region) {
        Region = region;
    }

    public int getTradeFlag() {
        return TradeFlag;
    }

    public void setTradeFlag(int tradeFlag) {
        TradeFlag = tradeFlag;
    }

    public int getTeamID() {
        return TeamID;
    }

    public void setTeamID(int teamID) {
        TeamID = teamID;
    }

    public int getUserType() {
        return UserType;
    }

    public void setUserType(int userType) {
        UserType = userType;
    }

    public int getFee() {
        return Fee;
    }

    public void setFee(int fee) {
        Fee = fee;
    }

    public String getEpgGroup() {
        return EpgGroup;
    }

    public void setEpgGroup(String epgGroup) {
        EpgGroup = epgGroup;
    }

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }

    public String getProductList() {
        return ProductList;
    }

    public void setProductList(String productList) {
        ProductList = productList;
    }

    public Date getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(Date activeTime) {
        this.activeTime = activeTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }
}
