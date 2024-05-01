package fr.inria.coming.codefeatures.codeanalyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeElementInfo;
import fr.inria.coming.codefeatures.CodeFeatures;
import fr.inria.coming.utils.StringDistance;
import fr.inria.coming.utils.VariableResolver;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.reference.CtTypeReferenceImpl;

public class MethodAnalyzer extends AbstractCodeAnalyzer {

	public MethodAnalyzer (CodeElementInfo inputinfo) {
		super(inputinfo);
	}
	
	@Override
	public void analyze() {

		analyzeM1_eM2_M3_M4_M8_M9SimilarMethod(elementinfo.element, elementinfo.context, elementinfo.parentClass, 
				elementinfo.allMethods, elementinfo.invocations);
		analyzeM5(elementinfo.element, elementinfo.context, elementinfo.invocations, elementinfo.varsInScope);
		analyzeM67_ReplaceVarGetAnotherInvocation(elementinfo.invocations,  elementinfo.context, 
				elementinfo.invocationsFromClass, elementinfo.constructorcallsFromClass);
		
		analyzeFeature_ExtendFromVar(elementinfo.context, elementinfo.invocations);
		
		analyzeMethodFeature_Extend(elementinfo.element, elementinfo.context, elementinfo.parentClass,
				elementinfo.invocationsFromClass, elementinfo.invocations);
		
		analyzeWhetherMethodWraptedM10(elementinfo.invocations,  elementinfo.context, 
				elementinfo.invocationsFromClass, elementinfo.constructorcallsFromClass);
		
		analyzeWhetherMethodSatrtsWithGetM11 (elementinfo.invocations, elementinfo.context);

		analyzeVarMethodNameSimilarM12 (elementinfo.invocations,  elementinfo.varsInScope, elementinfo.context);
		
		analyzeVarMethodArgumentPrimitiveM13 (elementinfo.invocations,  elementinfo.context);

	}
	
