package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类业务层
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 统计从begin到end的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.isEqual(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 根据日期查询营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 查询当天的营业额
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            if (turnover == null) {
                turnover = 0.0;
            }
            turnoverList.add(turnover);
        }

        
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 统计从begin到end的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.isEqual(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 根据日期查询用户数量以及每天新增数量
        List<Integer> totalUserList = new ArrayList<>();
        //查询第一天的用户总数
        LocalDateTime beginTotalTime = LocalDateTime.of(dateList.get(0), LocalTime.MIN);
        Map mapTotal = new HashMap();
        mapTotal.put("end", beginTotalTime);
        Integer totalUser = userMapper.countByMap(mapTotal);
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 查询当天的新增用户数量
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            newUserList.add(userMapper.countByMap(map));
        }
        // totalUserList = 第一天之前的总人数 + 累增人数
        totalUserList.add(newUserList.get(0)+totalUser);
        for (int i = 1; i < dateList.size(); i++) {
            totalUserList.add(totalUserList.get(i-1)+newUserList.get(i));
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }


}
