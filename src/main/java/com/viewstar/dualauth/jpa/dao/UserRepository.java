package com.viewstar.dualauth.jpa.dao;

import com.viewstar.dualauth.jpa.api.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 我们这里只需要写接口，不需要写实现，spring boot会帮忙自动实现
     *
     * */

    @Query("from User where userid =:userid ")
    public User getUserByUserID(String userid);

    @Query("from User where userid in (:idList) ")
    public List<User> findByUseridIn(List<String> idList);

    @Query("from User where state >=10 and state <20 ")
    public List<User> listWhiteAllUser();

    @Query("from User where state <20 and state >=10 ")
    public List<User> listWhiteUser(Pageable page);

    @Query("from User where state >= 20 ")
    public List<User> listBlackAllUser();
    @Query("from User where state >= 20 ")
    public List<User> listBlackUser(Pageable page);

}