	private void analyzeVarMethodArgumentPrimitiveM13 (List<CtInvocation> invocationsaffected, Cntx<Object> context) {
		
		 try {
			 for (CtInvocation invAffected : invocationsaffected) {
				
				boolean M13ArgumentHasPrimitive = false;
				
				List<CtExpression> invocationArguments = invAffected.getArguments();
				
				for(int index=0; index<invocationArguments.size(); index++ ) {
					
					CtExpression certainexpression=invocationArguments.get(index);
					
					if (certainexpression.getType()!=null && (certainexpression.getType().isPrimitive() || 
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("string") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("list") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().indexOf("string")!=-1 ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("long") || 
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("boolean") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("double") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("byte")||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("short")||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("float") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("chart") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("character") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("integer")||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("string[]") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("long[]") || 
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("boolean[]") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("double[]") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("byte[]")||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("short[]")||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("float[]") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("chart[]") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("character[]") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("integer[]") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().startsWith("java.util.") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().startsWith("java.nio.") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().startsWith("java.io."))) {
						
						M13ArgumentHasPrimitive = true;
						break;
					}
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(invAffected),
						CodeFeatures.M13_Argument_Has_Primitive, 
						M13ArgumentHasPrimitive, "FEATURES_METHODS");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeWhetherMethodSatrtsWithGetM11 (List<CtInvocation> invocationsaffected, Cntx<Object> context) {
		
		 try {
			 for (CtInvocation invAffected : invocationsaffected) {
				
				boolean M11SatrtWithGet = false;
				
				String name=invAffected.getExecutable().getSimpleName();
				
				if(name.toLowerCase().startsWith("get"))
					M11SatrtWithGet = true;
				
				writeGroupedInfo(context, adjustIdentifyInJson(invAffected),
						CodeFeatures.M11_Satrt_With_Get, 
						M11SatrtWithGet, "FEATURES_METHODS");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeVarMethodNameSimilarM12 (List<CtInvocation> invocationsaffected, List<CtVariable> scopevars, Cntx<Object> context) {
		
		 try {
			 for (CtInvocation invAffected : invocationsaffected) {
				
				boolean M12hasvarsimiplarinname = false;
				
				String methodname=invAffected.getExecutable().getSimpleName(); 
				
				for (CtVariable aVarInScope : scopevars) {
					
					String varname=aVarInScope.getSimpleName();
					varname=varname.replaceAll("[^a-zA-Z0-9]", "");
					if(varname.length()>3) {
						if(methodname.toLowerCase().endsWith(varname.toLowerCase()) || methodname.toLowerCase().equals("length") ||
								methodname.toLowerCase().contains("version") || methodname.toLowerCase().contains("clone")) {
							M12hasvarsimiplarinname = true;
							break;
						}
					}
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(invAffected),
						CodeFeatures.M12_Has_Var_Similar_In_Name, 
						M12hasvarsimiplarinname, "FEATURES_METHODS");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeWhetherMethodWraptedM10 (List<CtInvocation> invocationsaffected, Cntx<Object> context,
			List<CtInvocation> invocationsFromClass, List<CtConstructorCall> constructorcallsFromClass) {
		
		 try {
			 for (CtInvocation invAffected : invocationsaffected) {
				
				boolean M10wrapttedinothers = false;
				
				for (CtInvocation specificinvocation : invocationsFromClass) {
					
					if(invAffected.equals(specificinvocation))
						continue;
					
					List<CtElement> specificarguments = specificinvocation.getArguments(); 
					
					for(int index=0; index< specificarguments.size(); index++) {
						
						CtElement specificargument = specificarguments.get(index);
						
						List<CtInvocation> invocationsinargument = specificargument.getElements(e -> (e instanceof CtInvocation)).stream()
								.map(CtInvocation.class::cast).collect(Collectors.toList());
						
						for(int innerindex=0; innerindex<invocationsinargument.size(); innerindex++) {
						
						   if(invocationsinargument.get(innerindex).getExecutable().getSimpleName().
								   equals(invAffected.getExecutable().getSimpleName())) {
							   M10wrapttedinothers = true;
							   break;
						   }	
						}
						
						if(M10wrapttedinothers)
							break;
					}
					
					if(M10wrapttedinothers)
						break;
				}
				
				if(!M10wrapttedinothers) {
					
					for (CtConstructorCall specificconstructor : constructorcallsFromClass) {
						
						List<CtElement> specificarguments = specificconstructor.getArguments(); 
						
						for(int index=0; index< specificarguments.size(); index++) {
							
							CtElement specificargument = specificarguments.get(index);
							
							List<CtInvocation> invocationsinargument = specificargument.getElements(e -> (e instanceof CtInvocation)).stream()
									.map(CtInvocation.class::cast).collect(Collectors.toList());
							
							for(int innerindex=0; innerindex<invocationsinargument.size(); innerindex++) {
								
								   if(invocationsinargument.get(innerindex).getExecutable().getSimpleName().
										   equals(invAffected.getExecutable().getSimpleName())) {
									   M10wrapttedinothers = true;
									   break;
								  }	
							 }

							if(M10wrapttedinothers)
								break;
						}
						
						if(M10wrapttedinothers)
							break;
					}
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(invAffected),
						CodeFeatures.M10_WRAPTTED_IN_OTHER_CALLS, 
						M10wrapttedinothers, "FEATURES_METHODS");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeFeature_ExtendFromVar (Cntx<Object> context, List<CtInvocation> invocations) {
		
        for(CtInvocation invocationAffected : invocations) {
        	
        	CtExpression targetexpression = invocationAffected.getTarget();
        	
			List<CtVariableAccess> varsAffected = VariableResolver.collectVariableAccess(targetexpression, false);
			
			boolean M1_LOCAL_VAR_NOT_USED = false;
			boolean M1_LOCAL_VAR_NOT_ASSIGNED =false;
			boolean M2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD=false;
			boolean M5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD =false;
			boolean M2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD =false;
			boolean M5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD =false;
			boolean M4_Field_NOT_USED =false;
			boolean M4_Field_NOT_ASSIGNED = false;
			boolean M7_OBJECT_USED_IN_ASSIGNMENT =false;
			boolean M8_PRIMITIVE_USED_IN_ASSIGNMENT =false;
			
			for (CtVariableAccess aVarAffected : varsAffected) {	
				
				try {	
				    Cntx<Object> featuresVar = (Cntx<Object>) context.getInformation().get("FEATURES_VARS");  
				    
				    if(featuresVar!=null) {
				    	
						Cntx<Object> particularVar = (Cntx<Object>) featuresVar.getInformation().
								get(adjustIdentifyInJson(aVarAffected));
						
						if(particularVar!=null) {
						
						    if(Boolean.valueOf(particularVar.getInformation().get("V1_LOCAL_VAR_NOT_USED").toString()))
							    M1_LOCAL_VAR_NOT_USED = true;
						
						    if(Boolean.valueOf(particularVar.getInformation().get("V1_LOCAL_VAR_NOT_ASSIGNED").toString()))
							    M1_LOCAL_VAR_NOT_ASSIGNED = true;

							if(particularVar.isBooleanValueTrue("V2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD"))
						    	M2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD = true;

							if(particularVar.isBooleanValueTrue("V5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD"))
						    	M5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD = true;

							if(particularVar.isBooleanValueTrue("V2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD"))
						    	M2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD = true;

							if(particularVar.isBooleanValueTrue("V5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD"))
						    	M5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD = true;
						
						    if(Boolean.valueOf(particularVar.getInformation().get("V4_Field_NOT_USED").toString()))
						    	M4_Field_NOT_USED = true;
						
						    if(Boolean.valueOf(particularVar.getInformation().get("V4_Field_NOT_ASSIGNED").toString()))
						    	M4_Field_NOT_ASSIGNED = true;

							if(particularVar.isBooleanValueTrue("V7_OBJECT_USED_IN_ASSIGNMENT"))
						    	M7_OBJECT_USED_IN_ASSIGNMENT = true;

							if(particularVar.isBooleanValueTrue("V8_PRIMITIVE_USED_IN_ASSIGNMENT"))
						    	M8_PRIMITIVE_USED_IN_ASSIGNMENT = true;
						}
				    }
				} catch (Throwable e) {
					System.err.println("error caught at " + new Exception().getStackTrace()[0].toString());
					e.printStackTrace();
				}
			}

			writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected),
					CodeFeatures.M1_LOCAL_VAR_NOT_USED,
					M1_LOCAL_VAR_NOT_USED, "FEATURES_METHODS");  
			
			writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected),
					CodeFeatures.M1_LOCAL_VAR_NOT_ASSIGNED,
					M1_LOCAL_VAR_NOT_ASSIGNED, "FEATURES_METHODS"); 
			
			writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected),
					CodeFeatures.M2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD,
					M2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD, "FEATURES_METHODS"); 
			
			writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected),
					CodeFeatures.M5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD,
					M5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD, "FEATURES_METHODS");
			
			writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected),
					CodeFeatures.M2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD,
					M2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD, "FEATURES_METHODS");
			
			writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected),
					CodeFeatures.M5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD,
					M5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD, "FEATURES_METHODS");
			
			writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected),
					CodeFeatures.M4_Field_NOT_USED,
					M4_Field_NOT_USED, "FEATURES_METHODS");
			
			writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected),
					CodeFeatures.M4_Field_NOT_ASSIGNED,
					M4_Field_NOT_ASSIGNED, "FEATURES_METHODS");
			
			writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected),
					CodeFeatures.M7_OBJECT_USED_IN_ASSIGNMENT,
					M7_OBJECT_USED_IN_ASSIGNMENT, "FEATURES_METHODS");
			
			writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected),
					CodeFeatures.M8_PRIMITIVE_USED_IN_ASSIGNMENT,
					M8_PRIMITIVE_USED_IN_ASSIGNMENT, "FEATURES_METHODS");
		}	
    }
	
	
	private void analyzeMethodFeature_Extend (CtElement originalElement, Cntx<Object> context,
			CtClass parentClass, List<CtInvocation> invocationsFromClass, List<CtInvocation> invocations) {
		
		List<CtConstructorCall> emptyconstructorcallfromclass = new ArrayList<CtConstructorCall>();
		List<CtConstructorCall> emptyconstructorcallunderstudy = new ArrayList<CtConstructorCall>();

		for(CtInvocation invocationAffected : invocations) {
            
            boolean[] methdofeature91012 = analyze_SamerMethodWithGuardOrTrywrap(originalElement, parentClass, invocationsFromClass,
            		Arrays.asList(invocationAffected), emptyconstructorcallfromclass, emptyconstructorcallunderstudy);

            if(methdofeature91012 != null) {
				
            	writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected), CodeFeatures.M9_METHOD_CALL_WITH_NORMAL_GUARD, 
            			methdofeature91012[0], "FEATURES_METHODS");
				
            	writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected), CodeFeatures.M10_METHOD_CALL_WITH_NULL_GUARD, 
            			methdofeature91012[1], "FEATURES_METHODS");
            	
            	writeGroupedInfo(context, adjustIdentifyInJson(invocationAffected), CodeFeatures.M12_METHOD_CALL_WITH_TRY_CATCH, 
            			methdofeature91012[2], "FEATURES_METHODS");
			}         
		}	
	}
	
	private void analyzeM1_eM2_M3_M4_M8_M9SimilarMethod(CtElement element, Cntx<Object> context, CtClass parentClass,
			List allMethodsFromClass, List<CtInvocation> invocations) {
		try {
			// m1: For each method invocation, whether the method has overloaded method
			
			// m2: For each method invocation, whether there exist methods that return the same
			// type (or type compatible) and are similar in identifier name with the called
			// method (again, we limit the search to the faulty class, search both method
			// definition and method invocations in the faulty class

			// m3: For each method invocation, whether has method definitions or method calls
			// (in the fault class) that take the return type of the method invocation as
			// one
			// of its parameters and the return type of the method is type compatible with
			// the return type of the method invocation.

			// m4: For each method invocation, whether the types of some of its parameters are
			// same or compatible with the return type of the method.

			for (CtInvocation invocation : invocations) {

				boolean m1methodHasSameName = false;
				boolean m2methodhasMinDist = false;
				boolean m3methodhasCompatibleParameterAndReturnWithOtherMethod = false;
				boolean m4methodHasCompatibleParameterAndReturnSameMethod = false;
				boolean m8methodprimitive = false;
				boolean m9methodobjective = false;
				
				if ((invocation.getType()!=null && (invocation.getType().isPrimitive() || 
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("string") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("list") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().indexOf("string")!=-1||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("long") || 
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("boolean") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("double") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("byte")||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("short")||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("float") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("chart") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("character") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("integer")||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("string[]") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("long[]") || 
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("boolean[]") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("double[]") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("byte[]")||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("short[]")||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("float[]") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("chart[]") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("character[]") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("integer[]") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("java.util.") ||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("java.nio.")||
						invocation.getType().getQualifiedName().toString().toLowerCase().endsWith("java.io."))) || 
						whetherhasprimitive(inferPotentionalTypes(invocation, parentClass))) {
					
					m8methodprimitive = true;
				}
				
				if(m8methodprimitive)
					m9methodobjective = false;
				else m9methodobjective = true;
				
				for (Object anObjArgument : invocation.getArguments()) {
					CtExpression anArgument = (CtExpression) anObjArgument;

					if (compareTypes(invocation.getType(), anArgument.getType())) {
						m4methodHasCompatibleParameterAndReturnSameMethod = true;
						break;
					}
				}

				for (Object omethod : allMethodsFromClass) {

					if (!(omethod instanceof CtMethod))
						continue;

					CtMethod anotherMethod = (CtMethod) omethod;
					// Ignoring if it's the same
					if (anotherMethod == null || anotherMethod.getSignature().equals(invocation.getExecutable().getSignature()))
						continue;

					if (anotherMethod.getSimpleName().equals(invocation.getExecutable().getSimpleName())) {
						// It's override
						m1methodHasSameName = true;
					}
					
					// try to get information from the invocaked class
					if (!m1methodHasSameName) {
						List<CtInvocation> invocationsFromAnotherMethod = anotherMethod
								.getElements(e -> (e instanceof CtInvocation)).stream().map(CtInvocation.class::cast)
								.collect(Collectors.toList());
						for (CtInvocation ctInvocation : invocationsFromAnotherMethod) {
							  if(!ctInvocation.getExecutable().getSignature().equals(invocation.getExecutable().getSignature()))
							     if(ctInvocation.getExecutable().getSimpleName().equals(invocation.getExecutable().getSimpleName())) {
								    m1methodHasSameName = true;
								    break;
							}		
						}
					}
					
					// If the return types are compatibles
					if (anotherMethod.getType() != null && invocation.getType() != null) {

						// Check if the method has the return type compatible with the affected
						// invocation
						boolean compatibleReturnTypes = compareTypes(anotherMethod.getType(),
								invocation.getType());
						if (compatibleReturnTypes) {
							// Check name similarity:
							int dist = StringDistance.calculate(anotherMethod.getSimpleName(),
									invocation.getExecutable().getSimpleName());
							if ((dist > 0 && dist < 3) || anotherMethod.getSimpleName().startsWith(invocation.getExecutable().getSimpleName())
									|| anotherMethod.getSimpleName().endsWith(invocation.getExecutable().getSimpleName())||
									invocation.getExecutable().getSimpleName().startsWith(anotherMethod.getSimpleName()) ||
									invocation.getExecutable().getSimpleName().endsWith(anotherMethod.getSimpleName())) {
								m2methodhasMinDist = true;
							}

							// Check if the method has a parameter compatible with the affected invocation
							boolean hasSameParam = checkTypeInParameter(anotherMethod, invocation.getExecutable());
							if (hasSameParam) {
								m3methodhasCompatibleParameterAndReturnWithOtherMethod = true;
							}
						}
					}
					
					if (!m2methodhasMinDist) {

						List<CtInvocation> invocationsFromAnotherMethod = anotherMethod
								.getElements(e -> (e instanceof CtInvocation)).stream().map(CtInvocation.class::cast)
								.collect(Collectors.toList());
						
						for (CtInvocation ctInvocation : invocationsFromAnotherMethod) {
							boolean compatibleReturnTypes = compareTypes(invocation.getType(),
									ctInvocation.getType());
							
							if (compatibleReturnTypes) {
								// Check name similarity:
								   if(!ctInvocation.getExecutable().getSignature().equals(invocation.getExecutable().getSignature())) {
								      int dist = StringDistance.calculate(ctInvocation.getExecutable().getSimpleName(),
										invocation.getExecutable().getSimpleName());
								      if ((dist > 0 && dist < 3) || ctInvocation.getExecutable().getSimpleName().startsWith(invocation.getExecutable().getSimpleName())
								    		  || ctInvocation.getExecutable().getSimpleName().endsWith(invocation.getExecutable().getSimpleName())||
								    		  invocation.getExecutable().getSimpleName().startsWith(ctInvocation.getExecutable().getSimpleName())||
								    		  invocation.getExecutable().getSimpleName().endsWith(ctInvocation.getExecutable().getSimpleName())) {
									     m2methodhasMinDist = true;
									     break;
								      } 
							     }
							}
						}
					}
					
					if (!m3methodhasCompatibleParameterAndReturnWithOtherMethod) {

						List<CtInvocation> invocationsFromAnotherMethod = anotherMethod
								.getElements(e -> (e instanceof CtInvocation)).stream().map(CtInvocation.class::cast)
								.collect(Collectors.toList());
						for (CtInvocation ctInvocation : invocationsFromAnotherMethod) {

								if(!ctInvocation.getExecutable().getSignature().equals(invocation.getExecutable().getSignature())) {

								   if (compareTypes(ctInvocation.getType(), invocation.getType())
										&& checkTypeInParameter(ctInvocation.getExecutable(), invocation.getExecutable())) {
									 m3methodhasCompatibleParameterAndReturnWithOtherMethod = true;
									 break;
								  }
							   }
						  }
					}	
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY,
						m4methodHasCompatibleParameterAndReturnSameMethod, "FEATURES_METHODS");

				writeGroupedInfo(context, adjustIdentifyInJson(invocation), CodeFeatures.M1_OVERLOADED_METHOD,
						m1methodHasSameName, "FEATURES_METHODS");

				writeGroupedInfo(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN, m2methodhasMinDist, "FEATURES_METHODS");

				writeGroupedInfo(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP,
						m3methodhasCompatibleParameterAndReturnWithOtherMethod, "FEATURES_METHODS");
				
				writeGroupedInfo(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M8_RETURN_PRIMITIVE,
						m8methodprimitive, "FEATURES_METHODS");
				
				writeGroupedInfo(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M9_RETURN_OBJECTIVE,
						m9methodobjective, "FEATURES_METHODS");

			} // end invocation

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
   private boolean whetherhasprimitive(List<CtTypeReference> inferredtypes) {
		
		for (int index=0; index<inferredtypes.size(); index++) {
			
			if(inferredtypes.get(index).isPrimitive()) {
				return true;
			}
		}
		
		return false;
	}
	
    private boolean checkTypeInParameter(CtMethod anotherMethod, CtExecutableReference minvokedInAffected) {
		
		for (Object oparameter : anotherMethod.getParameters()) {
			CtParameter parameter = (CtParameter) oparameter;

			if (compareTypes(minvokedInAffected.getType(), parameter.getType())) {
				return true;
			}
		}
		return false;
	}
    
    private boolean checkTypeInParameter(CtExecutableReference anotherMethod, CtExecutableReference minvokedInAffected) {
		for (Object oparameter : anotherMethod.getParameters()) {
			CtTypeReference parameter = (CtTypeReferenceImpl) oparameter;

			if (compareTypes(minvokedInAffected.getType(), parameter)) {
				return true;
			}
		}
		return false;
	}
    
    private void analyzeM5(CtElement element, Cntx<Object> context, List<CtInvocation> invocations,
			List<CtVariable> varsInScope) {
		
		try {
			for (CtInvocation invocation : invocations) {
				boolean currentInvocationWithCompVar = false;
				CtTypeReference type = invocation.getType();

				if (type != null) {
					// for the variables in scope
					for (CtVariable varInScope : varsInScope) {
						if (compareTypes(type, varInScope.getType())) {
							currentInvocationWithCompVar = true;
							break;
						}
					}
				}

				writeGroupedInfo(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M5_MI_WITH_COMPATIBLE_VAR_TYPE, 
						currentInvocationWithCompVar, "FEATURES_METHODS");			
			}
		  } catch (Exception e) {
		}
	}
    
    private void analyzeM67_ReplaceVarGetAnotherInvocation (List<CtInvocation> invocationsaffected, Cntx<Object> context,
			List<CtInvocation> invocationsFromClass, List<CtConstructorCall> constructorcallsFromClass) {
		
		try {

			for (CtInvocation invAffected : invocationsaffected) {
				
				CtInvocation parentInvocation = invAffected.getParent(CtInvocation.class);
				
				boolean M6ReplacewithVarCurrent = false;
				
				boolean M7ReplacewithInvocationCurrent = false;

				if (parentInvocation != null) {
					
					List<CtElement> arguments = parentInvocation.getArguments();
					
					for (CtInvocation specificinvocation : invocationsFromClass) {

						if(parentInvocation.equals(specificinvocation))
							continue;
						
						List<CtElement> specificarguments = specificinvocation.getArguments();

						if(parentInvocation.getExecutable().getSimpleName().equals
								(specificinvocation.getExecutable().getSimpleName()) && 
								arguments.size() == specificarguments.size()) {
							
							int[] comparisionresult= argumentDiffMethod(arguments, specificarguments, invAffected);
							
							if(comparisionresult[0]==1 && comparisionresult[1]==1)
								M6ReplacewithVarCurrent =true;
							
							if(comparisionresult[0]==1 && comparisionresult[2]==1)
								M7ReplacewithInvocationCurrent =true;
						}
						
						if(M6ReplacewithVarCurrent && M7ReplacewithInvocationCurrent)
							break;
					}
				}
				
				if(!M6ReplacewithVarCurrent || !M7ReplacewithInvocationCurrent) {
					
					CtConstructorCall parentConstructor = invAffected.getParent(CtConstructorCall.class);

					if (parentConstructor != null) {
						
						List<CtElement> arguments = parentConstructor.getArguments();
						
						for (CtConstructorCall specificconstructor : constructorcallsFromClass) {

							if(parentConstructor.equals(specificconstructor))
								continue;
							
							List<CtElement> specificarguments = specificconstructor.getArguments();

							if(parentConstructor.getExecutable().getSimpleName().equals
									(specificconstructor.getExecutable().getSimpleName()) && 
									arguments.size() == specificarguments.size()) {
								
								int[] comparisionresult= argumentDiffMethod(arguments, specificarguments, invAffected);
								
								if(comparisionresult[0]==1 && comparisionresult[1]==1)
									M6ReplacewithVarCurrent =true;
								
								if(comparisionresult[0]==1 && comparisionresult[2]==1)
									M7ReplacewithInvocationCurrent =true;
							}
							
							if(M6ReplacewithVarCurrent && M7ReplacewithInvocationCurrent)
								break;
						}
					}
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(invAffected),
						CodeFeatures.M6_INV_Invocation_INV_REPLACE_BY_VAR, 
						M6ReplacewithVarCurrent, "FEATURES_METHODS");
				
				writeGroupedInfo(context, adjustIdentifyInJson(invAffected),
						CodeFeatures.M7_INV_Invocation_INV_REPLACE_BY_INV, 
						M7ReplacewithInvocationCurrent, "FEATURES_METHODS");

			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
    
    private int[] argumentDiffMethod(List<CtElement> argumentsoriginal, List<CtElement> argumentsother, 
 		   CtInvocation invocationaccess) {
 		
 		int numberdiffargument =0;
 		int numberdiffmethodreplacebyvar =0;
 		int numberdiffmethodreplacebymethod =0;
 		
 		for(int index=0; index<argumentsoriginal.size(); index++) {
 			
 			CtElement original = argumentsoriginal.get(index);
 			CtElement other = argumentsother.get(index);
 			
 			if(original.equals(other)) {
 				// same
 			} else {
 				numberdiffargument+=1;
 				if(original instanceof CtInvocation && original.equals(invocationaccess)) {
 					if(other instanceof CtVariableAccess)
 						numberdiffmethodreplacebyvar+=1;
 					else if(other instanceof CtInvocation || other instanceof CtConstructorCall)
 						numberdiffmethodreplacebymethod+=1;
 					else {
 						// do nothing
 					}
 				}
 			}
 		}

 		int diffarray[]=new int[3];
 		diffarray[0]=numberdiffargument;
 		diffarray[1]=numberdiffmethodreplacebyvar;
 		diffarray[2]=numberdiffmethodreplacebymethod;

        return diffarray;
 	}
}
