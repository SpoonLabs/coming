package fr.inria.coming.codefeatures.codeanalyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeElementInfo;
import fr.inria.coming.codefeatures.CodeFeatures;
import fr.inria.coming.utils.StringDistance;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

public class ConstructorAnalyzer extends AbstractCodeAnalyzer {
	
	public ConstructorAnalyzer (CodeElementInfo inputinfo) {
		super(inputinfo);
	}

	@Override
	public void analyze() {

		analyzeCon1_ConstructorOverload(elementinfo.element, elementinfo.context, elementinfo.parentClass, 
				elementinfo.constructorcalls);
		analyzeCon2_ConstructorSimilar(elementinfo.element, elementinfo.context, elementinfo.parentClass,
				elementinfo.constructorcalls);
		
		analyzeConstructorFeature_Extend(elementinfo.element, elementinfo.context, elementinfo.parentClass,
				elementinfo.constructorcallsFromClass, elementinfo.constructorcalls);
		
		analyzeWhetherConstructorWraptedCon3 (elementinfo.constructorcalls,  elementinfo.context, 
				elementinfo.invocationsFromClass, elementinfo.constructorcallsFromClass);
		
		analyzeWhetherConstructorreturnprimitive (elementinfo.constructorcalls,  elementinfo.context);
		
		analyzeWhetherConstructorOftheclass (elementinfo.constructorcalls,  elementinfo.context, elementinfo.parentClass);
		
		analyzeVarConstructorArgumentPrimitive (elementinfo.constructorcalls, elementinfo.context);

	}
	
