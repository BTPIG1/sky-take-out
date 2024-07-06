package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.vo.OrderSubmitVO;

import java.util.List;

public interface OrderService {


    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
}
