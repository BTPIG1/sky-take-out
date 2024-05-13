package com.sky.controller.admin;

import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
* 套餐管理
* */
@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐管理")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /*
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * return
     * */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    // 查询类需要返回数据，因此要指定泛型<PageResult>
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询：参数为：{}", setmealPageQueryDTO);
        // 动态给sql添加limited语句 （页数、大小）
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
    * 删除套餐
    * @param ids
    * return
    * */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result delete(@RequestParam List<Long> ids){
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /*
    * 根据id查询套餐
    * @param id
    * return
    * */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /*
    * 修改套餐
    * @param setmealDTO
    * return
    * */
    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /*
    * 起售禁售商品
    * @param id
    * @param status
    * return
    * */
    @PostMapping("status/{status}")
    @ApiOperation("起售禁售商品")
    public Result startOrStop(@RequestParam Long id, @PathVariable Integer status){
        setmealService.startOrStop(status,id);
        return Result.success();
    }

}
