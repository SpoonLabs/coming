package fr.inria.coming.codefeatures.codeanalyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeElementInfo;
import fr.inria.coming.codefeatures.CodeFeatures;
import fr.inria.coming.utils.MapCounter;
import fr.inria.coming.utils.StringDistance;
import fr.inria.coming.utils.VariableResolver;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.LineFilter;
import spoon.reflect.visitor.filter.TypeFilter;

public class VariableAnalyzer extends AbstractCodeAnalyzer {

	public VariableAnalyzer(CodeElementInfo inputinfo) {
		super(inputinfo);
	}
	
	@Override
	public void analyze() {

		analyzeV1_V6_V16(elementinfo.varsAffected,elementinfo.element, elementinfo.context, elementinfo.allMethods, 
				elementinfo.invocationsFromClass, elementinfo.parentClass);
	
		analyzeV2_AffectedDistanceVarName(elementinfo.varsAffected, elementinfo.varsInScope, elementinfo.element, elementinfo.context);
		
		analyzeV3_AffectedHasConstant(elementinfo.varsAffected, elementinfo.element, elementinfo.context);
		
		analyzeV4(elementinfo.varsAffected, elementinfo.element, elementinfo.context);
		
		analyzeV5_AffectedVariablesInTransformation(elementinfo.varsAffected, elementinfo.element, elementinfo.context);
		
		analyzeV8_TypesVarsAffected(elementinfo.varsAffected, elementinfo.element, elementinfo.context);

		analyzeV9_VarSimilarLiteral(elementinfo.element, elementinfo.context, elementinfo.parentClass, elementinfo.varsAffected);
		
		analyzV10_AffectedWithCompatibleTypes(elementinfo.varsAffected, elementinfo.varsInScope, elementinfo.element, elementinfo.context);
		
		analyzV11_ConditionWithCompatibleTypes(elementinfo.varsAffected, elementinfo.varsInScope, elementinfo.element, elementinfo.context);
		
		analyzeV1213_ReplaceVarGetAnotherInvocation(elementinfo.varsAffected,  elementinfo.context, elementinfo.invocationsFromClass, 
				elementinfo.constructorcallsFromClass);
		
		analyzeV14_VarInstanceOfClass(elementinfo.varsAffected,  elementinfo.context, elementinfo.parentClass);
		
		analyzeV15_LastthreeVariableIntroduction(elementinfo.varsAffected, elementinfo.element, elementinfo.context);
		
		analyzeV17_IsEnum(elementinfo.varsAffected, elementinfo.context, elementinfo.parentClass);
		
		analyzeFeature_Extend(elementinfo.varsAffected, elementinfo.element, elementinfo.context, elementinfo.parentClass,
				elementinfo.statements);
		
		analyzeV18_HasMethodSimilarInName (elementinfo.varsAffected, elementinfo.context, elementinfo.invocationsFromClass,
				elementinfo.allMethods, elementinfo.constructorcallsFromClass, elementinfo.parentClass);
		
		analyzeV19_VarWithSpecialName (elementinfo.varsAffected, elementinfo.context);

	}
	
