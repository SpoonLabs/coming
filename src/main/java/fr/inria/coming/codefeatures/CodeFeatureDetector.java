package fr.inria.coming.codefeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.gumtreediff.tree.ITree;

import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.utils.MapCounter;
import fr.inria.coming.utils.StringDistance;
import fr.inria.coming.utils.TimeChrono;
import fr.inria.coming.utils.VariableResolver;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtArrayRead;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtWhile;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.path.CtPath;
import spoon.reflect.path.impl.CtPathElement;
import spoon.reflect.path.impl.CtPathImpl;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.LineFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.reference.CtTypeReferenceImpl;

/**
 *
 * @author Matias Martinez
 *
 */
public class CodeFeatureDetector {

	public static final String ENUM_KEY = "ENUM_KEY";
	public static final String CONSTANT_KEY = "CONSTANT";
	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Cntx<?> analyzeFeatures(CtElement element) {

		Cntx<Object> context = new Cntx<>(determineKey(element));

		// Vars in scope at the position of element
		TimeChrono cr = new TimeChrono();
		cr.start();
		List<CtVariable> varsInScope = VariableResolver.searchVariablesInScope(element);
		CtClass parentClass = null;
		if (element instanceof CtClass)
			parentClass = (CtClass) element;
		else
			parentClass = element.getParent(CtClass.class);

		if (parentClass == null) {
			log.error("Parent null, we dont analyze the features");
			return null;
		}
		List<CtStatement> statements = parentClass.getElements(new LineFilter());

		log.debug("------Total vars  of " + ": " + cr.stopAndGetSeconds());

		List allMethods = getAllMethodsFromClass(parentClass);
		List<CtInvocation> invocationsFromClass = parentClass.getElements(e -> (e instanceof CtInvocation)).stream()
				.map(CtInvocation.class::cast).collect(Collectors.toList());
		List<CtConstructorCall> constructorcallsFromClass = parentClass.getElements(e -> (e instanceof CtConstructorCall)).stream()
				.map(CtConstructorCall.class::cast).collect(Collectors.toList());

		log.debug("------Total methods of " + ": " + cr.stopAndGetSeconds());

		putVarInContextInformation(context, varsInScope);

		log.debug("------Total context of " + ": " + cr.stopAndGetSeconds());

		CtElement elementToStudy = retrieveElementToStudy(element);

		//	List<CtVariableAccess> varsAffected = VariableResolver.collectVariableRead(elementToStudy);
		List<CtVariableAccess> varsAffected = VariableResolver.collectVariableAccess(elementToStudy, false);

		// Get all invocations inside the faulty element
		List<CtInvocation> invocations = elementToStudy.getElements(e -> (e instanceof CtInvocation)).stream()
				.map(CtInvocation.class::cast).collect(Collectors.toList());

		List<CtLiteral> literalsFromFaultyLine = elementToStudy.getElements(e -> (e instanceof CtLiteral)).stream()
				.map(CtLiteral.class::cast).collect(Collectors.toList());

		// due to partial program, some enum values and constant are viwed as typeaccess
		List<CtTypeAccess> typeaccess = elementToStudy.getElements(e -> (e instanceof CtTypeAccess)).stream()
				.map(CtTypeAccess.class::cast).collect(Collectors.toList());

		List<CtConstructorCall> constructorcall = elementToStudy.getElements(e -> (e instanceof CtConstructorCall)).stream()
				.map(CtConstructorCall.class::cast).collect(Collectors.toList());

		List<CtExpression> logicalExpressions = new ArrayList();
		List<CtExpression> expressionssFromFaultyLine = elementToStudy.getElements(e -> (e instanceof CtExpression)).stream()
				.map(CtExpression.class::cast).collect(Collectors.toList());

		LinkedHashSet<CtExpression> hashSetExpressions = new LinkedHashSet<>(expressionssFromFaultyLine);
		ArrayList<CtExpression> listExpressionWithoutDuplicates = new ArrayList<>(hashSetExpressions);

		ArrayList<CtExpression> removeUndesirable = new ArrayList<>();

		for(int index=0; index<listExpressionWithoutDuplicates.size(); index++) {

			CtExpression certainExpression = listExpressionWithoutDuplicates.get(index);

			if(certainExpression instanceof CtVariableAccess || certainExpression instanceof CtLiteral ||
					certainExpression instanceof CtInvocation || certainExpression instanceof CtConstructorCall ||
					certainExpression instanceof CtArrayRead || analyzeWhetherAE(certainExpression))
				removeUndesirable.add(certainExpression);
		}

//		for(int index=0; index<removeUndesirable.size(); index++)
//			System.out.println(removeUndesirable.get(index));

		for(int index=0; index<expressionssFromFaultyLine.size(); index++) {

			if(isBooleanExpressionNew(expressionssFromFaultyLine.get(index)) &&
					!whetherparentboolean(expressionssFromFaultyLine.get(index)) &&
					!logicalExpressions.contains(expressionssFromFaultyLine.get(index))) {
				logicalExpressions.add(expressionssFromFaultyLine.get(index));
			}
		}

		log.debug("------Total vars of " + ": " + cr.stopAndGetSeconds());

		log.debug("------Total v8 of " + ": " + cr.stopAndGetSeconds());
		analyzeS1_AffectedAssigned(varsAffected, element, context, true);
		log.debug("------Total s1 of " + ": " + cr.stopAndGetSeconds());
		analyzeS1_AffectedVariablesUsed(varsAffected, element, context, statements, true);

		analyzeS7S8_AffectedObjectLastAppear(varsAffected, element, context, statements, true);
		analyzeS9S10S12_SamerMethodWithGuardOrTrywrap(element, context, parentClass, invocationsFromClass,
				invocations, constructorcallsFromClass, constructorcall, true);
		log.debug("------Total s1b of " + ": " + cr.stopAndGetSeconds());
		analyzeS2_S5_SametypewithGuard(varsAffected, element, context, parentClass, statements, true);
		log.debug("------Total s2 of " + ": " + cr.stopAndGetSeconds());
		analyzeS3_TypeOfFaulty(element, context);
		log.debug("------Total s3 of " + ": " + cr.stopAndGetSeconds());
		analyzeS4_AffectedFielfs(varsAffected, element, context, parentClass, true);
		analyzeS4_AffectedFieldAssigned(varsAffected, element, context, parentClass, true);
		log.debug("------Total s4 of " + ": " + cr.stopAndGetSeconds());
		analyzeS6S11_Method_Method_Features (element, context);
		analyzeS13_TypeOfBeforeAfterFaulty(element, context);
		analyzeS14_TypeOfFaultyParent(element, context);
		analyzeS15_HasObjectiveInvocations(element, context, parentClass, invocations);

		log.debug("------Total s6 of " + ": " + cr.stopAndGetSeconds());
		analyzeV1_V6_V16(varsAffected, element, context, allMethods, invocationsFromClass, parentClass);
		log.debug("------Total v1 of " + ": " + cr.stopAndGetSeconds());
		analyzeV2_AffectedDistanceVarName(varsAffected, varsInScope, element, context);
		log.debug("------Total v2 of " + ": " + cr.stopAndGetSeconds());
		analyzeV3_AffectedHasConstant(varsAffected, element, context);
		log.debug("------Total v3 of " + ": " + cr.stopAndGetSeconds());
		analyzeV4(varsAffected, element, context);
		log.debug("------Total  v4 of " + ": " + cr.stopAndGetSeconds());
		analyzeV5_AffectedVariablesInTransformation(varsAffected, element, context);
		log.debug("------Total v5 of " + ": " + cr.stopAndGetSeconds());
		analyzeV8_TypesVarsAffected(varsAffected, element, context);
		analyzeV9_VarSimilarLiteral(element, context, parentClass, varsAffected);
		analyzV10_AffectedWithCompatibleTypes(varsAffected, varsInScope, element, context);
		analyzV11_ConditionWithCompatibleTypes(varsAffected, varsInScope, element, context);
		analyzeV1213_ReplaceVarGetAnotherInvocation(varsAffected,  context, invocationsFromClass, constructorcallsFromClass);
		analyzeV14_VarInstanceOfClass(varsAffected,  context, parentClass);
		analyzeV15_LastthreeVariableIntroduction(varsAffected, element, context);
		analyzeV17_IsEnum(varsAffected, context, parentClass);

		analyzeM1_eM2_M3_M4_M8_M9SimilarMethod(element, context, parentClass, allMethods, invocations);
		analyzeM5(element, context, invocations, varsInScope);
		analyzeM67_ReplaceVarGetAnotherInvocation(invocations,  context, invocationsFromClass, constructorcallsFromClass);

		analyzeCon1_ConstructorOverload(element, context, parentClass, constructorcall);
		analyzeCon2_ConstructorSimilar(element, context, parentClass, constructorcall);

		log.debug("------Total  Mx of " + ": " + cr.stopAndGetSeconds());

		analyzeLE1LE8_AffectedVariablesUsed(logicalExpressions, varsInScope, context, parentClass, statements);
		log.debug("------Total le1 of " + ": " + cr.stopAndGetSeconds());
		analyzeLE2_AffectedVariablesInMethod(logicalExpressions, context, allMethods, invocationsFromClass, parentClass);
		log.debug("------Total le2 of " + ": " + cr.stopAndGetSeconds());
		analyzeLE3_PrimitiveWithCompatibleNotUsed(logicalExpressions, varsInScope, context);
		log.debug("------Total le3  of " + ": " + cr.stopAndGetSeconds());
		analyzeLE4_BooleanVarNotUsed(logicalExpressions, varsInScope, context);
		analyzeLE5_Analyze_ComplexReference(logicalExpressions, context);
		log.debug("------Total le4 of " + ": " + cr.stopAndGetSeconds());
		//	analyzeLE5_BinaryInvolved(elementToStudy, context);
		log.debug("------Total le5  of " + ": " + cr.stopAndGetSeconds());
		analyzeLE6_UnaryInvolved(logicalExpressions, context);
		log.debug("------Total le6 of " + ": " + cr.stopAndGetSeconds());
		analyzeLE7_VarDirectlyUsed(logicalExpressions, context, invocations);
		analyzeLE9_BothNULLAndNormal(logicalExpressions, context);
		log.debug("------Total le7 of " + ": " + cr.stopAndGetSeconds());
		// analyzeLE8_LocalVariablesVariablesUsed(varsAffected, element, context);
		log.debug("------Total le8 of " + ": " + cr.stopAndGetSeconds());
		analyzeLE10_Analyze_Atomic_Boolexps(logicalExpressions, context);

		analyzeC1_Constant(element, context, parentClass, varsInScope, varsAffected, literalsFromFaultyLine);
		log.debug("------Total c1 of " + ": " + cr.stopAndGetSeconds());
		analyzeC5_UseEnum(elementToStudy, context, parentClass);
		log.debug("------Total c2 of " + ": " + cr.stopAndGetSeconds());
		analyzeC3C4_SimilarTypeAccessActualVar(element, context, typeaccess, parentClass);

		analyzeExpressionFeatures(removeUndesirable, element, context, parentClass, statements, varsInScope, allMethods
				, invocationsFromClass, constructorcallsFromClass);

		analyzeAE1(elementToStudy, context, allMethods, invocationsFromClass, parentClass);
		log.debug("------Total ae1 of " + ": " + cr.stopAndGetSeconds());

		// Other features not enumerated
		analyzeAffectedWithCompatibleTypes(varsAffected, varsInScope, element, context);
		log.debug("------Total cp of " + ": " + cr.stopAndGetSeconds());
		analyzeParentTypes(element, context);
		log.debug("------Total py of " + ": " + cr.stopAndGetSeconds());
		analyze_UseEnumAndConstants(elementToStudy, context);
		log.debug("------Total enum of " + ": " + cr.stopAndGetSeconds());

		return context;
	}

	private boolean analyzeWhetherAE(CtExpression expression) {

		try {

			List<BinaryOperatorKind> opKinds = new ArrayList<>();
			opKinds.add(BinaryOperatorKind.DIV);
			opKinds.add(BinaryOperatorKind.PLUS);
			opKinds.add(BinaryOperatorKind.MINUS);
			opKinds.add(BinaryOperatorKind.MUL);
			opKinds.add(BinaryOperatorKind.MOD);

			if(expression instanceof CtBinaryOperator && opKinds.contains(((CtBinaryOperator) expression).getKind()))
				return true;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return false;
	}

	private void analyzeExpressionFeatures(ArrayList<CtExpression> expressionsToStudy, CtElement originalElement, Cntx<Object> context,
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

			writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E1_LOCAL_VAR_NOT_USED,
					analyzeS1_AffectedVariablesUsed (varsAffected, originalElement, context, allstatementsinclass, false),
					"FEATURES_EXPRESSION");

			writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E1_LOCAL_VAR_NOT_ASSIGNED,
					analyzeS1_AffectedAssigned (varsAffected, originalElement, context, false),
					"FEATURES_EXPRESSION");

			boolean[] expressionfeatures = analyzeS2_S5_SametypewithGuard(varsAffected, originalElement, context, parentClass, allstatementsinclass, false);

			if(expressionfeatures != null) {

				writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD,
						expressionfeatures[0], "FEATURES_EXPRESSION");

				writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD,
						expressionfeatures[1], "FEATURES_EXPRESSION");

				writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD,
						expressionfeatures[2], "FEATURES_EXPRESSION");

				writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD,
						expressionfeatures[3], "FEATURES_EXPRESSION");
			}

			writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E4_Field_NOT_USED,
					analyzeS4_AffectedFielfs(varsAffected, originalElement, context, parentClass, false),
					"FEATURES_EXPRESSION");

			writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E4_Field_NOT_ASSIGNED,
					analyzeS4_AffectedFieldAssigned(varsAffected, originalElement, context, parentClass, false),
					"FEATURES_EXPRESSION");

			boolean[] expressionvalueS7S8 = analyzeS7S8_AffectedObjectLastAppear(varsAffected, originalElement, context, allstatementsinclass, false);

			if(expressionvalueS7S8 != null) {

				writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E7_OBJECT_USED_IN_ASSIGNMENT,
						expressionvalueS7S8[0], "FEATURES_EXPRESSION");

				writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E8_PRIMITIVE_USED_IN_ASSIGNMENT,
						expressionvalueS7S8[1], "FEATURES_EXPRESSION");
			}

			boolean[] expressionvalueS9S10 = analyzeS9S10S12_SamerMethodWithGuardOrTrywrap(originalElement, context, parentClass, invocationsFromClass, invocations,
					constructorcallsFromClass, constructorcall, false);

			if(expressionvalueS9S10 != null) {

				writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E9_METHOD_CALL_WITH_NORMAL_GUARD,
						expressionvalueS9S10[0], "FEATURES_EXPRESSION");

				writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E10_METHOD_CALL_WITH_NULL_GUARD,
						expressionvalueS9S10[1], "FEATURES_EXPRESSION");
			}

			writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E1_RETURN_PRIMITIVE,
					getExpressionType(specificexpression), "FEATURES_EXPRESSION");

			boolean[] expressionvalueType = analyzeExpression(specificexpression, context, allMethodsFromClass, invocationsFromClass, parentClass);

			if(expressionvalueType != null) {

				writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E2_COMPATIBLE_INVOCATION_PAREMETER_RETURN,
						expressionvalueType[0], "FEATURES_EXPRESSION");

				writeGroupedByVar(context, "expression_"+Integer.toString(expressionindex), CodeFeatures.E3_COMPATIBLE_INVOCATION_PAREMETER,
						expressionvalueType[1], "FEATURES_EXPRESSION");
			}
		}
	}

	private void analyzeS15_HasObjectiveInvocations (CtElement element, Cntx<Object> context,  CtClass parentClass,
													 List<CtInvocation> invocationstostudy) {

		try {
			boolean S15anyReturnObjective = false;

			for (CtInvocation invocation : invocationstostudy) {

				CtStatement parent = invocation.getParent(new LineFilter());

				if(isNormalGuard(invocation, (parent)) || isNullCheckGuard(invocation, (parent)))
					continue;

				if ((invocation.getType()!=null && !invocation.getType().isPrimitive()) ||
						whetherhasobjective(inferPotentionalTypes(invocation, parentClass))) {
					S15anyReturnObjective = true;
				}

				if(S15anyReturnObjective)
					break;
			}

			context.put(CodeFeatures.S15_HAS_OBJECTIVE_METHOD_CALL, S15anyReturnObjective);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeV17_IsEnum (List<CtVariableAccess> varsAffected, Cntx<Object> context, CtClass parentClass) {
		try {

			boolean useEnum = false;

			if (parentClass == null)
				return;
			// Get all enums
			List<CtEnum> enums = parentClass.getElements(new TypeFilter<>(CtEnum.class));

			// For each var access
			for (CtVariableAccess varAccess : varsAffected) {

				boolean isVarAccessTypeEnum = false;

				if (varAccess.getVariable().getType() != null
						&& enums.contains(varAccess.getVariable().getType().getDeclaration())) {
					useEnum = true;
					isVarAccessTypeEnum = true;
				}

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(varAccess), CodeFeatures.V17_VAR_IS_ENUMERATION,
						isVarAccessTypeEnum);
			}

			context.put(CodeFeatures.V17_VAR_IS_ENUMERATION, useEnum);
		} catch (Throwable e) {
			e.printStackTrace();
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

	public CtMethod checkMethodDeclarationWithParemetrCompatibleType (List allMethods,
																	  CtTypeReference typeToMatch) {

		for (Object omethod : allMethods) {

			if (!(omethod instanceof CtMethod))
				continue;

			CtMethod anotherMethodInBuggyClass = (CtMethod) omethod;

			for (Object oparameter : anotherMethodInBuggyClass.getParameters()) {
				CtParameter parameter = (CtParameter) oparameter;

				if (compareTypes(typeToMatch, parameter.getType())) {

					return anotherMethodInBuggyClass;
				}
			}
		}

		return null;
	}

	public CtInvocation checkInvocationWithParemetrCompatibleType (List<CtInvocation> invocationsFromClass,
																   CtTypeReference type) {

		// For each invocation found in the class
		for (CtInvocation anInvocation : invocationsFromClass) {
			// For each argument in the invocation
			for (Object anObjArgument : anInvocation.getArguments()) {
				CtExpression anArgument = (CtExpression) anObjArgument;

				if (compareTypes(type, anArgument.getType())) {
					return anInvocation;
				}
			}
		}

		return null;
	}

	private void analyzeM5(CtElement element, Cntx<Object> context, List<CtInvocation> invocations,
						   List<CtVariable> varsInScope) {

		boolean hasMIcompatibleVar = false;

		try {
			for (CtInvocation invocation : invocations) {
				boolean currentInvocationWithCompVar = false;
				CtTypeReference type = invocation.getType();

				if (type != null) {
					// for the variables in scope
					for (CtVariable varInScope : varsInScope) {
						if (compareTypes(type, varInScope.getType())) {
							hasMIcompatibleVar = true;
							currentInvocationWithCompVar = true;
						}
					}
				}

				writeGroupedByVar(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M5_MI_WITH_COMPATIBLE_VAR_TYPE,
						currentInvocationWithCompVar, "FEATURES_METHODS");
			}
		} catch (Exception e) {
		}
		context.put(CodeFeatures.M5_MI_WITH_COMPATIBLE_VAR_TYPE, hasMIcompatibleVar);
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

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(invAffected),
						CodeFeatures.M6_INV_Invocation_INV_REPLACE_BY_VAR,
						(M6ReplacewithVarCurrent));

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(invAffected),
						CodeFeatures.M7_INV_Invocation_INV_REPLACE_BY_INV, M7ReplacewithInvocationCurrent);

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

			if(original.equals(other) || original.toString().equals(other.toString())) {
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

	@Deprecated
	public List<CtVariableAccess> retrieveVariablesInsideElement(CtElement element) {

		if (element instanceof CtIf) {
			return VariableResolver.collectVariableRead(((CtIf) element).getCondition());
		} else if (element instanceof CtWhile) {
			return VariableResolver.collectVariableRead(((CtWhile) element).getLoopingExpression());
		} else if (element instanceof CtFor) {
			return VariableResolver.collectVariableRead(((CtFor) element).getExpression());
		} else if (element instanceof CtDo) {
			return VariableResolver.collectVariableRead(((CtDo) element).getLoopingExpression());
		} else if (element instanceof CtConditional) {
			return VariableResolver.collectVariableRead(((CtConditional) element).getCondition());
		} else
			return VariableResolver.collectVariableRead(element);
	}

	public CtElement retrieveElementToStudy(CtElement element) {

		if (element instanceof CtIf) {
			return (((CtIf) element).getCondition());
		} else if (element instanceof CtWhile) {
			return (((CtWhile) element).getLoopingExpression());
		} else if (element instanceof CtFor) {
			return (((CtFor) element).getExpression());
		} else if (element instanceof CtDo) {
			return (((CtDo) element).getLoopingExpression());
//		} else if (element instanceof CtConditional) {
//			return (((CtConditional) element).getCondition());
		} else if (element instanceof CtForEach) {
			return (((CtForEach) element).getExpression());
		} else if (element instanceof CtSwitch) {
			return (((CtSwitch) element).getSelector());
		} else
			return (element);

	}

	public boolean whetherparentboolean (CtExpression tostudy) {

		CtElement parent= tostudy;
		while(parent!=null) {
			parent=parent.getParent();

			if(isBooleanExpressionNew(parent))
				return true;
		}

		return false;
	}

	public boolean isBooleanExpressionNew(CtElement currentexpression) {

		if (currentexpression == null)
			return false;

		if (isLogicalExpressionNew(currentexpression)) {
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

	public boolean isLogicalExpressionNew (CtElement currentElement) {
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
					|| binOp.getKind().equals(BinaryOperatorKind.NE)
					|| (binOp.getType() != null &&
					binOp.getType().unbox().getSimpleName().equals("boolean")))

				return true;
		}

		if(currentElement.getParent() instanceof CtConditional) {
			CtConditional cond = (CtConditional) currentElement.getParent();
			if(currentElement.equals(cond.getCondition()))
				return true;
		}

		if(currentElement.getParent() instanceof CtIf) {
			CtIf ifcond = (CtIf) currentElement.getParent();
			if(currentElement.equals(ifcond.getCondition()))
				return true;
		}

		if(currentElement.getParent() instanceof CtWhile) {
			CtWhile whilecond = (CtWhile) currentElement.getParent();
			if(currentElement.equals(whilecond.getLoopingExpression()))
				return true;
		}

		if(currentElement.getParent() instanceof CtDo) {
			CtDo docond = (CtDo) currentElement.getParent();
			if(currentElement.equals(docond.getLoopingExpression()))
				return true;
		}

		if(currentElement.getParent() instanceof CtFor) {
			CtFor forcond = (CtFor) currentElement.getParent();
			if(currentElement.equals(forcond.getExpression()))
				return true;
		}

		return false;
	}

//	private void analyzeC1_Constant(CtElement element, Cntx<Object> context, CtClass parentClass,
//			List<CtVariable> varsInScope, List<CtVariableAccess> varsAffected, List<CtLiteral> literalsFromFaultyLine) {
//		try {
//			boolean hasSimilarLiterals = false;
//
//			List<CtTypedElement> allConstant = new ArrayList();
//
//			allConstant.addAll(literalsFromFaultyLine);
//
//			// we filter all variables that are constant
//			List<CtVariable> contantVars = varsAffected.stream().filter(e -> isConstantVariableAccess(e))
//					.map(e -> e.getVariable().getDeclaration()).collect(Collectors.toList());
//
//			allConstant.addAll(contantVars);
//
//			// Get all literals from the class under repair
//			List<CtLiteral> literalsFromClass = parentClass.getElements(e -> (e instanceof CtLiteral)).stream()
//					.map(CtLiteral.class::cast).collect(Collectors.toList());
//
//			// Get all constant in from the var in scope
//			List<CtVariable> constantVarsInScope = varsInScope.stream().filter(e -> isConstantVariable(e))
//					.map(CtVariable.class::cast).collect(Collectors.toList());
//
//			// Literals + constants in scope can be use for replacing the faulty literal
//			List<CtTypedElement> allTypeInScope = new ArrayList();
//			allTypeInScope.addAll(constantVarsInScope);
//			allTypeInScope.addAll(literalsFromClass);
//
//			if (allConstant.size() > 0) {
//
//				for (CtTypedElement literalFormFaulty : allConstant) {
//					boolean currentLiteralHasSimilarLiteral = false;
//					for (CtTypedElement anotherConstant : allTypeInScope) {
//						if (// Compare types
//						compareTypes(anotherConstant.getType(), literalFormFaulty.getType())
//								&& !anotherConstant.toString().equals(literalFormFaulty.toString())) {
//
//							hasSimilarLiterals = true;
//							currentLiteralHasSimilarLiteral = true;
//							break;
//						}
//					}
//					String name = (literalFormFaulty instanceof CtNamedElement)
//							? ((CtNamedElement) literalFormFaulty).getSimpleName()
//							: literalFormFaulty.toString();
//					writeGroupedByVar(context, adjustIdentifyInJson(literalFormFaulty), CodeFeatures.C1_SAME_TYPE_CONSTANT,
//							currentLiteralHasSimilarLiteral, CONSTANT_KEY);
//				}
//
//			}
//			context.put(CodeFeatures.C1_SAME_TYPE_CONSTANT, hasSimilarLiterals);
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}

	private boolean whetherSimilarTypeAccessActualVar(CtTypeAccess access1, CtTypeAccess access2) {
		String name1=access1.getAccessedType().getQualifiedName();
		String name2=access2.getAccessedType().getQualifiedName();
		String[] splited1=name1.split("\\.");
		String[] splited2=name2.split("\\.");
		if(splited1.length>1 && splited2.length>1) {
			if(splited1[splited1.length-2].equals(splited2[splited2.length-2])
					&& !splited1[splited1.length-1].equals(splited2[splited2.length-1]))
				return true;
		}

		return false;
	}

	private void analyzeC3C4_SimilarTypeAccessActualVar(CtElement element, Cntx<Object> context,
														List<CtTypeAccess> typeaccessaaffected, CtClass parentClass) {

		try {
			boolean c3AnyOtherTypeAccessActualVar=false;
			boolean c4AnyOtherSimilarTypeAccessActualVar = false;

			List<CtTypeAccess> typeaccesss = parentClass.getElements(new TypeFilter<>(CtTypeAccess.class));

			for (CtTypeAccess virtualtypeaccess : typeaccessaaffected) {

				boolean c3CurrentOtherTypeAccessActualVar = false;
				boolean c4CurrentOtherSimilarTypeAccessActualVar = false;

				if(isTypeAccessActualVar(virtualtypeaccess)) {
					c3AnyOtherTypeAccessActualVar=true;
					c3CurrentOtherTypeAccessActualVar=true;

					for(CtTypeAccess certaintypeaccess: typeaccesss) {
						if(isTypeAccessActualVar(certaintypeaccess)) {
							if(whetherSimilarTypeAccessActualVar(virtualtypeaccess, certaintypeaccess)) {
								c4AnyOtherSimilarTypeAccessActualVar=true;
								c4CurrentOtherSimilarTypeAccessActualVar=true;
							}
						}
					}
				}

				writeGroupedByVar(context, adjustIdentifyInJson(virtualtypeaccess), CodeFeatures.C3_TYPEACCESS_ACTUAL_VAR,
						c3CurrentOtherTypeAccessActualVar, "FEATURES_TYPEACCESS");

				writeGroupedByVar(context, adjustIdentifyInJson(virtualtypeaccess), CodeFeatures.C4_SIMILAR_TYPEACCESS_ACTUAL_VAR,
						c4CurrentOtherSimilarTypeAccessActualVar, "FEATURES_TYPEACCESS");
			}

			context.put(CodeFeatures.C3_TYPEACCESS_ACTUAL_VAR,
					c3AnyOtherTypeAccessActualVar);

			context.put(CodeFeatures.C4_SIMILAR_TYPEACCESS_ACTUAL_VAR, c4AnyOtherSimilarTypeAccessActualVar);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeC1_Constant(CtElement element, Cntx<Object> context, CtClass parentClass,
									List<CtVariable> varsInScope, List<CtVariableAccess> varsAffected, List<CtLiteral> literalsFromFaultyLine) {

		try {
			boolean hasSimilarLiterals = false;
			boolean hasSimilarConstantVars = false;
			boolean hasSimilarVars = false;

			List<CtLiteral> allConstant = new ArrayList();

			allConstant.addAll(literalsFromFaultyLine);

			// Get all literals from the class under repair
			List<CtLiteral> literalsFromClass = parentClass.getElements(e -> (e instanceof CtLiteral)).stream()
					.map(CtLiteral.class::cast).collect(Collectors.toList());

			// Get all constant in from the var in scope
			List<CtVariable> constantVarsInScope = varsInScope.stream().filter(e -> isConstantVariable(e))
					.map(CtVariable.class::cast).collect(Collectors.toList());

			if (allConstant.size() > 0) {

				for (CtLiteral literalFormFaulty : allConstant) {

					boolean currentLiteralHasSimilarLiteral = false;
					String[] currentLiteralTypeAndValue=getLiteralTypeAndValue(literalFormFaulty);
					for (CtLiteral anotherConstant : literalsFromClass) {
						String[] anotherLiteralTypeAndValue=getLiteralTypeAndValue(anotherConstant);
						if (currentLiteralTypeAndValue[0].equals(anotherLiteralTypeAndValue[0])
								&& !currentLiteralTypeAndValue[1].equals(anotherLiteralTypeAndValue[1])) {
							hasSimilarLiterals = true;
							currentLiteralHasSimilarLiteral = true;
							break;
						}
					}

					writeGroupedByVar(context, adjustIdentifyInJson(literalFormFaulty), CodeFeatures.C1_SAME_TYPE_CONSTANT,
							currentLiteralHasSimilarLiteral, CONSTANT_KEY);

					boolean currentLiteralHasSimilarConstantVar = false;
					for (CtVariable anotherConstant : constantVarsInScope) {
						if (compareLiteralAndConstantType(currentLiteralTypeAndValue[0], anotherConstant)) {
							hasSimilarConstantVars = true;
							currentLiteralHasSimilarConstantVar = true;
							break;
						}
					}

					writeGroupedByVar(context, adjustIdentifyInJson(literalFormFaulty), CodeFeatures.C2_SAME_TYPE_CONSTANT_VAR,
							currentLiteralHasSimilarConstantVar, CONSTANT_KEY);

					boolean currentLiteralHasSimilarVar = false;
					for (CtVariable anotherVar : varsInScope) {
						if (compareLiteralAndConstantType(currentLiteralTypeAndValue[0], anotherVar)) {
							hasSimilarVars = true;
							currentLiteralHasSimilarVar = true;
							break;
						}
					}

					writeGroupedByVar(context, adjustIdentifyInJson(literalFormFaulty), CodeFeatures.C2_SAME_TYPE_VAR,
							currentLiteralHasSimilarVar, CONSTANT_KEY);
				}
			}

			context.put(CodeFeatures.C1_SAME_TYPE_CONSTANT, hasSimilarLiterals);
			context.put(CodeFeatures.C2_SAME_TYPE_CONSTANT_VAR, hasSimilarConstantVars);
			context.put(CodeFeatures.C2_SAME_TYPE_VAR, hasSimilarVars);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeV9_VarSimilarLiteral(CtElement element, Cntx<Object> context, CtClass parentClass,
											 List<CtVariableAccess> varsAffected) {

		try {
			boolean varhasSimilarLiterals = false;

			// Get all literals from the class under repair
			List<CtLiteral> allliteralsFromClass = parentClass.getElements(e -> (e instanceof CtLiteral)).stream()
					.map(CtLiteral.class::cast).collect(Collectors.toList());

			for (CtVariableAccess varAffected : varsAffected) {

				boolean currentVarhasSimilarLiteral = false;

				for (CtLiteral literalinclass : allliteralsFromClass) {

					String[] anotherLiteralTypeAndValue=getLiteralTypeAndValue(literalinclass);

					if(compareVarAccessAndLiteralType(anotherLiteralTypeAndValue[0], varAffected)) {
						currentVarhasSimilarLiteral=true;
						varhasSimilarLiterals=true;
						break;
					}
				}

				writeGroupedByVar(context, adjustIdentifyInJson(varAffected), CodeFeatures.V9_VAR_TYPE_Similar_Literal,
						currentVarhasSimilarLiteral, "FEATURES_VARS");
			}

			context.put(CodeFeatures.V9_VAR_TYPE_Similar_Literal, varhasSimilarLiterals);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public boolean compareLiteralAndConstantType(String literaltype, CtVariable var) {

		Boolean typecompatiable=false;
		// not sure whther string is deemed as primitive, so use special processing
		if(var.getType().toString().toLowerCase().endsWith("string")) {
			if(literaltype.equals("string"))
				typecompatiable=true;
		}
		else  {
			if(var.getType().isPrimitive()) {
				if(var.getType().toString().toLowerCase().endsWith("char")) {
					if(literaltype.equals("char"))
						typecompatiable=true;
				}
				else if(var.getType().toString().toLowerCase().endsWith("boolean")) {
					if(literaltype.equals("boolean"))
						typecompatiable=true;
				}
				else {
					if(literaltype.equals("numerical"))
						typecompatiable=true;
				}
			}
		}
		return typecompatiable;
	}

	public boolean compareVarAccessAndLiteralType(String literaltype, CtVariableAccess varaccess) {

		Boolean typecompatiable=false;
		// not sure whther string is deemed as primitive, so use special processing
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

	public static String[] getLiteralTypeAndValue(CtLiteral inputLiteral) {

		String[] literaltypeandvalue=new String[2];

		if(inputLiteral.toString().trim().startsWith("'")) {
			literaltypeandvalue[0]="char";
			literaltypeandvalue[1]=inputLiteral.getValue().toString();
		}
		else if(inputLiteral.toString().trim().startsWith("\"")) {
			literaltypeandvalue[0]="string";
			literaltypeandvalue[1]=inputLiteral.getValue().toString();
		}
		else if(inputLiteral.toString().indexOf("null")!=-1) {
			literaltypeandvalue[0]="null";
			literaltypeandvalue[1]="null";
		}
		else {
			if(inputLiteral.getValue().toString().equals("true")||inputLiteral.getValue().toString().equals("false")) {
				literaltypeandvalue[0]="boolean";
				literaltypeandvalue[1]=inputLiteral.getValue().toString();
			}
			else  {
				literaltypeandvalue[0]="numerical";
				literaltypeandvalue[1]=inputLiteral.getValue().toString();
			}
		}

		return literaltypeandvalue;
	}

	public static boolean isConstantVariableAccess(CtVariableAccess ctVariableAccess) {
		if (ctVariableAccess.getVariable() != null) {
			Set<ModifierKind> modifiers = ctVariableAccess.getVariable().getModifiers();
			if (modifiers.contains(ModifierKind.FINAL)) {
				return true;
			} else {
				String simpleName = ctVariableAccess.getVariable().getSimpleName();
				if (simpleName.toUpperCase().equals(simpleName)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isConstantVariable(CtVariable ctVariable) {

		Set<ModifierKind> modifiers = ctVariable.getModifiers();
		if (modifiers.contains(ModifierKind.FINAL)) {
			return true;
		} else {
			String simpleName = ctVariable.getSimpleName();
			if (simpleName.toUpperCase().equals(simpleName)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isTypeAccessActualVar(CtElement element) {

		if(element instanceof CtTypeAccess) {
			CtTypeAccess typeaccess= (CtTypeAccess) element;
			String fullname=typeaccess.getAccessedType().getQualifiedName();
			String[] splitname=fullname.split("\\.");
			if (splitname.length>1) {
				String simplename=splitname[splitname.length-1];
				if (simplename.toUpperCase().equals(simplename))
					return true;
			}
		}
		return false;
	}

	public static boolean isConstantTypeAccess(CtTypeAccess ctTypeAccess) {

		Set<ModifierKind> modifiers = ctTypeAccess.getType().getModifiers();
		if (modifiers.contains(ModifierKind.FINAL)) {
			return true;
		} else {
			String simpleName = ctTypeAccess.getAccessedType().getSimpleName();
			if (simpleName.toUpperCase().equals(simpleName)) {
				return true;
			}
		}
		return false;
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

	@SuppressWarnings("unused")
	public Cntx<?> retrieveInfoOfElement(CtElement element) {

		Cntx<Object> context = new Cntx<>(determineKey(element));

		retrievePath(element, context);
		retrieveType(element, context);

		context.put(CodeFeatures.CODE, element.toString());

		Cntx<Object> buggyPositionCntx = new Cntx<>();
		retrievePosition(element, buggyPositionCntx);
		context.put(CodeFeatures.POSITION, buggyPositionCntx);

		return context;
	}

	private void analyzeC5_UseEnum(CtElement element, Cntx<Object> context, CtClass parentClass) {
		try {
			boolean useEnum = false;

			if (parentClass == null)
				return;
			// Get all enums
			List<CtEnum> enums = parentClass.getElements(new TypeFilter<>(CtEnum.class));

			// Get variable read from suspicious element
			List<CtVariableRead> varAccessFromSusp = element.getElements(new TypeFilter<>(CtVariableRead.class));

			// For each var access
			for (CtVariableRead varAccess : varAccessFromSusp) {

				boolean isVarAccessTypeEnum = false;
				if (varAccess.getVariable().getType() != null
						&& enums.contains(varAccess.getVariable().getType().getDeclaration())) {
					useEnum = true;
					isVarAccessTypeEnum = true;
				}

				writeGroupedByVar(context, adjustIdentifyInJson(varAccess), CodeFeatures.C5_USES_ENUMERATION,
						isVarAccessTypeEnum, ENUM_KEY);
			}

			context.put(CodeFeatures.C5_USES_ENUMERATION, useEnum);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyze_UseEnumAndConstants(CtElement element, Cntx<Object> context) {

		List enumsValues = new ArrayList();
		List literalsValues = new ArrayList();

		CtScanner assignmentScanner = new CtScanner() {

			@Override
			public <T> void visitCtEnumValue(CtEnumValue<T> enumValue) {

				super.visitCtEnumValue(enumValue);
				enumsValues.add(enumValue);
			}

			@Override
			public <T> void visitCtLiteral(CtLiteral<T> literal) {

				super.visitCtLiteral(literal);
				literalsValues.add(literal);
			}

			@Override
			public <T extends Enum<?>> void visitCtEnum(CtEnum<T> ctEnum) {
				super.visitCtEnum(ctEnum);
				enumsValues.add(ctEnum);
			}

		};

		assignmentScanner.scan(element);
		context.put(CodeFeatures.USES_ENUM, enumsValues.size() > 0);
		context.put(CodeFeatures.USES_CONSTANT, literalsValues.size() > 0);
	}

	/**
	 *
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	private void analyzeV4(List<CtVariableAccess> varsAffected, CtElement element, Cntx<Object> context) {

		try {
			boolean hasOneVarAppearsMultiple = false;
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
								// it was not used before in a parameter of the method invocation
								writeDetailedInformationFromVariables(context,
										adjustIdentifyInJson(varInFaulty),
										CodeFeatures.V4_FIRST_TIME_USED_AS_PARAMETER, true);
							} else {
								// already used as parameter
								int count = parameterFound.get(varInFaulty);
//								writeDetailedInformationFromVariables(context,
//										adjustIdentifyInJson(varInFaulty) + "_" + (count + 1),
//										CodeFeatures.V4_FIRST_TIME_USED_AS_PARAMETER, false);
								writeDetailedInformationFromVariables(context,
										adjustIdentifyInJson(varInFaulty),
										CodeFeatures.V4_FIRST_TIME_USED_AS_PARAMETER, false);
							}
							parameterFound.add(varInFaulty);
						}
					}
				}

				if (appearsInParams > 1) {
					hasOneVarAppearsMultiple = true;
				}

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(varInFaulty),
						CodeFeatures.V4B_USED_MULTIPLE_AS_PARAMETER, (appearsInParams > 1));

			}

			context.put(CodeFeatures.V4B_USED_MULTIPLE_AS_PARAMETER, hasOneVarAppearsMultiple);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Besides the variables involved in a logical expression,whether there exist
	 * other local boolean variables in scope?
	 *
	 * @param varsAffectedInStatement
	 * @param varsInScope
	 * @param element
	 * @param context
	 */
	private void analyzeLE4_BooleanVarNotUsedold(List<CtVariableAccess> varsAffectedInStatement,
												 List<CtVariable> varsInScope, CtElement element, Cntx<Object> context) {
		try {
			boolean hasBooleanVarNotPresent = false;
			/**
			 * For each var in scope
			 */
			for (CtVariable aVarInScope : varsInScope) {

				if (aVarInScope.getType() != null && aVarInScope.getType().unbox().toString().equals("boolean")) {

					// Check if the var in scope is present in the list of var from the expression.
					boolean isPresentVar = varsAffectedInStatement.stream()
							.filter(e -> e.getVariable().getSimpleName().equals(aVarInScope.getSimpleName()))
							.findFirst().isPresent();
					if (!isPresentVar) {
						hasBooleanVarNotPresent = true;
						break;
					}
				}
			}
			context.put(CodeFeatures.LE4_EXISTS_LOCAL_UNUSED_VARIABLES, hasBooleanVarNotPresent);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeLE4_BooleanVarNotUsed(List<CtExpression> logicalExperssions,
											  List<CtVariable> varsInScope, Cntx<Object> context) {

		try {

			for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {

				CtExpression logicalexpression = logicalExperssions.get(indexlogical);

				List<CtVariableAccess> varsAffected = VariableResolver.collectVariableAccess(logicalexpression, false);

				boolean hasBooleanVarNotPresent = false;
				/**
				 * For each var in scope
				 */
				for (CtVariable aVarInScope : varsInScope) {

					if (aVarInScope.getType() != null && aVarInScope.getType().unbox().toString().equals("boolean")) {

						// Check if the var in scope is present in the list of var from the expression.
						boolean isPresentVar = varsAffected.stream()
								.filter(e -> e.getVariable().getSimpleName().equals(aVarInScope.getSimpleName()))
								.findFirst().isPresent();
						if (!isPresentVar) {
							hasBooleanVarNotPresent = true;
							break;
						}
					}
				}

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE4_EXISTS_LOCAL_UNUSED_VARIABLES,
						hasBooleanVarNotPresent, "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * For a logical expression, if the logical expression involves comparison over
	 * primitive type variables (that is, some boolean expressions are comparing the
	 * primitive values), is there any other visible local primitive type variables
	 * that are not included in the logical expression (–chart 9). (returns a single
	 * binary value)
	 *
	 * @param varsAffectedInStatement
	 * @param varsInScope
	 * @param element
	 * @param context
	 */
	private void analyzeLE3_PrimitiveWithCompatibleNotUsedold(List<CtVariableAccess> varsAffectedInStatement,
															  List<CtVariable> varsInScope, CtElement element, Cntx<Object> context) {
		try {
			boolean hasCompatibleVarNoPresent = false;

			for (CtVariableAccess aVarFromAffected : varsAffectedInStatement) {

				if (aVarFromAffected.getType() == null || !aVarFromAffected.getType().isPrimitive()
						// parent is binary operator
						|| // !isparentBinaryComparison(aVarFromAffected))
						aVarFromAffected.getParent(CtBinaryOperator.class) == null)
					continue;

				// For each var in scope
				for (CtVariable aVarFromScope : varsInScope) {
					// If the var name are different
					if (!aVarFromScope.getSimpleName().equals(aVarFromAffected.getVariable().getSimpleName())) {

						// Let's check if the type are compatible (i.e., the same primitive type)
						if (compareTypes(aVarFromScope.getType(), aVarFromAffected.getType())) {

							boolean presentInExpression = varsAffectedInStatement.stream()
									.filter(e -> e.getVariable().getSimpleName().equals(aVarFromScope.getSimpleName()))
									.findFirst().isPresent();
							if (!presentInExpression) {
								hasCompatibleVarNoPresent = true;
								context.put(CodeFeatures.LE3_IS_COMPATIBLE_VAR_NOT_INCLUDED, hasCompatibleVarNoPresent);
								return;
							}
						}

					}
				}
			}
			context.put(CodeFeatures.LE3_IS_COMPATIBLE_VAR_NOT_INCLUDED, hasCompatibleVarNoPresent);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

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

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE3_IS_COMPATIBLE_VAR_NOT_INCLUDED,
						hasCompatibleVarNoPresent, "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	private boolean isparentBinaryComparison(CtElement element) {

		CtBinaryOperator binParent = element.getParent(CtBinaryOperator.class);

		if (binParent == null)
			return false;
		CtBinaryOperator binop = (CtBinaryOperator) binParent;
		if (binop.getKind().equals(BinaryOperatorKind.AND) || binop.getKind().equals(BinaryOperatorKind.OR)
				|| binop.getKind().equals(BinaryOperatorKind.EQ) || binop.getKind().equals(BinaryOperatorKind.NE)
				|| (binop.getType() != null && binop.getType().unbox().getSimpleName().equals("boolean")))
			return true;

//			return isLogicalExpressionInParent(currentElement.getParent(CtBinaryOperator.class));
//		}
		return false;
	}

	/**
	 * For the logical expression, whether there exists a boolean expression which
	 * is simply a boolean variable or a method call whose parent is CtThisAccess
	 * (i.e., not function call, equality comparison,
	 * etc.
	 *
	 * @param varsAffectedInStatement
	 * @param varsInScope
	 * @param element
	 * @param context
	 */
	private void analyzeLE7_VarDirectlyUsedold(List<CtVariableAccess> varsAffectedInStatement,
											   List<CtVariable> varsInScope, CtElement element, Cntx<Object> context,
											   List<CtInvocation> invocationssInStatement) {

		List<String> binOps = new ArrayList();
		CtScanner scanner = new CtScanner() {

			@Override
			public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
				super.visitCtBinaryOperator(operator);
				binOps.add(operator.getKind().toString());
			}
		};
		// CtElement toScan = null;
		ExpressionCapturerScanner scanner2 = new ExpressionCapturerScanner();
		scanner2.scan(element);
		if (scanner2.toScan != null) {
			scanner.scan(scanner2.toScan);
		} else {
			scanner.scan(element);
		}

		boolean containsAnd = binOps.contains(BinaryOperatorKind.AND.toString());
		boolean containsOr = binOps.contains(BinaryOperatorKind.OR.toString());

		try {
			boolean hasVarDirectlyUsed = false;

			for (CtVariableAccess aVarFromAffected : varsAffectedInStatement) {

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

				if(!(invocation.getTarget() instanceof CtThisAccess)) {
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

			if(containsAnd || containsOr)
				context.put(CodeFeatures.LE7_SIMPLE_VAR_OR_METHOD_IN_LOGIC, hasVarDirectlyUsed || hasMethodDirectlyUsed);
			else context.put(CodeFeatures.LE7_SIMPLE_VAR_OR_METHOD_IN_LOGIC, false);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

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

					if(!invocation.getTarget().toString().isEmpty()) {
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

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE7_SIMPLE_VAR_OR_METHOD_IN_LOGIC,
						(containsAnd || containsOr) && (hasVarDirectlyUsed || hasMethodDirectlyUsed), "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzV10_AffectedWithCompatibleTypes(List<CtVariableAccess> varsAffected, List<CtVariable> varsInScope,
													   CtElement element, Cntx<Object> context) {
		try {
			boolean hasSimTypeAny = false;

			for (CtVariableAccess aVariableAccessInStatement : varsAffected) {
				boolean currentHasSimType = false;
				for (CtVariable aVariableInScope : varsInScope) {
					if (!aVariableInScope.getSimpleName().equals(aVariableAccessInStatement.getVariable().getSimpleName())) {
						if (compareTypes(aVariableInScope.getType(), aVariableAccessInStatement.getType())) {
							hasSimTypeAny = true;
							currentHasSimType=true;
							break;
						}
					}
				}

				writeGroupedByVar(context, adjustIdentifyInJson(aVariableAccessInStatement),
						CodeFeatures.V10_VAR_TYPE_Similar_VAR, currentHasSimType, "FEATURES_VARS");
			}
			context.put(CodeFeatures.V10_VAR_TYPE_Similar_VAR, hasSimTypeAny);
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

				// Get all vars from variables
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

				writeGroupedByVar(context, adjustIdentifyInJson(aVariableAccessInStatement),
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

	private void analyzeAffectedWithCompatibleTypes(List<CtVariableAccess> varsAffected, List<CtVariable> varsInScope,
													CtElement element, Cntx<Object> context) {
		try {
			boolean hasSimType = false;
			for (CtVariableAccess aVariableAccessInStatement : varsAffected) {
				for (CtVariable aVariableInScope : varsInScope) {
					if (!aVariableInScope.getSimpleName()
							.equals(aVariableAccessInStatement.getVariable().getSimpleName())) {

						try {
							if (compareTypes(aVariableInScope.getType(), aVariableAccessInStatement.getType())) {
								hasSimType = true;
								context.put(CodeFeatures.HAS_VAR_SIM_TYPE, hasSimType);
								return;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			context.put(CodeFeatures.HAS_VAR_SIM_TYPE, hasSimType);
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
			// Collect Assignments and var declaration (local)
			assignmentScanner.scan(methodParent);

			boolean v5_anyhasvar = false;
			// For each variable affected
			for (CtVariableAccess variableAffected : varsAffected) {

				boolean v5_currentVarHasvar = false;

				// For each assignment in the methid
				for (CtExpression assignment : assignments) {

					if (!isElementBeforeVariable(variableAffected, assignment))
						continue;

					// let's collect the var access in the right part
					List<CtVariableAccess> varsInRightPart = VariableResolver.collectVariableRead(assignment); // VariableResolver.collectVariableAccess(assignment);

					// if the var access in the right is the same that the affected
					for (CtVariableAccess varInAssign : varsInRightPart) {
						if (hasSameName(variableAffected, varInAssign)) {

							v5_anyhasvar = true;
							v5_currentVarHasvar = true;
							break;
						}
					}
				}
				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(variableAffected),
						CodeFeatures.V5_HAS_VAR_IN_TRANSFORMATION, (v5_currentVarHasvar));

			}
			context.put(CodeFeatures.V5_HAS_VAR_IN_TRANSFORMATION, v5_anyhasvar);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return if one element is before the variable
	 *
	 * @param variableAffected
	 * @param element
	 * @return
	 */
	private boolean isElementBeforeVariable(CtVariableAccess variableAffected, CtElement element) {

		try {
			CtStatement stst = (element instanceof CtStatement) ? (CtStatement) element
					: element.getParent(CtStatement.class);

			CtStatement target = (variableAffected instanceof CtStatement) ? (CtStatement) variableAffected
					: variableAffected.getParent(CtStatement.class);

			return target.getPosition() != null && getParentNotBlock(stst) != null
					&& target.getPosition().getSourceStart() > stst.getPosition().getSourceStart();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return false;

	}

	/**
	 * :For any variablevinvolved in a logical expression, whetherexist other
	 * boolean expressions that involve using variablewhose type is same withv—note
	 * it is OK for the booleanexpression to also use some other variable types, we
	 * justrequire variable of typevis involved (as we do not assumhe availability
	 * of the whole program, we confine the searchof boolean expression in the same
	 * class) (–closure 20, theinvolved variable in the expression is value, whose
	 * type isNode, we can find there are other boolean expressions in thefaulty
	 * class that involve using variables of Node type, likearg.getNext() !=
	 * null–arg is Node type, callTarget.isName()–callTarget is Node type). (returns
	 * a single binary value,
	 *
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	@SuppressWarnings("rawtypes")
	private void analyzeLE1_AffectedVariablesUsedold(List<CtVariableAccess> varsAffected, CtElement element,
													 Cntx<Object> context, CtClass parentClass, List<CtStatement> statements) {
		try {

			if (parentClass == null)
				return;

			// List<CtStatement> statements = parentClass.getElements(new LineFilter());

			int similarUsedBefore = 0;

			// For each variable affected
			for (CtVariableAccess variableAffected : varsAffected) {

				// boolean used = false;
				boolean foundSimilarVarUsed = false;

				CtStatement parentstatement=(CtStatement)(variableAffected.getParent(CtStatement.class));

				CtStatement parent = variableAffected.getParent(new LineFilter());

				boolean isInBinaryExpression = isParentBooleanExpression(variableAffected);

				if (!isInBinaryExpression)
					continue;

				// let's find other boolean expressions in the statements
				for (CtStatement aStatement : statements) {

					if(parentstatement==aStatement || parent==aStatement)
						continue;

					// let's find all binary expressions in the statement
					List<CtElement> elements = aStatement.getElements(e -> isBooleanExpression(e)).stream()
							.map(CtElement.class::cast).collect(Collectors.toList());

					for (CtElement specificelement : elements) {

						// retrieve all variables
						List<CtVariableAccess> varsInOtherExpressions = VariableResolver
								.collectVariableRead(specificelement);
						for (CtVariableAccess varInAnotherExpression : varsInOtherExpressions) {
							//	if (!hasSameName(variableAffected, varInAnotherExpression)) {
							// Different name, so it's another variable

							// involve using variable whose type is same with v
							if (compareTypes(variableAffected.getVariable().getType(),
									varInAnotherExpression.getVariable().getType())) {
								foundSimilarVarUsed = true;
							}

							//}

						}
					}
				}

				if (foundSimilarVarUsed)
					similarUsedBefore++;
			}

			context.put(CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION, (similarUsedBefore) > 0);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	private void analyzeLE1LE8_AffectedVariablesUsed(List<CtExpression> logicalExperssions,
													 List<CtVariable> varsInScope, Cntx<Object> context, CtClass parentClass, List<CtStatement> statements) {

		try {

			for (int indexlogical=0; indexlogical<logicalExperssions.size(); indexlogical++) {

				CtExpression logicalexpression = logicalExperssions.get(indexlogical);

				List<CtVariableAccess> varsAffected = VariableResolver.collectVariableAccess(logicalexpression, false);

				// List<CtStatement> statements = parentClass.getElements(new LineFilter());

				int similarUsedBefore = 0;

				int otherVarUsedinBool = 0;

				CtStatement parentstatement=(CtStatement)(logicalexpression.getParent(CtStatement.class));

				CtStatement parent = logicalexpression.getParent(new LineFilter());

				// For each variable affected
				for (CtVariableAccess variableAffected : varsAffected) {

					// boolean used = false;
					boolean foundSimilarVarUsed = false;

					// let's find other boolean expressions in the statements
					for (CtStatement aStatement : statements) {

						if(parentstatement==aStatement || parent==aStatement)
							continue;

						// let's find all binary expressions in the statement
						List<CtElement> elements = aStatement.getElements(e -> isBooleanExpression(e)).stream()
								.map(CtElement.class::cast).collect(Collectors.toList());

						for (CtElement specificelement : elements) {

							// retrieve all variables
							List<CtVariableAccess> varsInOtherExpressions = VariableResolver
									.collectVariableRead(specificelement);
							for (CtVariableAccess varInAnotherExpression : varsInOtherExpressions) {
								//	if (!hasSameName(variableAffected, varInAnotherExpression)) {
								// Different name, so it's another variable

								// involve using variable whose type is same with v
								if (compareTypes(variableAffected.getVariable().getType(),
										varInAnotherExpression.getVariable().getType())) {
									foundSimilarVarUsed = true;
								}

								//}

							}
						}
					}

					if (foundSimilarVarUsed)
						similarUsedBefore++;
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

					// let's find other boolean expressions in the statements
					for (CtStatement aStatement : statements) {

						if(parentstatement==aStatement || parent==aStatement)
							continue;

						// let's find all binary expressions in the statement
						List<CtElement> elements = aStatement.getElements(e -> isBooleanExpression(e)).stream()
								.map(CtElement.class::cast).collect(Collectors.toList());

						for (CtElement specificelement : elements) {

							// retrieve all variables
							List<CtVariableAccess> varsInOtherExpressions = VariableResolver
									.collectVariableRead(specificelement);
							for (CtVariableAccess varInAnotherExpression : varsInOtherExpressions) {

								if (aVarInScope.getSimpleName().
										equals(varInAnotherExpression.getVariable().getSimpleName())) {
									foundVarUsed = true;
								}
							}
						}
					}

					if (foundVarUsed)
						otherVarUsedinBool++;
				}

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE1_EXISTS_RELATED_BOOLEAN_EXPRESSION,
						(similarUsedBefore) > 0, "FEATURES_LOGICAL_EXPRESSION");

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE8_SCOPE_VAR_USED_OTHER_BOOLEXPER,
						(otherVarUsedinBool) > 0, "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * // If the logical expression only uses local variables,whether all of the
	 * local variables have been used in other statements (exclude statements inside
	 * control flow structure) since the introduction
	 *
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
//	@SuppressWarnings("rawtypes")
//	private void analyzeLE8_LocalVariablesVariablesUsed(List<CtVariableAccess> varsAffected, CtElement element,
//			Cntx<Object> context) {
//		try {
//			CtExecutable methodParent = element.getParent(CtExecutable.class);
//
//			if (methodParent == null)
//				return;
//
//			List<CtStatement> statements = methodParent.getBody().getStatements();// methodParent.getElements(new
//																					// LineFilter());
//
//			// int similarUsedBefore = 0;
//			boolean allLocalVariableUsed = true;
//
//			for(CtVariableAccess variableAffected : varsAffected) {
//
//			}
//			// For each variable affected
//			for (CtVariableAccess variableAffected : varsAffected) {
//
//				boolean aVarUsed = false;
//
//				if (variableAffected.getVariable().getType() != null
//						&& !(variableAffected.getVariable().getDeclaration() instanceof CtLocalVariable)) {
//					continue;
//				}
//
//				boolean isInBinaryExpression = isParentBooleanExpression(variableAffected);
//
//				// For any variable involved in a logical expression,
//				if (!isInBinaryExpression)
//					continue;
//
//				// For each assignment in the methid
//				for (CtStatement aStatement : statements) {
//
//					CtStatement parent = variableAffected.getParent(new LineFilter());
//
//                    List<CtVariableAccess> varsInRightPart;
//
//					if (!isElementBeforeVariable(variableAffected, aStatement))
//						continue;
//
//					if (isStatementInControl(parent, aStatement) || parent==aStatement)
//						continue;
//
//					if(aStatement instanceof CtIf || aStatement instanceof CtLoop) {
//						varsInRightPart = VariableResolver.collectVariableRead(retrieveElementToStudy(aStatement));
//					} else
//						varsInRightPart = VariableResolver.collectVariableRead(aStatement);
//
//					// if the var access in the right is the same that the affected
//					for (CtVariableAccess varInStatement : varsInRightPart) {
//						if (hasSameName(variableAffected, varInStatement)) {
//							aVarUsed = true;
//						}
//					}
//				}
//				// one variable is not used before the faulty
//				if (!aVarUsed) {
//					allLocalVariableUsed = false;
//					break;
//				}
//			}
//
//			context.put(CodeFeatures.LE8_LOGICAL_WITH_USED_LOCAL_VARS, allLocalVariableUsed);
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * If the faulty statement involves object reference to local variables (i.e.,
	 * use object type local variables), do there exist certain referenced local
	 * variable(s) that have never been referenced in other statements (exclude
	 * statements inside control flow structure) before the faulty statement since
	 * its introduction (declaration) (chart-4)
	 *
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	@SuppressWarnings("rawtypes")
	private boolean analyzeS1_AffectedVariablesUsed(List<CtVariableAccess> varsAffected, CtElement element,
													Cntx<Object> context, List<CtStatement> statements, boolean whetherstatementlevel) {

		try {
			CtExecutable methodParent = element.getParent(CtExecutable.class);

			if (methodParent == null)
				// the element is not in a method.
				return false;
			statements=methodParent.getElements(new LineFilter());

			int usedObjects = 0;
			int notUsedObjects = 0;

			int usedObjectsLocal = 0;
			int usedPrimitiveLocal = 0;
			int notUsedObjectsLocal = 0;
			int notUsedPrimitiveLocal = 0;

			// For each variable affected
			for (CtVariableAccess variableAffected : varsAffected) {

				boolean aVarUsed = false;
				// boolean foundSimilarVarUsedBefore = false;

				CtStatement parent = variableAffected.getParent(new LineFilter());

				if(isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;

				// For each assignment in the methid
				for (CtStatement aStatement : statements) {

					List<CtVariableAccess> varsInRightPart;

					if (!isElementBeforeVariable(variableAffected, aStatement))
						continue;

					if (isStatementInControl(parent, aStatement) || parent==aStatement)
						continue;

					if(aStatement instanceof CtIf || aStatement instanceof CtLoop) {
						varsInRightPart = VariableResolver.collectVariableRead(retrieveElementToStudy(aStatement));
					} else
						varsInRightPart = VariableResolver.collectVariableRead(aStatement);

					// if the var access in the right is the same that the affected
					for (CtVariableAccess varInStatement : varsInRightPart) {
						if (hasSameName(variableAffected, varInStatement)
						) {
							aVarUsed = true;
						}
					}
					if (aVarUsed)
						break;
				}
				// Now, let's check the type of the var to see if it's local or not
				if (variableAffected.getVariable().getType() != null) {

					if (!variableAffected.getVariable().getType().isPrimitive()) {
						if (aVarUsed)
							usedObjects++;
						else
							notUsedObjects++;

						if (variableAffected.getVariable().getDeclaration() instanceof CtLocalVariable) {
							if (aVarUsed)
								usedObjectsLocal++;
							else
								notUsedObjectsLocal++;
						}
					} else {

						if (variableAffected.getVariable().getType().isPrimitive()
								&& (variableAffected.getVariable().getDeclaration() instanceof CtLocalVariable))
							if (aVarUsed)
								usedPrimitiveLocal++;
							else
								notUsedPrimitiveLocal++;
					}
				}
			}

			if(whetherstatementlevel) {
				context.put(CodeFeatures.NR_OBJECT_USED, usedObjects);
				context.put(CodeFeatures.NR_OBJECT_NOT_USED, notUsedObjects);

				context.put(CodeFeatures.NR_OBJECT_USED_LOCAL_VAR, usedObjectsLocal);
				context.put(CodeFeatures.NR_OBJECT_NOT_USED_LOCAL_VAR, notUsedObjectsLocal);

				context.put(CodeFeatures.NR_PRIMITIVE_USED_LOCAL_VAR, usedPrimitiveLocal);
				context.put(CodeFeatures.NR_PRIMITIVE_NOT_USED_LOCAL_VAR, notUsedPrimitiveLocal);

				context.put(CodeFeatures.S1_LOCAL_VAR_NOT_USED, (notUsedObjectsLocal) > 0);
			}

			return (notUsedObjectsLocal) > 0;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return false;
	}

	// S7: If the faulty statement involves object reference (either local or class field),
	// do there exist certain referenced variable(s) for which the last time they appear in the faulty class
	// (before the faulty statement and exclude statements in control structure) are left-hand side of assignment.
	// S8: same, but primitive type variables
	private boolean[] analyzeS7S8_AffectedObjectLastAppear(List<CtVariableAccess> varsAffected, CtElement element,
														   Cntx<Object> context, List<CtStatement> statements, boolean whetherstatementlevel) {

		try {
			CtExecutable methodParent = element.getParent(CtExecutable.class);

			if (methodParent == null)
				// the element is not in a method.
				return null;

			statements=methodParent.getElements(new LineFilter());

			int objectsLastAssign = 0;
			int objectsLastUse = 0;
			int primitiveLastAssign = 0;
			int primitiveLastUse = 0;

			// For each variable affected
			for (CtVariableAccess variableAffected : varsAffected) {

				boolean aVarAppearLastAssign = false;
				// boolean foundSimilarVarUsedBefore = false;

				CtStatement parent = variableAffected.getParent(new LineFilter());

				if(isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;

				// For each assignment in the methid
				for (CtStatement aStatement : statements) {

					List<CtVariableAccess> varsInRightPart;

					if (!isElementBeforeVariable(variableAffected, aStatement))
						continue;

					if (isStatementInControl(parent, aStatement) || parent==aStatement)
						continue;

					if(aStatement instanceof CtIf || aStatement instanceof CtLoop) {
						varsInRightPart = VariableResolver.collectVariableRead(retrieveElementToStudy(aStatement));
					} else
						varsInRightPart = VariableResolver.collectVariableRead(aStatement);

					// if the var access in the right is the same that the affected
					for (CtVariableAccess varInStatement : varsInRightPart) {
						if (hasSameName(variableAffected, varInStatement)
						) {
							aVarAppearLastAssign = false;
						}
					}

					if(aStatement instanceof CtAssignment) {
						CtAssignment assignment=(CtAssignment)aStatement;
						if (assignment.getAssigned().toString().equals(variableAffected.getVariable().getSimpleName())) {
							aVarAppearLastAssign = true;
						}
					}

					if(aStatement instanceof CtLocalVariable) {
						CtLocalVariable ctLocalVariable=(CtLocalVariable)aStatement;

						if (ctLocalVariable.getReference().getSimpleName()
								.equals(variableAffected.getVariable().getSimpleName()))
							aVarAppearLastAssign = true;
					}
				}

				// Now, let's check the type of the var to see if it's local or not
				if (variableAffected.getVariable().getType() != null) {

					if (!variableAffected.getVariable().getType().isPrimitive()) {
						if (aVarAppearLastAssign)
							objectsLastAssign++;
						else
							objectsLastUse++;

					} else {

						if (variableAffected.getVariable().getType().isPrimitive())
							if (aVarAppearLastAssign)
								primitiveLastAssign++;
							else
								primitiveLastUse++;
					}
				}
			}

			if(whetherstatementlevel) {
				context.put(CodeFeatures.S7_OBJECT_USED_IN_ASSIGNMENT, (objectsLastAssign) > 0);
				context.put(CodeFeatures.S8_PRIMITIVE_USED_IN_ASSIGNMENT, (primitiveLastAssign) > 0);
			}

			boolean[] expressionfeatures= new boolean[2];

			expressionfeatures[0]=(objectsLastAssign > 0);
			expressionfeatures[1]=(primitiveLastAssign > 0);

			return expressionfeatures;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean[] analyzeS9S10S12_SamerMethodWithGuardOrTrywrap (CtElement element, Cntx<Object> context, CtClass parentClass,
																	 List<CtInvocation> allinvocationsFromClass, List<CtInvocation> invocationstostudy,
																	 List<CtConstructorCall> allconstructorcallsFromClass, List<CtConstructorCall> constructorcallstostudy, boolean whetherstatementlevel) {

		try {
			// For each method invocation, whether the method has overloaded method
			boolean S9anyhasNormalGuard = false;
			boolean S10anyhasNULLGuard = false;

			for (CtInvocation invocation : invocationstostudy) {

				CtStatement parent = invocation.getParent(new LineFilter());

				if(isNormalGuard(invocation, (parent)) || isNullCheckGuard(invocation, (parent)))
					continue;

				// For each method in the class
				for (CtInvocation specificinvocation : allinvocationsFromClass) {

					if(invocation.equals(specificinvocation))
						continue;

					if(invocation.getExecutable().getSimpleName().equals
							(specificinvocation.getExecutable().getSimpleName())) {

						CtStatement specificparent = specificinvocation.getParent(new LineFilter());

						if (isNormalGuard(specificinvocation, (specificparent)))
							S9anyhasNormalGuard =true;

						if (isNullCheckGuard(specificinvocation, (specificparent)))
							S10anyhasNULLGuard =true;
					}

					if(S9anyhasNormalGuard && S10anyhasNULLGuard)
						break;
				}

				if(S9anyhasNormalGuard && S10anyhasNULLGuard)
					break;
			} // end invocation

			if(!(S9anyhasNormalGuard && S10anyhasNULLGuard)) {
				for (CtConstructorCall constructorcall : constructorcallstostudy) {

					CtStatement parent = constructorcall.getParent(new LineFilter());

					if(isNormalGuard(constructorcall, (parent)) || isNullCheckGuard(constructorcall, (parent)))
						continue;

					// For each method in the class
					for (CtConstructorCall specificconstructorcall : allconstructorcallsFromClass) {

						if(constructorcall.equals(specificconstructorcall))
							continue;

						if(constructorcall.getExecutable().getSimpleName().equals
								(specificconstructorcall.getExecutable().getSimpleName())) {

							CtStatement specificparent = specificconstructorcall.getParent(new LineFilter());

							if (isNormalGuard(specificconstructorcall, (specificparent)))
								S9anyhasNormalGuard =true;

							if (isNullCheckGuard(specificconstructorcall, (specificparent)))
								S10anyhasNULLGuard =true;
						}

						if(S9anyhasNormalGuard && S10anyhasNULLGuard)
							break;
					}

					if(S9anyhasNormalGuard && S10anyhasNULLGuard)
						break;
				} // end invocation
			}

			if(whetherstatementlevel) {
				context.put(CodeFeatures.S9_METHOD_CALL_WITH_NORMAL_GUARD, S9anyhasNormalGuard);
				context.put(CodeFeatures.S10_METHOD_CALL_WITH_NULL_GUARD, S10anyhasNULLGuard);
			}

			boolean S12anyhasTryCatch = false;

			for (CtInvocation invocation : invocationstostudy) {

				CtStatement parent = invocation.getParent(new LineFilter());

				CtTry potentionalTryCatch = invocation.getParent(CtTry.class);

				if(potentionalTryCatch != null && whethereffectivetrycatch (potentionalTryCatch,parent))
					continue;

				// For each method in the class
				for (CtInvocation specificinvocation : allinvocationsFromClass) {

					if(invocation.equals(specificinvocation))
						continue;

					if(invocation.getExecutable().getSimpleName().equals
							(specificinvocation.getExecutable().getSimpleName())) {

						CtStatement parentspecific = specificinvocation.getParent(new LineFilter());

						CtTry potentionalTryCatchspecific = specificinvocation.getParent(CtTry.class);

						if(potentionalTryCatchspecific != null &&
								whethereffectivetrycatch (potentionalTryCatchspecific, parentspecific))
							S12anyhasTryCatch = true;
					}

					if(S12anyhasTryCatch)
						break;
				}

				if(S12anyhasTryCatch)
					break;
			} // end invocation

			if(!S12anyhasTryCatch) {
				for (CtConstructorCall constructorcall : constructorcallstostudy) {

					CtStatement parent = constructorcall.getParent(new LineFilter());

					CtTry potentionalTryCatch = constructorcall.getParent(CtTry.class);

					if(potentionalTryCatch != null && whethereffectivetrycatch (potentionalTryCatch, parent))
						continue;

					// For each method in the class
					for (CtConstructorCall specificconstructorcall : allconstructorcallsFromClass) {

						if(constructorcall.equals(specificconstructorcall))
							continue;

						if(constructorcall.getExecutable().getSimpleName().equals
								(specificconstructorcall.getExecutable().getSimpleName())) {

							CtStatement parentspecific = specificconstructorcall.getParent(new LineFilter());

							CtTry potentionalTryCatchspecific = specificconstructorcall.getParent(CtTry.class);

							if(potentionalTryCatchspecific != null &&
									whethereffectivetrycatch (potentionalTryCatchspecific, parentspecific))
								S12anyhasTryCatch = true;
						}

						if(S12anyhasTryCatch)
							break;
					}

					if(S12anyhasTryCatch)
						break;
				} // end invocation
			}

			if(whetherstatementlevel) {
				context.put(CodeFeatures.S12_METHOD_CALL_WITH_TRY_CATCH, S12anyhasTryCatch);
			}

			boolean[] expressionfeatures=new boolean[2];

			expressionfeatures[0] = S9anyhasNormalGuard;
			expressionfeatures[1] = S10anyhasNULLGuard;

			return expressionfeatures;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return null;
	}

	public static boolean whethereffectivetrycatch (CtTry trystatement, CtStatement targetstatement) {

		CtBlock tryblock=trystatement.getBody();

		if(tryblock!=null) {
			List<CtStatement> trystatements=tryblock.getStatements();
			//	if(trystatements.size()>0 && trystatements.get(0)==targetstatement)
			if(trystatements.size() > 0)
				return true;
		}

		return false;
	}

	private boolean isStatementInControl(CtStatement targetstatement, CtStatement statementtocompare) {
		CtElement parentelement = targetstatement.getParent();
		int layer=0;
		CtElement parent;
		parent=statementtocompare;
		do {
			parent= parent.getParent();
			layer++;
		} while (parent!=parentelement && parent!=null);

		if(layer>1 && parent!=null)
			return true;
		else return false;
	}

	private boolean[] analyzeS2_S5_SametypewithGuard(List<CtVariableAccess> varsAffected, CtElement element,
													 Cntx<Object> context, CtClass parentClass, List<CtStatement> statements, boolean whetherstatementlevel) {

		boolean hasPrimitiveSimilarTypeWithNormalGuard = false;
		boolean hasObjectSimilarTypeWithNormalGuard = false;
		boolean hasPrimitiveSimilarTypeWithNullGuard = false;
		boolean hasObjectSimilarTypeWithNullGuard = false;

		try {
			CtExecutable faultyMethodParent = element.getParent(CtExecutable.class);

			if (faultyMethodParent == null)
				// the element is not in a method.
				return null;

			// For each variable affected
			for (CtVariableAccess variableAffected : varsAffected) {
				// for (CtStatement aStatement : statements) {
				CtStatement parent = variableAffected.getParent(new LineFilter());

				if(isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;

				// For each statement in the method (it includes the statements inside the
				// blocks (then, while)!)
				for (CtStatement aStatement : statements) {
					// for (CtVariableAccess variableAffected : varsAffected) {

					if (parent==aStatement)
						continue;

					List<CtVariableAccess> varsFromStatement;

					if(aStatement instanceof CtIf || aStatement instanceof CtLoop) {
						varsFromStatement = VariableResolver.collectVariableRead(retrieveElementToStudy(aStatement));
					} else
						varsFromStatement = VariableResolver.collectVariableRead(aStatement);
//
//					// let's collect the var access in the statement
//					List<CtVariableAccess> varsFromStatement = VariableResolver
//							.collectVariableReadIgnoringBlocks(aStatement);
					// if the var access is the same that the affected
					for (CtVariableAccess varInStatement : varsFromStatement) {
						// Has similar type but different name
						if (compareTypes(variableAffected.getVariable().getType(),
								varInStatement.getVariable().getType())) {
							//	&& !hasSameName(variableAffected, varInStatement)) {
							// Now, let's check if the parent is a guard
							// if (isGuard(getParentNotBlock(aStatement))) {
							if (isNormalGuard(varInStatement, (aStatement))) {

								// it's ok, now let's check the type
								if (variableAffected.getType() != null) {
									// for primitive type variables, we require it to be the same global variable
									if (variableAffected.getType().isPrimitive() && varInStatement.getVariable().getSimpleName().
											equals(variableAffected.getVariable().getSimpleName()))
										hasPrimitiveSimilarTypeWithNormalGuard = true;
									else
										hasObjectSimilarTypeWithNormalGuard = true;
								}
							}

							if (isNullCheckGuard(varInStatement, (aStatement))) {

								// it's ok, now let's check the type
								if (variableAffected.getType() != null) {

									if (variableAffected.getType().isPrimitive() && varInStatement.getVariable().getSimpleName().
											equals(variableAffected.getVariable().getSimpleName()))
										hasPrimitiveSimilarTypeWithNullGuard = true;
									else
										hasObjectSimilarTypeWithNullGuard = true;
								}
							}

						}
					}
					// If we find both cases, we can stop
					if (hasPrimitiveSimilarTypeWithNormalGuard && hasObjectSimilarTypeWithNormalGuard &&
							hasPrimitiveSimilarTypeWithNullGuard && hasObjectSimilarTypeWithNullGuard)
						break;
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}

		if(whetherstatementlevel) {
			context.put(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_NORMAL_GUARD, hasObjectSimilarTypeWithNormalGuard);
			context.put(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_NORMAL_GUARD, hasPrimitiveSimilarTypeWithNormalGuard);
			context.put(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_NULL_GUARD, hasObjectSimilarTypeWithNullGuard);
			context.put(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_NULL_GUARD, hasPrimitiveSimilarTypeWithNullGuard);
		}

		boolean[] expressionvalue = new boolean[4];
		expressionvalue[0]=hasObjectSimilarTypeWithNormalGuard;
		expressionvalue[1]=hasPrimitiveSimilarTypeWithNormalGuard;
		expressionvalue[2]=hasObjectSimilarTypeWithNullGuard;
		expressionvalue[3]=hasPrimitiveSimilarTypeWithNullGuard;

		return expressionvalue;

	}

	/**
	 * Returns a parent that is not a block
	 *
	 * @param aStatement
	 * @return
	 */
	public CtElement getParentNotBlock(CtElement aStatement) {

		if (aStatement == null)
			return null;
		if (aStatement.getParent() instanceof CtBlock)
			return getParentNotBlock(aStatement.getParent());

		return aStatement.getParent();
	}

	/**
	 * Return if the element is a guard
	 *
	 * @param element
	 * @return
	 */
//	private boolean isGuard(CtElement element) {
//
//		// First, find the condition
//		CtExpression condition = null;
//		if (element instanceof CtIf) {
//
//			CtIf guardCandidateIf = (CtIf) element;
//
//			condition = guardCandidateIf.getCondition();
//
//		} else if (element instanceof CtConditional) {
//			CtConditional cond = (CtConditional) element;
//			condition = cond.getCondition();
//
//		}
//		checkGuardCondition(condition);
//		return false;
//	}

	/**
	 * Return if the element is a guard
	 *
	 * @param element
	 * @return
	 */
	private boolean isNormalGuard(CtElement element, CtStatement parentStatement) {

		// Two cases: if and conditional
		CtExpression condition = null;
		CtConditional parentConditional = element.getParent(CtConditional.class);

		if (parentConditional != null) { // TODO, maybe force that the var must be in the condition, or not.
			CtConditional cond = (CtConditional) parentConditional;
			condition = cond.getCondition();
			return checkNormalGuardCondition(condition);
		} else {
			CtElement parentElement = getParentNotBlock(parentStatement);
			// First, find the condition

			if (parentElement instanceof CtIf) {

				CtIf guardCandidateIf = (CtIf) parentElement;

				if(whethereffectiveguard(guardCandidateIf, parentStatement)) {
					condition = guardCandidateIf.getCondition();
					boolean isConditionAGuard = checkNormalGuardCondition(condition);
					return isConditionAGuard;
				}
			}
		}
		return false;
	}

	private boolean isNullCheckGuard(CtElement element, CtStatement parentStatement) {

		// Two cases: if and conditional
		CtExpression condition = null;
		CtConditional parentConditional = element.getParent(CtConditional.class);

		if (parentConditional != null) {// TODO, maybe force that the var must be in the condition, or not.
			CtConditional cond = (CtConditional) parentConditional;
			condition = cond.getCondition();
			return checkNullCheckGuardCondition(condition);

		} else {
			CtElement parentElement = getParentNotBlock(parentStatement);
			// First, find the condition

			if (parentElement instanceof CtIf) {

				CtIf guardCandidateIf = (CtIf) parentElement;

				if(whethereffectiveguard(guardCandidateIf, parentStatement)) {
					condition = guardCandidateIf.getCondition();
					boolean isConditionAGuard = checkNullCheckGuardCondition(condition);
					return isConditionAGuard;
				}
			}
		}
		return false;
	}

	public static boolean whethereffectiveguard(CtIf ifstatement, CtStatement targetstatement) {
		CtBlock thenBlock = ifstatement.getThenStatement();
		CtBlock elseBlock = ifstatement.getElseStatement();

		if(thenBlock!=null) {
			List<CtStatement> thenstatements=thenBlock.getStatements();
			if(thenstatements.size()>0 && thenstatements.get(0)==targetstatement)
				return true;
		}

		if(elseBlock!=null) {
			List<CtStatement> elsestatements=elseBlock.getStatements();
			if(elsestatements.size()>0 && elsestatements.get(0)==targetstatement)
				return true;
		}

		return false;
	}

	/**
	 * Return if the Condition is a guard
	 *
	 * @param condition
	 * @return
	 */
	// we want the condition not to be null related check
	public boolean checkNormalGuardCondition(CtExpression condition) {
		if (condition != null) {
			List<CtBinaryOperator> binOp = condition.getElements(new TypeFilter<>(CtBinaryOperator.class));
			if (binOp != null && binOp.size() > 0) {

				for (CtBinaryOperator ctBinaryOperator : binOp) {
					if (ctBinaryOperator.getRightHandOperand().toString().equals("null")
							|| ctBinaryOperator.getLeftHandOperand().toString().equals("null")) {

						return false;
					}
				}
			}
//			// If it's a unary, we keep the operand
//			if (condition instanceof CtUnaryOperator) {
//				condition = ((CtUnaryOperator) condition).getOperand();
//			}
//			// check if the if is a a boolean invocation
//			if (condition instanceof CtInvocation) {
//
//				// CtInvocation invocation = (CtInvocation) condition;
//				// the method invocation must return a boolean, so not necessary to
//				// check
//				// if (invocation.getType() != null &&
//				// invocation.getType().unbox().toString().equals("boolean"))
//				return true;
//			}
			return true;
		}
		return false;
	}

	public boolean checkNullCheckGuardCondition(CtExpression condition) {
		if (condition != null) {
			List<CtBinaryOperator> binOp = condition.getElements(new TypeFilter<>(CtBinaryOperator.class));
			if (binOp != null && binOp.size() > 0) {

				for (CtBinaryOperator ctBinaryOperator : binOp) {
					if (!ctBinaryOperator.getRightHandOperand().toString().equals("null")
							&& !ctBinaryOperator.getLeftHandOperand().toString().equals("null")) {

						return false;
					}
				}

				return true;
			}
//			// If it's a unary, we keep the operand
//			if (condition instanceof CtUnaryOperator) {
//				condition = ((CtUnaryOperator) condition).getOperand();
//			}
//			// check if the if is a a boolean invocation
//			if (condition instanceof CtInvocation) {
//
//				// CtInvocation invocation = (CtInvocation) condition;
//				// the method invocation must return a boolean, so not necessary to
//				// check
//				// if (invocation.getType() != null &&
//				// invocation.getType().unbox().toString().equals("boolean"))
//				return true;
//			}
			return false;
		}
		return false;
	}

	public boolean hasSameName(CtVariableAccess variableAffected, CtVariableAccess varInStatement) {
		return varInStatement.getVariable().getSimpleName().equals(variableAffected.getVariable().getSimpleName())
				|| varInStatement.equals(variableAffected);
	}

	public boolean isParentBooleanExpression(CtVariableAccess varInStatement) {

		CtExpression currentElement = varInStatement;
		CtExpression expressionsParent = null;
		do {
			expressionsParent = currentElement.getParent(CtExpression.class);
			if (expressionsParent != null) {
				currentElement = expressionsParent;

				// If it's binary, the result is Boolean
				boolean isLogical = isLogicalExpressionInParent(currentElement);
				if (isLogical)
					return true;

				// If we have the type, check if it's boolean
				if ((currentElement.getType() != null
						&& currentElement.getType().unbox().toString().equals("boolean"))) {
					return true;
				}
			}
		} while (expressionsParent != null);

		return false;

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

	public boolean isLogicalExpressionInParent(CtElement currentElement) {
		if (currentElement == null)
			return false;
		if (isLogicalExpression(currentElement)) {
			return true;
		}

		return isLogicalExpressionInParent(currentElement.getParent(CtBinaryOperator.class));
	}

	public boolean isLogicalExpression(CtElement currentElement) {
		if (currentElement == null)
			return false;
		if ((currentElement instanceof CtBinaryOperator)) {
			CtBinaryOperator binOp = (CtBinaryOperator) currentElement;
//			if (binop.getKind().equals(BinaryOperatorKind.AND) || binop.getKind().equals(BinaryOperatorKind.OR)
//					|| binop.getKind().equals(BinaryOperatorKind.EQ) || binop.getKind().equals(BinaryOperatorKind.NE)
//			// || (binop.getType() != null &&
//			// binop.getType().unbox().getSimpleName().equals("boolean"))
//			)

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
	 * If the faulty statement involves object reference to local variables (i.e.,
	 * use object type local variables), do there exist certain referenced local
	 * variable(s) that have never been referenced in other statements (exclude
	 * statements inside control flow structure) before the faulty statement since
	 * its introduction (declaration)(chart-4)
	 *
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	@SuppressWarnings("rawtypes")
	private boolean analyzeS1_AffectedAssigned(List<CtVariableAccess> varsAffected, CtElement element,
											   Cntx<Object> context, boolean whetherstatementlevel) {
		try {
			CtMethod methodParent = element.getParent(CtMethod.class);

			List<CtAssignment> assignments = new ArrayList<>();
			List<CtLocalVariable> localsVariable = new ArrayList<>();

			// Get all vars from variables
			CtScanner assignmentScanner = new CtScanner() {

				@Override
				public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {

					assignments.add(assignement);
				}

				@Override
				public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {

					localsVariable.add(localVariable);
				}
			};

			assignmentScanner.scan(methodParent);
			boolean hasIncomplete = false;
			int nrOfVarWithAssignment = 0;
			int nrOfVarWithoutAssignment = 0;

			int nrOfLocalVarWithAssignment = 0;
			int nrOfLocalVarWithoutAssignment = 0;

			// For each variable affected
			for (CtVariableAccess variableAffected : varsAffected) {

				boolean varHasAssig = false;

				CtStatement parent = variableAffected.getParent(new LineFilter());

				if(isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;
				// For each assignment in the method
				for (CtAssignment assignment : assignments) {

					if (!isElementBeforeVariable(variableAffected, assignment))
						continue;

					if (isStatementInControl(parent, assignment) || parent==assignment)
						continue;

					if (assignment.getAssigned().toString().equals(variableAffected.getVariable().getSimpleName())) {
						varHasAssig = true;
					}
					boolean incomplete = retrieveNotAllInitialized(variableAffected, assignment, assignments);
					if (incomplete) {
						hasIncomplete = true;
					}
				}
				// Let's find in local declaration
				// if it was not assigned before
				if (!varHasAssig) {

					for (CtLocalVariable ctLocalVariable : localsVariable) {

						if (!isElementBeforeVariable(variableAffected, ctLocalVariable))
							continue;

						if (ctLocalVariable.getReference().getSimpleName()
								.equals(variableAffected.getVariable().getSimpleName())
								&& ctLocalVariable.getDefaultExpression() != null
								&& !"null".equals(ctLocalVariable.getDefaultExpression().toString()))
							varHasAssig = true;
					}

				}

				if (varHasAssig)
					nrOfVarWithAssignment++;
				else
					nrOfVarWithoutAssignment++;

				if (variableAffected.getVariable().getDeclaration() instanceof CtLocalVariable) {
					if (varHasAssig)
						nrOfLocalVarWithAssignment++;
					else
						nrOfLocalVarWithoutAssignment++;
				}

			}

			if(whetherstatementlevel) {
				context.put(CodeFeatures.NR_VARIABLE_ASSIGNED, nrOfVarWithAssignment);
				context.put(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED, nrOfVarWithoutAssignment);
				context.put(CodeFeatures.NR_FIELD_INCOMPLETE_INIT, hasIncomplete);
				context.put(CodeFeatures.NR_OBJECT_ASSIGNED_LOCAL, nrOfLocalVarWithAssignment);
				context.put(CodeFeatures.NR_OBJECT_NOT_ASSIGNED_LOCAL, nrOfLocalVarWithoutAssignment);

				// S1 is if NR_OBJECT_ASSIGNED_LOCAL > 0 then
				// if NR_VARIABLE_NOT_ASSIGNED = 0 then S1 = false else S1 = true
				// Else S1= false

				context.put(CodeFeatures.S1_LOCAL_VAR_NOT_ASSIGNED, (nrOfLocalVarWithoutAssignment > 0));
			}

			return nrOfLocalVarWithoutAssignment > 0;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean analyzeS4_AffectedFieldAssigned (List<CtVariableAccess> varsAffected, CtElement element, Cntx<Object> context,
													 CtClass parentClass, boolean whetherstatementlevel) {

		try {
			CtMethod methodParent = element.getParent(CtMethod.class);

			if (parentClass == null || methodParent == null)
				return false;

			List<CtStatement> statements = methodParent.getElements(new LineFilter());

			List<CtAssignment> assignments = new ArrayList<>();
			List<CtField> allfields = new ArrayList<>();

			// Get all vars from variables
			CtScanner assignmentScanner = new CtScanner() {

				@Override
				public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {

					assignments.add(assignement);
				}

				@Override
				public <T> void visitCtField(CtField<T> filed) {

					allfields.add(filed);
				}
			};

			assignmentScanner.scan(parentClass);

			boolean hasFieldNeverAssigned = false;
			// For each variable affected in the faulty statement
			for (CtVariableAccess variableAffected : varsAffected) {

				CtStatement parent = variableAffected.getParent(new LineFilter());

				if(isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;

				// if it's a field
				if (variableAffected instanceof CtFieldAccess) {

					if(variableAffected.getVariable().getType() == null ||
							variableAffected.getVariable().getType().isPrimitive())
						continue;

					boolean isFieldAssigned = false;

					for (CtAssignment assignment : assignments) {

						CtMethod methodParentAssign = assignment.getParent(CtMethod.class);

						if(methodParentAssign!=null && methodParentAssign.equals(methodParent)) {

							if (!isElementBeforeVariable(variableAffected, assignment))
								continue;

							if (isStatementInControl(parent, assignment) || parent==assignment)
								continue;

							if (assignment.getAssigned().toString().equals(variableAffected.getVariable().getSimpleName())) {
								isFieldAssigned = true;
							}
						} else {

							if (assignment.getAssigned().toString().equals(variableAffected.getVariable().getSimpleName())) {
								isFieldAssigned = true;
							}
						}

						if(isFieldAssigned)
							break;
					}

					if (!isFieldAssigned) {

						for (CtField specificField : allfields) {

							if (specificField.getReference().getSimpleName()
									.equals(variableAffected.getVariable().getSimpleName())
									&& specificField.getDefaultExpression() != null
									&& !"null".equals(specificField.getDefaultExpression().toString()))
								isFieldAssigned = true;
						}
					}

					// If the filed is never used on other methods and never used before the faulty statement in the faulty method
					if (!isFieldAssigned)
						hasFieldNeverAssigned = true;

					if(hasFieldNeverAssigned)
						break;
				}
			}

			if(whetherstatementlevel) {
				context.put(CodeFeatures.S4_Field_NOT_ASSIGNED, hasFieldNeverAssigned);
			}

			return hasFieldNeverAssigned;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * // If the faulty statement involves object reference to field (i.e., use
	 * object type class field), do there exist certain field(s) that have never
	 * been referenced in other methods of the faulty class.
	 *
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	@SuppressWarnings("rawtypes")
	private boolean analyzeS4_AffectedFielfs(List<CtVariableAccess> varsAffected, CtElement element, Cntx<Object> context,
											 CtClass parentClass, boolean whetherstatementlevel) {
		try {
			CtMethod methodParent = element.getParent(CtMethod.class);
			if (parentClass == null || methodParent == null)
				return false;

			List<CtStatement> statements = methodParent.getElements(new LineFilter());

			boolean hasFieldNeverUsedOutside = false;
			// For each variable affected in the faulty statement
			for (CtVariableAccess variableAffected : varsAffected) {

				CtStatement parent = variableAffected.getParent(new LineFilter());

				if(isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;

				// if it's a field
				if (variableAffected instanceof CtFieldAccess) {

					if(variableAffected.getVariable().getType() == null ||
							variableAffected.getVariable().getType().isPrimitive())
						continue;

					boolean isFieldUsed = false;

					// For the other methods
					for (Object amethod : parentClass.getAllMethods()) {

						CtMethod anotherMethod = (CtMethod) amethod;
						// ignore current method (where is the faulty)
						if (amethod.equals(methodParent))
							continue;

						// get all field access on the method
						List<CtElement> fieldsaccsess = anotherMethod.getElements(e -> e instanceof CtFieldAccess);
						for (CtElement ef : fieldsaccsess) {
							// check is the access is the same from that one used in the faulty
							CtFieldAccess faccess = (CtFieldAccess) ef;
							if (faccess.getVariable().getSimpleName()
									.equals(variableAffected.getVariable().getSimpleName())) {
								isFieldUsed = true;
							}
						}

						if(isFieldUsed)
							break;
					}

					boolean aVarUsedInFaultyMethod = false;

					for (CtStatement aStatement : statements) {

						List<CtVariableAccess> varsInRightPart;

						if (!isElementBeforeVariable(variableAffected, aStatement))
							continue;

						if (isStatementInControl(parent, aStatement) || parent==aStatement)
							continue;

						if(aStatement instanceof CtIf || aStatement instanceof CtLoop) {
							varsInRightPart = VariableResolver.collectVariableRead(retrieveElementToStudy(aStatement));
						} else
							varsInRightPart = VariableResolver.collectVariableRead(aStatement);

						// if the var access in the right is the same that the affected
						for (CtVariableAccess varInStatement : varsInRightPart) {
							if (hasSameName(variableAffected, varInStatement)
							) {
								aVarUsedInFaultyMethod = true;
							}
						}
						if (aVarUsedInFaultyMethod)
							break;
					}
					// If the filed is never used on other methods and never used before the faulty statement in the faulty method
					if (!isFieldUsed && !aVarUsedInFaultyMethod)
						hasFieldNeverUsedOutside = true;

					if(hasFieldNeverUsedOutside)
						break;
				}
			}

			if(whetherstatementlevel) {
				context.put(CodeFeatures.S4_Field_NOT_USED, hasFieldNeverUsedOutside);
			}

			return hasFieldNeverUsedOutside;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return false;
	}

	@SuppressWarnings("rawtypes")
	private boolean retrieveNotAllInitialized(CtVariableAccess variableAffected, CtAssignment assignment,
											  List<CtAssignment> assignments) {

		// Let's check if the var is a field reader to an Object
		String varAffected = null;
		if (variableAffected instanceof CtFieldRead) {
			CtFieldRead fr = (CtFieldRead) ((CtFieldRead) variableAffected);

			if (fr.getVariable().getType() == null || fr.getVariable().getType().isPrimitive()
					|| fr.getVariable().getDeclaration() == null) {

				return false;
			}
			varAffected = fr.getVariable().getDeclaration().getSimpleName();
		} else {
			return false;
		}

		// let's part of the assignment that we want to check if it's a field assignment
		CtExpression leftPAssignment = assignment.getAssigned();

		if (leftPAssignment instanceof CtFieldWrite) {
			// Field assignment
			CtFieldWrite fieldW = (CtFieldWrite) leftPAssignment;

			// here lets take fX
			if (fieldW.getTarget() instanceof CtFieldRead) {

				// The object where the assignment is done.
				CtVariableRead targetField = (CtVariableRead) fieldW.getTarget();
				// check if the variable is the same than the affected
				if (targetField.getVariable().getSimpleName().toString().equals(varAffected)) {

					Collection<CtFieldReference<?>> fields = targetField.getVariable().getType().getAllFields();
					String initialVar = targetField.getVariable().getSimpleName();
					for (CtFieldReference<?> otherFieldsFromVar : fields) {

						if (!otherFieldsFromVar.getFieldDeclaration().getVisibility().equals(ModifierKind.PRIVATE)
								&& otherFieldsFromVar.getSimpleName() != initialVar) {
							boolean fieldAssigned = false;
							for (CtAssignment otherAssignment : assignments) {

								// if (otherAssignment != assignment) {

								CtExpression leftOther = otherAssignment.getAssigned();

								if (leftOther instanceof CtFieldWrite) {
									CtFieldWrite fwriteOther = (CtFieldWrite) leftOther;
									// Let's check the
									if (// isElementBeforeVariable(fwriteOther, element)
										// &&
											fwriteOther.getVariable().getSimpleName()
													.equals(otherFieldsFromVar.getSimpleName())) {
										if (fwriteOther.getTarget() instanceof CtVariableRead) {
											CtVariableRead otherVar = (CtVariableRead) fwriteOther.getTarget();

											if (otherVar.getVariable().getSimpleName().equals(varAffected)) {

												fieldAssigned = true;
												break;
											}
										}
									}

								}

							}
							if (!fieldAssigned) {
								return true;
							}
						}
					}

				}
			}
		}
		return false;
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
			boolean anyhasMinDist = false;
			boolean v2SimilarNameCompatibleType = false;

			for (CtVariableAccess aVarAffected : varsAffected) {

				boolean v2VarSimilarNameCompatibleType = false;
				boolean v2VarSimilarName = false;

				for (CtVariable aVarInScope : varsInScope) {
					if (!aVarInScope.getSimpleName().equals(aVarAffected.getVariable().getSimpleName())) {
						int dist = StringDistance.calculate(aVarInScope.getSimpleName(),
								aVarAffected.getVariable().getSimpleName());
						if ((dist > 0 && dist < 3) || nameStartEndWithOther (aVarInScope.getSimpleName(),
								aVarAffected.getVariable().getSimpleName())) {
							anyhasMinDist = true;
							v2VarSimilarName=true;
							if (compareTypes(aVarAffected.getType(), aVarInScope.getType())) {
								v2SimilarNameCompatibleType = true;
								v2VarSimilarNameCompatibleType = true;
								// to save computation
								// break;
							}
						}
					}
				}
				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(aVarAffected),
						CodeFeatures.V2_HAS_VAR_SIM_NAME, (v2VarSimilarName));

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(aVarAffected),
						CodeFeatures.V2_HAS_VAR_SIM_NAME_COMP_TYPE, (v2VarSimilarNameCompatibleType));

			}

			context.put(CodeFeatures.HAS_VAR_SIM_NAME, anyhasMinDist);
			context.put(CodeFeatures.V2_HAS_VAR_SIM_NAME_COMP_TYPE, v2SimilarNameCompatibleType);
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
	 * @param varsInScope
	 * @param element
	 * @param context
	 */
	private void analyzeV3_AffectedHasConstant(List<CtVariableAccess> varsAffected, CtElement element,
											   Cntx<Object> context) {
		try {
			boolean hasConstant = false;
			for (CtVariableAccess aVarAffected : varsAffected) {
				boolean currentIsConstant = false;
				if (aVarAffected.getVariable() instanceof CtFieldReference &&
						// Check if it's uppercase
						aVarAffected.getVariable().getSimpleName().toUpperCase()
								.equals(aVarAffected.getVariable().getSimpleName())) {
					hasConstant = true;
					currentIsConstant = true;
				}
				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(aVarAffected),
						CodeFeatures.V3_HAS_CONSTANT, (currentIsConstant));
			}
			context.put(CodeFeatures.V3_HAS_CONSTANT, hasConstant);
		} catch (Throwable e) {
			e.printStackTrace();
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
			// For each involved variable, whether has method definitions or method calls
			// (in the fault class) that take the type of the involved variable as one of
			// its parameters and the return type of the method is type compatible with the
			// type of the involved variable
			boolean v1AnyVarCompatibleReturnAndParameterTypes = false;

			// For each involved variable, whether has methods in scope(method definitions
			// or method calls in the faulty class) that return a type which is the same or
			// compatible with the typeof the involved variable.
			boolean v6AnyVarReturnCompatible = false;

			boolean v16AnyVarParemeterCompatible = false;

			for (CtVariableAccess varAffected : varsAffected) {

				boolean v6CurrentVarReturnCompatible = false;
				boolean v1CurrentVarCompatibleReturnAndParameterTypes = false;
				boolean v16CurrentVarParameterCompatible = false;

				if (checkMethodDeclarationWithParameterReturnCompatibleType(allMethods, varAffected.getType()) != null
						|| checkInvocationWithParameterReturnCompatibleType(invocationsFromClass,
						varAffected.getType(), parentclass) != null) {
					v1AnyVarCompatibleReturnAndParameterTypes = true;
					v1CurrentVarCompatibleReturnAndParameterTypes = true;
				}

				if (checkMethodDeclarationWithReturnCompatibleType(allMethods, varAffected.getType()) != null
						|| checkInvocationWithReturnCompatibleType(invocationsFromClass,
						varAffected.getType(), parentclass) != null) {
					v6AnyVarReturnCompatible = true;
					v6CurrentVarReturnCompatible = true;
				}

				if (checkMethodDeclarationWithParemetrCompatibleType(allMethods, varAffected.getType()) != null
						|| checkInvocationWithParemetrCompatibleType (invocationsFromClass,
						varAffected.getType()) != null) {
					v16AnyVarParemeterCompatible = true;
					v16CurrentVarParameterCompatible = true;
				}

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(varAffected),
						CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN,
						(v1CurrentVarCompatibleReturnAndParameterTypes));

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(varAffected),
						CodeFeatures.V6_IS_METHOD_RETURN_TYPE_VAR, v6CurrentVarReturnCompatible);

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(varAffected),
						CodeFeatures.V16_IS_METHOD_PARAMETER_TYPE_VAR, v16CurrentVarParameterCompatible);

			}

			context.put(CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN,
					(v1AnyVarCompatibleReturnAndParameterTypes));

			context.put(CodeFeatures.V6_IS_METHOD_RETURN_TYPE_VAR, v6AnyVarReturnCompatible);

			context.put(CodeFeatures.V16_IS_METHOD_PARAMETER_TYPE_VAR, v16AnyVarParemeterCompatible);

		} catch (Throwable e) {
			e.printStackTrace();
		}
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

							if(parentConstructorCall.getExecutable().getSimpleName().equals
									(specificonstructorcall.getExecutable().getSimpleName()) &&
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

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(varAffected),
						CodeFeatures.V12_VAR_Invocation_VAR_REPLACE_BY_VAR,
						(v12ReplacewithVarCurrent));

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(varAffected),
						CodeFeatures.V13_VAR_Invocation_VAR_REPLACE_BY_INVOCATION, v13ReplacewithInvocationCurrent);

			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
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

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(varAffected),
						CodeFeatures.V14_VAR_INSTANCE_OF_CLASS,
						(v14VarInstanceOfClass));
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
								&& compareTypes(ctLocalVariable.getType(), variableAffected.getType()))
							lastthreesametypeloc = true;
					}
				}

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(variableAffected),
						CodeFeatures.V15_VAR_LAST_THREE_SAME_TYPE_LOC,
						(lastthreesametypeloc));
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

			if(original.equals(other) || original.toString().equals(other.toString())) {
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

	/**
	 * // For any variable involved in a logical expression,whether exist methods //
	 * (method declaration or method call) in scope (that is in the same faulty //
	 * class // since we do not assume full program) that take variable whose type
	 * is same // with vas one of its parameters and return boolean
	 *
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	private void analyzeLE2_AffectedVariablesInMethodold(List<CtVariableAccess> varsAffected, CtElement element,
														 Cntx<Object> context, List allMethods, List<CtInvocation> invocationsFromClass, CtClass parentclass) {
		try {

			boolean hasAnyLES2paramCompatibleWithBooleanReturn = false;

			for (CtVariableAccess varAffected : varsAffected) {

				if (!isParentBooleanExpression(varAffected))
					continue;

				boolean isCurrentVarLE2paramCompatibleWithBooleanReturn = false;

				if (// First, Let's analyze the method declaration
						checkBooleanMethodDeclarationWithTypeInParameter(allMethods, varAffected) != null
								// Second, let's inspect invocations
								|| checkBooleanInvocationWithParameterReturn(invocationsFromClass, varAffected, parentclass) != null) {
					hasAnyLES2paramCompatibleWithBooleanReturn = true;
					isCurrentVarLE2paramCompatibleWithBooleanReturn = true;
				}

				writeDetailedInformationFromVariables(context, adjustIdentifyInJson(varAffected),
						CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR,
						(isCurrentVarLE2paramCompatibleWithBooleanReturn));

			}

			context.put(CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR,
					(hasAnyLES2paramCompatibleWithBooleanReturn));

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

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

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR,
						hasAnyLES2paramCompatibleWithBooleanReturn, "FEATURES_LOGICAL_EXPRESSION");
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * For each arithmetic expression, whether has method definitions or method
	 * calls (in the fault class) that take the return type of the arithmetic
	 * expression as one of its parameters and the return type of the method is type
	 * compatible with the return type of the arithmetic expression.
	 *
	 * @param element
	 * @param context
	 */
	private void analyzeAE1(CtElement element, Cntx<Object> context, List allMethods,
							List<CtInvocation> invocationsFromClass, CtClass parentclass) {
		try {

			List<BinaryOperatorKind> opKinds = new ArrayList<>();
			opKinds.add(BinaryOperatorKind.DIV);
			opKinds.add(BinaryOperatorKind.PLUS);
			opKinds.add(BinaryOperatorKind.MINUS);
			opKinds.add(BinaryOperatorKind.MUL);
			opKinds.add(BinaryOperatorKind.MUL);

			boolean hasArithmeticCompatible = false;

			List<CtBinaryOperator> arithmeticOperators = element.getElements(
					e -> e instanceof CtBinaryOperator && opKinds.contains(((CtBinaryOperator) e).getKind()));

			for (CtBinaryOperator anAritmeticOperator : arithmeticOperators) {

				// First, Let's analyze the method declaration
				if (checkMethodDeclarationWithParameterReturnCompatibleType(allMethods,
						anAritmeticOperator.getType()) != null) {
					hasArithmeticCompatible = true;
				}

				// Second, let's inspect invocations
				else {
					if (checkInvocationWithParameterReturnCompatibleType(invocationsFromClass,
							anAritmeticOperator.getType(), parentclass) != null) {
						hasArithmeticCompatible = true;
					}
				}
			}

			context.put(CodeFeatures.AE1_COMPATIBLE_RETURN_TYPE, (hasArithmeticCompatible));

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

			// Check the parameters
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
	 * Check if a method declaration has a parameter compatible and return with the
	 * cttype as argument
	 *
	 * @param allMethods
	 * @param varAffected
	 * @return
	 */
	public CtMethod checkMethodDeclarationWithParameterReturnCompatibleType(List allMethods,
																			CtTypeReference typeToMatch) {
		for (Object omethod : allMethods) {

			if (!(omethod instanceof CtMethod))
				continue;

			CtMethod anotherMethodInBuggyClass = (CtMethod) omethod;

			// Check the parameters
			for (Object oparameter : anotherMethodInBuggyClass.getParameters()) {
				CtParameter parameter = (CtParameter) oparameter;

				if (compareTypes(typeToMatch, parameter.getType())
						&& compareTypes(typeToMatch, anotherMethodInBuggyClass.getType())) {

					return anotherMethodInBuggyClass;
				}
			}
		}

		return null;
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
			// For each invocation found in the class
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
							}
						}

						if(certainbinary.getRightHandOperand() instanceof CtInvocation) {
							CtInvocation anotherinvocation=(CtInvocation)certainbinary.getRightHandOperand();
							if(anotherinvocation.getExecutable().getSignature().equals(anInvocation.getExecutable().getSignature())
									&& certainbinary.getLeftHandOperand().getType()!=null) {
								inferredtype=certainbinary.getLeftHandOperand().getType();
								inferredpotentionaltypes.add(inferredtype);
							}
						}
					}
				} else inferredtype=anInvocation.getType();

				// Check types
				if (inferredtype != null && (inferredtype.getSimpleName().equals("Boolean")
						|| inferredtype.unbox().toString().equals("boolean") ||
						whetherpotentionalboolean(inferredpotentionaltypes))) {

					// For each argument in the invocation
					for (Object anObjArgument : anInvocation.getArguments()) {
						CtExpression anArgument = (CtExpression) anObjArgument;

						// retrieve Var access

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

	public CtInvocation checkInvocationWithParameterReturnCompatibleType(List<CtInvocation> invocationsFromClass,
																		 CtTypeReference type, CtClass parentclass) {

		List<CtBinaryOperator> binaryOperatorInClass = parentclass.getElements(e ->
				(e instanceof CtBinaryOperator)).stream()
				.map(CtBinaryOperator.class::cast).collect(Collectors.toList());

		// For each invocation found in the class
		for (CtInvocation anInvocation : invocationsFromClass) {

			List<CtTypeReference> inferredpotentionaltypes = new ArrayList<CtTypeReference>();
			inferredpotentionaltypes.clear();

			// Compatible types
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
						}
					}

					if(certainbinary.getRightHandOperand() instanceof CtInvocation) {
						CtInvocation anotherinvocation=(CtInvocation)certainbinary.getRightHandOperand();
						if(anotherinvocation.getExecutable().getSignature().equals(anInvocation.getExecutable().getSignature())
								&& certainbinary.getLeftHandOperand().getType()!=null) {
							inferredtype=certainbinary.getLeftHandOperand().getType();
							inferredpotentionaltypes.add(inferredtype);
						}
					}
				}

			} else inferredtype=anInvocation.getType();


			if (compareTypes(type, inferredtype) || compareInferredTypes(type, inferredpotentionaltypes)) {
				// For each argument in the invocation
				for (Object anObjArgument : anInvocation.getArguments()) {
					CtExpression anArgument = (CtExpression) anObjArgument;

					if (compareTypes(type, anArgument.getType())) {
						return anInvocation;
					}

					// retrieve Var access

//					List<CtVariableAccess> varReadFromArguments = VariableResolver.collectVariableRead(anArgument);
//
//					for (CtVariableAccess aVarReadFrmArgument : varReadFromArguments) {
//
//						//
//						if (compareTypes(type, aVarReadFrmArgument.getType())
//								&& compareTypes(type, anInvocation.getType())) {
//							return anInvocation;
//						}
//					}
				}
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
						}
					}

					if(certainbinary.getRightHandOperand() instanceof CtInvocation) {
						CtInvocation anotherinvocation=(CtInvocation)certainbinary.getRightHandOperand();
						if(anotherinvocation.getExecutable().getSignature().equals(anInvocation.getExecutable().getSignature())
								&& certainbinary.getLeftHandOperand().getType()!=null) {
							inferredtype=certainbinary.getLeftHandOperand().getType();
							inferredpotentionaltypes.add(inferredtype);
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

	private boolean isSubtype(CtVariableAccess var, CtMethod method) {
		try {
			return method.getType().isSubtypeOf(var.getType());
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean compareTypes(CtTypeReference t1, CtTypeReference t2) {
		try {

			return t1 != null && t2 != null && (t1.toString().equals(t2.toString()) ||
					t1.toString().toLowerCase().endsWith(t2.toString().toLowerCase()) ||
					t2.toString().toLowerCase().endsWith(t1.toString().toLowerCase()) ||
					t1.equals(t2) || t1.isSubtypeOf(t2) || t2.isSubtypeOf(t1));
		} catch (Exception e) {
			log.debug("Error comparing types");
			log.debug(e);
			return false;
		}

	}

	public static boolean compareInferredTypes(CtTypeReference t1, List<CtTypeReference> potentionaltypes) {

		for(int i=0; i<potentionaltypes.size(); i++) {
			if(compareTypes(t1, potentionaltypes.get(i)))
				return true;
		}

		return false;
	}

	/**
	 * For the logical expression, whether there exists a boolean expression that
	 * starts with the "not" operator! (an exclamation mark)
	 *
	 * @param element
	 * @param context
	 * @param parentContext
	 */
	private void analyzeLE6_UnaryInvolvedold(CtElement element, Cntx<Object> parentContext) {
		try {
			Cntx<Object> context = null;

			if (ComingProperties.getPropertyBoolean("avoidgroupsubfeatures")) {
				context = parentContext;

			} else {
				context = new Cntx<>();
				parentContext.put(CodeFeatures.UNARY_PROPERTIES, context);

			}

			List<String> binOps = new ArrayList();
			CtScanner scanner = new CtScanner() {

				@Override
				public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {

					super.visitCtUnaryOperator(operator);
					binOps.add(operator.getKind().toString());
				}
			};

			ExpressionCapturerScanner expressionScanner = new ExpressionCapturerScanner();
			expressionScanner.scan(element);
			if (expressionScanner.toScan != null) {
				scanner.scan(expressionScanner.toScan);
			} else {
				scanner.scan(element);
			}
			context.put(CodeFeatures.involved_relation_unary_operators, binOps);

			List<String> binOpsreal = new ArrayList();
			CtScanner scannerreal = new CtScanner() {

				@Override
				public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
					super.visitCtBinaryOperator(operator);
					binOpsreal.add(operator.getKind().toString());
				}
			};
			// CtElement toScan = null;
			ExpressionCapturerScanner scanner2 = new ExpressionCapturerScanner();
			scanner2.scan(element);
			if (scanner2.toScan != null) {
				scannerreal.scan(scanner2.toScan);
			} else {
				scannerreal.scan(element);
			}

			boolean containsAnd = binOpsreal.contains(BinaryOperatorKind.AND.toString());
			boolean containsOr = binOpsreal.contains(BinaryOperatorKind.OR.toString());

			context.put(CodeFeatures.involve_POS_relation_operators, binOps.contains(UnaryOperatorKind.POS.toString()));
			context.put(CodeFeatures.involve_NEG_relation_operators, binOps.contains(UnaryOperatorKind.NEG.toString()));
			boolean containsNot = binOps.contains(UnaryOperatorKind.NOT.toString());
			context.put(CodeFeatures.involve_NOT_relation_operators, containsNot);
			context.put(CodeFeatures.involve_COMPL_relation_operators,
					binOps.contains(UnaryOperatorKind.COMPL.toString()));
			context.put(CodeFeatures.involve_PREINC_relation_operators,
					binOps.contains(UnaryOperatorKind.PREINC.toString()));
			context.put(CodeFeatures.involve_PREDEC_relation_operators,
					binOps.contains(UnaryOperatorKind.PREDEC.toString()));
			context.put(CodeFeatures.involve_POSTINC_relation_operators,
					binOps.contains(UnaryOperatorKind.POSTINC.toString()));
			context.put(CodeFeatures.involve_POSTDEC_relation_operators,
					binOps.contains(UnaryOperatorKind.POSTDEC.toString()));

			if(containsAnd || containsOr)
				parentContext.put(CodeFeatures.LE6_HAS_NEGATION, containsNot);
			else parentContext.put(CodeFeatures.LE6_HAS_NEGATION, false);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

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

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE6_HAS_NEGATION,
						(containsAnd || containsOr) && containsNot, "FEATURES_LOGICAL_EXPRESSION");
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Whether the number of boolean expressions in the logical expression is larger
	 * than 1
	 *
	 * @param element
	 * @param context
	 * @param parentContext
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

	private void analyzeLE9_BothNULLAndNormalold(CtElement element, Cntx<Object> context) {

		try {

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

			// CtElement toScan = null;
			ExpressionCapturerScanner scanner2 = new ExpressionCapturerScanner();
			scanner2.scan(element);
			if (scanner2.toScan != null) {
				scanner.scan(scanner2.toScan);
			} else {
				scanner.scan(element);
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
					}
				}
			}

			context.put(CodeFeatures.LE9_NORMAL_CHECK, (whethercontainnormalcheck && !whethercontainnullcheck));
			context.put(CodeFeatures.LE9_NULL_CHECK, (!whethercontainnormalcheck && whethercontainnullcheck));
			context.put(CodeFeatures.LE9_MIX_CHECK, (whethercontainnormalcheck && whethercontainnullcheck));
			context.put(CodeFeatures.LE9_EQUAL_NOTEQUAL_NULL_CHECK, (equalnullcheck && notequalnullcheck));

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
						}
					}
				}

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE9_NORMAL_CHECK,
						(whethercontainnormalcheck && !whethercontainnullcheck), "FEATURES_LOGICAL_EXPRESSION");

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE9_NULL_CHECK,
						(!whethercontainnormalcheck && whethercontainnullcheck), "FEATURES_LOGICAL_EXPRESSION");

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE9_MIX_CHECK,
						(whethercontainnormalcheck && whethercontainnullcheck), "FEATURES_LOGICAL_EXPRESSION");

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE9_EQUAL_NOTEQUAL_NULL_CHECK,
						(equalnullcheck && notequalnullcheck), "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
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

				analyzeExpressions(atomicboolexperssions, context, indexlogical);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeExpressions(List<CtExpression> atomicexperssions, Cntx<Object> context, int logicalindex) {

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

		writeGroupedByVar(context, "logical_expression_"+Integer.toString(logicalindex),
				CodeFeatures.LE10_ATOMIC_EXPRESSION_SAME_INVOCATION_TARGET,
				(invocationtarget.size()>=2 &&
						invocationtarget.size() != new HashSet<CtExpression>(invocationtarget).size()),
				"FEATURES_LOGICAL_EXPRESSION");

		writeGroupedByVar(context, "logical_expression_"+Integer.toString(logicalindex),
				CodeFeatures.LE10_ATOMIC_EXPRESSION_COMPARISION_SAME_LEFT,
				(comparisionleft.size()>=2 &&
						comparisionleft.size() != new HashSet<CtExpression>(comparisionleft).size()),
				"FEATURES_LOGICAL_EXPRESSION");

		writeGroupedByVar(context, "logical_expression_"+Integer.toString(logicalindex),
				CodeFeatures.LE10_ATOMIC_EXPRESSION_COMPARISION_SAME_RIGHT,
				(comparisionright.size()>=2 &&
						comparisionright.size() != new HashSet<CtExpression>(comparisionright).size()),
				"FEATURES_LOGICAL_EXPRESSION");

		writeGroupedByVar(context, "logical_expression_"+Integer.toString(logicalindex),
				CodeFeatures.LE10_ATOMIC_EXPRESSION_MULTIPLE_VAR_AS_BOOLEAN,
				variableaccess.size()>=2, "FEATURES_LOGICAL_EXPRESSION");

		writeGroupedByVar(context, "logical_expression_"+Integer.toString(logicalindex),
				CodeFeatures.LE10_ATOMIC_EXPRESSION_USED_IN_INVOCATION_COMPARISION_VARIABLE,
				whetherinvtargetincomparision, "FEATURES_LOGICAL_EXPRESSION");

		writeGroupedByVar(context, "logical_expression_"+Integer.toString(logicalindex),
				CodeFeatures.LE10_CONTAINS_ALL_INVOCATION_COMPARISION_VARIABLE,
				invocationtypes.size()>0 && variableaccess.size()>0 &&
						comparisiontypes.size()>0, "FEATURES_LOGICAL_EXPRESSION");
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

						if(splitted.length>1)
							whethercomplexreference = true;
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

							if(splitted.length>2)
								whethercomplexreference = true;
						}
					}
				}

				writeGroupedByVar(context, "logical_expression_"+Integer.toString(indexlogical),
						CodeFeatures.LE5_COMPLEX_REFERENCE,
						whethercomplexreference, "FEATURES_LOGICAL_EXPRESSION");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeLE5_BinaryInvolved(CtElement element, Cntx<Object> parentContext) {
		try {
			Cntx<Object> context = null;

			if (ComingProperties.getPropertyBoolean("avoidgroupsubfeatures")) {
				// we write the properties in the parent
				context = parentContext;
			} else {
				context = new Cntx<>();
				parentContext.put(CodeFeatures.BIN_PROPERTIES, context);

			}

			List<String> binOps = new ArrayList();
			CtScanner scanner = new CtScanner() {

				@Override
				public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
					super.visitCtBinaryOperator(operator);
					binOps.add(operator.getKind().toString());
				}
			};

			// CtElement toScan = null;
			ExpressionCapturerScanner scanner2 = new ExpressionCapturerScanner();
			scanner2.scan(element);
			if (scanner2.toScan != null) {
				scanner.scan(scanner2.toScan);
			} else {
				scanner.scan(element);
			}
			context.put(CodeFeatures.involved_relation_bin_operators, binOps);

			context.put(CodeFeatures.involve_GE_relation_operators, binOps.contains(BinaryOperatorKind.GE.toString()));
			boolean containsAnd = binOps.contains(BinaryOperatorKind.AND.toString());
			context.put(CodeFeatures.involve_AND_relation_operators, containsAnd);
			boolean containsOr = binOps.contains(BinaryOperatorKind.OR.toString());
			context.put(CodeFeatures.involve_OR_relation_operators, containsOr);
			boolean containsBitor = binOps.contains(BinaryOperatorKind.BITOR.toString());
			context.put(CodeFeatures.involve_BITOR_relation_operators, containsBitor);
			boolean containsBitxor = binOps.contains(BinaryOperatorKind.BITXOR.toString());
			context.put(CodeFeatures.involve_BITXOR_relation_operators, containsBitxor);
			boolean containsBitand = binOps.contains(BinaryOperatorKind.BITAND.toString());
			context.put(CodeFeatures.involve_BITAND_relation_operators, containsBitand);
			context.put(CodeFeatures.involve_EQ_relation_operators, binOps.contains(BinaryOperatorKind.EQ.toString()));
			context.put(CodeFeatures.involve_NE_relation_operators, binOps.contains(BinaryOperatorKind.NE.toString()));
			context.put(CodeFeatures.involve_LT_relation_operators, binOps.contains(BinaryOperatorKind.LT.toString()));
			context.put(CodeFeatures.involve_GT_relation_operators, binOps.contains(BinaryOperatorKind.GT.toString()));
			context.put(CodeFeatures.involve_LE_relation_operators, binOps.contains(BinaryOperatorKind.LE.toString()));
			context.put(CodeFeatures.involve_SL_relation_operators, binOps.contains(BinaryOperatorKind.SL.toString()));
			context.put(CodeFeatures.involve_SR_relation_operators, binOps.contains(BinaryOperatorKind.SR.toString()));
			context.put(CodeFeatures.involve_USR_relation_operators,
					binOps.contains(BinaryOperatorKind.USR.toString()));
			context.put(CodeFeatures.involve_PLUS_relation_operators,
					binOps.contains(BinaryOperatorKind.PLUS.toString()));
			context.put(CodeFeatures.involve_MINUS_relation_operators,
					binOps.contains(BinaryOperatorKind.MINUS.toString()));
			context.put(CodeFeatures.involve_MUL_relation_operators,
					binOps.contains(BinaryOperatorKind.MUL.toString()));
			context.put(CodeFeatures.involve_DIV_relation_operators,
					binOps.contains(BinaryOperatorKind.DIV.toString()));
			context.put(CodeFeatures.involve_MOD_relation_operators,
					binOps.contains(BinaryOperatorKind.MOD.toString()));

			context.put(CodeFeatures.involve_INSTANCEOF_relation_operators,
					binOps.contains(BinaryOperatorKind.INSTANCEOF.toString()));

//			parentContext.put(CodeFeatures.LE5_BOOLEAN_EXPRESSIONS_IN_FAULTY,
//					(containsAnd || containsBitand || containsBitor || containsBitxor || containsOr));
//			parentContext.put(CodeFeatures.LE5_BOOLEAN_EXPRESSIONS_IN_FAULTY,
//					(containsAnd || containsOr));

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void retrieveType(CtElement element, Cntx<Object> context) {
		context.put(CodeFeatures.TYPE, element.getClass().getSimpleName());

	}

	private void retrievePosition(CtElement element, Cntx<Object> context) {
		try {
			if (element.getPosition() != null && element.getPosition().getFile() != null) {
				context.put(CodeFeatures.FILE_LOCATION, element.getPosition().getFile().getAbsolutePath());

				context.put(CodeFeatures.LINE_LOCATION, element.getPosition().getLine());
			} else {
				context.put(CodeFeatures.FILE_LOCATION, "");
				context.put(CodeFeatures.LINE_LOCATION, "");

			}
			CtType parentClass = element.getParent(spoon.reflect.declaration.CtType.class);

			context.put(CodeFeatures.PARENT_CLASS, (parentClass != null) ? parentClass.getQualifiedName() : "");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public Object determineKey(CtElement element) {
		String key = null;
		if (element.getPosition() != null && element.getPosition().getFile() != null) {
			key = element.getPosition().getFile().getName().toString();
		} else {
			key = element.getShortRepresentation();// To see.
		}
		return key;
	}

	public void analyzeS3_TypeOfFaulty(CtElement element, Cntx<Object> context) {
		try {
			String type = element.getClass().getSimpleName().replaceAll("Ct", "").replaceAll("Impl", "");
			context.put(CodeFeatures.S3_TYPE_OF_FAULTY_STATEMENT, type);
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
				for (CtStatement stmt: CS.getStatements()) {
					if (stmt.equals(element)) {
						found = true;
						idx = tmp.size();
					}
					tmp.add(stmt);
				}
				assert(found);

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

			for(int indexbefore=0; indexbefore < stmtsBefore.size(); indexbefore++) {
				CtElement beforeelement = stmtsBefore.get(indexbefore);
				String type = beforeelement.getClass().getSimpleName().replaceAll("Ct", "").replaceAll("Impl", "");

				if((stmtsBefore.size()-indexbefore) == 1) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_1, type);
				} else if((stmtsBefore.size()-indexbefore) == 2) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_2, type);
				} else if((stmtsBefore.size()-indexbefore) == 3) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_3, type);
				}
			}

			for(int indexafter=0; indexafter < stmtsAfter.size(); indexafter++) {
				CtElement afterelement = stmtsAfter.get(indexafter);
				String type = afterelement.getClass().getSimpleName().replaceAll("Ct", "").replaceAll("Impl", "");

				if(indexafter == 0) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_AFTER_1, type);
				} else if(indexafter == 1) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_AFTER_2, type);
				} else if(indexafter == 2) {
					context.put(CodeFeatures.S13_TYPE_OF_FAULTY_STATEMENT_AFTER_3, type);
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Puts in the context's object the information of each var in scope
	 *
	 * @param context
	 * @param varsInScope
	 */
	public void putVarInContextInformation(Cntx<Object> context, List<CtVariable> varsInScope) {
		try {
			context.put(CodeFeatures.VARS_IN_SCOPE, varsInScope);
			List<Cntx> children = new ArrayList();
			for (CtVariable ctVariable : varsInScope) {
				Cntx c = new Cntx<>();
				c.put(CodeFeatures.VAR_VISIB,
						(ctVariable.getVisibility() == null) ? "" : (ctVariable.getVisibility()).toString());
				c.put(CodeFeatures.VAR_TYPE, ctVariable.getType().getQualifiedName());
				c.put(CodeFeatures.VAR_MODIF, ctVariable.getModifiers());
				c.put(CodeFeatures.VAR_NAME, ctVariable.getSimpleName());
				children.add(c);

			}
			context.put(CodeFeatures.VARS, children);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeCon1_ConstructorOverload(CtElement element, Cntx<Object> context, CtClass parentClass,
												 List<CtConstructorCall> constructorcalls) {
		try {
			boolean con1anyhasSameName=false;

			for (CtConstructorCall constructorcall : constructorcalls) {

				boolean con1SpecificHasSameName = false;

				List<CtConstructor> allconstructorsinclass = parentClass.getElements(new TypeFilter<>(CtConstructor.class));

				for (CtConstructor certainconstructorinclass : allconstructorsinclass) {

					CtConstructor anotherConstructor = (CtConstructor) certainconstructorinclass;
					// Ignoring if it's the same
					if (anotherConstructor == null || anotherConstructor.getSignature().
							equals(constructorcall.getExecutable().getSignature()))
						continue;

					if (anotherConstructor.getSimpleName().equals(constructorcall.getExecutable().getSimpleName())) {
						// It's override
						con1anyhasSameName = true;
						con1SpecificHasSameName = true;
					}
				}

				List<CtConstructorCall> allconstructorcallsinclass = parentClass.getElements(new TypeFilter<>(CtConstructorCall.class));

				if(!con1SpecificHasSameName) {

					for (CtConstructorCall certainconstructorcallinclass : allconstructorcallsinclass) {

						CtConstructorCall anotherConstructorCall = (CtConstructorCall) certainconstructorcallinclass;
						// Ignoring if it's the same
						if (anotherConstructorCall == null || anotherConstructorCall.getExecutable().getSignature().
								equals(constructorcall.getExecutable().getSignature()))
							continue;

						if (anotherConstructorCall.getExecutable().getSimpleName().equals(constructorcall.getExecutable().getSimpleName())) {
							// It's override
							con1anyhasSameName = true;
							con1SpecificHasSameName = true;
						}
					}
				}

				writeGroupedByVar(context, adjustIdentifyInJson(constructorcall), CodeFeatures.CON1_OVERLOADED_CONSTRUCTOR,
						con1SpecificHasSameName, "FEATURES_CONSTRUCTOR");

			} // end invocation

			context.put(CodeFeatures.CON1_OVERLOADED_CONSTRUCTOR, con1anyhasSameName);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeCon2_ConstructorSimilar(CtElement element, Cntx<Object> context, CtClass parentClass,
												List<CtConstructorCall> constructorcalls) {

		try {
			boolean con2anyhasSimilarName=false;

			for (CtConstructorCall constructorcall : constructorcalls) {

				boolean con2SpecificHasSimilarName = false;

				List<CtConstructor> allconstructorsinclass = parentClass.getElements(new TypeFilter<>(CtConstructor.class));

				for (CtConstructor certainconstructorinclass : allconstructorsinclass) {

					CtConstructor anotherConstructor = (CtConstructor) certainconstructorinclass;
					// Ignoring if it's the same
					if (anotherConstructor == null || anotherConstructor.getSignature().
							equals(constructorcall.getExecutable().getSignature()))
						continue;

					if (!anotherConstructor.getSimpleName().equals(constructorcall.getExecutable().getSimpleName())) {

						int dist = StringDistance.calculate(anotherConstructor.getSimpleName(),
								constructorcall.getExecutable().getSimpleName());
						if ((dist > 0 && dist < 3) || anotherConstructor.getSimpleName().startsWith(constructorcall.getExecutable().getSimpleName())
								|| anotherConstructor.getSimpleName().endsWith(constructorcall.getExecutable().getSimpleName())||
								constructorcall.getExecutable().getSimpleName().startsWith(anotherConstructor.getSimpleName()) ||
								constructorcall.getExecutable().getSimpleName().endsWith(anotherConstructor.getSimpleName())) {
							con2anyhasSimilarName = true;
							con2SpecificHasSimilarName = true;
						}
					}
				}

				List<CtConstructorCall> allconstructorcallsinclass = parentClass.getElements(new TypeFilter<>(CtConstructorCall.class));

				if(!con2SpecificHasSimilarName) {

					for (CtConstructorCall certainconstructorcallinclass : allconstructorcallsinclass) {

						CtConstructorCall anotherConstructorCall = (CtConstructorCall) certainconstructorcallinclass;
						// Ignoring if it's the same
						if (anotherConstructorCall == null || anotherConstructorCall.getExecutable().getSignature().
								equals(constructorcall.getExecutable().getSignature()))
							continue;

						if (!anotherConstructorCall.getExecutable().getSimpleName().equals(constructorcall.getExecutable().getSimpleName())) {

							int dist = StringDistance.calculate(anotherConstructorCall.getExecutable().getSimpleName(),
									constructorcall.getExecutable().getSimpleName());
							if ((dist > 0 && dist < 3) || anotherConstructorCall.getExecutable().getSimpleName().startsWith(constructorcall.getExecutable().getSimpleName())
									|| anotherConstructorCall.getExecutable().getSimpleName().endsWith(constructorcall.getExecutable().getSimpleName())||
									constructorcall.getExecutable().getSimpleName().startsWith(anotherConstructorCall.getExecutable().getSimpleName()) ||
									constructorcall.getExecutable().getSimpleName().endsWith(anotherConstructorCall.getExecutable().getSimpleName())) {
								con2anyhasSimilarName = true;
								con2SpecificHasSimilarName = true;
							}
						}
					}
				}

				writeGroupedByVar(context, adjustIdentifyInJson(constructorcall), CodeFeatures.CON2_SIMILAR_CONSTRUCTOR,
						con2SpecificHasSimilarName, "FEATURES_CONSTRUCTOR");

			} // end invocation

			context.put(CodeFeatures.CON2_SIMILAR_CONSTRUCTOR, con2anyhasSimilarName);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeM1_eM2_M3_M4_M8_M9SimilarMethod(CtElement element, Cntx<Object> context, CtClass parentClass,
														List allMethodsFromClass, List<CtInvocation> invocations) {
		try {
			// For each method invocation, whether the method has overloaded method
			boolean m1anyhasSameName = false;
			// For each method invocation, whether there exist methods that return the same
			// type (or type compatible) and are similar in identifier name with the called
			// method (again, we limit the search to the faulty class, search both method
			// definition and method invocations in the faulty class
			boolean m2anyhasMinDist = false;
			// For each method invocation, whether has method definitions or method calls
			// (in the fault class) that take the return type of the method invocation as
			// one
			// of its parameters and the return type of the method is type compatible with
			// the return type of the method invocation.
			boolean m3anyhasCompatibleParameterAndReturnWithOtherMethod = false;
			// For each method invocation, whether the types of some of its parameters are
			// same or compatible with the return type of the method.
			boolean m4anyhasCompatibleParameterAndReturnSameMethod = false;

			boolean m8anyhasmethodprimitive = false;

			boolean m9anyhasmethodobjective = false;

			for (CtInvocation invocation : invocations) {

				boolean m1methodHasSameName = false;
				boolean m2methodhasMinDist = false;
				boolean m3methodhasCompatibleParameterAndReturnWithOtherMethod = false;
				boolean m4methodHasCompatibleParameterAndReturnSameMethod = false;
				boolean m8methodprimitive = false;
				boolean m9methodobjective = false;

				if ((invocation.getType()!=null && invocation.getType().isPrimitive()) ||
						whetherhasprimitive(inferPotentionalTypes(invocation, parentClass))) {
					m8methodprimitive = true;
					m8anyhasmethodprimitive = true;
				}

				if ((invocation.getType()!=null && !invocation.getType().isPrimitive()) ||
						whetherhasobjective(inferPotentionalTypes(invocation, parentClass))) {
					m9methodobjective = true;
					m9anyhasmethodobjective = true;
				}

				for (Object anObjArgument : invocation.getArguments()) {
					CtExpression anArgument = (CtExpression) anObjArgument;

					if (compareTypes(invocation.getType(), anArgument.getType())) {
						m4anyhasCompatibleParameterAndReturnSameMethod = true;
						m4methodHasCompatibleParameterAndReturnSameMethod = true;
					}
				}

				// For each method in the class
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
						m1anyhasSameName = true;
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
									m1anyhasSameName = true;
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
								m2anyhasMinDist = true;
								context.put(CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN, m2anyhasMinDist);
								m2methodhasMinDist = true;
							}

							// Check if the method has a parameter compatible with the affected invocation
							boolean hasSameParam = checkTypeInParameter(anotherMethod, invocation.getExecutable());
							if (hasSameParam) {
								m3anyhasCompatibleParameterAndReturnWithOtherMethod = true;
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
										m2anyhasMinDist = true;
										context.put(CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN, m2anyhasMinDist);
										m2methodhasMinDist = true;
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
									m3anyhasCompatibleParameterAndReturnWithOtherMethod = true;
									m3methodhasCompatibleParameterAndReturnWithOtherMethod = true;
								}
							}

						}
					}
				}

				writeGroupedByVar(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY,
						m4methodHasCompatibleParameterAndReturnSameMethod, "FEATURES_METHODS");

				writeGroupedByVar(context, adjustIdentifyInJson(invocation), CodeFeatures.M1_OVERLOADED_METHOD,
						m1methodHasSameName, "FEATURES_METHODS");

				writeGroupedByVar(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN, m2methodhasMinDist, "FEATURES_METHODS");

				writeGroupedByVar(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP,
						m3methodhasCompatibleParameterAndReturnWithOtherMethod, "FEATURES_METHODS");

				writeGroupedByVar(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M8_RETURN_PRIMITIVE,
						m8methodprimitive, "FEATURES_METHODS");

				writeGroupedByVar(context, adjustIdentifyInJson(invocation),
						CodeFeatures.M9_RETURN_OBJECTIVE,
						m9methodobjective, "FEATURES_METHODS");

			} // end invocation

			context.put(CodeFeatures.M1_OVERLOADED_METHOD, m1anyhasSameName);
			context.put(CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN, m2anyhasMinDist);
			context.put(CodeFeatures.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP,
					m3anyhasCompatibleParameterAndReturnWithOtherMethod);
			context.put(CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY, m4anyhasCompatibleParameterAndReturnSameMethod);
			context.put(CodeFeatures.M8_RETURN_PRIMITIVE, m8anyhasmethodprimitive);
			context.put(CodeFeatures.M9_RETURN_OBJECTIVE, m9anyhasmethodobjective);

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

	private boolean whetherhasobjective(List<CtTypeReference> inferredtypes) {

		for (int index=0; index<inferredtypes.size(); index++) {

			if(!inferredtypes.get(index).isPrimitive()) {
				return true;
			}
		}

		return false;
	}

	private List<CtTypeReference> inferPotentionalTypes (CtInvocation ainvocation,
														 CtClass parentclass) {

		List<CtTypeReference> inferredpotentionaltypes = new ArrayList<CtTypeReference>();

		try {
			// For each invocation found in the class
			List<CtBinaryOperator> binaryOperatorInClass = parentclass.getElements(e ->
					(e instanceof CtBinaryOperator)).stream()
					.map(CtBinaryOperator.class::cast).collect(Collectors.toList());

			inferredpotentionaltypes.clear();

			CtTypeReference inferredtype = null;
			// do simple type inference
			if(ainvocation.getType()==null) {
				for(CtBinaryOperator certainbinary: binaryOperatorInClass) {
					if(certainbinary.getLeftHandOperand() instanceof CtInvocation) {

						CtInvocation anotherinvocation=(CtInvocation)certainbinary.getLeftHandOperand();
						if(anotherinvocation.getExecutable().getSignature().equals(ainvocation.getExecutable().getSignature())
								&& certainbinary.getRightHandOperand().getType()!=null) {

							inferredtype=certainbinary.getRightHandOperand().getType();
							inferredpotentionaltypes.add(inferredtype);
						}
					}

					if(certainbinary.getRightHandOperand() instanceof CtInvocation) {
						CtInvocation anotherinvocation=(CtInvocation)certainbinary.getRightHandOperand();
						if(anotherinvocation.getExecutable().getSignature().equals(ainvocation.getExecutable().getSignature())
								&& certainbinary.getLeftHandOperand().getType()!=null) {
							inferredtype=certainbinary.getLeftHandOperand().getType();
							inferredpotentionaltypes.add(inferredtype);
						}
					}
				}
			}
		} catch (Exception ex) {
		}

		return inferredpotentionaltypes;
	}


	private boolean checkTypeInParameter(CtMethod anotherMethod, CtExecutable minvokedInAffected) {

		for (Object oparameter : anotherMethod.getParameters()) {
			CtParameter parameter = (CtParameter) oparameter;

			if (compareTypes(minvokedInAffected.getType(), parameter.getType())) {
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

	public static List getAllMethodsFromClass(CtClass parentClass) {

		List allMethods = new ArrayList();

		try {
			allMethods.addAll(parentClass.getAllMethods());
			if (parentClass != null && parentClass.getParent() instanceof CtClass) {
				CtClass parentParentClass = (CtClass) parentClass.getParent();
				allMethods.addAll(parentParentClass.getAllMethods());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allMethods;
	}

	private void writeDetailedInformationFromMethod(Cntx<Object> context, CtMethod affectedMethod,
													CodeFeatures property, Boolean value) {

		if (ComingProperties.getPropertyBoolean("write_composed_feature"))
			context.getInformation().put(property.name() + "_" + affectedMethod.getSignature(), value);
		writeGroupedByVar(context, affectedMethod.getSignature(), property, value, "FEATURES_METHODS");
	}

	private void writeDetailedInformationFromLogicalExpression(Cntx<Object> context, CtExpression affectedLogicalExpression,
															   CodeFeatures property, Boolean value, int expressionindex) {

		if (ComingProperties.getPropertyBoolean("write_composed_feature"))
			context.getInformation().put(property.name() + "_" + "logical_expression_"+Integer.toString(expressionindex), value);

		writeGroupedByVar(context, "logical_expression_"+Integer.toString(expressionindex), property, value, "FEATURES_LOGICAL_EXPRESSION");
	}


	private void writeDetailedInformationFromMethod(Cntx<Object> context, CtExecutableReference affectedMethod,
													CodeFeatures property, Boolean value) {

		if (ComingProperties.getPropertyBoolean("write_composed_feature"))
			context.getInformation().put(property.name() + "_" + affectedMethod.getSignature(), value);
		writeGroupedByVar(context, affectedMethod.getSignature(), property, value, "FEATURES_METHODS");
	}


	private void writeDetailedInformationFromVariables(Cntx<Object> context, String key, CodeFeatures property,
													   Boolean value) {

		if (ComingProperties.getPropertyBoolean("write_composed_feature"))
			context.getInformation().put(property.name() + "_" + key, value);
		writeGroupedByVar(context, key, property, value, "FEATURES_VARS");

	}

	private void writeGroupedByVar(Cntx<Object> context, String key, CodeFeatures property, Boolean value,
								   String type) {

		Cntx<Object> featuresVar = (Cntx<Object>) context.getInformation().get(type);
		if (featuresVar == null) {
			featuresVar = new Cntx<>();
			context.getInformation().put(type, featuresVar);
		}
		Cntx<Object> particularVar = (Cntx<Object>) featuresVar.getInformation().get(key);
		if (particularVar == null) {
			particularVar = new Cntx<>();
			featuresVar.getInformation().put(key, particularVar);
		}
		particularVar.getInformation().put(property.name(), value);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void analyzeV8_TypesVarsAffected(List<CtVariableAccess> varsAffected, CtElement element,
											 Cntx<Object> context) {
		// Vars in scope at the position of element
		try {
			int nrPrimitives = 0;
			int nrObjectRef = 0;

			List<CtVariableAccess> objectAccess = new ArrayList<>();

			for (CtVariableAccess aVariableAccess : varsAffected) {

				CtVariable ctVariable = aVariableAccess.getVariable().getDeclaration();
				boolean isPrimitive = false;
				if (ctVariable != null && ctVariable.getReference() != null
						&& ctVariable.getReference().getType() != null) {
					if (ctVariable.getReference().getType().isPrimitive()) {
						nrPrimitives++;
						isPrimitive = true;
					} else {
						nrObjectRef++;
						objectAccess.add(aVariableAccess);
					}

//					writeDetailedInformationFromVariables(context, aVariableAccess.getVariable().getSimpleName(),
//							CodeFeatures.V8_VAR_PRIMITIVE, isPrimitive);

					writeGroupedByVar(context, adjustIdentifyInJson(aVariableAccess),
							CodeFeatures.V8_VAR_PRIMITIVE, isPrimitive, "FEATURES_VARS");
				}
			}
			context.put(CodeFeatures.NUMBER_PRIMITIVE_VARS_IN_STMT, nrPrimitives);
			context.put(CodeFeatures.NUMBER_OBJECT_REFERENCE_VARS_IN_STMT, nrObjectRef);
			context.put(CodeFeatures.NUMBER_TOTAL_VARS_IN_STMT, nrPrimitives + nrObjectRef);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * whether the associated method or class for the faulty line throws exception
	 *
	 * @param element
	 * @param context
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void analyzeS6S11_Method_Method_Features(CtElement element, Cntx<Object> context) {
		try {
			CtMethod parentMethod = element.getParent(CtMethod.class);
			CtClass parentClass= element.getParent(CtClass.class);

			CtStatement parent = element.getParent(new LineFilter());
			CtTry potentionalTryCatch = element.getParent(CtTry.class);

			if(potentionalTryCatch != null && whethereffectivetrycatch (potentionalTryCatch,parent)) {

				context.put(CodeFeatures.S6_METHOD_THROWS_EXCEPTION, false);
				context.put(CodeFeatures.S11_FAULTY_CLASS_EXCEPTION_TYPE, false);

			} else {

				if (parentMethod != null) {
					context.put(CodeFeatures.METHOD_RETURN_TYPE,
							(parentMethod.getType() != null) ? parentMethod.getType().getQualifiedName() : null);

//				  context.put(CodeFeatures.M8_RETURN_PRIMITIVE,
//						(parentMethod.getType() != null) ? parentMethod.getType().isPrimitive() : null);
					// Param
					List<CtParameter> parameters = parentMethod.getParameters();
					List<String> parametersTypes = new ArrayList<>();
					for (CtParameter ctParameter : parameters) {
						parametersTypes.add(ctParameter.getType().getSimpleName());
					}
					context.put(CodeFeatures.METHOD_PARAMETERS, parametersTypes);

					// Modif
					context.put(CodeFeatures.METHOD_MODIFIERS, parentMethod.getModifiers());

					// Comments
					context.put(CodeFeatures.METHOD_COMMENTS, parentMethod.getComments());

					// Exception
					context.put(CodeFeatures.S6_METHOD_THROWS_EXCEPTION, parentMethod.getThrownTypes().size() > 0);
				}

				boolean s11ExceptionType = false;

				if(parentClass != null) {

					Set<CtTypeReference<?>> superInterfaces = parentClass.getSuperInterfaces();
					CtTypeReference<?> superType = parentClass.getSuperclass();

					if(superType != null && superType.getQualifiedName().toLowerCase().indexOf("exception")!=-1)
						s11ExceptionType=true;

					if(superInterfaces.size()>0) {
						for(CtTypeReference specificreference : superInterfaces) {
							if(specificreference!=null && specificreference.getQualifiedName().toLowerCase().indexOf("exception")!=-1) {
								s11ExceptionType=true;
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

	private void retrievePath(CtElement element, Cntx<Object> context) {

		try {
			CtPath path = element.getPath();

			context.put(CodeFeatures.SPOON_PATH, path.toString());
			if (path instanceof CtPathImpl) {
				CtPathImpl pi = (CtPathImpl) path;
				List<CtPathElement> elements = pi.getElements();
				List<String> paths = elements.stream().map(e -> e.toString()).collect(Collectors.toList());
				context.put(CodeFeatures.PATH_ELEMENTS, paths);
			}
		} catch (Throwable e) {
		}
	}

	private void analyzeParentTypes(CtElement element, Cntx<Object> context) {

		CtElement parent = element.getParent();
		List<String> parentNames = new ArrayList<>();
		try {
			do {
				parentNames.add(parent.getClass().getSimpleName());
				parent = parent.getParent();
			} while (parent != null);
		} catch (Exception e) {
		}

		context.put(CodeFeatures.PARENTS_TYPE, parentNames);
	}

	public String adjustIdentifyInJson (CtElement spoonElement) {

		ITree gumtreeObject=(ITree) spoonElement.getMetadata("gtnode");

		return gumtreeObject.getLabel();
	}
}
