package com.project.scenarioanalysis.services;

import com.project.scenarioanalysis.model.ScenarioModel;
import org.springframework.stereotype.Service;

@Service
public interface ScenarioService {

    ScenarioModel executeScenario(final ScenarioModel scenarioModel);

    String getScenarioReport(final ScenarioModel scenarioModel);
}
/*
*
WHEN:

!S1.WEEKDAY==1 AND ( !S3.LOW<!S1.LOW OR !S2.LOW<!S1.LOW )

THEN:

!S4.LOW<!S3.LOW
*
*
* */