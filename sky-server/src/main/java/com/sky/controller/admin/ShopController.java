package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "商铺相关接口")
@Slf4j
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /*
    * 设置商铺营业状态
    * @param status
    * @return
    * */
    @PutMapping("/{status}")
    @ApiOperation(value = "设置商铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置商铺营业状态为：{}",status == 1 ? "营业中" : "休息中");
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    /*
    * 查询店铺状态
    * @param
    * @return
    * */
    @GetMapping("/status")
    @ApiOperation(value = "查询商铺营业状态")
    public Result getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("查询商铺营业状态为：{}",status == 1 ? "营业中" : "休息中");
        return Result.success(status);
    }

}
