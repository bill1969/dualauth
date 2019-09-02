package com.viewstar.dualauth.jpa.core;


import com.viewstar.dualauth.config.DataSourceConfig;
import com.viewstar.dualauth.config.TargetDateSource;
import com.viewstar.dualauth.jpa.api.ActionLog;
import com.viewstar.dualauth.jpa.api.Employee;
import com.viewstar.dualauth.jpa.api.Invitecode;
import com.viewstar.dualauth.jpa.api.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserService {

    @TargetDateSource(dataSource = DataSourceConfig.WRITE_DATASOURCE_KEY)
    String save(User entity) throws Exception;

    @TargetDateSource(dataSource = DataSourceConfig.WRITE_DATASOURCE_KEY)
    Long logSave(ActionLog entity) throws Exception;

    @TargetDateSource(dataSource = DataSourceConfig.WRITE_DATASOURCE_KEY)
    Long employeeSave(Employee entity) throws Exception;

    @TargetDateSource(dataSource = DataSourceConfig.WRITE_DATASOURCE_KEY)
    Boolean employeeDel(Long id) throws Exception;

    @TargetDateSource(dataSource = DataSourceConfig.WRITE_DATASOURCE_KEY)
    void updateInviteCode(String userid,String code) throws Exception;

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    User getUserByUserId(String userid);

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    Invitecode checkInviteCode(String code);

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    List<User> getUserByUserIdString(List<String> idList);

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    Page<User> findUserNoCriteria(Integer page, Integer size,Integer status);

    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    List<User> listWhiteAllUser();
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    List<User> listWhiteUser(Integer page, Integer size);
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    List<User> listBlackAllUser();
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    List<User> listBlackUser(Integer page, Integer size);

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    Optional<Employee> getById(Long id);

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    Employee checkMobile(String mobile);

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    Employee checkEmail(String email);

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    Employee checkAccount(String account);

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    Employee checkLogin(String name,String pass,int type);

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    List<ActionLog> listActionLog(String userid, int size);

    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    List<ActionLog> listOperationLog(String accountName, int size);
}
