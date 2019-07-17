package fr.inria.coming.codefeatures.codeanalyze;

import java.util.List;
import java.util.stream.Collectors;

import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeElementInfo;
import fr.inria.coming.codefeatures.CodeFeatures;
import fr.inria.coming.utils.VariableResolver;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;

public class ExpressionAnalyzer extends AbstractCodeAnalyzer {
	
	public ExpressionAnalyzer (CodeElementInfo inputinfo) {
		super(inputinfo);
	}

	@Override
	public void analyze() {

		analyzeExpressionFeatures(elementinfo.desirableExpressions, elementinfo.element, elementinfo.context,
				elementinfo.parentClass, elementinfo.statements, elementinfo.varsInScope, elementinfo.allMethods
				, elementinfo.invocationsFromClass, elementinfo.constructorcallsFromClass);	
		
	}
	
	private void analyzeExpressionFeatures(List<CtExpression> expressionsToStudy, CtElement originalElement, Cntx<Object> context,
			CtClass parentClass, List<CtStatement> allstatementsinclass, List<CtVariable> varsInScope, 
			List allMethodsFromClass, List<CtInvocation> invocationsFromClass, List<CtConstructorCall> constructorcallsFromClass) {

		for(int expressionindex=0; expressionindex<expressionsToStudy.size(); expressionindex++) {
			
			CtExpression specificexpression = expressionsToStudy.get(expressionindex);
			
			List<CtVariableAccess> varsAffected = VariableResolver.collectVariableAccess(specificexpression, false);

			// Get all invocations inside the faulty element
			List<CtInvocation> invocations = specificexpression.getElements(e -> (e instanceof CtInvocation)).stream()
					.map(CtInvocation.class::cast).collect(Collectors.toList());
			
			List<CtConstructorCall> constructorcall = specificexpression.getElements(e -> (e instanceof CtConstructorCall)).stream()
					.map(CtConstructorCall.class::cast).collect(Collectors.toList());
			
			writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E1_LOCAL_VAR_NOT_USED, 
					analyze_AffectedVariablesUsed (varsAffected, originalElement, allstatementsinclass), 
					"FEATURES_EXPRESSION");
			
			writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E1_LOCAL_VAR_NOT_ASSIGNED, 
					analyze_AffectedAssigned (varsAffected, originalElement), 
					"FEATURES_EXPRESSION");
			
			boolean[] expressionfeatures = analyze_SametypewithGuard(varsAffected, originalElement, parentClass, allstatementsinclass);

			if(expressionfeatures != null) {
				
				writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD, 
						expressionfeatures[0], "FEATURES_EXPRESSION");
				
				writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD, 
						expressionfeatures[1], "FEATURES_EXPRESSION");
				
				writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD, 
						expressionfeatures[2], "FEATURES_EXPRESSION");
				
				writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD, 
						expressionfeatures[3], "FEATURES_EXPRESSION");
			}
			
			writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E4_Field_NOT_USED, 
					analyze_AffectedFielfs(varsAffected, originalElement, parentClass), 
					"FEATURES_EXPRESSION");
			
			writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E4_Field_NOT_ASSIGNED, 
					analyze_AffectedFieldAssigned(varsAffected, originalElement, parentClass), 
					"FEATURES_EXPRESSION");
			
			boolean[] expressionvalueS7S8 = analyze_AffectedObjectLastAppear(varsAffected, originalElement, allstatementsinclass);

            if(expressionvalueS7S8 != null) {
				
            	writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E7_OBJECT_USED_IN_ASSIGNMENT, 
						expressionvalueS7S8[0], "FEATURES_EXPRESSION");
				
            	writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E8_PRIMITIVE_USED_IN_ASSIGNMENT, 
						expressionvalueS7S8[1], "FEATURES_EXPRESSION");
			}
            
            boolean[] expressionvalueS9S10 = analyze_SamerMethodWithGuardOrTrywrap(originalElement, parentClass, invocationsFromClass, invocations,
            		constructorcallsFromClass, constructorcall);

            if(expressionvalueS9S10 != null) {
				
            	writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E9_METHOD_CALL_WITH_NORMAL_GUARD, 
						expressionvalueS9S10[0], "FEATURES_EXPRESSION");
				
            	writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E10_METHOD_CALL_WITH_NULL_GUARD, 
						expressionvalueS9S10[1], "FEATURES_EXPRESSION");
			}     
            
            writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E1_RETURN_PRIMITIVE, 
            		getExpressionType(specificexpression), "FEATURES_EXPRESSION");
            
            boolean[] expressionvalueType = analyzeExpression(specificexpression, context, allMethodsFromClass, invocationsFromClass, parentClass);

            if(expressionvalueType != null) {
				
            	writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E2_COMPATIBLE_INVOCATION_PAREMETER_RETURN, 
						expressionvalueType[0], "FEATURES_EXPRESSION");
				
            	writeGroupedInfo(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E3_COMPATIBLE_INVOCATION_PAREMETER, 
						expressionvalueType[1], "FEATURES_EXPRESSION");
			}    
		}	
	}
	
    private boolean getExpressionType (CtExpression anexpression) {
		
		if (anexpression.getType()!=null && anexpression.getType().isPrimitive()) {		
			return true;
		}
		
		return false;
	}
    
    private boolean[] analyzeExpression(CtExpression expressionToStudy, Cntx<Object> context, List allMethods,
			List<CtInvocation> invocationsFromClass, CtClass parentclass) {
		
		try {
			
			if(expressionToStudy instanceof CtVariableAccess)
				return null;
			
			boolean hasExpressionReturnParmeterCompatible = false;
			boolean hasExpressionParmeterCompatible = false;
			
			if(checkMethodDeclarationWithParameterReturnCompatibleType(allMethods, expressionToStudy.getType()) != null 
				|| checkInvocationWithParameterReturnCompatibleType(invocationsFromClass,
							expressionToStudy.getType(), parentclass) != null) {
				hasExpressionReturnParmeterCompatible = true;
			}
			
			if (checkMethodDeclarationWithParemetrCompatibleType(allMethods, expressionToStudy.getType()) != null
					|| checkInvocationWithParemetrCompatibleType (invocationsFromClass,
							expressionToStudy.getType()) != null) {
				hasExpressionParmeterCompatible = true;
			}

			boolean[] expressionTypeFeatures= new boolean[2];
			
			expressionTypeFeatures[0] = hasExpressionReturnParmeterCompatible;
			
			expressionTypeFeatures[1] = hasExpressionParmeterCompatible;

			return expressionTypeFeatures;

		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
