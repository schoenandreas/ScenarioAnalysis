package com.project.scenarioanalysis.services;

import com.project.scenarioanalysis.model.BarModel;
import com.project.scenarioanalysis.model.SequenceModel;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Service
public interface SequenceService {

    SequenceModel createSequence(final List<BarModel> bars);

    SequenceModel createSequence(final String symbol) throws Exception;

    List<SequenceModel> splitSequenceByDays(final SequenceModel sequenceModel,final String symbol);

    DayOfWeek getDayOfWeek(final SequenceModel sequenceModel);

    LocalDateTime getHighTimeForSequence(final SequenceModel sequenceModel);

    LocalDateTime getLowTimeForSequence(final SequenceModel sequenceModel);
}
