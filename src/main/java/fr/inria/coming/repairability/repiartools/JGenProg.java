package fr.inria.coming.repairability.repiartools;

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

public class JGenProg extends AbstractRepairTool {

    private static final String[] patternFileNames = {
            "any_statement_s.xml",
            "any_statement_d.xml"
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
        Operation anyOperation = instance.getActions().get(0);

        CtElement element;
        if (anyOperation instanceof InsertOperation) {
            element = anyOperation.getSrcNode(); // See why are using SrcNode: https://github.com/SpoonLabs/coming/issues/72#issuecomment-508123273

        } else if (anyOperation instanceof UpdateOperation) {
            element = anyOperation.getDstNode(); // See why are using DstNode: https://github.com/SpoonLabs/coming/issues/72#issuecomment-508123273
        } else if (anyOperation instanceof DeleteOperation) {
            // ASSUMPTION ONLY A STATEMENT CAN BE DELETED
            return anyOperation.getSrcNode().getRoleInParent() == CtRole.STATEMENT;
        } else if (anyOperation instanceof MoveOperation) {
            // based on move never occurs actually
            // TODO : improve this
            return false;
        } else {
            return false;
        }

        while (element.getRoleInParent() != CtRole.STATEMENT) {
            element = element.getParent();
        }


        // get a spoon model of the previous file
        String previousVersionString = (String) revision.getChildren().get(0).getPreviousVersion();

        boolean matchFound = false;


        try {


            try (PrintWriter out = new PrintWriter("/tmp/" + revision.getName() + ".java")) {
                out.println(previousVersionString);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }

            Launcher launcher = new Launcher();
            launcher.addInputResource("/tmp/" + revision.getName() + ".java");
            launcher.buildModel();
            CtModel model = launcher.getModel();


        /*
        TODO: This pattern is not built properly
        Error in Pattern properties. Note: It's a silent failure.
        > "Method threw 'java.lang.StringIndexOutOfBoundsException' exception. Cannot evaluate spoon.pattern.internal.node.ListOfNodes.toString()"
        Maybe because of the changes to the spoon object when it went through coming or gt.
         */
            Pattern pattern = PatternBuilder.create(element).build();

//        System.out.println(pattern); // THIS THROWS AN EXCEPTION NOW: java.lang.StringIndexOutOfBoundsException: String index out of range: -1

            List<Match> matches = new ArrayList<>();
            for (CtType<?> ctType : model.getAllTypes()) {
                matches.addAll(pattern.getMatches(ctType));
            }

            System.out.println("\n\nMATCHES START\n\n");
            for (Match m : matches) {
                System.err.println("matches with: " + m.toString());
            }
            System.out.println("\n\nMATCHES END\n\n");

            matchFound = matches.size() > 0;

        } catch (SpoonException ignored) {

        }

        return matchFound || previousVersionString.contains(element.toString());
    }
}
