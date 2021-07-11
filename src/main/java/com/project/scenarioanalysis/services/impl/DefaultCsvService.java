package com.project.scenarioanalysis.services.impl;

import com.project.scenarioanalysis.model.CsvBarModel;
import com.project.scenarioanalysis.model.CsvBeanModel;
import com.project.scenarioanalysis.services.CsvService;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DefaultCsvService implements CsvService {


    @Override
    public List<CsvBarModel> importBars(final String symbol) throws Exception {
        Path path = Paths.get(
                ClassLoader.getSystemResource("csv/"+ symbol +".csv").toURI());
        return (List<CsvBarModel>)(Object)CsvBeanModel.buildCsvModels(path, CsvBarModel.class);
    }
}
