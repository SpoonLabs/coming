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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Arja extends AbstractRepairTool {

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
            // based on move never occurs actually based on the analysis of our dataset but it may occur when in case of swaps(as described in the paper)
            // TODO : improve this
            return true;
        } else {
            return false;
        }

        // we operate on statement level while the pattern file can match any element of the statement in element
        while (element.getRoleInParent() != CtRole.STATEMENT) {
            element = element.getParent();
        }

        // see if the inserted statement occurs in the previous version of the file
        String previousVersionString = (String) revision.getChildren().get(0).getPreviousVersion();
        boolean res=previousVersionString.contains(element.toString());

        String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];

        //invocations

        CtClass ctClass = Launcher.parseClass(previousVersionString);
        List<CtMethod> ctMethods = ctClass.getElements(new TypeFilter<>(CtMethod.class));//source file methods
        List<CtInvocation> ctInvocations = element.getElements(new TypeFilter<>(CtInvocation.class));//our invocation

        for(CtInvocation ctInvocation : ctInvocations) {
            System.out.println("ctInvocation "+ctInvocation.getShortRepresentation());

//            System.out.println("ctInvocation "+ctInvocation);
            res=true;
            String methodName = ctInvocation.getShortRepresentation();
            List<CtTypeReference> typeReferences = ctInvocation.getActualTypeArguments();

            List<CtTypeParameter> ctTypeParameterst = new ArrayList<>();
            for(CtTypeReference ctTypeReference : typeReferences) {
                ctTypeParameterst.add(ctTypeReference.getTypeParameterDeclaration());
            }

            for ( CtMethod ctMethod: ctMethods){
                if(ctMethod.getShortRepresentation().equals(methodName)){
                    System.out.println("ctInvocation "+ctInvocation.getShortRepresentation());
                    System.out.println("ctMethod.getSimpleName() "+ctMethod.getSimpleName());

                    List<CtTypeParameter> ctTypeParameters = ctMethod.getFormalCtTypeParameters();

                    if(ctTypeParameterst.size()==ctTypeParameters.size()){
                        for(int i=0;i<ctTypeParameters.size();i++){
                            if(ctTypeParameters.get(i).equals(ctTypeParameters.get(i))){
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
//                    System.out.println("bo "+boT.getShortRepresentation());
//                    System.out.println("ctMethod.getSimpleName() "+boS.getShortRepresentation());

                    if((boS.getLeftHandOperand().getTypeCasts().equals(boT.getLeftHandOperand().getTypeCasts())) &&  (boS.getRightHandOperand().getTypeCasts().equals(boT.getRightHandOperand().getTypeCasts()))){
                         res=true;
//                         System.out.println("left hand op S "+boS.getLeftHandOperand());
//                         System.out.println("left hand op T "+boT.getLeftHandOperand());
//
//                         System.out.println("right hand op S " +boS.getRightHandOperand());
//                         System.out.println("right hand op T " +boT.getRightHandOperand());
                    }
                }
            }
        }

        return res;
    }

}
