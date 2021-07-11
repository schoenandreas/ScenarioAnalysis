package com.project.scenarioanalysis.model;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

@Data
public class CsvBarModel extends CsvBeanModel {

    @CsvBindByPosition(position = 0)
    private String time;

    @CsvBindByPosition(position = 1)
    private String open;

    @CsvBindByPosition(position = 2)
    private String high;

    @CsvBindByPosition(position = 3)
    private String low;

    @CsvBindByPosition(position = 4)
    private String close;

    @CsvBindByPosition(position = 5)
    private String volume;
}
