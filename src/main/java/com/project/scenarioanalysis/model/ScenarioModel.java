package com.project.scenarioanalysis.model;

import com.bpodgursky.jbool_expressions.Expression;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScenarioModel {

    private final List<SequenceModel> sequenceModels;

    private final String title;

    private final int numberOfSequences;

    private final Expression<String> when;

    private final Expression<String> then;

    private List<List<SequenceModel>> occurrences;

    private List<List<SequenceModel>> positiveOccurrences;

    private int numberOfOccurrences;

    private int numberOfPositiveOccurrences;

    private double percentPositive;

}
