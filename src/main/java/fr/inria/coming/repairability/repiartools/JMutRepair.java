package fr.inria.coming.repairability.repiartools;

import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.core.entities.RevisionResult;

import java.util.ArrayList;
import java.util.List;

public class JMutRepair extends AbstractRepairTool {

    private static final String[] patternFileNames = {
            "binary.xml",
            "unary.xml"
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
     * @param revisionResult
     * @return
     */
    @Override
    public RevisionResult filter(RevisionResult revisionResult) {
        return revisionResult;
    }
}
