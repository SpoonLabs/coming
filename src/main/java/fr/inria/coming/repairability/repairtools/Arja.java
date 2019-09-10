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
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 Arja tries to correct the code by inserting statements that are in the source file either in a 
 direct approach or a type matching approach.
 
 Direct Approach:
 the extracted variable/Method with the same name and compatible types exists in the variable/Method scope.
 
 statement  or the invocation exists in the source code with compatible types. (Read more about this on
 https://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=8485732)
 
 *
 */

public class Arja extends AbstractRepairTool {

    private static final String[] patternFileNames = {
            "any_statement_s.xml",
            "any_statement_d.xml"
    };
    private boolean res1=false;
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
        String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];

        //invocations
        CtClass ctClass = Launcher.parseClass(previousVersionString);
        List<CtMethod> ctMethodsSourcefile = ctClass.getElements(new TypeFilter<>(CtMethod.class));//source file methods
        List<CtInvocation> ctInvocationssSourcefile = ctClass.getElements(new TypeFilter<>(CtInvocation.class));//source file invocations
        List<CtInvocation> ctInvocations = element.getElements(new TypeFilter<>(CtInvocation.class));//our invocation

        for(CtInvocation ctInvocation : ctInvocations) {

            String ourmethodName = ctInvocation.getExecutable().getSimpleName();//.getShortRepresentation();
            List<CtTypeParameter> ctTypeParameterstarget = ctInvocation.getActualTypeArguments();

            for (CtMethod ctMethod: ctMethodsSourcefile){
                if(ctMethod.getSimpleName().equals(ourmethodName)){
                    res1=true;
                }
            }

            for (CtInvocation ctinvoc: ctInvocationssSourcefile){
                    List<CtTypeParameter> ctTypeParameters = ctinvoc.getActualTypeArguments();
                    if(ctTypeParameterstarget.size()==ctTypeParameters.size()){
                        if(ctTypeParameterstarget.size()==0)
                            res=true;
                        else
                            for(int i=0;i<ctTypeParameters.size();i++){
                                if(ctTypeParameterstarget.get(i).equals(ctTypeParameters.get(i))){
                                    res=res1;
                                }
                                else{
                                    res=false;
                                    System.out.println("about to break");
                                    break;
                                }

                            }
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
                        if(previousVersionString.contains(boT.getRightHandOperand().toString()) && previousVersionString.contains(boT.getLeftHandOperand().toString())){                    
                            res=true;
                        }

                        if(boS.getRightHandOperand().equals(boT.getRightHandOperand()) && boS.getLeftHandOperand().equals(boT.getLeftHandOperand())){
                            res=true;
                        }
                }
            }
        }


        List<CtElement> ctelement = element.getElements(new TypeFilter<>(CtElement.class));//our elements
        List<CtElement> ctelementsource = ctClass.getElements(new TypeFilter<>(CtElement.class));//source file elements

        for(CtElement elementS:ctelementsource){
            if(ctelement.get(0).equals(elementS)){
                res=true;
            }
        }

        List<CtConstructorCall> ctconst = element.getElements(new TypeFilter<>(CtConstructorCall.class));//our elements

        res=res||previousVersionString.contains(element.toString());


        return res;
    }
}
