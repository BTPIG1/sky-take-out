package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;



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

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
         // 统计从begin到end的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.isEqual(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 根据日期查询订单数量以及每天有效订单数量
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 查询当天的订单数量
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer orderCount = orderMapper.countByMap(map);

            // 查询当天的有效订单数量
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            if(validOrderCount == null){
                validOrderCount = 0;
            }
            if (orderCount == null) {
                orderCount = 0;
            }

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        // 计算总订单数、有效订单数、订单完成率
        Integer totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        Integer validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        Double orderCompletionRate=0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount * 1.0 / totalOrderCount;
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        // 查询销量排名前10的商品
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        Map map = new HashMap();
        map.put("begin", beginTime);
        map.put("end", endTime);

        List<String> NameList = new ArrayList<>();
        List<Integer> NumberList = new ArrayList<>();
        // select od.name,sum(od.number) as number from order_detail od,orders o where od.order_id=o.id and o.status=5 and o.create_time between #{begin} and #{end} group by od.name order by number desc limit 10
        List<GoodsSalesDTO> goodsSalesDTOList = orderDetailMapper.getTop(map);
        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOList) {
            NameList.add(goodsSalesDTO.getName());
            NumberList.add(goodsSalesDTO.getNumber());
        }
        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(NameList, ","))
                .numberList(StringUtils.join(NumberList, ","))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据--查询最近30天的营业数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            // 获取excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            // 获取表格文件的sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            /*
            * 填充概览数据
            * */
            // 填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间："+ begin + "至" + end);
            // 填充数据--营业额
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            // 填充数据--订单完成率
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            // 填充数据--新增用户
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            // 填充数据--有效订单
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            // 填充数据--客单价
            sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());

            /*
            * 填充明细数据
            * */
            for (int i=0;i<30;i++){
                LocalDate date = begin.plusDays(i);
                businessDataVO=workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                // 创建行
                XSSFRow row = sheet.createRow(i + 7);
                // 填充数据--时间
                row.createCell(1).setCellValue(date.toString());
                // 填充数据--营业额
                row.createCell(2).setCellValue(businessDataVO.getTurnover());
                // 填充数据--有效订单
                row.createCell(3).setCellValue(businessDataVO.getValidOrderCount());
                // 填充数据--订单完成率
                row.createCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                // 填充数据--客单价
                row.createCell(5).setCellValue(businessDataVO.getUnitPrice());
                // 填充数据--新增用户
                row.createCell(6).setCellValue(businessDataVO.getNewUsers());
            }

            //3. 通过输出刘将Excel文件下载到客户端
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //4. 关闭资源
            out.close();
            excel.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
