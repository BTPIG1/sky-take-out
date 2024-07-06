package com.sky.mapper;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.vo.OrderSubmitVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /*
     * 批量插入订单明细数据
     * @param orderDetailList
     * @return
     * */
    void insertBatch(List<OrderDetail> orderDetailList);
}
