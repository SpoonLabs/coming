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
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class JGenProg extends AbstractRepairTool {

    private static final String[] patternFileNames = {
            "any_statement_s.xml",
            "any_statement_d.xml"
    };
    private boolean res=false;

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
            // based on move never occurs actually based on the analysis of our dataset but it may occur when in case of swaps(as described in the paper)
            // TODO : improve this
            return false;
        } else {
            return false;
        }

        // we operate on statement level while the pattern file can match any element of the statement in element
        while (element.getRoleInParent() != CtRole.STATEMENT) {
            element = element.getParent();
        }

        // see if the inserted statement occurs in the previous version of the file
        String previousVersionString = (String) revision.getChildren().get(0).getPreviousVersion();
        boolean ans=previousVersionString.contains(element.toString());

        CtClass ctClass = Launcher.parseClass(previousVersionString);
        List<CtInvocation> ctInvocationsSource = ctClass.getElements(new TypeFilter<>(CtInvocation.class));//source file methods
        List<CtInvocation> ctInvocations = element.getElements(new TypeFilter<>(CtInvocation.class));//our invocation

        for(CtInvocation ctInvocation : ctInvocations) {

            String methodName = ctInvocation.getExecutable().getSimpleName();
            List<Object> arguments = ctInvocation.getArguments();
//            System.out.println("arguments " + arguments);

            for ( CtInvocation ctInvocSource: ctInvocationsSource){
                if(ctInvocSource.getExecutable().getSimpleName().equals(methodName)){
                    List ctTypeParameters = ctInvocSource.getArguments();
                    if(arguments.size()==ctTypeParameters.size()){
                        if(arguments.size()==0){
                            res=true;
                        }else
                            for(int i=0;i<ctTypeParameters.size();i++){
                                if(arguments.get(i).equals(ctTypeParameters.get(i))){
                                    res=true;
                                    continue;
                                }
                                else
                                    res=false;
                            }}
                }
            }
        }

        // Binary operators
        List<CtBinaryOperator> ctBoTarget = element.getElements(new TypeFilter<>(CtBinaryOperator.class));//our methods
        List<CtBinaryOperator> ctBoSource = ctClass.getElements(new TypeFilter<>(CtBinaryOperator.class));//source file BO's

        for(CtBinaryOperator boT : ctBoTarget) {

            String methodName = boT.getShortRepresentation();

            for ( CtBinaryOperator boS: ctBoSource){
                if(boS.getShortRepresentation().equals(methodName)){
                    if((boS.getLeftHandOperand().equals(boT.getLeftHandOperand())) &&  (boS.getRightHandOperand().equals(boT.getRightHandOperand()))){
                        res=true;
                    }
                }
            }
        }

        return ans|| res;
    }

//    private boolean isElementInStringAst(String mainFile, CtElement element) {
//
//        boolean matchFound = false;
//
//        try {
//            try (PrintWriter out = new PrintWriter("/tmp/" + "tmp_prev_file" + ".java")) {
//                out.println(mainFile);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                System.exit(1);
//            }
//
//            // get CtModel of previousString or the mail file
//            Launcher launcher = new Launcher();
//            launcher.addInputResource("/tmp/" + "tmp_prev_file" + ".java");
//            launcher.buildModel();
//            CtModel model = launcher.getModel();
//
//
//        /*
//        TODO: This pattern is not built properly
//        Error in Pattern properties. Note: It's a silent failure.
//        > "Method threw 'java.lang.StringIndexOutOfBoundsException' exception. Cannot evaluate spoon.pattern.internal.node.ListOfNodes.toString()"
//        Maybe because of the changes to the spoon object when it went through coming or gt.
//         */
//            Pattern pattern = PatternBuilder.create(element).build();
//
//            //System.out.println(pattern); // THIS THROWS AN EXCEPTION NOW: java.lang.StringIndexOutOfBoundsException: String index out of range: -1
//
//            List<Match> matches = new ArrayList<>();
//            for (CtType<?> ctType : model.getAllTypes()) {
//                matches.addAll(pattern.getMatches(ctType));
//            }
//
//            System.out.println("\n\nMATCHES START\n\n");
//            for (Match m : matches) {
//                System.err.println("matches with: " + m.toString());
//            }
//            System.out.println("\n\nMATCHES END\n\n");
//
//            matchFound = matches.size() > 0;
//
//        } catch (SpoonException ignored) {
//
//        }
//
//        return matchFound;
//    }
}

