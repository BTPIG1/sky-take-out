package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /*
     * 新增员工
     * @param employeeDTO
     * */
    public void save(EmployeeDTO employeeDTO) {

        // 当前线程id
        System.out.println("当前线程id:" + Thread.currentThread().getId());

        //将employeeDTO转换为employee
        Employee employee = new Employee();

//       employee.setName(employeeDTO.getName()); 可以像这样一个个设置但是太麻烦
//       使用BeanUtils.copyProperties()方法可以将一个对象的相同属性值拷贝到另一个对象中
        BeanUtils.copyProperties(employeeDTO, employee);

//       设置employee中其他不同的属性值
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码，默认为123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 设置当前记录创建人的id和修改人id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /*
    * 员工分页查询
    * @param employeeQueryDTO
    * @return
    * */
    public PageResult pageQuery(EmployeePageQueryDTO employeeQueryDTO) {
        // select * from employee limit 0,10
        // 开始分页查询
        /*
        * PageHelper做了一个什么操作？创建了一个Page对象（保存了页码、记录数），然后保存到了ThreadLocal中！！
        * */
        PageHelper.startPage(employeeQueryDTO.getPage(), employeeQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeeQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total, records);
    }

    /*
     *  启用、禁用员工账号
     * @param status
     * @param id
     * return
     * */
    public void startOrStop(Integer status, Long id) {
        // update employee set status = ? where id = ?

        /* 第一种写法
        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);
        */

        // 第二种写法
        Employee employee = Employee.builder().status(status)
                .id(id).build();

        employeeMapper.update(employee);
    }

    /*
     * 根据id查询员工信息
     * @param id
     * @return
     * */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("******");
        return employee;
    }

    /*
     * 编辑员工信息
     * @param id
     * @return
     * 这里实现的是：改了啥就修改啥，因此这里employee对象不需要与新增员工一样初始化所有值
     * */
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置当前记录的修改时间
        employee.setUpdateTime(LocalDateTime.now());

        // 设置当前记录修改人的id
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }

}
