package com.project.scenarioanalysis.services.impl;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Variable;
import com.project.scenarioanalysis.model.ScenarioModel;
import com.project.scenarioanalysis.model.SequenceModel;
import com.project.scenarioanalysis.services.AnalysisService;
import com.project.scenarioanalysis.services.ScenarioService;
import com.project.scenarioanalysis.services.SequenceService;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
@Data
public class DefaultAnalysisService implements AnalysisService {

    private static Logger LOG = LoggerFactory.getLogger(DefaultAnalysisService.class);

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private ScenarioService scenarioService;

    List<SequenceModel> dailySequenceModels;

    @Override
    public void analyseAll(final String symbol) throws Exception {
        init(symbol);
        analyseWeekdayHighLowOfWeek(symbol);
        analyseDayTwoAboveBelowDayOneContinuesDayThree(symbol);
        analyseHighLowByTimeOfDay(symbol);
    }

    @Override
    public void analyseWeekdayHighLowOfWeek(final String symbol) throws Exception {
        init(symbol);
        analyseDayHighOfWeekIsMonday();
        analyseDayHighOfWeekIsTuesday();
        analyseDayHighOfWeekIsWednesday();
        analyseDayHighOfWeekIsThursday();
        analyseDayHighOfWeekIsFriday();
        analyseDayLowOfWeekIsMonday();
        analyseDayLowOfWeekIsTuesday();
        analyseDayLowOfWeekIsWednesday();
        analyseDayLowOfWeekIsThursday();
        analyseDayLowOfWeekIsFriday();
    }

    @Override
    public void analyseDayTwoAboveBelowDayOneContinuesDayThree(final String symbol) throws Exception {
        init(symbol);
        analyseDayTwoAboveDayOneContinuesDayThree(DayOfWeek.MONDAY);
        analyseDayTwoAboveDayOneContinuesDayThree(DayOfWeek.TUESDAY);
        analyseDayTwoAboveDayOneContinuesDayThree(DayOfWeek.WEDNESDAY);
        analyseDayTwoAboveDayOneContinuesDayThree(DayOfWeek.THURSDAY);
        analyseDayTwoAboveDayOneContinuesDayThree(DayOfWeek.FRIDAY);
        analyseDayTwoBelowDayOneContinuesDayThree(DayOfWeek.MONDAY);
        analyseDayTwoBelowDayOneContinuesDayThree(DayOfWeek.TUESDAY);
        analyseDayTwoBelowDayOneContinuesDayThree(DayOfWeek.WEDNESDAY);
        analyseDayTwoBelowDayOneContinuesDayThree(DayOfWeek.THURSDAY);
        analyseDayTwoBelowDayOneContinuesDayThree(DayOfWeek.FRIDAY);
    }

    @Override
    public void analyseHighLowByTimeOfDay(final String symbol) throws Exception {
        init(symbol);
        final SortedMap<Integer,Integer> reachedNumberOfHighsAtTime = new TreeMap<>();
        final SortedMap<Integer,Integer> reachedNumberOfLowsAtTime = new TreeMap<>();
        for (final SequenceModel day: dailySequenceModels){
            final LocalDateTime highTime = sequenceService.getHighTimeForSequence(day);
            final LocalDateTime lowTime = sequenceService.getLowTimeForSequence(day);
            int highTimeKey = Integer.parseInt(""+highTime.getHour() + (highTime.getMinute() < 10 ? "0"+highTime.getMinute() : highTime.getMinute()));
            int lowTimeKey = Integer.parseInt(""+lowTime.getHour() + (lowTime.getMinute() < 10 ? "0"+lowTime.getMinute() : lowTime.getMinute()));
            reachedNumberOfHighsAtTime.put(highTimeKey,1+reachedNumberOfHighsAtTime.getOrDefault(highTimeKey,0));
            reachedNumberOfLowsAtTime.put(lowTimeKey,1+reachedNumberOfLowsAtTime.getOrDefault(lowTimeKey,0));
        }
        final SortedMap<Integer, BigDecimal> reachedNumberOfHighsAtTimePercAcc = sumUpAndPercentMap(reachedNumberOfHighsAtTime);
        final SortedMap<Integer,BigDecimal > reachedNumberOfLowsAtTimePercAcc = sumUpAndPercentMap(reachedNumberOfLowsAtTime);
        LOG.info("Percentage of high reached at time:\n"+reachedNumberOfHighsAtTimePercAcc.toString()+"\n");
        LOG.info("Percentage of low reached at time:\n"+reachedNumberOfLowsAtTimePercAcc.toString()+"\n");
    }

