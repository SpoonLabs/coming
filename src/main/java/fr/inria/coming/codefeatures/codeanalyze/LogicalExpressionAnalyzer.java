package fr.inria.coming.codefeatures.codeanalyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeElementInfo;
import fr.inria.coming.codefeatures.CodeFeatures;
import fr.inria.coming.utils.VariableResolver;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtWhile;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.LineFilter;
import spoon.reflect.visitor.filter.TypeFilter;

public class LogicalExpressionAnalyzer extends AbstractCodeAnalyzer {

	public LogicalExpressionAnalyzer (CodeElementInfo inputinfo) {
		super(inputinfo);
	}
	
	@Override
	public void analyze() {
		
		analyzeLE1LE8_AffectedVariablesUsed(elementinfo.logicalExpressions, elementinfo.varsInScope, elementinfo.context,
				elementinfo.parentClass, elementinfo.statements);
		
		analyzeLE2_AffectedVariablesInMethod(elementinfo.logicalExpressions, elementinfo.context, elementinfo.allMethods,
				elementinfo.invocationsFromClass, elementinfo.parentClass);
		
		analyzeLE3_PrimitiveWithCompatibleNotUsed(elementinfo.logicalExpressions, elementinfo.varsInScope, elementinfo.context);
		
		analyzeLE4_BooleanVarNotUsed(elementinfo.logicalExpressions, elementinfo.varsInScope, elementinfo.context);
		
		analyzeLE5_Analyze_ComplexReference(elementinfo.logicalExpressions, elementinfo.context);
		
		analyzeLE6_UnaryInvolved(elementinfo.logicalExpressions, elementinfo.context);
		
		analyzeLE7_VarDirectlyUsed(elementinfo.logicalExpressions, elementinfo.context, elementinfo.invocations);
		
		analyzeLE9_BothNULLAndNormal(elementinfo.logicalExpressions, elementinfo.context);

		analyzeLE10_Analyze_Atomic_Boolexps(elementinfo.logicalExpressions, elementinfo.context);
		
	}
	
