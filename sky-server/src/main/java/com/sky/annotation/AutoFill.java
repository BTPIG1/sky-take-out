package com.sky.annotation;
import com.sky.enumeration.OperationType;

import java.lang.annotation.*;
/*
* 自定义注解，用于标识需要进行公共字段自动填充的方法
* */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //数据库操作类型：UPDATE、INSERT
    OperationType value();


}
