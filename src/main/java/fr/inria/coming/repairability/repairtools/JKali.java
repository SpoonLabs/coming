package fr.inria.coming.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * JKali fixes programs by doing three things:
 * <p>
 * 1. Adding return statements
 * 2. Replace conditions with true or false
 * 3. delete single line or a block of code
 * <p>
 * Pattern file add_rtrn.xml takes care of number 1.
 * we search for return statements with which has been added.
 * <p>
 * <p>
 * Pattern file false.xml and true.xml takes care of number 2.
 * we search for conditions which have been replaced with true or false value.
 * <p>
 * Pattern file del.xml takes care of number 3.
 * we searched for deleted statements.
 */
public class JKali extends AbstractRepairTool {

    private static final String[] patternFileNames = {
            "del.xml",
            "true.xml",
            "false.xml",
            "add_rtrn.xml"
    };

    /**
     * Encodes the search space of JMutRepair
     *
     * @return a List of ChangePatternSpecifications that are supposed to be mined by PatternInstanceAnalyzer
     */
    @Override
    protected List<ChangePatternSpecification> readPatterns() {
        List<ChangePatternSpecification> patterns = new ArrayList<>();
        for (String fileName : patternFileNames) {
            patterns.add(PatternXMLParser.parseFile(getPathFromResources(fileName)));
        }
        return patterns;
    }

    /**
     * Certain patterns/characteristics of search-space of a repair tool can't be represented by ChangePatternSpecification
     * This filter is supposed to delete/remove such instances from the results given by PatternInstanceAnalyser.
     *
     * @param patternInstance
     * @param diff
     * @return
     */
    @Override
    public boolean filter(ChangePatternInstance patternInstance, IRevision revision, Diff diff) {
        return true;
    }

    @Override
    protected boolean coveredByInstanceNodes
            (
                    ChangePatternInstance instance,
                    Set<CtElement> instanceCoveredNodes,
                    Operation diffOperation
            ) {
        if (instance.getActions().get(0).getAction().getName().equals("INS")
                && diffOperation instanceof DeleteOperation) {
            return ((InsertOperation) instance.getActions().get(0)).getParent()
                    .equals(diffOperation.getSrcNode().getParent());
        } else {
            return super.coveredByInstanceNodes(instance, instanceCoveredNodes, diffOperation);
        }
    }


}
