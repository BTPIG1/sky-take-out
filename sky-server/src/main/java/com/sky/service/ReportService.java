package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    /*
    * 统计营业额
    * */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);


    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
}
