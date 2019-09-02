package com.viewstar.dualauth.jpa.core;

import com.viewstar.dualauth.config.DataSourceConfig;
import com.viewstar.dualauth.config.TargetDateSource;
import com.viewstar.dualauth.entity.LoginEnum;
import com.viewstar.dualauth.jpa.api.ActionLog;
import com.viewstar.dualauth.jpa.api.Employee;
import com.viewstar.dualauth.jpa.api.Invitecode;
import com.viewstar.dualauth.jpa.api.User;
import com.viewstar.dualauth.jpa.dao.ActionLogRepository;
import com.viewstar.dualauth.jpa.dao.EmployeeRepository;
import com.viewstar.dualauth.jpa.dao.InvitecodeRepository;
import com.viewstar.dualauth.jpa.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImp implements UserService{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ActionLogRepository actionLogRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private InvitecodeRepository invitecodeRepository;

    @Override
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public User getUserByUserId(String userid) {
        User user = userRepository.getUserByUserID(userid);
        return user;
    }
    @Override
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public List<User> getUserByUserIdString(List<String> idList) {
        return userRepository.findByUseridIn(idList);
    }

    @Override
    public Page<User> findUserNoCriteria(Integer page, Integer size,Integer status ) {
        Pageable pageable = new PageRequest(page, size, Sort.Direction.ASC, "userid");
        switch(status){
            case 0:
            case 1:
            case 2:
            case 3:
            case 10:
            case 11:
            case 12:
            case 13:
            case 20:
            case 21:
            case 22:
            case 23:
                User user = new User();
                user.setState(status);
                return userRepository.findAll(Example.of(user),pageable);
            default:
                return userRepository.findAll(pageable);
        }
    }
    @Override
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.WRITE_DATASOURCE_KEY)
    public String save(User entity) throws Exception {
        if (entity.getUserid() != null) {
            User perz = userRepository.save(entity);
            return perz.getUserid();
        }
        User perz = userRepository.save(entity);
        return perz.getUserid();
    }

    @Override
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.WRITE_DATASOURCE_KEY)
    public Long logSave(ActionLog entity) throws Exception {
        if (entity.getId() != null) {
            ActionLog perz = actionLogRepository.save(entity);
            return perz.getId();
        }
        ActionLog perz = actionLogRepository.save(entity);
        return perz.getId();
    }

    @Override
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.WRITE_DATASOURCE_KEY)
    public Long employeeSave(Employee entity) throws Exception {
        employeeRepository.save(entity);
        return entity.getId();
    }
    @Override
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.WRITE_DATASOURCE_KEY)
    public Boolean employeeDel(Long id) throws Exception {
        if (employeeRepository.findById(id)!=null){
            employeeRepository.deleteById(id);
            return true;
        }
        else
            return false;
    }
    @Override
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public Optional<Employee> getById(Long  id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee;
    }

    @Override
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public Employee checkMobile(String  mobile) {
        Employee user = employeeRepository.getByMobile(mobile);
        return user;
    }

    @Override
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public Employee checkEmail(String  email) {
        Employee user = employeeRepository.getByEmail(email);
        return user;
    }


    @Override
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public Employee checkAccount(String acount) {
        Employee user = employeeRepository.checkAccount(acount);
        return user;
    }

    @Override
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public Employee checkLogin(String name,String pass,int type) {
        if(type==LoginEnum.ACCOUNT_LOGIN.getValue()){
            Employee rlt0=employeeRepository.loginByAccount(name,pass);
            return rlt0;
        }

        if(type==LoginEnum.MOBILE_LOGIN.getValue()){
            Employee rlt1=employeeRepository.loginByMobile(name,pass);
            return rlt1;
        }
        return null;
    }

    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    @Override
    public List<User> listWhiteAllUser() {
        return userRepository.listWhiteAllUser();
    }

    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    @Override
    public List<User> listWhiteUser(Integer page, Integer size) {
        Pageable pageable = new PageRequest(page, size, Sort.Direction.ASC, "userid");
        return userRepository.listWhiteUser(pageable);
    }

    @Override
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public List<User> listBlackUser(Integer page, Integer size) {
        Pageable pageable = new PageRequest(page, size, Sort.Direction.ASC, "userid");
        return userRepository.listBlackUser(pageable);
    }

    @Override
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public List<User> listBlackAllUser() {
        return userRepository.listBlackAllUser();
    }
    @Override
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public List<ActionLog> listActionLog(String userid,int size) {
        Pageable pageable = new PageRequest(0, size, Sort.Direction.DESC, "id");
        return actionLogRepository.listActionLog(userid,pageable);
    }

    @Override
    @Transactional
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public List<ActionLog> listOperationLog(String accountName,int size) {
        Pageable pageable = new PageRequest(0, size, Sort.Direction.DESC, "id");
        return actionLogRepository.listOperationLog(accountName,pageable);
    }

    @Override
    @TargetDateSource(dataSource = DataSourceConfig.READ_DATASOURCE_KEY)
    public Invitecode checkInviteCode(String code){
        return invitecodeRepository.checkInviteCode(code);
    }
    @Override
    @TargetDateSource(dataSource = DataSourceConfig.WRITE_DATASOURCE_KEY)
    public void updateInviteCode(String userid,String code){
        invitecodeRepository.updateInviteCode(userid,code);
    }
}
