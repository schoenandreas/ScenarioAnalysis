package com.project.scenarioanalysis.converters;

import com.project.scenarioanalysis.model.BarModel;
import com.project.scenarioanalysis.model.CsvBarModel;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CsvBarModelToBarModelConverter implements Converter<CsvBarModel, BarModel> {

    @SneakyThrows
    @Override
    public BarModel convert(final CsvBarModel source) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d HH:mm");
        final LocalDateTime dateTime = LocalDateTime.parse(source.getTime(),formatter).plusHours(2);
        final BarModel target = new BarModel();
        target.setDateTime(dateTime);
        target.setOpen(Double.parseDouble(source.getOpen()));
        target.setHigh(Double.parseDouble(source.getHigh()));
        target.setLow(Double.parseDouble(source.getLow()));
        target.setClose(Double.parseDouble(source.getClose()));
        target.setVolume(Integer.parseInt(source.getVolume()));
        return target;
    }

    public List<BarModel> convertAll(final List<CsvBarModel> sourceList){
        return sourceList.stream().map(this::convert).collect(Collectors.toList());
    }
}
