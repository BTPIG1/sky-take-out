package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

import java.util.List;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /*
    * 新增员工
    * @param employeeDTO
    * @return
    * */
    void save(EmployeeDTO employeeDTO);

    /*
    * 分页查询
     */
    PageResult pageQuery(EmployeePageQueryDTO employeeQueryDTO);

    /*
    * 根据id启用禁用员工
     */
    void startOrStop(Integer status, Long id);

    /*
     * 根据id查询员工信息
     */
    Employee getById(Long id);


    /*
     * 编辑员工信息
     */
    void update(EmployeeDTO employeeDTO);

}