    private SortedMap<Integer,BigDecimal > sumUpAndPercentMap(final SortedMap<Integer,Integer> map){
        final SortedMap<Integer,BigDecimal > result = new TreeMap<>();
        float sum = map.values().stream().reduce(0, Integer::sum);
        int currentvalue= 0;
        for (Map.Entry<Integer,Integer> entry: map.entrySet() ) {
            currentvalue +=entry.getValue();
            final BigDecimal value = new BigDecimal(currentvalue/sum).setScale(3, BigDecimal.ROUND_HALF_UP);
            result.put(entry.getKey(), value);
        }
        return result;
    }

    private void init(final String symbol) throws Exception {
        if(CollectionUtils.isEmpty(dailySequenceModels)){
            final SequenceModel sequenceModel = sequenceService.createSequence(symbol);
            dailySequenceModels = sequenceService.splitSequenceByDays(sequenceModel,symbol);
        }
    }

    private void analyseDayTwoAboveDayOneContinuesDayThree(final DayOfWeek dayOfWeek){
        final String dayLabel = dayOfWeek.toString();
        final int day = dayOfWeek.getValue();
        final String title = "When the day after "+dayLabel+" trades above the lows of "+dayLabel+", the next day trades above the lows of day 2";
        int numberOfSequences = 3;
        final Expression<String> when = And.of(Variable.of("!S1.WEEKDAY=="+day),Variable.of("!S1.LOW<!S2.LOW"));
        final Expression<String> then = Variable.of("!S2.LOW<!S3.LOW");
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }

    private void analyseDayTwoBelowDayOneContinuesDayThree(final DayOfWeek dayOfWeek){
        final String dayLabel = dayOfWeek.toString();
        final int day = dayOfWeek.getValue();
        final String title = "When the day after "+dayLabel+" trades below the highs of "+dayLabel+", the next day trades below the highs of day 2";
        int numberOfSequences = 3;
        final Expression<String> when = And.of(Variable.of("!S1.WEEKDAY=="+day),Variable.of("!S1.HIGH>!S2.HIGH"));
        final Expression<String> then = Variable.of("!S2.HIGH>!S3.HIGH");
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }

    private void analyseDayHighOfWeekIsMonday(){
        final String title = "How often Monday is High of the Week";
        int numberOfSequences = 5;
        final Expression<String> when = Variable.of("!S1.WEEKDAY==1");
        final Expression<String> then = And.of(Variable.of("!S1.HIGH>!S2.HIGH"),Variable.of("!S1.HIGH>!S3.HIGH"),
                                                Variable.of("!S1.HIGH>!S4.HIGH"),Variable.of("!S1.HIGH>!S5.HIGH"));
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }

    private void analyseDayHighOfWeekIsTuesday(){
        final String title = "How often Tuesday is High of the Week";
        int numberOfSequences = 5;
        final Expression<String> when = Variable.of("!S1.WEEKDAY==1");
        final Expression<String> then = And.of(Variable.of("!S2.HIGH>!S1.HIGH"),Variable.of("!S2.HIGH>!S3.HIGH"),
                Variable.of("!S2.HIGH>!S4.HIGH"),Variable.of("!S2.HIGH>!S5.HIGH"));
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }
    private void analyseDayHighOfWeekIsWednesday(){
        final String title = "How often Wednesday is High of the Week";
        int numberOfSequences = 5;
        final Expression<String> when = Variable.of("!S1.WEEKDAY==1");
        final Expression<String> then = And.of(Variable.of("!S3.HIGH>!S1.HIGH"),Variable.of("!S3.HIGH>!S2.HIGH"),
                Variable.of("!S3.HIGH>!S4.HIGH"),Variable.of("!S3.HIGH>!S5.HIGH"));
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }

