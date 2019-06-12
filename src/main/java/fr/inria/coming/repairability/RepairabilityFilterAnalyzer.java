package fr.inria.coming.repairability;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromDiff;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.repairability.repiartools.AbstractRepairTool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RepairabilityFilterAnalyzer implements Analyzer {
    /**
     * This goes through each instances and pass it to the filter of corresponding repair-tool
     *
     * @param input           input to be analyzer
     * @param previousResults results from PatternInstanceAnalyzer with patterns made by subclasses of AbstractRepairTool
     *
     * @return results that passes the corresponding filter
     */
    @Override
    public AnalysisResult analyze(IRevision input, RevisionResult previousResults) {


        PatternInstancesFromRevision result = (PatternInstancesFromRevision) previousResults.getResultFromClass(PatternInstanceAnalyzer.class);

        // Will store PatternInstancesFromDiff that pass the filter of the corresponding repair tool
        List<PatternInstancesFromDiff> allInstances = new ArrayList<>();

        for (PatternInstancesFromDiff instancesPerDiff : result.getInfoPerDiff()) {

            // Will store ChangePatternInstance that pass the filter of the corresponding repair tool
            List<ChangePatternInstance> patternInstanceList = new ArrayList<>();

            for (ChangePatternInstance instancePattern : instancesPerDiff.getInstances()) {
                // for each matching instance

                // get the repair-tool
                AbstractRepairTool tool = RepairTools.getRepairToolInstance(instancePattern.getPattern().getName().split(File.pathSeparator)[0]);

                if (tool.filter(instancePattern)) {
                    // if filter is passed add it too patternInstanceList
                    patternInstanceList.add(instancePattern);
                }
            }

            // modify this PatternInstancesFromDiff to contain only filtered elements
            instancesPerDiff.setInstances(patternInstanceList);

            allInstances.add(instancesPerDiff);
        }

        PatternInstancesFromRevision finalResult = new PatternInstancesFromRevision(input, allInstances);
        return finalResult;
    }
}
