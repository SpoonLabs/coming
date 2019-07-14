package fr.inria.coming.repairability.repiartools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;

import java.util.ArrayList;
import java.util.List;

/**

JKali fixes programs by doing three things:

1. Adding return statements
2. Replace conditions with true or false
3. delete single line or a block of code

Pattern file add_rtrn.xml takes care of number 1.
    we search for return statements with which has been added.


Pattern file false.xml and true.xml takes care of number 2.
    we search for conditions which have been replaced with true or false value.

Pattern file del.xml takes care of number 3.
        we searched for deleted statements.


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
     * @return
     */
    @Override
    public boolean filter(ChangePatternInstance patternInstance, IRevision revision) {
        return true;
    }

}
