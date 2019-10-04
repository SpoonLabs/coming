package fr.inria.coming.repairability;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromDiff;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.repairability.repairtools.AbstractRepairTool;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RepairabilityAnalyzer implements Analyzer {
    /**
     * This goes through each instances and pass it to the filter of corresponding repair-tool.
     * It checks if the input revision has not more than one file modification.
     * It makes sure that each file modification or revision has not more than one instance of any repair-tool.
     *
     * @param input           input to be analyzer. it should have not more than one file modification/Children
     * @param previousResults results from PatternInstanceAnalyzer with patterns made by subclasses of AbstractRepairTool
     * @return Final instances of each repair tools
     */
    @Override
    public AnalysisResult analyze(IRevision input, RevisionResult previousResults) {

        PatternInstancesFromRevision result = (PatternInstancesFromRevision) previousResults.getResultFromClass(PatternInstanceAnalyzer.class);

        // We will store PatternInstancesFromDiff that pass the filter of the corresponding repair tool
        List<PatternInstancesFromDiff> allInstances = new ArrayList<>();

        /*
         * ASSUMPTION: If a revision modifies more than one file then, it couldn't have be generated by a repair-tool.
         */
        if (input.getChildren().size() != 1) {
            return (new PatternInstancesFromRevision(input, allInstances));
        }

        /*
         * ASSUMPTION: Each commit(aka IRevision) should have only one instances of a particular repair tool.
         * This is because the repair tools we are dealing make patches which affects just one file and each patch corresponds to a revision or commit.
         *
         * More discussion about the same can be found here: https://github.com/SpoonLabs/coming/issues/94
         * - one instancePerDiff represent one result/one revision and in one revision there should be at max of one instance of any repair-tool
         */
        List<String> toolsSeen = new ArrayList<>();

        for (PatternInstancesFromDiff instancesPerDiff : result.getInfoPerDiff()) {

            // Will store ChangePatternInstance that pass the filter of the corresponding repair tool
            List<ChangePatternInstance> patternInstanceList = new ArrayList<>();

            for (ChangePatternInstance instancePattern : instancesPerDiff.getInstances()) {
                // for each matching instance

                // get the repair-tool
                String toolName = instancePattern.getPattern().getName().split(File.pathSeparator)[0];
                AbstractRepairTool tool = RepairTools.getRepairToolInstance(toolName);

                if (tool.filter(instancePattern, input)) {
                    // if filter is passed add it too patternInstanceList

                    if (!toolsSeen.contains(toolName)
                            || ComingProperties.getPropertyBoolean("include_all_instances_for_each_tool")) {
                        /* ignore if the tool has been seen before and
                           the "include_all_instances_for_each_tool" is not used */
                        if(!ComingProperties.getPropertyBoolean("exclude_repair_patterns_not_covering_the_whole_diff")
                                || coversTheWholeDiff(instancePattern, instancesPerDiff.getDiff())) {
                            /* ignore if the found instances do not cover the whole diff and
                                "exclude_repair_patterns_not_covering_the_whole_diff" is used
                             */
                            patternInstanceList.add(instancePattern);
                            toolsSeen.add(toolName);
                        }
                    }
                }
            }

            // modify this PatternInstancesFromDiff to contain only filtered elements
            // FIXME: this also changes PatternInstanceAnalyzer result
            instancesPerDiff.setInstances(patternInstanceList);

            allInstances.add(instancesPerDiff);
        }

        PatternInstancesFromRevision finalResult = new PatternInstancesFromRevision(input, allInstances,result.getRow_list());

        return finalResult;
    }

    private boolean coversTheWholeDiff(ChangePatternInstance instancePattern, Diff diff) {
        for(Operation diffOperation : diff.getRootOperations()){
            boolean foundOp = false;
            for(Operation instanceOperation : instancePattern.getActions()){
                if(diffOperation.equals(instanceOperation)){
                    foundOp = true;
                    break;
                }
            }
            if(!foundOp)
                return false;
        }
        return true;
    }
}