	private void analyzeVarConstructorArgumentPrimitive (List<CtConstructorCall> constructorsaffected, Cntx<Object> context) {
		
		 try {
			 for (CtConstructorCall conAffected : constructorsaffected) {
				
				boolean Con6ArgumentHasPrimitive = false;
				
				List<CtExpression> invocationArguments = conAffected.getArguments();
				
				for(int index=0; index<invocationArguments.size(); index++ ) {
					
					CtExpression certainexpression=invocationArguments.get(index);
					
					if (certainexpression.getType()!=null && (certainexpression.getType().isPrimitive() || 
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("string") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("long") || 
							certainexpression.getType().getQualifiedName().toString().toLowerCase().endsWith("list") ||
							certainexpression.getType().getQualifiedName().toString().toLowerCase().indexOf("string")!=-1 ||
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
						
						Con6ArgumentHasPrimitive = true;
						break;
					}
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(conAffected),
						CodeFeatures.CON6_Argument_Has_Primitive, 
						Con6ArgumentHasPrimitive, "FEATURES_CONSTRUCTOR");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeWhetherConstructorOftheclass (List<CtConstructorCall> constructorsaffected, Cntx<Object> context,
			CtClass parentclss) {
		
		 try {
			 for (CtConstructorCall conAffected : constructorsaffected) {
				
				boolean con5oftheclass = false;
				
				if (conAffected.getType()!=null && (conAffected.getType().getQualifiedName().endsWith(parentclss.getSimpleName()))) {
					con5oftheclass = true;
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(conAffected),
						CodeFeatures.CON5_Of_Class, 
						con5oftheclass, "FEATURES_CONSTRUCTOR");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeWhetherConstructorreturnprimitive (List<CtConstructorCall> constructorsaffected, Cntx<Object> context) {
		
		 try {
			 for (CtConstructorCall conAffected : constructorsaffected) {
				
				boolean con4returnprimitive = false;
				
				if (conAffected.getType()!=null && (conAffected.getType().isPrimitive() || 
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("string") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("list") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().indexOf("string")!=-1 ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("long") || 
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("boolean") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("double") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("byte")||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("short")||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("float") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("chart") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("character") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("integer")||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("string[]") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("long[]") || 
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("boolean[]") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("double[]") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("byte[]")||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("short[]")||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("float[]") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("chart[]") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("character[]") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().endsWith("integer[]") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().startsWith("java.util.") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().startsWith("java.nio.") ||
						conAffected.getType().getQualifiedName().toString().toLowerCase().startsWith("java.io."))) {
					con4returnprimitive = true;
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(conAffected),
						CodeFeatures.CON4_Return_Primitive, 
						con4returnprimitive, "FEATURES_CONSTRUCTOR");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeWhetherConstructorWraptedCon3 (List<CtConstructorCall> constructorsaffected, Cntx<Object> context,
			List<CtInvocation> invocationsFromClass, List<CtConstructorCall> constructorcallsFromClass) {
		
		 try {
			 for (CtConstructorCall conAffected : constructorsaffected) {
				
				boolean con3wrapttedinothers = false;
				
				for (CtInvocation specificinvocation : invocationsFromClass) {
					
					List<CtElement> specificarguments = specificinvocation.getArguments(); 
					
					for(int index=0; index< specificarguments.size(); index++) {
						
						CtElement specificargument = specificarguments.get(index);
						
						List<CtConstructorCall> invocationsinargument = specificargument.getElements(e -> (e instanceof CtConstructorCall)).stream()
								.map(CtConstructorCall.class::cast).collect(Collectors.toList());
						
						for(int innerindex=0; innerindex<invocationsinargument.size(); innerindex++) {
						
						   if(getSimplenameForConstructorCall(invocationsinargument.get(innerindex)).
								   equals(getSimplenameForConstructorCall(conAffected))) {
							   con3wrapttedinothers = true;
							   break;
						   }	
						}
						
						if(con3wrapttedinothers)
							break;
					}
					
					if(con3wrapttedinothers)
						break;
				}
				
				if(!con3wrapttedinothers) {
					
					for (CtConstructorCall specificconstructor : constructorcallsFromClass) {
						
						List<CtElement> specificarguments = specificconstructor.getArguments(); 
						
						for(int index=0; index< specificarguments.size(); index++) {
							
							CtElement specificargument = specificarguments.get(index);
							
							List<CtConstructorCall> invocationsinargument = specificargument.getElements(e -> (e instanceof CtConstructorCall)).stream()
									.map(CtConstructorCall.class::cast).collect(Collectors.toList());
							
							for(int innerindex=0; innerindex<invocationsinargument.size(); innerindex++) {
								
								   if(getSimplenameForConstructorCall(invocationsinargument.get(innerindex)).
										   equals(getSimplenameForConstructorCall(conAffected))) {
									   con3wrapttedinothers = true;
									   break;
								   }
							 }

							if(con3wrapttedinothers)
								break;
						}
						
						if(con3wrapttedinothers)
							break;
					}
				}
				
				writeGroupedInfo(context, adjustIdentifyInJson(conAffected),
						CodeFeatures.CON3_WRAPTTED_IN_OTHER_CALLS, 
						con3wrapttedinothers, "FEATURES_CONSTRUCTOR");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeConstructorFeature_Extend (CtElement originalElement, Cntx<Object> context,
		CtClass parentClass, List<CtConstructorCall> allconstructorcallsFromClass, List<CtConstructorCall> constructorcallstostudy) {
		
		List<CtInvocation> emptyinvocationfromclass = new ArrayList<CtInvocation>();
		List<CtInvocation> emptyinvocationunderstudy = new ArrayList<CtInvocation>();

		for(CtConstructorCall constructorcallAffected : constructorcallstostudy) {
            
            boolean[] constructorcallfeature91012 = analyze_SamerMethodWithGuardOrTrywrap(originalElement, parentClass, emptyinvocationfromclass,
            		emptyinvocationunderstudy, allconstructorcallsFromClass, Arrays.asList(constructorcallAffected));

            if(constructorcallfeature91012 != null) {
				
            	writeGroupedInfo(context, adjustIdentifyInJson(constructorcallAffected), CodeFeatures.CON9_METHOD_CALL_WITH_NORMAL_GUARD, 
            			constructorcallfeature91012[0], "FEATURES_CONSTRUCTOR");
				
            	writeGroupedInfo(context, adjustIdentifyInJson(constructorcallAffected), CodeFeatures.CON10_METHOD_CALL_WITH_NULL_GUARD, 
            			constructorcallfeature91012[1], "FEATURES_CONSTRUCTOR");
            	
            	writeGroupedInfo(context, adjustIdentifyInJson(constructorcallAffected), CodeFeatures.CON12_METHOD_CALL_WITH_TRY_CATCH, 
            			constructorcallfeature91012[2], "FEATURES_CONSTRUCTOR");
			}         
		}	
	}
	
	private void analyzeCon1_ConstructorOverload(CtElement element, Cntx<Object> context, CtClass parentClass,
			 List<CtConstructorCall> constructorcalls) {
		
		try {
			for (CtConstructorCall constructorcall : constructorcalls) {

				boolean con1SpecificHasSameName = false;
				
				List<CtConstructor> allconstructorsinclass = new ArrayList();
				if(parentClass!=null)
				     allconstructorsinclass = parentClass.getElements(new TypeFilter<>(CtConstructor.class));
	
				for (CtConstructor certainconstructorinclass : allconstructorsinclass) {

						CtConstructor anotherConstructor = (CtConstructor) certainconstructorinclass;
						// Ignoring if it's the same
						if (anotherConstructor == null || anotherConstructor.getSignature().
								equals(constructorcall.getExecutable().getSignature()))
							continue;

						if (anotherConstructor.getSimpleName().equals(getSimplenameForConstructorCall(constructorcall))) {
							// It's override
							con1SpecificHasSameName = true;
							break;
						}
				}
				
				List<CtConstructorCall> allconstructorcallsinclass = new ArrayList();

				if(parentClass!=null)
				     allconstructorcallsinclass = parentClass.getElements(new TypeFilter<>(CtConstructorCall.class));

			    if(!con1SpecificHasSameName) {
			    	
			    	for (CtConstructorCall certainconstructorcallinclass : allconstructorcallsinclass) {

			    		CtConstructorCall anotherConstructorCall = (CtConstructorCall) certainconstructorcallinclass;
						if (anotherConstructorCall == null || anotherConstructorCall.getExecutable().getSignature().
								equals(constructorcall.getExecutable().getSignature()))
							continue;

						if (getSimplenameForConstructorCall(anotherConstructorCall).equals(getSimplenameForConstructorCall(constructorcall))) {
							con1SpecificHasSameName = true;
							break;
						}
				    }
			    }
			    
			    writeGroupedInfo(context, adjustIdentifyInJson(constructorcall), CodeFeatures.CON1_OVERLOADED_CONSTRUCTOR,
			    		con1SpecificHasSameName, "FEATURES_CONSTRUCTOR");
			} 		
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
   
   private void analyzeCon2_ConstructorSimilar(CtElement element, Cntx<Object> context, CtClass parentClass,
			 List<CtConstructorCall> constructorcalls) {
		
		try {
			
			for (CtConstructorCall constructorcall : constructorcalls) {

				boolean con2SpecificHasSimilarName = false;
				
				List<CtConstructor> allconstructorsinclass = new ArrayList();
                if(parentClass!=null)
				     allconstructorsinclass = parentClass.getElements(new TypeFilter<>(CtConstructor.class));
	
				for (CtConstructor certainconstructorinclass : allconstructorsinclass) {

						CtConstructor anotherConstructor = (CtConstructor) certainconstructorinclass;
						if (anotherConstructor == null || anotherConstructor.getSignature().
								equals(constructorcall.getExecutable().getSignature()))
							continue;

						if (!anotherConstructor.getSimpleName().equals(getSimplenameForConstructorCall(constructorcall))) {
							
							int dist = StringDistance.calculate(anotherConstructor.getSimpleName(),
									getSimplenameForConstructorCall(constructorcall));
							if ((dist > 0 && dist < 3) || anotherConstructor.getSimpleName().startsWith(getSimplenameForConstructorCall(constructorcall))
									|| anotherConstructor.getSimpleName().endsWith(getSimplenameForConstructorCall(constructorcall))||
									getSimplenameForConstructorCall(constructorcall).startsWith(anotherConstructor.getSimpleName()) ||
									getSimplenameForConstructorCall(constructorcall).endsWith(anotherConstructor.getSimpleName())) {
							    con2SpecificHasSimilarName = true;
							    break;
							}
					   }
				}
				
				List<CtConstructorCall> allconstructorcallsinclass = new ArrayList();

				if(parentClass!=null)
				     allconstructorcallsinclass = parentClass.getElements(new TypeFilter<>(CtConstructorCall.class));

			    if(!con2SpecificHasSimilarName) {
			    	
			    	for (CtConstructorCall certainconstructorcallinclass : allconstructorcallsinclass) {

			    		CtConstructorCall anotherConstructorCall = (CtConstructorCall) certainconstructorcallinclass;
						if (anotherConstructorCall == null || anotherConstructorCall.getExecutable().getSignature().
								equals(constructorcall.getExecutable().getSignature()))
							continue;

						if (!getSimplenameForConstructorCall(anotherConstructorCall).equals(getSimplenameForConstructorCall(constructorcall))) {
							
							int dist = StringDistance.calculate(getSimplenameForConstructorCall(anotherConstructorCall),
									getSimplenameForConstructorCall(constructorcall));
							if ((dist > 0 && dist < 3) || getSimplenameForConstructorCall(anotherConstructorCall).startsWith(getSimplenameForConstructorCall(constructorcall))
									|| getSimplenameForConstructorCall(anotherConstructorCall).endsWith(getSimplenameForConstructorCall(constructorcall))||
									getSimplenameForConstructorCall(constructorcall).startsWith(getSimplenameForConstructorCall(anotherConstructorCall)) ||
									getSimplenameForConstructorCall(constructorcall).endsWith(getSimplenameForConstructorCall(anotherConstructorCall))) {
								con2SpecificHasSimilarName = true;
								break;
							}
						}
				    }
			    }
			    
			    writeGroupedInfo(context, adjustIdentifyInJson(constructorcall), CodeFeatures.CON2_SIMILAR_CONSTRUCTOR,
			    		con2SpecificHasSimilarName, "FEATURES_CONSTRUCTOR");
			} 
						
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
   
}