    private void analyseDayHighOfWeekIsThursday(){
        final String title = "How often Thursday is High of the Week";
        int numberOfSequences = 5;
        final Expression<String> when = Variable.of("!S1.WEEKDAY==1");
        final Expression<String> then = And.of(Variable.of("!S4.HIGH>!S1.HIGH"),Variable.of("!S4.HIGH>!S2.HIGH"),
                Variable.of("!S4.HIGH>!S3.HIGH"),Variable.of("!S4.HIGH>!S5.HIGH"));
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }
    private void analyseDayHighOfWeekIsFriday(){
        final String title = "How often Friday is High of the Week";
        int numberOfSequences = 5;
        final Expression<String> when = Variable.of("!S1.WEEKDAY==1");
        final Expression<String> then = And.of(Variable.of("!S5.HIGH>!S1.HIGH"),Variable.of("!S5.HIGH>!S2.HIGH"),
                Variable.of("!S5.HIGH>!S3.HIGH"),Variable.of("!S5.HIGH>!S4.HIGH"));
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }

    private void analyseDayLowOfWeekIsMonday(){
        final String title = "How often Monday is Low of the Week";
        int numberOfSequences = 5;
        final Expression<String> when = Variable.of("!S1.WEEKDAY==1");
        final Expression<String> then = And.of(Variable.of("!S1.LOW<!S2.LOW"),Variable.of("!S1.LOW<!S3.LOW"),
                Variable.of("!S1.LOW<!S4.LOW"),Variable.of("!S1.LOW<!S5.LOW"));
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }

    private void analyseDayLowOfWeekIsTuesday(){
        final String title = "How often Tuesday is Low of the Week";
        int numberOfSequences = 5;
        final Expression<String> when = Variable.of("!S1.WEEKDAY==1");
        final Expression<String> then = And.of(Variable.of("!S2.LOW<!S1.LOW"),Variable.of("!S2.LOW<!S3.LOW"),
                Variable.of("!S2.LOW<!S4.LOW"),Variable.of("!S2.LOW<!S5.LOW"));
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }

    private void analyseDayLowOfWeekIsWednesday(){
        final String title = "How often Wednesday is Low of the Week";
        int numberOfSequences = 5;
        final Expression<String> when = Variable.of("!S1.WEEKDAY==1");
        final Expression<String> then = And.of(Variable.of("!S3.LOW<!S1.LOW"),Variable.of("!S3.LOW<!S2.LOW"),
                Variable.of("!S3.LOW<!S4.LOW"),Variable.of("!S3.LOW<!S5.LOW"));
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }
    private void analyseDayLowOfWeekIsThursday(){
        final String title = "How often Thursday is Low of the Week";
        int numberOfSequences = 5;
        final Expression<String> when = Variable.of("!S1.WEEKDAY==1");
        final Expression<String> then = And.of(Variable.of("!S4.LOW<!S1.LOW"),Variable.of("!S4.LOW<!S2.LOW"),
                Variable.of("!S4.LOW<!S3.LOW"),Variable.of("!S4.LOW<!S5.LOW"));
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }
    private void analyseDayLowOfWeekIsFriday(){
        final String title = "How often Friday is Low of the Week";
        int numberOfSequences = 5;
        final Expression<String> when = Variable.of("!S1.WEEKDAY==1");
        final Expression<String> then = And.of(Variable.of("!S5.LOW<!S1.LOW"),Variable.of("!S5.LOW<!S2.LOW"),
                Variable.of("!S5.LOW<!S3.LOW"),Variable.of("!S5.LOW<!S4.LOW"));
        final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(dailySequenceModels).
                title(title).numberOfSequences(numberOfSequences).when(when).then(then).build();
        LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
    }
}
