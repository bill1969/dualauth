package com.viewstar.dualauth.jpa.dao;

import com.viewstar.dualauth.jpa.api.Employee;
import com.viewstar.dualauth.jpa.api.Invitecode;
import com.viewstar.dualauth.jpa.api.User;
import org.hibernate.annotations.SQLUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository  extends JpaRepository<Employee, Long> {
    @Query("from Employee where mobile =:mo")
    public Employee getByMobile(String mo);

    @Query("from Employee where email =:em")
    public Employee getByEmail(String em);

    @Query("from Employee where account =:ac")
    public Employee checkAccount(String ac);

    @Query("from Employee where account =:account and passwd=:password")
    public Employee loginByAccount(String account,String password);

    @Query("from Employee where mobile =:mobile and passwd=:password")
    public  Employee loginByMobile(String mobile,String password);
}
