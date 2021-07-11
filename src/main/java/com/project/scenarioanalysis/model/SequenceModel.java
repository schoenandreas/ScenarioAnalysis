package com.project.scenarioanalysis.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SequenceModel {

    private List<BarModel> bars;

    private int timeFrameInMinutes;

    private double open;

    private double high;

    private double low;

    private double close;

    private int volume;

    @Override
    public String toString() {
        return "SequenceModel{" +
                "bars=" + bars.toString().replaceAll("Bar","\nBar")+
                ",\ntimeFrameInMinutes=" + timeFrameInMinutes +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                '}';
    }
}
