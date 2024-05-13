package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.utils.AliOssUtil;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /*
     * 新增菜品和对应的口味
     * @param dishDTO
     * 因为这里涉及到两张表的操作，所以需要使用事务（同成同败）
     * */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表插入1条数据
        dishMapper.insert(dish);

        // 因为dish中没有赋值id属性，因此需要修改上一条insert语句！！（具体见里面）
        // 获取insert语句生成的主键值
        Long dishId = dish.getId();

        // 向菜品口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishId);
            });
            // 向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /*
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     * */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // select * from dish limit 0,10
        // 开始分页查询
        /*
         * PageHelper做了一个什么操作？创建了一个Page对象（保存了页码、记录数），然后保存到了ThreadLocal中！！
         * */
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        long total = page.getTotal();
        List<DishVO> records = page.getResult();

        return new PageResult(total, records);
    }

    /*
     * 批量删除菜品
     * @param ids
     * */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断当前菜品是否能够删除---是否存在起售中菜品？？
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断当前菜品是否能够删除---是否被套餐关联了？？
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds!=null && setmealIds.size()>0){
            // 当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // // 删除菜品表中的菜品数据
        // for (Long id : ids) {
        //     dishMapper.deleteById(id);
        //     // 删除菜品中关联的口味数据
        //     dishFlavorMapper.deleteByDishId(id);
        // }

        /*
        * 优化上述代码，
        * */
        // 根据菜品ids集合批量删除菜品数据
        // sql:delete from dish where id in (?,?,?)
        dishMapper.deleteByIds(ids);

        // 根据菜品ids集合批量删除关联的口味数据
        // sql:delete from dish_flavor where dish_id in (?,?,?)
        dishFlavorMapper.deleteByDishIds(ids);

    }

    /*
     * 根据id查询菜品和对应的口味
     * @param id
     * @return
     * 因为涉及到两张表所以得用事务
     * */
    @Transactional
    public DishVO getByIdWithFlavor(Long id) {
        // 根据id查询菜品
        Dish dish = dishMapper.getById(id);

        // 根据菜品id查询对应的口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        // 封装数据到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /*
     * 根据id修改菜品基本信息和对应得口味信息
     * @param dishDTO
     * */
    public void updateWithFlavor(DishDTO dishDTO) {
        // 修改菜品表中的数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // 修改菜品口味表中的数据
        // 先删除原来的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        // 再插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /*
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     * */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder().categoryId(categoryId).build();
        return dishMapper.list(dish);
    }

    /*
    * 启用禁用菜品
    * @param status
    * @param id
    * */
    public void startOrStop(Integer status, Long id) {

        // 先判断当前菜品是否被套餐关联了
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(id);
        if(setmealIds!=null && setmealIds.size()>0){
            // Current dish is associated with a set meal, handle accordingly
            throw new SetmealEnableFailedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }
}