	private void analyzeV19_VarWithSpecialName (List<CtVariableAccess> varsAffected, Cntx<Object> context) {
		
		 try {
			  for (CtVariableAccess aVarAffected : varsAffected) {
				
				boolean V19WithSpecialName = false;
				String varname= aVarAffected.getVariable().getSimpleName().toLowerCase(); 
				
				if(varname.endsWith("length") || varname.endsWith("size") || varname.endsWith("count") || varname.endsWith("value")
						|| varname.endsWith("key") || varname.equals("class"))
					V19WithSpecialName =true;
				
				if(!V19WithSpecialName && aVarAffected.getType()!=null && aVarAffected.getType().getSimpleName().toLowerCase().endsWith("exception"))
					V19WithSpecialName =true;
				
			
				writeGroupedInfo(context, adjustIdentifyInJson(aVarAffected),
						CodeFeatures.V19_With_Special_Name, 
						V19WithSpecialName, "FEATURES_VARS");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeV18_HasMethodSimilarInName (List<CtVariableAccess> varsAffected, Cntx<Object> context, 
			List<CtInvocation> invocationsFromClass, List allMethodsFromClass, 
			List<CtConstructorCall> allconstructorcallsFromClass, CtClass parentClass) {
		
		 try {
			  for (CtVariableAccess aVarAffected : varsAffected) {
				
				boolean V18HasMethodSimilarInName = false;
				String varname= aVarAffected.getVariable().getSimpleName();
				varname=varname.replaceAll("[^a-zA-Z0-9]", "");

				for (CtInvocation invInClass : invocationsFromClass) {
					
					String methodname=invInClass.getExecutable().getSimpleName(); 
					if(varname.length()>3) {
						if(methodname.toLowerCase().endsWith(varname.toLowerCase())) {
							V18HasMethodSimilarInName = true;
							break;
						}
					}
				}
				
				if(!V18HasMethodSimilarInName) {
					for (Object omethod : allMethodsFromClass) {

						if (!(omethod instanceof CtMethod))
							continue;

						CtMethod anotherMethod = (CtMethod) omethod;	
						String methodname=anotherMethod.getSimpleName(); 
						
						if(varname.length()>3) {
							if(methodname.toLowerCase().endsWith(varname.toLowerCase())) {
								V18HasMethodSimilarInName = true;
								break;
							}
						}
				    }
				}
				
				if(!V18HasMethodSimilarInName) {
					
					for (CtConstructorCall certainconstructorcallinclass : allconstructorcallsFromClass) {
					
						String methodname=getSimplenameForConstructorCall(certainconstructorcallinclass); 
						
						if(varname.length()>3) {
							if(methodname.toLowerCase().endsWith(varname.toLowerCase())) {
								V18HasMethodSimilarInName = true;
								break;
							}
						}	
			         }
			    }
				
				if(!V18HasMethodSimilarInName) {
					
					List<CtConstructor> allconstructorsinclass = parentClass.getElements(new TypeFilter<>(CtConstructor.class));
					
					for (CtConstructor certainconstructorinclass : allconstructorsinclass) {

						String methodname=certainconstructorinclass.getSimpleName();

						if(varname.length()>3) {
							if(methodname.toLowerCase().endsWith(varname.toLowerCase())) {
								V18HasMethodSimilarInName = true;
								break;
							}
						}
				    }
				}
			
				writeGroupedInfo(context, adjustIdentifyInJson(aVarAffected),
						CodeFeatures.V18_Has_Method_Similar_In_Name, 
						V18HasMethodSimilarInName, "FEATURES_VARS");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeFeature_Extend(List<CtVariableAccess> varsAffected, CtElement originalElement, Cntx<Object> context,
			CtClass parentClass, List<CtStatement> allstatementsinclass ) {

		for (CtVariableAccess varAffected : varsAffected) {
			
			writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V1_LOCAL_VAR_NOT_USED, 
					analyze_AffectedVariablesUsed (Arrays.asList(varAffected), originalElement, allstatementsinclass), 
					"FEATURES_VARS");
			
			writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V1_LOCAL_VAR_NOT_ASSIGNED, 
					analyze_AffectedAssigned (Arrays.asList(varAffected), originalElement), 
					"FEATURES_VARS");
			
			boolean[] varfeatures = analyze_SametypewithGuard(Arrays.asList(varAffected), originalElement, parentClass,
					allstatementsinclass);

			if(varfeatures != null) {
				
				writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD, 
						varfeatures[0], "FEATURES_VARS");
				
				writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD, 
						varfeatures[1], "FEATURES_VARS");
				
				writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD, 
						varfeatures[2], "FEATURES_VARS");
				
				writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD, 
						varfeatures[3], "FEATURES_VARS");
			}
			
			writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V4_Field_NOT_USED, 
					analyze_AffectedFielfs(Arrays.asList(varAffected), originalElement, parentClass), "FEATURES_VARS");
			
			writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V4_Field_NOT_ASSIGNED, 
					analyze_AffectedFieldAssigned(Arrays.asList(varAffected), originalElement, parentClass), "FEATURES_VARS");
			
			boolean[] varvalue78 = analyze_AffectedObjectLastAppear(Arrays.asList(varAffected), originalElement, allstatementsinclass);

            if(varvalue78 != null) {
				
            	writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V7_OBJECT_USED_IN_ASSIGNMENT, 
            			varvalue78[0], "FEATURES_VARS");
				
