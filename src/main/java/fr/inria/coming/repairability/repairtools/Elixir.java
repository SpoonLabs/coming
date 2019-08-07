package fr.inria.coming.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.CtBinaryOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Elixir extends AbstractRepairTool {

    private static final String[] patternFileNames = {
            "ep_1.xml",
            "ep_2.xml",
            "ep_3.xml",
            "ep_4.xml",
            "ep_5.xml",
            "ep_6.xml",
            "ep_7.xml",
            "ep_8.xml",
            "ep_9.xml"
    };

    /**
     * Encodes the search space of Elixir
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

        String patternType = patternInstance.getPattern().getName().split(File.pathSeparator)[1];
        if (patternType.startsWith("ep_4")) {

            Operation upd = patternInstance.getActions().get(0);
            CtBinaryOperator src = (CtBinaryOperator) upd.getSrcNode();
            CtBinaryOperator dst = (CtBinaryOperator) upd.getDstNode();

            return src.getLeftHandOperand().equals(dst.getLeftHandOperand())
                    && src.getRightHandOperand().equals(dst.getRightHandOperand());
        }

        return true;
    }
}
