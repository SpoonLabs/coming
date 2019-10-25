package fr.inria.coming.codefeatures.codeanalyze;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeElementInfo;
import fr.inria.coming.codefeatures.CodeFeatures;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.LineFilter;

public class WholeStatementAnalyzer extends AbstractCodeAnalyzer {

	public WholeStatementAnalyzer(CodeElementInfo inputinfo) {
		super(inputinfo);
	}

	@Override
	public void analyze() {

//		analyzeS1_AffectedAssigned(elementinfo.varsAffected, elementinfo.element, elementinfo.context);
//		analyzeS1_AffectedVariablesUsed(elementinfo.varsAffected, elementinfo.element, 
//				elementinfo.context, elementinfo.statements);
//		analyzeS2_S5_SametypewithGuard(elementinfo.varsAffected, elementinfo.element, 
//				elementinfo.context, elementinfo.parentClass, elementinfo.statements);
		analyzeS3_TypeOfFaulty(elementinfo.element, elementinfo.context);
//		analyzeS4_AffectedFielfs(elementinfo.varsAffected, elementinfo.element, elementinfo.context,
//				elementinfo.parentClass);
//		analyzeS4_AffectedFieldAssigned(elementinfo.varsAffected, elementinfo.element, elementinfo.context, 
//				elementinfo.parentClass);
		analyzeS6S11_Method_Method_Features(elementinfo.element, elementinfo.context);
//		analyzeS7S8_AffectedObjectLastAppear(elementinfo.varsAffected, elementinfo.element, elementinfo.context, 
//				elementinfo.statements);
//		analyzeS9S10S12_SamerMethodWithGuardOrTrywrap(elementinfo.element, elementinfo.context, elementinfo.parentClass,
//				elementinfo.invocationsFromClass, 
//				elementinfo.invocations, elementinfo.constructorcallsFromClass, elementinfo.constructorcalls);
		analyzeS13_TypeOfBeforeAfterFaulty(elementinfo.element, elementinfo.context);
		analyzeS14_TypeOfFaultyParent(elementinfo.element, elementinfo.context);
		analyzeS15_HasObjectiveInvocations(elementinfo.element, elementinfo.context, elementinfo.parentClass,
				elementinfo.invocations);
		analyzeS16_HasInvocationsPronetoException(elementinfo.element, elementinfo.context, elementinfo.invocations,
				elementinfo.constructorcalls);

		// analyzeS17_HasExceptionImport(elementinfo.context, elementinfo.parentClass,
		// elementinfo.invocations);
		analyzeS18_InSynchronizedMethod(elementinfo.element, elementinfo.context);

		analyzeFeature_ExtendFromVar(elementinfo.context, elementinfo.varsAffected);
		analyzeFeature_ExtendFromMethod(elementinfo.context, elementinfo.invocations, elementinfo.constructorcalls);

	}

//	 private void analyzeS17_HasExceptionImport ( Cntx<Object> context,  CtClass parentClass,
//			   List<CtInvocation> invocationstostudy) {
//				
//				 try {
//					boolean S17HasExceptionImport = false; 
//					
//					 parentClass.getPosition().getCompilationUnit().getImports();
//					
////					for(CtImport animport: allimports) {
////						System.out.println(animport.getReference().getSimpleName());
////					}
//
//				} catch (Throwable e) {
//					e.printStackTrace();
//			 } 
//		}

