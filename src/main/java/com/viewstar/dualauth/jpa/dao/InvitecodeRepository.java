package com.viewstar.dualauth.jpa.dao;

import com.viewstar.dualauth.jpa.api.Invitecode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface InvitecodeRepository extends JpaRepository<Invitecode, Long> {

    @Query("from Invitecode where code =:code and userid is null")
    public Invitecode checkInviteCode(String code);

    @Query(value = "update invitecode set userid =:userid where code =:code",nativeQuery = true)
    @Modifying
    @Transactional
    public  void updateInviteCode(String userid,String code);
}
