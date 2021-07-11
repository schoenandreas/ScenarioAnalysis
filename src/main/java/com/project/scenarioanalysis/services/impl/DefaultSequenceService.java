package com.project.scenarioanalysis.services.impl;

import com.project.scenarioanalysis.converters.CsvBarModelToBarModelConverter;
import com.project.scenarioanalysis.model.BarModel;
import com.project.scenarioanalysis.model.CsvBarModel;
import com.project.scenarioanalysis.model.SequenceModel;
import com.project.scenarioanalysis.services.CsvService;
import com.project.scenarioanalysis.services.SequenceService;
import com.project.scenarioanalysis.util.Constant;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultSequenceService implements SequenceService {

    public static final int DAILY_MINUTES = 1440;

    @Autowired
    private CsvService csvService;

    @Autowired
    private CsvBarModelToBarModelConverter csvBarModelToBarModelConverter;

    @Override
    public SequenceModel createSequence(final List<BarModel> bars) {
        final SequenceModel sequenceModel = new SequenceModel();
        sequenceModel.setBars(bars);
        sequenceModel.setTimeFrameInMinutes(calculateTimeframe(bars));
        sequenceModel.setOpen(bars.get(0).getOpen());
        sequenceModel.setClose(bars.get(bars.size()-1).getClose());
        sequenceModel.setHigh(bars.stream().mapToDouble(BarModel::getHigh).max().getAsDouble());
        sequenceModel.setLow(bars.stream().mapToDouble(BarModel::getLow).min().getAsDouble());
        sequenceModel.setVolume(bars.stream().mapToInt(BarModel::getVolume).sum());
        return sequenceModel;
    }

    @Override
    public SequenceModel createSequence(final String symbol) throws Exception {
        final List<CsvBarModel> bars = csvService.importBars(symbol);
        final List<BarModel> barModels = csvBarModelToBarModelConverter.convertAll(bars);
        return createSequence(barModels);
    }

    @Override
    public List<SequenceModel> splitSequenceByDays(final SequenceModel sequenceModel, final String symbol) {
        final List<BarModel> barModels = sequenceModel.getBars();
        if(sequenceModel == null || CollectionUtils.isEmpty(barModels)){
            throw new IllegalArgumentException("Invalid SequenceModel to split.");
        }
        final List<SequenceModel> result = new ArrayList<>();
        List<BarModel> dayBars = new ArrayList<>();
        BarModel previousBar = barModels.get(0);
        for (final BarModel bar : barModels) {
            if (isNewDay(bar.getDateTime(),previousBar.getDateTime(),sequenceModel.getTimeFrameInMinutes())) {
                addSequenceToResult(sequenceModel, result, dayBars,symbol);
                dayBars = new ArrayList<>();
            }
            dayBars.add(bar);
            previousBar = bar;
        }
        addSequenceToResult(sequenceModel, result, dayBars,symbol);
        return result;
    }

    private void addSequenceToResult(SequenceModel sequenceModel, List<SequenceModel> result, List<BarModel> dayBars, final String symbol) {
        if(symbol.equals(Constant.US30M15) && sequenceModel.getTimeFrameInMinutes() * dayBars.size() == DAILY_MINUTES -105){
            result.add(createSequence(dayBars));
        }
        if(symbol.equals(Constant.XAUUSDM15) && sequenceModel.getTimeFrameInMinutes() * dayBars.size() == DAILY_MINUTES -60){
            result.add(createSequence(dayBars));
        }
        if (sequenceModel.getTimeFrameInMinutes() * dayBars.size() == DAILY_MINUTES) {
            result.add(createSequence(dayBars));
        }
    }

    @Override
    public DayOfWeek getDayOfWeek(final SequenceModel sequenceModel) {
        final List<BarModel> bars = sequenceModel.getBars();
        if (sequenceModel.getTimeFrameInMinutes() * bars.size() > DAILY_MINUTES ){
            throw new IllegalArgumentException("Sequence is bigger than a day!");
        }
        final BarModel barAtEight = bars.stream().filter(
                barModel -> barModel.getDateTime().getHour() == 8 && barModel.getDateTime().getMinute() == 0).findFirst().get();
        return barAtEight.getDateTime().getDayOfWeek();
    }

    @Override
    public LocalDateTime getHighTimeForSequence(final SequenceModel sequenceModel) {
        LocalDateTime dateTime = null;
        double currentHigh = -1;
        for (final BarModel barModel:sequenceModel.getBars()) {
            if(barModel.getHigh()>currentHigh){
                currentHigh = barModel.getHigh();
                dateTime = barModel.getDateTime();
            }
        }
        return dateTime;
    }

    @Override
    public LocalDateTime getLowTimeForSequence(final SequenceModel sequenceModel) {
        LocalDateTime dateTime = null;
        double currentLow = Double.MAX_VALUE;
        for (final BarModel barModel:sequenceModel.getBars()) {
            if(barModel.getLow()<currentLow){
                currentLow = barModel.getLow();
                dateTime = barModel.getDateTime();
            }
        }
        return dateTime;
    }

    private boolean isNewDay(final LocalDateTime dateTime, final LocalDateTime previousDateTime,final int timeFrameMinutes){
        return previousDateTime.until(dateTime,ChronoUnit.MINUTES)>timeFrameMinutes;
    }


    private int calculateTimeframe(final List<BarModel> bars){
        if(CollectionUtils.isNotEmpty(bars) && bars.size()>1){
            final long diff = bars.get(0).getDateTime().until(bars.get(1).getDateTime(), ChronoUnit.MINUTES);
            return Math.toIntExact(diff);
        }
        return 0;
    }
}
