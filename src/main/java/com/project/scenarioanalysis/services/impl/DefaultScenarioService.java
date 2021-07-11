package com.project.scenarioanalysis.services.impl;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.project.scenarioanalysis.model.ScenarioModel;
import com.project.scenarioanalysis.model.SequenceModel;
import com.project.scenarioanalysis.services.ScenarioService;
import com.project.scenarioanalysis.services.SequenceService;
import com.project.scenarioanalysis.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DefaultScenarioService implements ScenarioService {

    @Autowired
    private SequenceService sequenceService;

    @Override
    public ScenarioModel executeScenario(final ScenarioModel scenarioModel) {
        interpretWhen(scenarioModel);
        interpretThen(scenarioModel);
        return scenarioModel;
    }

    @Override
    public java.lang.String getScenarioReport(final ScenarioModel scenarioModel) {
        final List<LocalDateTime> occurrenceDates = scenarioModel.getOccurrences().stream().map(occ -> occ.get(0).getBars().get(0).getDateTime().plusHours(1)).collect(Collectors.toList());
        final List<LocalDateTime> posOccurrenceDates = scenarioModel.getPositiveOccurrences().stream().map(occ -> occ.get(0).getBars().get(0).getDateTime().plusHours(1)).collect(Collectors.toList());
        return "\n####### Report Start #######"+"\n"
                +scenarioModel.getTitle()+"\n"
                +"Occurrences:"+"\n"
                +occurrenceDates.toString()+"\n"
                +"Positive Occurrences:"+"\n"
                +posOccurrenceDates.toString()+"\n"
                +"Stats: "+scenarioModel.getNumberOfPositiveOccurrences()+" of "+scenarioModel.getNumberOfOccurrences()+ " are positive == "+scenarioModel.getPercentPositive()+"\n"
                +"####### Report End #######"+"\n";
    }

    private void interpretWhen(final ScenarioModel scenarioModel){
        final int numberOfSequences = scenarioModel.getNumberOfSequences();
        final List<SequenceModel> sequenceModels = scenarioModel.getSequenceModels();
        final Expression<java.lang.String> expression = scenarioModel.getWhen();
        final Set<java.lang.String> variables = expression.getAllK();
        final List<List<SequenceModel>> occurrences = new ArrayList<>();
        for (int i = 0; i < sequenceModels.size()-numberOfSequences; i++) {
            final List<SequenceModel> currentSequences = sequenceModels.subList(i,i+numberOfSequences);
            Expression<java.lang.String> resolvedExpression = expression;
            for (final java.lang.String v : variables) {
                resolvedExpression = RuleSet.assign(resolvedExpression, Collections.singletonMap(v, resolveVariable(v, currentSequences)));
            }
            if (Boolean.valueOf(resolvedExpression.toString())){
                occurrences.add(currentSequences);
            }
        }
        scenarioModel.setOccurrences(occurrences);
        scenarioModel.setNumberOfOccurrences(occurrences.size());
    }

    private void interpretThen(final ScenarioModel scenarioModel){
        final List<List<SequenceModel>> occurrences = scenarioModel.getOccurrences();
        final Expression<java.lang.String> expression = scenarioModel.getThen();
        final Set<java.lang.String> variables = expression.getAllK();
        final List<List<SequenceModel>> positiveOccurrences = new ArrayList<>();
        for (final List<SequenceModel> currentSequences : occurrences ) {
            Expression<java.lang.String> resolvedExpression = expression;
            for (final java.lang.String v : variables) {
                resolvedExpression = RuleSet.assign(resolvedExpression, Collections.singletonMap(v, resolveVariable(v, currentSequences)));
            }
            if (Boolean.valueOf(resolvedExpression.toString())){
                positiveOccurrences.add(currentSequences);
            }
        }
        scenarioModel.setPositiveOccurrences(positiveOccurrences);
        scenarioModel.setNumberOfPositiveOccurrences(positiveOccurrences.size());
        scenarioModel.setPercentPositive((double)scenarioModel.getNumberOfPositiveOccurrences()/scenarioModel.getNumberOfOccurrences());
    }

    private boolean resolveVariable(final java.lang.String variable, final List<SequenceModel> sequences){
        if(variable.contains("WEEKDAY")){
            int queryDay = Character.getNumericValue(variable.charAt(variable.indexOf("==")+2));
            int sequenceIndex = Character.getNumericValue(variable.charAt(variable.indexOf(Constant.SEQUENCE_MARKER)+2));
            return sequenceService.getDayOfWeek(sequences.get(sequenceIndex-1)).getValue() == queryDay;
        }else if(variable.contains("<") || variable.contains(">")){
            final java.lang.String comparator = variable.contains("<") ? "<" : ">";
            final java.lang.String left = variable.substring(0,variable.indexOf(comparator));
            final java.lang.String right = variable.substring(variable.indexOf(comparator)+1);
            final double leftValue = resolveSequenceValue(left,sequences);
            final double rightValue = resolveSequenceValue(right, sequences);
            return comparator.equals("<") ? leftValue<rightValue : leftValue>rightValue;
        }
        throw new IllegalArgumentException("Invalid variable "+variable+"!");
    }

    private double resolveSequenceValue(final java.lang.String sequenceValue, final List<SequenceModel> sequences){
        int sequenceIndex = Character.getNumericValue(sequenceValue.charAt(sequenceValue.indexOf(Constant.SEQUENCE_MARKER)+2));
        final SequenceModel sequenceModel = sequences.get(sequenceIndex-1);
        final java.lang.String requestedValue = sequenceValue.substring(sequenceValue.indexOf(".")+1).toUpperCase();
        switch (requestedValue){
            case "OPEN":
                return sequenceModel.getOpen();
            case "HIGH":
                return sequenceModel.getHigh();
            case "LOW":
                return sequenceModel.getLow();
            case "CLOSE":
                return sequenceModel.getClose();
        }
        throw new IllegalArgumentException("Invalid sequence value "+sequenceValue+" in variable!");
    }

}
