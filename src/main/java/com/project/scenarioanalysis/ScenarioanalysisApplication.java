package com.project.scenarioanalysis;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;
import com.project.scenarioanalysis.model.ScenarioModel;
import com.project.scenarioanalysis.model.SequenceModel;
import com.project.scenarioanalysis.services.AnalysisService;
import com.project.scenarioanalysis.services.ScenarioService;
import com.project.scenarioanalysis.services.SequenceService;
import com.project.scenarioanalysis.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;

import java.util.List;

@SpringBootApplication
public class ScenarioanalysisApplication implements CommandLineRunner {

    private static Logger LOG = LoggerFactory.getLogger(ScenarioanalysisApplication.class);

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private AnalysisService analysisService;

    public static void main(java.lang.String[] args) {
        SpringApplication.run(ScenarioanalysisApplication.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(java.lang.String... args) {
        LOG.info("EXECUTING : command line runner");
        StopWatch watch = new StopWatch("CSV import");
        watch.start();

        try {
            //testCSV();
            //testScenario();
            analysisService.analyseAll(Constant.XAUUSDM15);
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        LOG.info("Total execution time: {}",watch.getTotalTimeSeconds());

    }

    private void testScenario(){

        try {
            final SequenceModel sequenceModel = sequenceService.createSequence(Constant.GBPUSDM15);
            final List<SequenceModel> splitSequenceModels = sequenceService.splitSequenceByDays(sequenceModel,Constant.GBPUSDM15);
            final String title = "WHEN S1 is Wednesday and Thursday or Friday trade below Wednesday THEN Monday trades below Wednesday";
            final Expression<String> when = And.of(Variable.of("!S1.WEEKDAY==3"), Or.of(Variable.of("!S3.LOW<!S1.LOW"), Variable.of("!S2.LOW<!S1.LOW")));
            final Expression<String> then = Variable.of("!S4.LOW<!S1.LOW");
            final ScenarioModel scenarioModel = ScenarioModel.builder().sequenceModels(splitSequenceModels).
                    title(title).numberOfSequences(4).when(when).then(then).build();

            //System.out.println(scenarioModel.toString());

            //System.out.println(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
            LOG.info(scenarioService.getScenarioReport(scenarioService.executeScenario(scenarioModel)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testCSV(){
        try {
            final SequenceModel sequenceModel = sequenceService.createSequence(Constant.XAUUSDM15);
            System.out.println(sequenceModel);
            final List<SequenceModel> splitSequenceModels = sequenceService.splitSequenceByDays(sequenceModel,Constant.XAUUSDM15);
            System.out.println(splitSequenceModels);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
