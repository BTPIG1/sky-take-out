package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * 菜品相关接口
 * */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    /*
    * 新增菜品
    * @param dishDTO
    * @return
    * */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /*
     * 菜品分页查询
     * @param DishPageQueryDTO
     * return
     * */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    // 查询类需要返回数据，因此要指定泛型<PageResult>
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：参数为：{}", dishPageQueryDTO);
        // 动态给sql添加limited语句 （页数、大小）
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
     * 批量删除菜品
     * @param ids
     * return
     * */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    // 查询类需要返回数据，因此要指定泛型<PageResult>
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除菜品：参数为：{}", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }


}
