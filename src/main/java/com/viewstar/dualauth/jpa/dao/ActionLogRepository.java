package com.viewstar.dualauth.jpa.dao;

import com.viewstar.dualauth.jpa.api.ActionLog;
import com.viewstar.dualauth.jpa.api.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface  ActionLogRepository extends JpaRepository<ActionLog, Long> {
    @Query("from ActionLog where userid=:userid ")
    public List<ActionLog> listActionLog(String userid, Pageable page) ;

    @Query("from ActionLog where operator=:accountName ")
    public List<ActionLog> listOperationLog(String accountName, Pageable page) ;
}