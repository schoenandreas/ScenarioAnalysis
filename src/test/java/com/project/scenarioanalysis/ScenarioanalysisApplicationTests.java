package com.project.scenarioanalysis;

import com.project.scenarioanalysis.model.CsvBarModel;
import com.project.scenarioanalysis.services.CsvService;
import com.project.scenarioanalysis.util.Constant;
import lombok.Setter;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@Log
@Setter
@SpringBootTest
class ScenarioanalysisApplicationTests {

    private CsvService csvService;

    @Test
    void testCSV(){
        List<CsvBarModel> bars = null;
        try {
            bars = csvService.importBars(Constant.GBPUSDM15);
            log.info(bars.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
    }

}
