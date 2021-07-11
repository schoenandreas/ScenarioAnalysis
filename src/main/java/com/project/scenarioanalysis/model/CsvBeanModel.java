package com.project.scenarioanalysis.model;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.project.scenarioanalysis.util.pojo.CsvTransfer;
import lombok.Data;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Data
public class CsvBeanModel {

    public static List<CsvBeanModel> buildCsvModels(Path path, Class clazz) throws Exception {
        CsvTransfer csvTransfer = new CsvTransfer();
        ColumnPositionMappingStrategy ms = new ColumnPositionMappingStrategy();
        ms.setType(clazz);

        Reader reader = Files.newBufferedReader(path);
        CsvToBean cb = new CsvToBeanBuilder(reader)
                .withType(clazz)
                .withMappingStrategy(ms)
                .build();

        csvTransfer.setCsvList(cb.parse());
        reader.close();
        return csvTransfer.getCsvList();
    }
}
