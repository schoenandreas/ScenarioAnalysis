package com.project.scenarioanalysis.services;

import org.springframework.stereotype.Service;

@Service
public interface AnalysisService {

    void analyseAll(final String symbol) throws Exception;

    void analyseWeekdayHighLowOfWeek(final String symbol) throws Exception;

    void analyseDayTwoAboveBelowDayOneContinuesDayThree(final String symbol) throws Exception;

    void analyseHighLowByTimeOfDay(final String symbol) throws Exception;
}
