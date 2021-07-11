package com.project.scenarioanalysis.services;

import com.project.scenarioanalysis.model.CsvBarModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CsvService {

    List<CsvBarModel> importBars(final String symbol) throws Exception;
}
