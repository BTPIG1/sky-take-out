package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController("reportController")
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /*
    * 根据起始时间统计营业额
    * */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("统计营业额")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end){
        log.info("营业额数据统计:{},{}",begin,end);

        TurnoverReportVO turnoverReportVO = reportService.getTurnoverStatistics(begin, end);
        return Result.success(turnoverReportVO);
    }

    /*
    * 用户统计
    * */
    @GetMapping("/userStatistics")
    @ApiOperation("统计用户")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end){
        log.info("用户数据统计:{},{}",begin,end);
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    /*
    * 订单统计
    * */
    @GetMapping("/ordersStatistics")
    @ApiOperation("统计订单")
    public Result<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end){
        log.info("订单数据统计:{},{}",begin,end);
        return Result.success(reportService.getOrderStatistics(begin, end));
    }

    /*
    * 销量排名
    * */
    @GetMapping("/top10")
    @ApiOperation("销量排名")
    public Result<SalesTop10ReportVO> orderOverview(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end){
        log.info("销量排名:{},{}",begin,end);
        return Result.success(reportService.getSalesTop10(begin, end));
    }

    /*
    * 工作台数据导出
    * */
    @GetMapping("/export")
    @ApiOperation("工作台数据导出")
    public void export(HttpServletResponse response){
        reportService.exportBusinessData(response);
    }


}
