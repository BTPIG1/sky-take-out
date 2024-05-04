package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /*
    * 插入员工数据
    * @param employee
    * 单表查询不用写入mapper.xml文件,使用注解方式
    * */
    @Insert("insert into employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)"
            + "values " +
            "(#{name}, #{username}, #{password}, #{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     * 动态sql
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /*
     * 根据id动态更新属性
     * @param employee
     * return
     * */
    void update(Employee employee);

    /*
     * 根据id查询员工信息
     * @param id
     * return
     * 单表查询不用写入mapper.xml文件,使用注解方式
    * */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}