            	writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V8_PRIMITIVE_USED_IN_ASSIGNMENT, 
            			varvalue78[1], "FEATURES_VARS");
			}   
		}	
	}
	
	
	/**
	 * Check is a variable affected is compatible with a method type or parameter
	 * 
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	private void analyzeV1_V6_V16(List<CtVariableAccess> varsAffected, CtElement element, Cntx<Object> context,
			List allMethods, List<CtInvocation> invocationsFromClass, CtClass parentclass) {
		
		try {
			// v1: For each involved variable, whether has method definitions or method calls
			// (in the fault class) that take the type of the involved variable as one of
			// its parameters and the return type of the method is type compatible with the
			// type of the involved variable
			
			// v6 :For each involved variable, whether has methods in scope(method definitions
			// or method calls in the faulty class) that return a type which is the same or
			// compatible with the typeof the involved variable.
			

			for (CtVariableAccess varAffected : varsAffected) {

				boolean v6CurrentVarReturnCompatible = false;
				boolean v1CurrentVarCompatibleReturnAndParameterTypes = false;
				boolean v16CurrentVarParameterCompatible = false;
				
				if(varAffected.getType()!=null && !varAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("object") &&
						varAffected.getType().getQualifiedName().toString().toLowerCase().indexOf("java.lang.object")==-1) {

				  if (checkMethodDeclarationWithParameterReturnCompatibleType(allMethods, varAffected.getType()) != null
						|| checkInvocationWithParameterReturnCompatibleType(invocationsFromClass,
								varAffected.getType(), parentclass) != null) {
					  v1CurrentVarCompatibleReturnAndParameterTypes = true;
				  }

				  if (checkMethodDeclarationWithReturnCompatibleType(allMethods, varAffected.getType()) != null
						|| checkInvocationWithReturnCompatibleType(invocationsFromClass,
								varAffected.getType(), parentclass) != null) {
					  v6CurrentVarReturnCompatible = true;
				  }
				
				  if (checkMethodDeclarationWithParemetrCompatibleType(allMethods, varAffected.getType()) != null
						|| checkInvocationWithParemetrCompatibleType (invocationsFromClass,
								varAffected.getType()) != null) {
					  v16CurrentVarParameterCompatible = true;
				   }
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(varAffected), 
						CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN,
						v1CurrentVarCompatibleReturnAndParameterTypes, "FEATURES_VARS");
				
				writeGroupedInfo(context, adjustIdentifyInJson(varAffected), 
						CodeFeatures.V6_IS_METHOD_RETURN_TYPE_VAR,
						v6CurrentVarReturnCompatible, "FEATURES_VARS");
				
				writeGroupedInfo(context, adjustIdentifyInJson(varAffected), 
						CodeFeatures.V16_IS_METHOD_PARAMETER_TYPE_VAR,
						v16CurrentVarParameterCompatible, "FEATURES_VARS");
				
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public CtMethod checkMethodDeclarationWithReturnCompatibleType(List allMethods, CtTypeReference typeToMatch) {
		for (Object omethod : allMethods) {

			if (!(omethod instanceof CtMethod))
				continue;

			CtMethod anotherMethodInBuggyClass = (CtMethod) omethod;

			if (compareTypes(typeToMatch, anotherMethodInBuggyClass.getType())) {

				return anotherMethodInBuggyClass;
			}
		}
		return null;
	}
	
	public CtInvocation checkInvocationWithReturnCompatibleType(List<CtInvocation> invocationsFromClass,
			CtTypeReference type, CtClass parentclass) {
		
		List<CtBinaryOperator> binaryOperatorInClass = parentclass.getElements(e -> 
		(e instanceof CtBinaryOperator)).stream().map(CtBinaryOperator.class::cast).collect(Collectors.toList());
		
		// For each invocation found in the class
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
			

			if (compareTypes(type, inferredtype) || compareInferredTypes(type, inferredpotentionaltypes)) {
				return anInvocation;
			}
		}
		
		return null;
	}
    
    /**
	 * For each involved variable, whether has any other variables in scope that are
	 * similar in identifier name and type compatible
	 * 
	 * @param varsAffected
	 * @param varsInScope
	 * @param element
	 * @param context
	 */
	private void analyzeV2_AffectedDistanceVarName(List<CtVariableAccess> varsAffected, List<CtVariable> varsInScope,
			CtElement element, Cntx<Object> context) {
		try {

			for (CtVariableAccess aVarAffected : varsAffected) {

				boolean v2VarSimilarNameCompatibleType = false;
				boolean v2VarSimilarName = false;

				for (CtVariable aVarInScope : varsInScope) {
					if (!aVarInScope.getSimpleName().equals(aVarAffected.getVariable().getSimpleName())) {
						int dist = StringDistance.calculate(aVarInScope.getSimpleName(),
								aVarAffected.getVariable().getSimpleName());
						if ((dist > 0 && dist < 3) || nameStartEndWithOther (aVarInScope.getSimpleName(), 
								aVarAffected.getVariable().getSimpleName())) {
							v2VarSimilarName=true;
							if (compareTypes(aVarAffected.getType(), aVarInScope.getType())) {
								v2VarSimilarNameCompatibleType = true;
								break;
							}
						}
					}
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(aVarAffected), 
						CodeFeatures.V2_HAS_VAR_SIM_NAME,
						v2VarSimilarName, "FEATURES_VARS");
				
				writeGroupedInfo(context, adjustIdentifyInJson(aVarAffected), 
						CodeFeatures.V2_HAS_VAR_SIM_NAME_COMP_TYPE,
						v2VarSimilarNameCompatibleType, "FEATURES_VARS");
				
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
    private boolean nameStartEndWithOther(String name1, String name2) {
		
		if(name1.length()==1 || name2.length()==1) 
			return false;
		else {
			if(name1.startsWith(name2) || name1.endsWith(name2) ||
					name2.startsWith(name1) || name2.endsWith(name1))
				return true;
			else return false;
		}
	}
    
    /**
	 * For each involved variable, is it constant?–can assume variables whose
	 * identifier names are majorly capital letters are constant variables
	 * 
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	private void analyzeV3_AffectedHasConstant(List<CtVariableAccess> varsAffected, CtElement element,
			Cntx<Object> context) {
		try {
			
			for (CtVariableAccess aVarAffected : varsAffected) {
				boolean currentIsConstant = false;
				if (aVarAffected.getVariable() instanceof CtFieldReference &&
				// Check if it's uppercase
						aVarAffected.getVariable().getSimpleName().toUpperCase()
								.equals(aVarAffected.getVariable().getSimpleName())) {
					currentIsConstant = true;
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(aVarAffected), 
						CodeFeatures.V3_HAS_CONSTANT,
						currentIsConstant, "FEATURES_VARS");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	private void analyzeV4(List<CtVariableAccess> varsAffected, CtElement element, Cntx<Object> context) {
		
		try {
			for (CtVariableAccess varInFaulty : varsAffected) {

				CtInvocation parentInvocation = varInFaulty.getParent(CtInvocation.class);
				int appearsInParams = 0;
				MapCounter<CtElement> parameterFound = new MapCounter<>();
				if (parentInvocation != null) {
					List<CtElement> arguments = parentInvocation.getArguments();
					for (CtElement i_Argument : arguments) {
						List<CtVariableAccess> varsAccessInParameter = VariableResolver.collectVariableRead(i_Argument);
						// .stream().filter(e -> e.getRoleInParent().equals(CtRole.PARAMETER))
						// .collect(Collectors.toList());
						if (varsAccessInParameter.contains(varInFaulty)) {
							appearsInParams++;

							if (!parameterFound.containsKey(varInFaulty)) {
								
								writeGroupedInfo(context, adjustIdentifyInJson(varInFaulty), 
										CodeFeatures.V4_FIRST_TIME_USED_AS_PARAMETER,
										true, "FEATURES_VARS");
							} else {
								
								writeGroupedInfo(context, adjustIdentifyInJson(varInFaulty), 
										CodeFeatures.V4_FIRST_TIME_USED_AS_PARAMETER,
										false, "FEATURES_VARS");
							}
							parameterFound.add(varInFaulty);
						}
						
						if(appearsInParams > 1)
							break;
					}
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(varInFaulty), 
						CodeFeatures.V4B_USED_MULTIPLE_AS_PARAMETER,
						(appearsInParams > 1), "FEATURES_VARS");
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * For each involved variable, is there any other variable in scope that is a
	 * certain function transformation of the involved variable
	 * 
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	@SuppressWarnings("rawtypes")
	private void analyzeV5_AffectedVariablesInTransformation(List<CtVariableAccess> varsAffected, CtElement element,
			Cntx<Object> context) {
		try {
			CtMethod methodParent = element.getParent(CtMethod.class);

			List<CtExpression> assignments = new ArrayList<>();

			CtScanner assignmentScanner = new CtScanner() {

				@Override
				public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {
					if (assignement.getAssignment() != null)
						assignments.add(assignement.getAssignment());
				}

				@Override
				public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
					if (localVariable.getAssignment() != null)
						assignments.add(localVariable.getAssignment());
				}

			};
			assignmentScanner.scan(methodParent);

			for (CtVariableAccess variableAffected : varsAffected) {

				boolean v5_currentVarHasvar = false;

				for (CtExpression assignment : assignments) {

					if (!isElementBeforeVariable(variableAffected, assignment))
						continue;

					// let's collect the var access in the right part
					List<CtVariableAccess> varsInRightPart = VariableResolver.collectVariableRead(assignment); // VariableResolver.collectVariableAccess(assignment);

					// if the var access in the right is the same that the affected
					for (CtVariableAccess varInAssign : varsInRightPart) {
						if (hasSameName(variableAffected, varInAssign)) {

							v5_currentVarHasvar = true;
							break;
						}
					}
				}

				writeGroupedInfo(context, adjustIdentifyInJson(variableAffected), 
						CodeFeatures.V5_HAS_VAR_IN_TRANSFORMATION,
						(v5_currentVarHasvar), "FEATURES_VARS");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void analyzeV8_TypesVarsAffected(List<CtVariableAccess> varsAffected, CtElement element,
			Cntx<Object> context) {
		try {

			for (CtVariableAccess aVariableAccess : varsAffected) {

				CtVariable ctVariable = aVariableAccess.getVariable().getDeclaration();
				boolean isPrimitive = false; 
				boolean isObject = false;
				
				if (ctVariable != null && ctVariable.getReference() != null
						&& ctVariable.getReference().getType() != null) {
					if (ctVariable.getReference().getType().isPrimitive() ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("string") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("list") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().indexOf("string")!=-1||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("long") || 
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("boolean") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("double") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("byte")||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("short")||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("float") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("chart") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("character") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("integer")||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("string[]") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("long[]") || 
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("boolean[]") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("double[]") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("byte[]")||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("short[]")||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("float[]") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("chart[]") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("character[]") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("integer[]") ||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("java.util.")||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("java.nio.")||
					ctVariable.getReference().getType().getQualifiedName().toString().toLowerCase().endsWith("java.io.")) {
						isPrimitive = true;
					} 
				}
				
				if(isPrimitive)
					isObject=false;
				else isObject=true;
				
				writeGroupedInfo(context, adjustIdentifyInJson(aVariableAccess),
						CodeFeatures.V8_VAR_PRIMITIVE, isPrimitive, "FEATURES_VARS");
				writeGroupedInfo(context, adjustIdentifyInJson(aVariableAccess),
						CodeFeatures.V8_VAR_OBJECT, isObject, "FEATURES_VARS");
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeV9_VarSimilarLiteral(CtElement element, Cntx<Object> context, CtClass parentClass,
			List<CtVariableAccess> varsAffected) {
		
		try {
			List<CtLiteral> allliteralsFromClass = new ArrayList();
			if(parentClass!=null)
			    allliteralsFromClass = parentClass.getElements(e -> (e instanceof CtLiteral)).stream()
					.map(CtLiteral.class::cast).collect(Collectors.toList());
			
			for (CtVariableAccess varAffected : varsAffected) {

				boolean currentVarhasSimilarLiteral = false;	
                for (CtLiteral literalinclass : allliteralsFromClass) {
                	
					String[] anotherLiteralTypeAndValue=getLiteralTypeAndValue(literalinclass);

                	if(compareVarAccessAndLiteralType(anotherLiteralTypeAndValue[0], varAffected)) {
                		currentVarhasSimilarLiteral=true;
                		break;
                	}
                }
                	
                writeGroupedInfo(context, adjustIdentifyInJson(varAffected), CodeFeatures.V9_VAR_TYPE_Similar_Literal,
                		currentVarhasSimilarLiteral, "FEATURES_VARS");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	  
	  public boolean compareVarAccessAndLiteralType(String literaltype, CtVariableAccess varaccess) {
			
			Boolean typecompatiable=false;
			
			if(varaccess.getType()!=null) {
			   if(varaccess.getType().toString().toLowerCase().endsWith("string")) {
				   if(literaltype.equals("string"))
				      typecompatiable=true; 
			   }
			   else  {
			      if(varaccess.getType().isPrimitive()) {
				     if(varaccess.getType().toString().toLowerCase().endsWith("char")) {
					    if(literaltype.equals("char"))
						   typecompatiable=true; 
				     }
				     else if(varaccess.getType().toString().toLowerCase().endsWith("boolean")) {
					    if(literaltype.equals("boolean"))
					       typecompatiable=true; 
				     }
				     else {
					   if(literaltype.equals("numerical"))
					       typecompatiable=true; 
				     }
			     }
			  }
			}
			return typecompatiable;
		}
	  
		private void analyzV10_AffectedWithCompatibleTypes(List<CtVariableAccess> varsAffected, List<CtVariable> varsInScope,
				CtElement element, Cntx<Object> context) {
			try {
				
				for (CtVariableAccess aVariableAccessInStatement : varsAffected) {
					boolean currentHasSimType = false;
					for (CtVariable aVariableInScope : varsInScope) {
						if (!aVariableInScope.getSimpleName().equals(aVariableAccessInStatement.getVariable().getSimpleName())) {
							if (compareTypes(aVariableInScope.getType(), aVariableAccessInStatement.getType())) {
								currentHasSimType=true;
								break;
							}
						}
					}
					
					writeGroupedInfo(context, adjustIdentifyInJson(aVariableAccessInStatement),
							CodeFeatures.V10_VAR_TYPE_Similar_VAR, currentHasSimType, "FEATURES_VARS");
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		private void analyzV11_ConditionWithCompatibleTypes (List<CtVariableAccess> varsAffected, List<CtVariable> varsInScope,
				CtElement element, Cntx<Object> context) {
			
			try {
				for (CtVariableAccess aVariableAccessInStatement : varsAffected) {
					boolean currentHasSimType = false;	
					CtStatement parent = aVariableAccessInStatement.getParent(new LineFilter());
					CtElement parentCondition = getPotentionalParentCondition(parent);
					CtElement expression = retrieveExpressionToStudy(parentCondition);

					List<CtVariableAccess> varsInExpression =new ArrayList<>();
					if(expression!=null)
					    varsInExpression = VariableResolver.collectVariableAccess(expression, false);

					List<CtLocalVariable> localsVariable = new ArrayList<>();

					CtScanner scanner = new CtScanner() {
						
						@Override
						public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {

							localsVariable.add(localVariable);
						}
					};

					scanner.scan(expression);
					
					for (CtVariableAccess aVariableInScope : varsInExpression) {

						if (!aVariableInScope.getVariable().getSimpleName().equals(aVariableAccessInStatement.getVariable().getSimpleName())) {
							if (compareTypes(aVariableInScope.getType(), aVariableAccessInStatement.getType())) {
								currentHasSimType=true;
								break;
							}
						}
					}
					
					if(!currentHasSimType) {
						for (CtLocalVariable aVariableInScope : localsVariable) {

							if (!aVariableInScope.getReference().getSimpleName().equals(aVariableAccessInStatement.getVariable().getSimpleName())) {
								if (compareTypes(aVariableInScope.getType(), aVariableAccessInStatement.getType())) {
									currentHasSimType=true;
									break;
								}
							}
						}
					}
					
					writeGroupedInfo(context, adjustIdentifyInJson(aVariableAccessInStatement),
							CodeFeatures.V11_VAR_COMPATIBLE_TYPE_IN_CONDITION, currentHasSimType, "FEATURES_VARS");
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		private CtElement getPotentionalParentCondition (CtStatement toStudy) {
			CtElement parent;
			parent=toStudy;
			do {
				parent= parent.getParent();
			} while (!whetherConditionalStat(parent) && parent!=null);
			
			return parent;
		}
		
		private boolean whetherConditionalStat(CtElement input) {
			
			if(input instanceof CtIf || input instanceof CtWhile || input instanceof CtFor || input
					instanceof CtForEach)
				return true;
			else return false;	
		}
		
		public CtElement retrieveExpressionToStudy(CtElement element) {

			if (element instanceof CtIf) {
				return (((CtIf) element).getCondition());
			} else if (element instanceof CtWhile) {
				return (((CtWhile) element).getLoopingExpression());
			} else if (element instanceof CtFor) {
				return (((CtFor) element).getExpression());
			} else if (element instanceof CtForEach) {
				return (((CtForEach) element).getExpression());
			}  else
				return (element);
		}
		
		private void analyzeV1213_ReplaceVarGetAnotherInvocation (List<CtVariableAccess> varsAffected, Cntx<Object> context,
				List<CtInvocation> invocationsFromClass, List<CtConstructorCall> constructorcallsFromClass) {
			
			try {

				for (CtVariableAccess varAffected : varsAffected) {
					
					CtInvocation parentInvocation = varAffected.getParent(CtInvocation.class);
					
					boolean v12ReplacewithVarCurrent = false;
					
					boolean v13ReplacewithInvocationCurrent = false;

					if (parentInvocation != null) {
						
						List<CtElement> arguments = parentInvocation.getArguments();
						
						for (CtInvocation specificinvocation : invocationsFromClass) {

							if(parentInvocation.equals(specificinvocation))
								continue;
							
							List<CtElement> specificarguments = specificinvocation.getArguments();

							if(parentInvocation.getExecutable().getSimpleName().equals
									(specificinvocation.getExecutable().getSimpleName()) && 
									arguments.size() == specificarguments.size()) {
								
								int[] comparisionresult= argumentDiff(arguments, specificarguments, varAffected);
								
								if(comparisionresult[0]==1 && comparisionresult[1]==1)
									v12ReplacewithVarCurrent =true;
								
								if(comparisionresult[0]==1 && comparisionresult[2]==1)
									v13ReplacewithInvocationCurrent =true;
							}
							
							if(v12ReplacewithVarCurrent && v13ReplacewithInvocationCurrent)
								break;
						}
					}
					
					if(!v12ReplacewithVarCurrent || !v13ReplacewithInvocationCurrent) {
						
						CtConstructorCall parentConstructorCall = varAffected.getParent(CtConstructorCall.class);

						if (parentConstructorCall != null) {
							
							List<CtElement> arguments = parentConstructorCall.getArguments();
							
							for (CtConstructorCall specificonstructorcall : constructorcallsFromClass) {

								if(parentConstructorCall.equals(specificonstructorcall))
									continue;
								
								List<CtElement> specificarguments = specificonstructorcall.getArguments();

								if(getSimplenameForConstructorCall(parentConstructorCall).equals
										(getSimplenameForConstructorCall(specificonstructorcall)) && 
										arguments.size() == specificarguments.size()) {
									
									int[] comparisionresult= argumentDiff(arguments, specificarguments, varAffected);
									
									if(comparisionresult[0]==1 && comparisionresult[1]==1)
										v12ReplacewithVarCurrent =true;
									
									if(comparisionresult[0]==1 && comparisionresult[2]==1)
										v13ReplacewithInvocationCurrent =true;
								}
								
								if(v12ReplacewithVarCurrent && v13ReplacewithInvocationCurrent)
									break;
							}
						}
					}
					
					writeGroupedInfo(context, adjustIdentifyInJson(varAffected), 
							CodeFeatures.V12_VAR_Invocation_VAR_REPLACE_BY_VAR,
							(v12ReplacewithVarCurrent), "FEATURES_VARS");
					
					writeGroupedInfo(context, adjustIdentifyInJson(varAffected), 
							CodeFeatures.V13_VAR_Invocation_VAR_REPLACE_BY_INVOCATION,
							(v13ReplacewithInvocationCurrent), "FEATURES_VARS");
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		private int[] argumentDiff(List<CtElement> argumentsoriginal, List<CtElement> argumentsother, CtVariableAccess varaccess) {
			
			int numberdiffargument =0;
			int numberdiffvarreplacebyvar =0;
			int numberdiffvarreplacebymethod =0;
			
			for(int index=0; index<argumentsoriginal.size(); index++) {
				
				CtElement original = argumentsoriginal.get(index);
				CtElement other = argumentsother.get(index);
				
				if(original.equals(other)) {
					// same
				} else {
					numberdiffargument+=1;
					if(original instanceof CtVariableAccess && original.equals(varaccess)) {
						if(other instanceof CtVariableAccess)
							numberdiffvarreplacebyvar+=1;
						else if(other instanceof CtInvocation || other instanceof CtConstructorCall)
							numberdiffvarreplacebymethod+=1;
						else {
							// do nothing
						}
					}
				}
			}

			int diffarray[]=new int[3];
			diffarray[0]=numberdiffargument;
			diffarray[1]=numberdiffvarreplacebyvar;
			diffarray[2]=numberdiffvarreplacebymethod;

	        return diffarray;
		}
		
		private void analyzeV14_VarInstanceOfClass (List<CtVariableAccess> varsAffected, Cntx<Object> context,
				CtClass parentClass) {
			
			try {
				for (CtVariableAccess varAffected : varsAffected) {
					
					boolean v14VarInstanceOfClass= false;
					
					if(varAffected.getType()!=null) {
					   if(varAffected.getType().toString().equals(parentClass.getQualifiedName()) ||
							varAffected.getType().toString().endsWith(parentClass.getQualifiedName()) ||
							parentClass.getQualifiedName().endsWith(varAffected.getType().toString()))
						v14VarInstanceOfClass=true;
					}
					
					writeGroupedInfo(context, adjustIdentifyInJson(varAffected), 
							CodeFeatures.V14_VAR_INSTANCE_OF_CLASS,
							(v14VarInstanceOfClass), "FEATURES_VARS");
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		private void analyzeV15_LastthreeVariableIntroduction (List<CtVariableAccess> varsAffected, CtElement element,
				Cntx<Object> context) {
			try {
				
				CtExecutable methodParent = element.getParent(CtExecutable.class);

				if (methodParent == null)
					// the element is not in a method.
					return;
				
				List<CtStatement> statements=methodParent.getElements(new LineFilter());

				// For each variable affected
				for (CtVariableAccess variableAffected : varsAffected) {

					List<CtStatement> statementbefore = new ArrayList<>();
					
					boolean lastthreesametypeloc=false;

					for (CtStatement aStatement : statements) {

						CtStatement parent = variableAffected.getParent(new LineFilter());
											
						if (!isElementBeforeVariable(variableAffected, aStatement))
							continue;
						
						if (isStatementInControl(parent, aStatement) || parent==aStatement)
							continue;
						
						if(aStatement instanceof CtIf || aStatement instanceof CtLoop) 
							continue;
						
						statementbefore.add(aStatement);
					}
					
					List<CtStatement> statinterest = new ArrayList<>();
					
					if(statementbefore.size()<=4)
						statinterest=statementbefore;
					else {
						statinterest.add(statementbefore.get(statementbefore.size()-1));
						statinterest.add(statementbefore.get(statementbefore.size()-2));
						statinterest.add(statementbefore.get(statementbefore.size()-3));
						statinterest.add(statementbefore.get(statementbefore.size()-4));
					}

					for (int index=0; index< statinterest.size(); index++) {
						if(statinterest.get(index) instanceof CtLocalVariable) {
							CtLocalVariable ctLocalVariable=(CtLocalVariable)statinterest.get(index);

							if (!ctLocalVariable.getReference().getSimpleName()
									.equals(variableAffected.getVariable().getSimpleName()) 
									&& compareTypes(ctLocalVariable.getType(), variableAffected.getType())) {
								lastthreesametypeloc = true;
								break;
							}
						}
					}
					
					writeGroupedInfo(context, adjustIdentifyInJson(variableAffected), 
							CodeFeatures.V15_VAR_LAST_THREE_SAME_TYPE_LOC,
							(lastthreesametypeloc), "FEATURES_VARS");
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		private void analyzeV17_IsEnum (List<CtVariableAccess> varsAffected, Cntx<Object> context, CtClass parentClass) {
			try {
				
				if (parentClass == null)
					return;
				// Get all enums
				List<CtEnum> enums = parentClass.getElements(new TypeFilter<>(CtEnum.class));

				// For each var access
				for (CtVariableAccess varAccess : varsAffected) {
					
					boolean isVarAccessTypeEnum = false;
					
					if (varAccess.getVariable().getType() != null
							&& enums.contains(varAccess.getVariable().getType().getDeclaration())) {
						isVarAccessTypeEnum = true;
					}
					
					writeGroupedInfo(context, adjustIdentifyInJson(varAccess), 
							CodeFeatures.V17_VAR_IS_ENUMERATION,
							(isVarAccessTypeEnum), "FEATURES_VARS");
				}

			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
}
