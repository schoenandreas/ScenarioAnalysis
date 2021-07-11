package com.project.scenarioanalysis.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BarModel {

    private LocalDateTime dateTime;

    private double open;

    private double high;

    private double low;

    private double close;

    private int volume;
}
