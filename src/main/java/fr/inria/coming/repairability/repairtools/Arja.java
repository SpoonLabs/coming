package fr.inria.coming.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import gumtree.spoon.diff.operations.*;
import spoon.Launcher;
import spoon.SpoonException;
import spoon.pattern.Match;
import spoon.pattern.Pattern;
import spoon.pattern.PatternBuilder;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtRole;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Arja extends AbstractRepairTool {

    private static final String[] patternFileNames = {
            "any_statement_s.xml"
//            "any_statement_d.xml"
    };

    @Override
    protected List<ChangePatternSpecification> readPatterns() {
        List<ChangePatternSpecification> patterns = new ArrayList<>();
        for (String fileName : patternFileNames) {
            patterns.add(PatternXMLParser.parseFile(getPathFromResources(fileName)));
        }
        return patterns;
    }


    @Override
    public boolean filter(ChangePatternInstance instance, IRevision revision) {
//        Operation anyOperation = instance.getActions().get(0);
//
//        CtElement element;
//        if (anyOperation instanceof InsertOperation) {
//            element = anyOperation.getSrcNode(); // See why are using SrcNode: https://github.com/SpoonLabs/coming/issues/72#issuecomment-508123273
//        } else if (anyOperation instanceof UpdateOperation) {
//            element = anyOperation.getDstNode(); // See why are using DstNode: https://github.com/SpoonLabs/coming/issues/72#issuecomment-508123273
//        } else if (anyOperation instanceof DeleteOperation) {
//            // ASSUMPTION ONLY A STATEMENT CAN BE DELETED
//            return anyOperation.getSrcNode().getRoleInParent() == CtRole.STATEMENT;
//        } else if (anyOperation instanceof MoveOperation) {
//            // based on move never occurs actually based on the analysis of our dataset but it may occur when in case of swaps(as described in the paper)
//            // TODO : improve this
//            return false;
//        } else {
//            return false;
//        }
//
//        // we operate on statement level while the pattern file can match any element of the statement in element
//        while (element.getRoleInParent() != CtRole.STATEMENT) {
//            element = element.getParent();
//        }
//
//        // see if the inserted statement occurs in the previous version of the file
//        String previousVersionString = (String) revision.getChildren().get(0).getPreviousVersion();
//        return previousVersionString.contains(element.toString());

        return true;
    }

}
