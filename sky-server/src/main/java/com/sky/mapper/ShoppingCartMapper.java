package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {


    /*
    * 动态条件查询
    * */
    List<ShoppingCart> List(ShoppingCart shoppingCart);

    /*
    * 根据id修改购物车商品数量
    * */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /*
    * 插入购物车数据
    * */
    @Insert("insert into shopping_cart ( name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) VALUES (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);
}
