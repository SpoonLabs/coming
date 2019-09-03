package fr.inria.coming.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;


/**

 Elixir fixes programs by doing 8 things:

 1.loosening or tightening variable types => patter file ep_1
 2.changing the return statement => patter file ep_2
 3.adding if (variable != null) => patter file ep_3
 4.array index in bound check => patter file ep_4
 5.chnage of a boolean operator  => patter file ep_5
 6.loosening or tightening boolean expressions => patter file ep_6,7
 7.changing method invocations => patter file ep_8
 8.inserting the method invocation => patter file ep_9
 */


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
    private boolean check=false;
    private IRevision myrev;

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