	@SuppressWarnings("rawtypes")
	private void analyzeLE1LE8_AffectedVariablesUsed(List<CtExpression> logicalExperssions, 
			List<CtVariable> varsInScope, Cntx<Object> context, CtClass parentClass, List<CtStatement> statements) {
		
		try {
			
			for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {
				
				CtExpression logicalexpression = logicalExperssions.get(indexlogical);
									
				List<CtVariableAccess> varsAffected = VariableResolver.collectVariableAccess(logicalexpression, false);
				
				int similarUsedBefore = 0;
				
				int otherVarUsedinBool = 0;
				
				CtStatement parentstatement=(CtStatement)(logicalexpression.getParent(CtStatement.class));
				
				CtStatement parent = logicalexpression.getParent(new LineFilter());
				
				for (CtVariableAccess variableAffected : varsAffected) {

					boolean foundSimilarVarUsed = false;

					for (CtStatement aStatement : statements) {
						
						if(parentstatement==aStatement || parent==aStatement)
							continue; 

						List<CtElement> elements = aStatement.getElements(e -> isBooleanExpression(e)).stream()
								.map(CtElement.class::cast).collect(Collectors.toList());

						for (CtElement specificelement : elements) {

							List<CtVariableAccess> varsInOtherExpressions = VariableResolver
									.collectVariableRead(specificelement);
							for (CtVariableAccess varInAnotherExpression : varsInOtherExpressions) {
							
									if (compareTypes(variableAffected.getVariable().getType(),
											varInAnotherExpression.getVariable().getType())) {
										foundSimilarVarUsed = true;
										break;
									}
							}
							
							if(foundSimilarVarUsed)
								break;
						}
						
						if(foundSimilarVarUsed)
							break;
					}

					if (foundSimilarVarUsed) {
						similarUsedBefore++;
						break;
					}
				}
				
				
				for (CtVariable aVarInScope : varsInScope) {
					
					boolean whetherused = false;
					
					for (CtVariableAccess variableAffected : varsAffected) {
						if (aVarInScope.getSimpleName().equals(variableAffected.getVariable().getSimpleName())) {
							whetherused = true;
							break;
						}
					}
					
					if(whetherused)
						continue;
					
					boolean foundVarUsed = false;

					for (CtStatement aStatement : statements) {
						
						if(parentstatement==aStatement || parent==aStatement)
							continue; 

						List<CtElement> elements = aStatement.getElements(e -> isBooleanExpression(e)).stream()
								.map(CtElement.class::cast).collect(Collectors.toList());

						for (CtElement specificelement : elements) {

							List<CtVariableAccess> varsInOtherExpressions = VariableResolver
									.collectVariableRead(specificelement);
							for (CtVariableAccess varInAnotherExpression : varsInOtherExpressions) {
							
									if (aVarInScope.getSimpleName().
											equals(varInAnotherExpression.getVariable().getSimpleName())) {
										foundVarUsed = true;
										break;
									}
							}
							
							if(foundVarUsed)
								break;
						}
						
						if(foundVarUsed)
							break;
					}

					if (foundVarUsed) {
						otherVarUsedinBool++;
						break;
					}
				}
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression, 
						CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION,
						(similarUsedBefore) > 0, "FEATURES_LOGICAL_EXPRESSION");
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression, 
						CodeFeatures.LE8_SCOPE_VAR_USED_OTHER_BOOLEXPER,
						(otherVarUsedinBool) > 0, "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
    public boolean isBooleanExpression(CtElement currentexpression) {
		
		if (currentexpression == null|| currentexpression instanceof CtVariableAccess)
			return false;
		
		if (isLogicalExpression(currentexpression)) {
			return true;
		}
		
		if(currentexpression instanceof CtExpression) {
			CtExpression exper= (CtExpression) currentexpression;
		   try {
		      if (exper.getType() != null
				&& exper.getType().unbox().toString().equals("boolean")) {
			  return true;
		     }
		   } catch (Exception e) {
			   return false;
		   }
		}

		return false;
	}
    
	public boolean isLogicalExpression(CtElement currentElement) {
		if (currentElement == null)
			return false;
		if ((currentElement instanceof CtBinaryOperator)) {
			CtBinaryOperator binOp = (CtBinaryOperator) currentElement;
						
			if(binOp.getKind().equals(BinaryOperatorKind.AND) || binOp.getKind().equals(BinaryOperatorKind.OR)
				|| binOp.getKind().equals(BinaryOperatorKind.EQ)
				|| binOp.getKind().equals(BinaryOperatorKind.GE)
				|| binOp.getKind().equals(BinaryOperatorKind.GT)
				|| binOp.getKind().equals(BinaryOperatorKind.INSTANCEOF)
				|| binOp.getKind().equals(BinaryOperatorKind.LE)
				|| binOp.getKind().equals(BinaryOperatorKind.LT)
				|| binOp.getKind().equals(BinaryOperatorKind.NE))
				   return true;
		}
		
		return false;
	}
	
	/**
	 * // For any variable involved in a logical expression,whether exist methods //
	 * (method declaration or method call) in scope (that is in the same faulty //
	 * class // since we do not assume full program) that take variable whose type
	 * is same // with vas one of its parameters and return boolean
	 * 
	 * @param logicalExperssions
	 * @param context
	 * @param allMethods
	 * @param logicalExperssions
	 * @param invocationsFromClass
	 * @param parentclass
	 */
	private void analyzeLE2_AffectedVariablesInMethod(List<CtExpression> logicalExperssions,
			Cntx<Object> context, List allMethods, List<CtInvocation> invocationsFromClass, CtClass parentclass) {
		
		try {

			for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {
				
                CtExpression logicalexpression = logicalExperssions.get(indexlogical);
				
				List<CtVariableAccess> varsAffected = VariableResolver.collectVariableAccess(logicalexpression, false);
				
				boolean hasAnyLES2paramCompatibleWithBooleanReturn = false;

				for (CtVariableAccess varAffected : varsAffected) {

					if (// First, Let's analyze the method declaration
					checkBooleanMethodDeclarationWithTypeInParameter(allMethods, varAffected) != null
							// Second, let's inspect invocations
							|| checkBooleanInvocationWithParameterReturn(invocationsFromClass, varAffected, parentclass) != null) {
						hasAnyLES2paramCompatibleWithBooleanReturn = true;
					}

                    if(hasAnyLES2paramCompatibleWithBooleanReturn)
                    	break;
				}

				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression, 
						CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR,
						hasAnyLES2paramCompatibleWithBooleanReturn, "FEATURES_LOGICAL_EXPRESSION");
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if a method declaration has a parameter compatible with that one from
	 * the var affected
	 * 
	 * @param allMethods
	 * @param varAffected
	 * @return
	 */
	public CtMethod checkBooleanMethodDeclarationWithTypeInParameter(List allMethods, CtVariableAccess varAffected) {
		
		for (Object omethod : allMethods) {

			if (!(omethod instanceof CtMethod))
				continue;

			CtMethod anotherMethodInBuggyClass = (CtMethod) omethod;

			for (Object oparameter : anotherMethodInBuggyClass.getParameters()) {
				CtParameter parameter = (CtParameter) oparameter;

				if (compareTypes(varAffected.getType(), parameter.getType())) {
					if (anotherMethodInBuggyClass.getType().unbox().toString().equals("boolean")) {

						return anotherMethodInBuggyClass;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Return if there is an invocation with an argument of the same type of the Var
	 * Access
	 * 
	 * @param invocationsFromClass
	 * @param varAffected
	 * @return
	 */
	public CtInvocation checkBooleanInvocationWithParameterReturn(List<CtInvocation> invocationsFromClass,
			CtVariableAccess varAffected, CtClass parentclass) {

		try {
			List<CtBinaryOperator> binaryOperatorInClass = parentclass.getElements(e -> 
			(e instanceof CtBinaryOperator)).stream()
					.map(CtBinaryOperator.class::cast).collect(Collectors.toList());		
			
			for (CtInvocation anInvocation : invocationsFromClass) {
				
				List<CtTypeReference> inferredpotentionaltypes = new ArrayList<CtTypeReference>();
				inferredpotentionaltypes.clear();
               
				CtTypeReference inferredtype = null;
				// do simple type inference
				if(anInvocation.getType()==null) {
					for(CtBinaryOperator certainbinary: binaryOperatorInClass) {
						if(certainbinary.getLeftHandOperand() instanceof CtInvocation) {

							CtInvocation anotherinvocation=(CtInvocation)certainbinary.getLeftHandOperand();
							if(anotherinvocation.getExecutable().getSignature().equals(anInvocation.getExecutable().getSignature())
									&& certainbinary.getRightHandOperand().getType()!=null) {

								 inferredtype=certainbinary.getRightHandOperand().getType();
								 inferredpotentionaltypes.add(inferredtype);
								 break;
							}
						}
						
						if(certainbinary.getRightHandOperand() instanceof CtInvocation) {
							CtInvocation anotherinvocation=(CtInvocation)certainbinary.getRightHandOperand();
							if(anotherinvocation.getExecutable().getSignature().equals(anInvocation.getExecutable().getSignature())
									&& certainbinary.getLeftHandOperand().getType()!=null) {
								 inferredtype=certainbinary.getLeftHandOperand().getType();
								 inferredpotentionaltypes.add(inferredtype);
								 break;
							}
						}
					}	
				} else inferredtype=anInvocation.getType();			
				
				if (inferredtype != null && (inferredtype.getSimpleName().equals("Boolean")
						|| inferredtype.unbox().toString().equals("boolean") || 
						whetherpotentionalboolean(inferredpotentionaltypes))) {

					for (Object anObjArgument : anInvocation.getArguments()) {
						CtExpression anArgument = (CtExpression) anObjArgument;

						List<CtVariableAccess> varReadFromArguments = VariableResolver.collectVariableRead(anArgument);

						for (CtVariableAccess aVarReadFrmArgument : varReadFromArguments) {

							if (compareTypes(varAffected.getType(), aVarReadFrmArgument.getType())) {
								return anInvocation;
							}
						}
					}
				}
			}
		 } catch (Exception ex) {
		}
		
		return null;
	}
	
	public static boolean whetherpotentionalboolean(List<CtTypeReference> potentionaltypes) {
		
		for(int i=0; i<potentionaltypes.size(); i++) {
			
			if(potentionaltypes.get(i)!=null && (potentionaltypes.get(i).getSimpleName().equals("Boolean") ||
					potentionaltypes.get(i).unbox().toString().equals("boolean")))
				return true;
		}
		
		return false;
	}
	
	/**
	 * For a logical expression, if the logical expression involves comparison over
	 * primitive type variables (that is, some boolean expressions are comparing the
	 * primitive values), is there any other visible local primitive type variables
	 * that are not included in the logical expression (–chart 9). (returns a single
	 * binary value)
	 * 
	 * @param logicalExperssions
	 * @param varsInScope
	 * @param context
	 */
	private void analyzeLE3_PrimitiveWithCompatibleNotUsed( List<CtExpression> logicalExperssions, 
			List<CtVariable> varsInScope, Cntx<Object> context) {
		
		 try {
			for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {
				
                CtExpression logicalexpression = logicalExperssions.get(indexlogical);
				
				List<CtVariableAccess> varsAffected = VariableResolver.collectVariableAccess(logicalexpression, false);
				
				boolean hasCompatibleVarNoPresent = false;

				for (CtVariableAccess aVarFromAffected : varsAffected) {

					if (aVarFromAffected.getType() == null || !aVarFromAffected.getType().isPrimitive()
					// parent is binary operator
							|| // !isparentBinaryComparison(aVarFromAffected))
							aVarFromAffected.getParent(CtBinaryOperator.class) == null) 
						continue; // how if the boolean expression uses the variable multiple times?

					// For each var in scope
					for (CtVariable aVarFromScope : varsInScope) {
						// If the var name are different
						if (!aVarFromScope.getSimpleName().equals(aVarFromAffected.getVariable().getSimpleName())) {

							// Let's check if the type are compatible (i.e., the same primitive type)
							if (compareTypes(aVarFromScope.getType(), aVarFromAffected.getType())) {

								boolean presentInExpression = varsAffected.stream()
										.filter(e -> e.getVariable().getSimpleName().equals(aVarFromScope.getSimpleName()))
										.findFirst().isPresent();
								
								if (!presentInExpression) {
									hasCompatibleVarNoPresent = true;
									break;
								}
							}
						}
					}
					
					if(hasCompatibleVarNoPresent)
						break;	
				}
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression, 
						CodeFeatures.LE3_IS_COMPATIBLE_VAR_NOT_INCLUDED,
						hasCompatibleVarNoPresent, "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Besides the variables involved in a logical expression,whether there exist
	 * other local boolean variables in scope?
	 * 
	 * @param logicalExperssions
	 * @param varsInScope
	 * @param context
	 */
	private void analyzeLE4_BooleanVarNotUsed(List<CtExpression> logicalExperssions, 
			List<CtVariable> varsInScope, Cntx<Object> context) {
		
		try {
			
			for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {
				
                CtExpression logicalexpression = logicalExperssions.get(indexlogical);
				
				List<CtVariableAccess> varsAffected = VariableResolver.collectVariableAccess(logicalexpression, false);
				
				boolean hasBooleanVarNotPresent = false;
				
				for (CtVariable aVarInScope : varsInScope) {

					if (aVarInScope.getType() != null && aVarInScope.getType().unbox().toString().equals("boolean")) {

						boolean isPresentVar = varsAffected.stream()
								.filter(e -> e.getVariable().getSimpleName().equals(aVarInScope.getSimpleName()))
								.findFirst().isPresent();
						if (!isPresentVar) {
							hasBooleanVarNotPresent = true;
							break;
						}
					}
				}
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression,  
						CodeFeatures.LE4_EXISTS_LOCAL_UNUSED_VARIABLES,
						hasBooleanVarNotPresent, "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	

    private void analyzeLE5_Analyze_ComplexReference (List<CtExpression> logicalExperssions, Cntx<Object> context) {
		
		try {
			
			for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {
				
				CtExpression logicalexpression = logicalExperssions.get(indexlogical);
				
				List<CtInvocation> invocationsFromClass = logicalexpression.getElements(e -> (e instanceof CtInvocation)).stream()
						.map(CtInvocation.class::cast).collect(Collectors.toList());
				
				boolean whethercomplexreference=false;

				for(int index=0; index<invocationsFromClass.size(); index++) {
					CtInvocation invocationToStudy = invocationsFromClass.get(index);

					String target = invocationToStudy.getTarget().toString();
					
					if(!target.isEmpty()) {
					    String[] splitted = target.split("\\.");
					
					    if(splitted.length>1) {
						   whethercomplexreference = true;
						   break;
					    }
					}
				}
				
				if(!whethercomplexreference) {
			     	List<CtVariableAccess> varsFromClass = logicalexpression.getElements(e -> (e instanceof CtVariableAccess)).stream()
						.map(CtVariableAccess.class::cast).collect(Collectors.toList());
				
				    for(int index=0; index<varsFromClass.size(); index++) {
					   CtVariableAccess variableAccessToStudy = varsFromClass.get(index);
					
					   String target = variableAccessToStudy.getShortRepresentation();
					
					   if(!target.isEmpty()) {
					      String[] splitted = target.split("\\.");
					
					      if(splitted.length>2) {
						      whethercomplexreference = true;
						      break;
					      }
					   }
				    }
				}
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression,  
						CodeFeatures.LE5_COMPLEX_REFERENCE,
						whethercomplexreference, "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
    
    /**
	 * For the logical expression, whether there exists a boolean expression that
	 * starts with the "not" operator! (an exclamation mark)
	 * 
	 * @param logicalExperssions
	 * @param context
	 */	
	private void analyzeLE6_UnaryInvolved (List<CtExpression> logicalExperssions, Cntx<Object> context) {
		 
		try {
			
			for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {
				
                CtExpression logicalexpression = logicalExperssions.get(indexlogical);		
				
				List<String> unaryOps = new ArrayList();
				CtScanner scanner = new CtScanner() {

					@Override
					public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {

						super.visitCtUnaryOperator(operator);
						unaryOps.add(operator.getKind().toString());
					}
				};
				
				scanner.scan(logicalexpression);
				
				List<String> binOps = new ArrayList();
				CtScanner scannerOps = new CtScanner() {

					@Override
					public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
						super.visitCtBinaryOperator(operator);
						binOps.add(operator.getKind().toString());
					}
				};
				
				scannerOps.scan(logicalexpression);
				
				boolean containsAnd = binOps.contains(BinaryOperatorKind.AND.toString());
				boolean containsOr = binOps.contains(BinaryOperatorKind.OR.toString());
				boolean containsNot = unaryOps.contains(UnaryOperatorKind.NOT.toString());
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression,  
						CodeFeatures.LE6_HAS_NEGATION,
						(containsAnd || containsOr) && containsNot, "FEATURES_LOGICAL_EXPRESSION");
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * For the logical expression, whether there exists a boolean expression which
	 * is simply a boolean variable or a method call whose parent is CtThisAccess 
	 * (i.e., not function call, equality comparison,
	 * etc.
	 * 
	 * @param logicalExperssions
	 * @param context
	 * @param invocationssInStatement
	 */
	
	private void analyzeLE7_VarDirectlyUsed(List<CtExpression> logicalExperssions, 
			Cntx<Object> context, List<CtInvocation> invocationssInStatement) {
		
         try {
			
			for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {
				
                CtExpression logicalexpression = logicalExperssions.get(indexlogical);
				
				List<CtVariableAccess> varsAffected = VariableResolver.collectVariableAccess(logicalexpression, false);
				
				List<String> binOps = new ArrayList();
				CtScanner scanner = new CtScanner() {

					@Override
					public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
						super.visitCtBinaryOperator(operator);
						binOps.add(operator.getKind().toString());
					}
				};
				
				scanner.scan(logicalexpression);
				
				boolean containsAnd = binOps.contains(BinaryOperatorKind.AND.toString());
				boolean containsOr = binOps.contains(BinaryOperatorKind.OR.toString());
				
				boolean hasVarDirectlyUsed = false;

				for (CtVariableAccess aVarFromAffected : varsAffected) {

					CtElement parent = aVarFromAffected.getParent();
					if (parent instanceof CtExpression) {
						// First case: the parent is a binary
						if (isLogicalExpression((CtExpression) parent)) {
							hasVarDirectlyUsed = true;
							break;
						} else {
							// Second case: the parent is a negation
							if (parent instanceof CtUnaryOperator) {
								if (isLogicalExpression(((CtUnaryOperator) parent).getParent())) {
									hasVarDirectlyUsed = true;
									break;
								}
							}
						}
					}
				}
				

				boolean hasMethodDirectlyUsed = false;

				for (CtInvocation invocation : invocationssInStatement) {
					
					if(invocation.getTarget() != null && !invocation.getTarget().toString().isEmpty()) {
						continue;
					}

					CtElement parent = invocation.getParent();
					if (parent instanceof CtExpression) {
						// First case: the parent is a binary
						if (isLogicalExpression((CtExpression) parent)) {
							hasMethodDirectlyUsed = true;
							break;
						} else {
							// Second case: the parent is a negation
							if (parent instanceof CtUnaryOperator) {
								if (isLogicalExpression(((CtUnaryOperator) parent).getParent())) {
									hasMethodDirectlyUsed = true;
									break;
								}
							}
						}
					}
				}
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression, 
						CodeFeatures.LE7_SIMPLE_VAR_OR_METHOD_IN_LOGIC,
						(containsAnd || containsOr) && (hasVarDirectlyUsed || hasMethodDirectlyUsed), "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
    private void analyzeLE9_BothNULLAndNormal(List<CtExpression> logicalExperssions, Cntx<Object> context) {
		
		try {
			
			List<BinaryOperatorKind> logicOperators = Arrays.asList(BinaryOperatorKind.OR, BinaryOperatorKind.AND);

			for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {
				
                CtExpression logicalexpression = logicalExperssions.get(indexlogical);	
				
				List<CtBinaryOperator> binOps = new ArrayList();
				
				CtScanner scanner = new CtScanner() {
					@Override
					public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
						super.visitCtBinaryOperator(operator);
						
						if (logicOperators.contains(operator.getKind()))
							binOps.add(operator);
					}
				};
				
				scanner.scan(logicalexpression);
				
				List<CtExpression> atomicboolexperssions = new ArrayList();
				
				for(int index=0; index<binOps.size(); index++) {
					CtExpression left= binOps.get(index).getLeftHandOperand();
					CtExpression right=binOps.get(index).getRightHandOperand();
					if(!wheteherCompundBoolExper(binOps, left))
						atomicboolexperssions.add(left);
					if(!wheteherCompundBoolExper(binOps, right))
						atomicboolexperssions.add(right);
				}
				
				boolean whethercontainnormalcheck=false;
				boolean whethercontainnullcheck=false;
				boolean equalnullcheck=false;
				boolean notequalnullcheck=false;
				
				for(int index=0; index<atomicboolexperssions.size(); index++) {
					
					if(!whethercontainnormalcheck) {
					  if(checkNormalGuardCondition(atomicboolexperssions.get(index)))
						  whethercontainnormalcheck=true;
					}
					
				    if(checkNullCheckGuardCondition(atomicboolexperssions.get(index))) {
				    	
						 whethercontainnullcheck=true;
					     List<CtBinaryOperator> specificbinOps = atomicboolexperssions.get(index).getElements(new TypeFilter<>(CtBinaryOperator.class));
	 
					     for(int binopindex=0; binopindex<specificbinOps.size(); binopindex++) {
					    	 
					    	 if(specificbinOps.get(binopindex).getKind().toString().equals(BinaryOperatorKind.EQ.toString()))
					    		 equalnullcheck=true;
					    	 if(specificbinOps.get(binopindex).getKind().toString().equals(BinaryOperatorKind.NE.toString()))
					    		 notequalnullcheck=true;
					    	 
					    	 if(equalnullcheck && notequalnullcheck)
					    		 break;
					     }   
					}	
				    
				    if(whethercontainnormalcheck && whethercontainnullcheck && equalnullcheck && notequalnullcheck)
				    	break;
				}
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression,  
						CodeFeatures.LE9_NORMAL_CHECK,
						(whethercontainnormalcheck && !whethercontainnullcheck), "FEATURES_LOGICAL_EXPRESSION");
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression,  
						CodeFeatures.LE9_NULL_CHECK,
						(!whethercontainnormalcheck && whethercontainnullcheck), "FEATURES_LOGICAL_EXPRESSION");
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression,  
						CodeFeatures.LE9_MIX_CHECK,
						(whethercontainnormalcheck && whethercontainnullcheck), "FEATURES_LOGICAL_EXPRESSION");
				
				writeGroupedInfo(context, "logical_expression_"+Integer.toString(indexlogical)+"_"+logicalexpression,  
						CodeFeatures.LE9_EQUAL_NOTEQUAL_NULL_CHECK,
						(equalnullcheck && notequalnullcheck), "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
    
    /**
	 * Whether the number of boolean expressions in the logical expression is larger
	 * than 1
	 * 
	 * @param binOps
	 * @param tostudy
	 */
	
	private boolean wheteherCompundBoolExper(List<CtBinaryOperator> binOps, CtExpression tostudy) {
		
		boolean whethercompound=false;
		for(int index=0; index<binOps.size(); index++) {
			if(binOps.get(index).equals(tostudy)) {
				whethercompound=true;
				break;
			}
		}
		return whethercompound;
	}
	
	 private void analyzeLE10_Analyze_Atomic_Boolexps(List<CtExpression> logicalExperssions, Cntx<Object> context) {
			
			try {	
				for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {
					
					CtExpression logicalexpression = logicalExperssions.get(indexlogical);
					
					List<BinaryOperatorKind> logicOperators = Arrays.asList(BinaryOperatorKind.OR, BinaryOperatorKind.AND);
					List<CtBinaryOperator> binOps = new ArrayList();
					
					CtScanner scanner = new CtScanner() {
						@Override
						public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
							super.visitCtBinaryOperator(operator);
							
							if (logicOperators.contains(operator.getKind()))
								binOps.add(operator);
						}
					};
					
					ExpressionCapturerScanner scanner2 = new ExpressionCapturerScanner();
					scanner2.scan(logicalexpression);
					if (scanner2.toScan != null) {
						scanner.scan(scanner2.toScan);
					} else {
						scanner.scan(logicalexpression);
					}
					
					List<CtExpression> atomicboolexperssions = new ArrayList();
					
					for(int index=0; index<binOps.size(); index++) {
						CtExpression left= binOps.get(index).getLeftHandOperand();
						CtExpression right=binOps.get(index).getRightHandOperand();
						if(!wheteherCompundBoolExper(binOps, left))
							atomicboolexperssions.add(left);
						if(!wheteherCompundBoolExper(binOps, right))
							atomicboolexperssions.add(right);
					}
					
					if(binOps.size()==0)
						atomicboolexperssions.add(logicalexpression);
					
					analyzeExpressions(atomicboolexperssions, context, indexlogical,logicalexpression);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	 
		private final class ExpressionCapturerScanner extends CtScanner {
			public CtElement toScan = null;

			@Override
			public void visitCtDo(CtDo doLoop) {
				toScan = doLoop.getLoopingExpression();
			}

			@Override
			public void visitCtFor(CtFor forLoop) {
				toScan = forLoop.getExpression();
			}

			@Override
			public void visitCtIf(CtIf ifElement) {
				toScan = ifElement.getCondition();
			}

			@Override
			public void visitCtWhile(CtWhile whileLoop) {
				toScan = whileLoop.getLoopingExpression();
			}
		}
		
		private void analyzeExpressions(List<CtExpression> atomicexperssions, Cntx<Object> context, int logicalindex,CtExpression logicalexpression) {
			
			List<CtInvocation> invocationtypes = new ArrayList();
			
			List<CtExpression> comparisiontypes = new ArrayList();
			
			List<CtVariableAccess> variableaccess = new ArrayList();
			
			for(CtExpression certainexpression : atomicexperssions) {

				CtExpression transformed = certainexpression;	
	            if(certainexpression instanceof CtUnaryOperator) {
	            	transformed = ((CtUnaryOperator)(certainexpression)).getOperand();
	            }
				
				if(transformed instanceof CtInvocation) {
					invocationtypes.add((CtInvocation) transformed);
				}
			}
			
			for(CtExpression certainexpression : atomicexperssions) {

				CtExpression transformed = certainexpression;	
	            if(certainexpression instanceof CtUnaryOperator) {
	            	transformed = ((CtUnaryOperator)(certainexpression)).getOperand();
	            }
				
				if(transformed instanceof CtVariableAccess) {
					variableaccess.add((CtVariableAccess) transformed);
				}
			}
			
			for (CtExpression certainexpression : atomicexperssions) {
				List<CtBinaryOperator> binOps = certainexpression.getElements(new TypeFilter<>(CtBinaryOperator.class));
	            for(CtBinaryOperator certainbinop : binOps) {
	            	if(certainbinop.getKind().equals(BinaryOperatorKind.EQ)
	        				|| certainbinop.getKind().equals(BinaryOperatorKind.GE)
	        				|| certainbinop.getKind().equals(BinaryOperatorKind.GT)
	        				|| certainbinop.getKind().equals(BinaryOperatorKind.LE)
	        				|| certainbinop.getKind().equals(BinaryOperatorKind.LT)
	        				|| certainbinop.getKind().equals(BinaryOperatorKind.NE)) {
	            		comparisiontypes.add(certainexpression);
	            	}      				
	            }
			}
			
			List<CtExpression> invocationtarget = new ArrayList();

			for(int index=0; index<invocationtypes.size(); index++) {
				CtInvocation invocationToStudy = invocationtypes.get(index);
				
				if(!(invocationToStudy.getTarget().toString().isEmpty()))
					invocationtarget.add(invocationToStudy.getTarget());
			}
			
			List<CtExpression> comparisionleft = new ArrayList();
			List<CtExpression> comparisionright = new ArrayList();

			for(int index=0; index<comparisiontypes.size(); index++) {
				CtBinaryOperator binexpressiontosstudy = (CtBinaryOperator) comparisiontypes.get(index);
				if(!(binexpressiontosstudy.getLeftHandOperand() instanceof CtLiteral)) {
					comparisionleft.add(binexpressiontosstudy.getLeftHandOperand());
				}
				
				if(!(binexpressiontosstudy.getRightHandOperand() instanceof CtLiteral)) {
					comparisionright.add(binexpressiontosstudy.getRightHandOperand());
				}
			}
			
			boolean whetherinvtargetincomparision=false;
			
			for(int variableindex=0; variableindex<variableaccess.size(); variableindex++) {
				CtVariableAccess tostudy=variableaccess.get(variableindex);
				
				for(int invocationindex=0; invocationindex<invocationtarget.size(); invocationindex++) {
					CtExpression invocationtostudy=invocationtarget.get(invocationindex);
					
					if(invocationtostudy.equals(tostudy)) {
						whetherinvtargetincomparision = true;
						break;
					}
				}
				
				if(whetherinvtargetincomparision)
					break;
				
				for(int leftindex=0; leftindex<comparisionleft.size(); leftindex++) {
					CtExpression lefttostudy = comparisionleft.get(leftindex);
					
					List<CtExpression> expressionssFromClass = lefttostudy.getElements(e -> (e instanceof CtExpression)).stream()
							.map(CtExpression.class::cast).collect(Collectors.toList());
					
					if(expressionssFromClass.contains(tostudy)) {
						whetherinvtargetincomparision = true;
						break;
					}
				}
				
				if(whetherinvtargetincomparision)
					break;
				
				for(int rightindex=0; rightindex<comparisionright.size(); rightindex++) {
	                CtExpression righttostudy = comparisionright.get(rightindex);
					
					List<CtExpression> expressionssFromClass = righttostudy.getElements(e -> (e instanceof CtExpression)).stream()
							.map(CtExpression.class::cast).collect(Collectors.toList());
					
					if(expressionssFromClass.contains(tostudy)) {
						whetherinvtargetincomparision = true;
						break;
					}
				}
				
				if(whetherinvtargetincomparision)
					break;
			}
			
			for(int index=0; index<invocationtarget.size(); index++) {
				
				if(whetherinvtargetincomparision)
					break;
				
				CtExpression tostudy=invocationtarget.get(index);
				
				for(int leftindex=0; leftindex<comparisionleft.size(); leftindex++) {
					CtExpression lefttostudy = comparisionleft.get(leftindex);
					
					List<CtExpression> expressionssFromClass = lefttostudy.getElements(e -> (e instanceof CtExpression)).stream()
							.map(CtExpression.class::cast).collect(Collectors.toList());
					
					if(expressionssFromClass.contains(tostudy)) {
						whetherinvtargetincomparision = true;
						break;
					}
				}
				
				if(whetherinvtargetincomparision)
					break;
				
				for(int rightindex=0; rightindex<comparisionright.size(); rightindex++) {
	                CtExpression righttostudy = comparisionright.get(rightindex);
					
					List<CtExpression> expressionssFromClass = righttostudy.getElements(e -> (e instanceof CtExpression)).stream()
							.map(CtExpression.class::cast).collect(Collectors.toList());
					
					if(expressionssFromClass.contains(tostudy)) {
						whetherinvtargetincomparision = true;
						break;
					}
				}
				
				if(whetherinvtargetincomparision)
					break;
			}
			
			writeGroupedInfo(context, "logical_expression_"+Integer.toString(logicalindex)+"_"+logicalexpression,  
					CodeFeatures.LE10_ATOMIC_EXPRESSION_SAME_INVOCATION_TARGET, 
					(invocationtarget.size()>=2 &&
			        invocationtarget.size() != new HashSet<CtExpression>(invocationtarget).size()), 
			        "FEATURES_LOGICAL_EXPRESSION");
			
			writeGroupedInfo(context, "logical_expression_"+Integer.toString(logicalindex)+"_"+logicalexpression, 
					CodeFeatures.LE10_ATOMIC_EXPRESSION_COMPARISION_SAME_LEFT,
					(comparisionleft.size()>=2 &&
					comparisionleft.size() != new HashSet<CtExpression>(comparisionleft).size()), 
			        "FEATURES_LOGICAL_EXPRESSION");
			
			writeGroupedInfo(context, "logical_expression_"+Integer.toString(logicalindex)+"_"+logicalexpression, 
					CodeFeatures.LE10_ATOMIC_EXPRESSION_COMPARISION_SAME_RIGHT,
					(comparisionright.size()>=2 &&
					comparisionright.size() != new HashSet<CtExpression>(comparisionright).size()), 
			        "FEATURES_LOGICAL_EXPRESSION");
			
			writeGroupedInfo(context, "logical_expression_"+Integer.toString(logicalindex)+"_"+logicalexpression, 
					CodeFeatures.LE10_ATOMIC_EXPRESSION_MULTIPLE_VAR_AS_BOOLEAN,
					variableaccess.size()>=2, "FEATURES_LOGICAL_EXPRESSION");
			
			writeGroupedInfo(context, "logical_expression_"+Integer.toString(logicalindex)+"_"+logicalexpression, 
					CodeFeatures.LE10_ATOMIC_EXPRESSION_USED_IN_INVOCATION_COMPARISION_VARIABLE,
					whetherinvtargetincomparision, "FEATURES_LOGICAL_EXPRESSION");
			
			writeGroupedInfo(context, "logical_expression_"+Integer.toString(logicalindex)+"_"+logicalexpression, 
					CodeFeatures.LE10_CONTAINS_ALL_INVOCATION_COMPARISION_VARIABLE,
					invocationtypes.size()>0 && variableaccess.size()>0 &&
					comparisiontypes.size()>0, "FEATURES_LOGICAL_EXPRESSION");
		}
}
