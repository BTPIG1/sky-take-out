package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShoppingCartController")
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端购物车相关接口")
@Slf4j
public class ShoppingCartController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private ShoppingCartService shoppingCartService;


    /*
    * 添加购物车
    * @param shoppingCartDTO
    * @return
    * */
    @PostMapping("/add")
    @ApiOperation(value = "添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车,商品信息为：{}", shoppingCartDTO);


        // 根据dishId查询菜品信息 或 setmealId查询套餐信息
        shoppingCartService.addShoppingCart(shoppingCartDTO);

        return Result.success();
    }

}
