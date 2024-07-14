package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;

public interface ReportService {

    /*
    * 统计营业额
    * */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

}
