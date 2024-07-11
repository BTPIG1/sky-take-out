package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /*
     * 每15分钟判断用户付款状态是否超时
     * */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());

        // select * from orders where status = ? and order_time < ?
        LocalDateTime orderTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, orderTime);

        // 判断orderList是否为空
        if(orderList.isEmpty()){
            return;
        }
        // 遍历orderList并更新订单状态
        for (Orders orders : orderList) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("超时未支付,订单自动取消");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }

    /*
    * 处理派送中订单,每日凌晨2点完成所有派送中订单
    * */
    @Scheduled(cron = "0 2 * * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理派送中订单:{}", LocalDateTime.now());

        // 挑选出状态为派送中的订单
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-120));
        // 判断orderList是否为空
        if(orderList.isEmpty()){
            return;
        }
        // 遍历orderList并更新订单状态
        for (Orders orders : orderList) {
            orders.setStatus(Orders.COMPLETED);
            orders.setDeliveryTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }

}