	private void analyzeS18_InSynchronizedMethod(CtElement element, Cntx<Object> context) {

		try {
			boolean S18Insynchronizedmethod = false;

			CtStatement parent = element.getParent(new LineFilter());
			CtTry potentionalTryCatch = element.getParent(CtTry.class);

			if (potentionalTryCatch != null && whethereffectivetrycatch(potentionalTryCatch, parent)) {
				context.put(CodeFeatures.S18_In_Synchronized_Method, false);
			} else {
				CtMethod methodParent = element.getParent(CtMethod.class);
				CtClass parentClass = element.getParent(CtClass.class);

				if (methodParent != null) {
					if (methodParent.getModifiers().contains(ModifierKind.SYNCHRONIZED))
						S18Insynchronizedmethod = true;
				}

				if (!S18Insynchronizedmethod && parentClass != null) {

					Set<CtTypeReference<?>> superInterfaces = parentClass.getSuperInterfaces();
					CtTypeReference<?> superType = parentClass.getSuperclass();

					if (superType != null && superType.getQualifiedName().toLowerCase().indexOf("thread") != -1)
						S18Insynchronizedmethod = true;

					if (superInterfaces.size() > 0) {
						for (CtTypeReference specificreference : superInterfaces) {
							if (specificreference != null
									&& specificreference.getQualifiedName().toLowerCase().indexOf("thread") != -1) {
								S18Insynchronizedmethod = true;
								break;
							}
						}
					}
				}

				context.put(CodeFeatures.S18_In_Synchronized_Method, S18Insynchronizedmethod);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeS16_HasInvocationsPronetoException(CtElement element, Cntx<Object> context,
			List<CtInvocation> invocationstostudy, List<CtConstructorCall> constructorcalls) {

		try {
			boolean S16HasInvocationsPronetoException = false;

			CtStatement parent = element.getParent(new LineFilter());
			CtTry potentionalTryCatch = element.getParent(CtTry.class);

			if (potentionalTryCatch != null && whethereffectivetrycatch(potentionalTryCatch, parent)) {
				context.put(CodeFeatures.S16_HAS_Invocations_Prone_Exception, false);
			} else {
				for (CtInvocation invocation : invocationstostudy) {
					String name = invocation.getExecutable().getSimpleName();
					if (!name.isEmpty() && name != null) {

						if (name.toLowerCase().indexOf("close") != -1 || name.toLowerCase().indexOf("connection") != -1
								|| name.toLowerCase().indexOf("stall") != -1
								|| name.toLowerCase().indexOf("accept") != -1
								|| name.toLowerCase().indexOf("context") != -1
								|| name.toLowerCase().indexOf("write") != -1
								|| name.toLowerCase().indexOf("getmethod") != -1
								|| name.toLowerCase().indexOf("getconstructor") != -1
								|| name.toLowerCase().indexOf("getdeclaredmethod") != -1
								|| name.toLowerCase().indexOf("getdeclaredfield") != -1
								|| name.toLowerCase().indexOf("read") != -1 || name.toLowerCase().indexOf("open") != -1
								|| name.toLowerCase().indexOf("resource") != -1
								|| name.toLowerCase().indexOf("parse") != -1
								|| name.toLowerCase().indexOf("waitfor") != -1
								|| name.toLowerCase().indexOf("install") != -1
								|| name.toLowerCase().indexOf("load") != -1
								|| name.toLowerCase().indexOf("synchron") != -1
								|| name.toLowerCase().indexOf("flush") != -1
								|| name.toLowerCase().indexOf("listen") != -1
								|| name.toLowerCase().indexOf("invoke") != -1
								|| name.toLowerCase().indexOf("clone") != -1
								|| name.toLowerCase().indexOf("shutdown") != -1
								|| name.toLowerCase().indexOf("connect") != -1) {
							S16HasInvocationsPronetoException = true;
							break;
						}
					}
				}

				if (!S16HasInvocationsPronetoException) {
					for (CtConstructorCall constructorcall : constructorcalls) {
						String name = getSimplenameForConstructorCall(constructorcall);
						if (!name.isEmpty() && name != null) {

							if (name.toLowerCase().indexOf("stream") != -1 || name.toLowerCase().indexOf("file") != -1
									|| name.toLowerCase().indexOf("output") != -1
									|| name.toLowerCase().indexOf("accept") != -1
									|| name.toLowerCase().indexOf("input") != -1) {
								S16HasInvocationsPronetoException = true;
								break;
							}
						}
					}
				}

				context.put(CodeFeatures.S16_HAS_Invocations_Prone_Exception, S16HasInvocationsPronetoException);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeFeature_ExtendFromVar(Cntx<Object> context, List<CtVariableAccess> varsAffected) {

		boolean S1_LOCAL_VAR_NOT_USED = false;
		boolean S1_LOCAL_VAR_NOT_ASSIGNED = false;
		boolean S2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD = false;
		boolean S5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD = false;
		boolean S2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD = false;
		boolean S5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD = false;
		boolean S4_Field_NOT_USED = false;
		boolean S4_Field_NOT_ASSIGNED = false;
		boolean S7_OBJECT_USED_IN_ASSIGNMENT = false;
		boolean S8_PRIMITIVE_USED_IN_ASSIGNMENT = false;

		for (CtVariableAccess aVarAffected : varsAffected) {

			try {
				Cntx<Object> featuresVar = (Cntx<Object>) context.getInformation().get("FEATURES_VARS");

				if (featuresVar != null) {
					Cntx<Object> particularVar = (Cntx<Object>) featuresVar.getInformation()
							.get(adjustIdentifyInJson(aVarAffected));

					if (particularVar != null) {

						if (Boolean.valueOf(particularVar.getInformation().get("V1_LOCAL_VAR_NOT_USED").toString()))
							S1_LOCAL_VAR_NOT_USED = true;

						if (Boolean.valueOf(particularVar.getInformation().get("V1_LOCAL_VAR_NOT_ASSIGNED").toString()))
							S1_LOCAL_VAR_NOT_ASSIGNED = true;

						if (particularVar.isBooleanValueTrue("V2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD"))
							S2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD = true;

						if (particularVar.isBooleanValueTrue("V5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD"))
							S5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD = true;

						if (particularVar.isBooleanValueTrue("V2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD"))
							S2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD = true;

						if (particularVar.isBooleanValueTrue("V5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD"))
							S5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD = true;

						if (particularVar.isBooleanValueTrue("V4_Field_NOT_USED"))
							S4_Field_NOT_USED = true;

						if (particularVar.isBooleanValueTrue("V4_Field_NOT_ASSIGNED"))
							S4_Field_NOT_ASSIGNED = true;

						if (particularVar.isBooleanValueTrue("V7_OBJECT_USED_IN_ASSIGNMENT"))
							S7_OBJECT_USED_IN_ASSIGNMENT = true;

						if (particularVar.isBooleanValueTrue("V8_PRIMITIVE_USED_IN_ASSIGNMENT"))
							S8_PRIMITIVE_USED_IN_ASSIGNMENT = true;
					}
				}
			} catch (Throwable e) {
				System.err.println("error caught at " + new Exception().getStackTrace()[0].toString());
				e.printStackTrace();
			}
		}

		context.put(CodeFeatures.S1_LOCAL_VAR_NOT_USED, S1_LOCAL_VAR_NOT_USED);
		context.put(CodeFeatures.S1_LOCAL_VAR_NOT_ASSIGNED, S1_LOCAL_VAR_NOT_ASSIGNED);
		context.put(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD, S2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD);
		context.put(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD,
				S5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD);
		context.put(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD, S2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD);
		context.put(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD, S5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD);
		context.put(CodeFeatures.S4_Field_NOT_USED, S4_Field_NOT_USED);
		context.put(CodeFeatures.S4_Field_NOT_ASSIGNED, S4_Field_NOT_ASSIGNED);
		context.put(CodeFeatures.S7_OBJECT_USED_IN_ASSIGNMENT, S7_OBJECT_USED_IN_ASSIGNMENT);
		context.put(CodeFeatures.S8_PRIMITIVE_USED_IN_ASSIGNMENT, S8_PRIMITIVE_USED_IN_ASSIGNMENT);
	}

	private void analyzeFeature_ExtendFromMethod(Cntx<Object> context, List<CtInvocation> invocations,
			List<CtConstructorCall> constructorcalls) {

		boolean S9_METHOD_CALL_WITH_NORMAL_GUARD = false;
		boolean S10_METHOD_CALL_WITH_NULL_GUARD = false;
		boolean S12_METHOD_CALL_WITH_TRY_CATCH = false;

		for (CtInvocation invocationAffected : invocations) {

			try {
				Cntx<Object> featuresMethod = (Cntx<Object>) context.getInformation().get("FEATURES_METHODS");

				if (featuresMethod != null) {
					Cntx<Object> particularMethod = (Cntx<Object>) featuresMethod.getInformation()
							.get(adjustIdentifyInJson(invocationAffected));

					if (particularMethod != null) {

						if (Boolean.valueOf(
								particularMethod.getInformation().get("M9_METHOD_CALL_WITH_NORMAL_GUARD").toString()))
							S9_METHOD_CALL_WITH_NORMAL_GUARD = true;

						if (Boolean.valueOf(
								particularMethod.getInformation().get("M10_METHOD_CALL_WITH_NULL_GUARD").toString()))
							S10_METHOD_CALL_WITH_NULL_GUARD = true;

						if (Boolean.valueOf(
								particularMethod.getInformation().get("M12_METHOD_CALL_WITH_TRY_CATCH").toString()))
							S12_METHOD_CALL_WITH_TRY_CATCH = true;
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		for (CtConstructorCall constructorcall : constructorcalls) {

			try {
				Cntx<Object> featuresConstructor = (Cntx<Object>) context.getInformation().get("FEATURES_CONSTRUCTOR");

				if (featuresConstructor != null) {
					Cntx<Object> particularConstructor = (Cntx<Object>) featuresConstructor.getInformation()
							.get(adjustIdentifyInJson(constructorcall));

					if (particularConstructor != null) {

						if (Boolean.valueOf(particularConstructor.getInformation()
								.get("CON9_METHOD_CALL_WITH_NORMAL_GUARD").toString()))
							S9_METHOD_CALL_WITH_NORMAL_GUARD = true;

						if (Boolean.valueOf(particularConstructor.getInformation()
								.get("CON10_METHOD_CALL_WITH_NULL_GUARD").toString()))
							S10_METHOD_CALL_WITH_NULL_GUARD = true;

						if (Boolean.valueOf(particularConstructor.getInformation()
								.get("CON12_METHOD_CALL_WITH_TRY_CATCH").toString()))
							S12_METHOD_CALL_WITH_TRY_CATCH = true;
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		context.put(CodeFeatures.S9_METHOD_CALL_WITH_NORMAL_GUARD, S9_METHOD_CALL_WITH_NORMAL_GUARD);
		context.put(CodeFeatures.S10_METHOD_CALL_WITH_NULL_GUARD, S10_METHOD_CALL_WITH_NULL_GUARD);
		context.put(CodeFeatures.S12_METHOD_CALL_WITH_TRY_CATCH, S12_METHOD_CALL_WITH_TRY_CATCH);
	}

//	private void analyzeS1_AffectedAssigned(List<CtVariableAccess> varsAffected, CtElement element,
//			Cntx<Object> context) {
//		
//	    context.put(CodeFeatures.S1_LOCAL_VAR_NOT_ASSIGNED, analyze_AffectedAssigned(varsAffected, element));   
//	}
//	
//	private void analyzeS1_AffectedVariablesUsed(List<CtVariableAccess> varsAffected, CtElement element,
//			Cntx<Object> context, List<CtStatement> statements) {
//
//		context.put(CodeFeatures.S1_LOCAL_VAR_NOT_USED, analyze_AffectedVariablesUsed(varsAffected, element, statements));    
//	}

//	private void analyzeS2_S5_SametypewithGuard(List<CtVariableAccess> varsAffected, CtElement element,
//			Cntx<Object> context, CtClass parentClass, List<CtStatement> statements) {
//
//		boolean[] expressionfeatures = analyze_SametypewithGuard(varsAffected, element, parentClass, statements);
//	    
//		if(expressionfeatures!=null) {
//			
//		    context.put(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD, expressionfeatures[0]);
//		    context.put(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD, expressionfeatures[1]);
//		    context.put(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD, expressionfeatures[2]);
//		    context.put(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD, expressionfeatures[3]);
//		} else {
//			
//		    context.put(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD, false);
//		    context.put(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD, false);
//		    context.put(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD, false);
//		    context.put(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD, false);
//		}
//
//	}

	public void analyzeS3_TypeOfFaulty(CtElement element, Cntx<Object> context) {
		try {
			String type = element.getClass().getSimpleName().replaceAll("Ct", "").replaceAll("Impl", "");
			context.put(CodeFeatures.S3_TYPE_OF_FAULTY_STATEMENT, type);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

//	private void analyzeS4_AffectedFielfs(List<CtVariableAccess> varsAffected, CtElement element, Cntx<Object> context,
//			CtClass parentClass) {
//
//			context.put(CodeFeatures.S4_Field_NOT_USED, analyze_AffectedFielfs(varsAffected, element, parentClass));
//	}
//	
//	private void analyzeS4_AffectedFieldAssigned (List<CtVariableAccess> varsAffected, CtElement element, Cntx<Object> context,
//			CtClass parentClass) {
//
//			context.put(CodeFeatures.S4_Field_NOT_ASSIGNED, analyze_AffectedFieldAssigned(varsAffected, element, parentClass));	
//	}

	/**
	 * whether the associated method or class for the faulty line throws exception
	 * 
	 * @param element
	 * @param context
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void analyzeS6S11_Method_Method_Features(CtElement element, Cntx<Object> context) {
		try {

			CtExecutable parentMethod = element.getParent(CtExecutable.class);

			CtClass parentClass = element.getParent(CtClass.class);

			CtStatement parent = element.getParent(new LineFilter());
			CtTry potentionalTryCatch = element.getParent(CtTry.class);

			if (potentionalTryCatch != null && whethereffectivetrycatch(potentionalTryCatch, parent)) {

				context.put(CodeFeatures.S6_METHOD_THROWS_EXCEPTION, false);
				context.put(CodeFeatures.S11_FAULTY_CLASS_EXCEPTION_TYPE, false);

			} else {

				if (parentMethod != null) {
					// Exception
					context.put(CodeFeatures.S6_METHOD_THROWS_EXCEPTION, parentMethod.getThrownTypes().size() > 0);
				}

				boolean s11ExceptionType = false;

				if (parentClass != null) {

					Set<CtTypeReference<?>> superInterfaces = parentClass.getSuperInterfaces();
					CtTypeReference<?> superType = parentClass.getSuperclass();

					if (superType != null && superType.getQualifiedName().toLowerCase().indexOf("exception") != -1)
						s11ExceptionType = true;

					if (superInterfaces.size() > 0) {
						for (CtTypeReference specificreference : superInterfaces) {
							if (specificreference != null
									&& specificreference.getQualifiedName().toLowerCase().indexOf("exception") != -1) {
								s11ExceptionType = true;
								break;
							}
						}
					}
					context.put(CodeFeatures.S11_FAULTY_CLASS_EXCEPTION_TYPE, s11ExceptionType);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

// 	private void analyzeS7S8_AffectedObjectLastAppear(List<CtVariableAccess> varsAffected, CtElement element,
// 			Cntx<Object> context, List<CtStatement> statements) {
//
//		boolean[] expressionfeatures = analyze_AffectedObjectLastAppear(varsAffected, element, statements);
//		
//        if(expressionfeatures!=null) {
//			
//		    context.put(CodeFeatures.S7_OBJECT_USED_IN_ASSIGNMENT, expressionfeatures[0]);
//		    context.put(CodeFeatures.S8_PRIMITIVE_USED_IN_ASSIGNMENT, expressionfeatures[1]);
//		} else {
//		    context.put(CodeFeatures.S7_OBJECT_USED_IN_ASSIGNMENT, false);
//		    context.put(CodeFeatures.S8_PRIMITIVE_USED_IN_ASSIGNMENT, false);
//		}
// 	}

// 	private void analyzeS9S10S12_SamerMethodWithGuardOrTrywrap (CtElement element, Cntx<Object> context, CtClass parentClass,
//			List<CtInvocation> allinvocationsFromClass, List<CtInvocation> invocationstostudy, 
//		List<CtConstructorCall> allconstructorcallsFromClass, List<CtConstructorCall> constructorcallstostudy) {
//
// 		boolean[] expressionfeatures = analyze_SamerMethodWithGuardOrTrywrap (element, parentClass, allinvocationsFromClass, 
// 				invocationstostudy, allconstructorcallsFromClass, constructorcallstostudy);
//
//        if(expressionfeatures!=null) {
//			
//		    context.put(CodeFeatures.S9_METHOD_CALL_WITH_NORMAL_GUARD, expressionfeatures[0]);
//		    context.put(CodeFeatures.S10_METHOD_CALL_WITH_NULL_GUARD, expressionfeatures[1]);
//		    context.put(CodeFeatures.S12_METHOD_CALL_WITH_TRY_CATCH, expressionfeatures[1]);
//
//		} else {
//			
//		    context.put(CodeFeatures.S9_METHOD_CALL_WITH_NORMAL_GUARD, false);
//		    context.put(CodeFeatures.S10_METHOD_CALL_WITH_NULL_GUARD, false);
//		    context.put(CodeFeatures.S12_METHOD_CALL_WITH_TRY_CATCH, false);
//		}
//	}

	public void analyzeS13_TypeOfBeforeAfterFaulty(CtElement element, Cntx<Object> context) {

		context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_1, "");
		context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_2, "");
		context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_3, "");
		context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_AFTER_1, "");
		context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_AFTER_2, "");
		context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_AFTER_3, "");

		try {
			CtElement parent = element.getParent();
			final int LOOKUP_DIS = 3;
			List<CtElement> stmtsBefore = new ArrayList<>();
			List<CtElement> stmtsAfter = new ArrayList<>();
			if (parent instanceof CtStatementList) {
				CtStatementList CS = (CtStatementList) parent;
				List<CtStatement> tmp = new ArrayList<>();
				int idx = 0;
				boolean found = false;
				for (CtStatement stmt : CS.getStatements()) {
					if (stmt.equals(element)) {
						found = true;
						idx = tmp.size();
					}
					tmp.add(stmt);
				}
				assert (found);

				int s = 0;
				if (idx > LOOKUP_DIS)
					s = idx - LOOKUP_DIS;
				int e = tmp.size();
				if (idx + LOOKUP_DIS + 1 < tmp.size())
					e = idx + LOOKUP_DIS + 1;
				boolean above = true;
				for (int i = s; i < e; i++) {
					if (!tmp.get(i).equals(element)) {
						if (above)
							stmtsBefore.add(tmp.get(i));
						else
							stmtsAfter.add(tmp.get(i));
					}
					if (tmp.get(i).equals(element))
						above = false;
				}
			}

			for (int indexbefore = 0; indexbefore < stmtsBefore.size(); indexbefore++) {
				CtElement beforeelement = stmtsBefore.get(indexbefore);
				String type = beforeelement.getClass().getSimpleName().replaceAll("Ct", "").replaceAll("Impl", "");

				if ((stmtsBefore.size() - indexbefore) == 1) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_1, type);
				} else if ((stmtsBefore.size() - indexbefore) == 2) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_2, type);
				} else if ((stmtsBefore.size() - indexbefore) == 3) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_3, type);
				}
			}

			for (int indexafter = 0; indexafter < stmtsAfter.size(); indexafter++) {
				CtElement afterelement = stmtsAfter.get(indexafter);
				String type = afterelement.getClass().getSimpleName().replaceAll("Ct", "").replaceAll("Impl", "");

				if (indexafter == 0) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_AFTER_1, type);
				} else if (indexafter == 1) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_AFTER_2, type);
				} else if (indexafter == 2) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_AFTER_3, type);
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void analyzeS14_TypeOfFaultyParent(CtElement element, Cntx<Object> context) {

		try {
			CtElement parentElement = getParentNotBlock(element);
			String type = parentElement.getClass().getSimpleName().replaceAll("Ct", "").replaceAll("Impl", "");
			context.put(CodeFeatures.S14_TYPE_OF_FAULTY_STATEMENT_PARENT, type);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeS15_HasObjectiveInvocations(CtElement element, Cntx<Object> context, CtClass parentClass,
			List<CtInvocation> invocationstostudy) {

		try {
			boolean S15anyReturnObjective = false;

			for (CtInvocation invocation : invocationstostudy) {

				CtStatement parent = invocation.getParent(new LineFilter());

				if (isNormalGuard(invocation, (parent)) || isNullCheckGuard(invocation, (parent)))
					continue;

				if ((invocation.getType() != null && !invocation.getType().isPrimitive())
						|| whetherhasobjective(inferPotentionalTypes(invocation, parentClass))) {
					S15anyReturnObjective = true;
				}

				if (S15anyReturnObjective)
					break;
			}

			context.put(CodeFeatures.S15_HAS_OBJECTIVE_METHOD_CALL, S15anyReturnObjective);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
