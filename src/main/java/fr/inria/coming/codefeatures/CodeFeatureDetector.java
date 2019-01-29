package fr.inria.coming.codefeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.manipulation.sourcecode.VariableResolver;
import fr.inria.astor.core.manipulation.synthesis.dynamoth.spoon.StaSynthBuilder;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.util.StringDistance;
import fr.inria.coming.utils.MapCounter;
import fr.inria.coming.utils.TimeChrono;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtWhile;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.path.CtPath;
import spoon.reflect.path.impl.CtPathElement;
import spoon.reflect.path.impl.CtPathImpl;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.LineFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtVariableReadImpl;

/**
 * 
 * @author Matias Martinez
 *
 */
public class CodeFeatureDetector {

	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void analyzeFeatures(CtElement element, Cntx<Object> context) {
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
			return;
		}
		List<CtStatement> statements = parentClass.getElements(new LineFilter());

		log.debug("------Total vars  of " + ": " + cr.stopAndGetSeconds());

		List allMethods = getAllMethodsFromClass(parentClass);
		List<CtInvocation> invocationsFromClass = parentClass.getElements(e -> (e instanceof CtInvocation)).stream()
				.map(CtInvocation.class::cast).collect(Collectors.toList());
		log.debug("------Total methods of " + ": " + cr.stopAndGetSeconds());

		putVarInContextInformation(context, varsInScope);

		log.debug("------Total context of " + ": " + cr.stopAndGetSeconds());

		List<CtVariableAccess> varsAffected = retrieveVariables(element);
		log.debug("------Total vars of " + ": " + cr.stopAndGetSeconds());

		analyzeV8_TypesVarsAffected(varsAffected, element, context);
		log.debug("------Total v8 of " + ": " + cr.stopAndGetSeconds());
		analyzeS1_AffectedAssigned(varsAffected, element, context);
		log.debug("------Total s1 of " + ": " + cr.stopAndGetSeconds());
		analyzeS1_AffectedVariablesUsed(varsAffected, element, context, statements);
		log.debug("------Total s1b of " + ": " + cr.stopAndGetSeconds());
		analyzeS2_S5_SametypewithGuard(varsAffected, element, context, parentClass, statements);
		log.debug("------Total s2 of " + ": " + cr.stopAndGetSeconds());
		analyzeS3_TypeOfFaulty(element, context);
		log.debug("------Total s3 of " + ": " + cr.stopAndGetSeconds());
		analyzeS4_AffectedFielfs(varsAffected, element, context, parentClass);
		log.debug("------Total s4 of " + ": " + cr.stopAndGetSeconds());
		analyzeS6_M5_Method_Method_Features(element, context);

		log.debug("------Total s6 of " + ": " + cr.stopAndGetSeconds());
		analyzeV1_V6(varsAffected, element, context, allMethods, invocationsFromClass);
		log.debug("------Total v1 of " + ": " + cr.stopAndGetSeconds());
		analyzeV2_AffectedDistanceVarName(varsAffected, varsInScope, element, context);
		log.debug("------Total v2 of " + ": " + cr.stopAndGetSeconds());
		analyzeV3_AffectedHasConstant(varsAffected, element, context);
		log.debug("------Total v3 of " + ": " + cr.stopAndGetSeconds());
		analyzeV4(varsAffected, element, context);
		log.debug("------Total  v4 of " + ": " + cr.stopAndGetSeconds());
		analyzeV5_AffectedVariablesInTransformation(varsAffected, element, context);
		log.debug("------Total v5 of " + ": " + cr.stopAndGetSeconds());

		// Get all invocations inside the faulty element
		List<CtInvocation> invocations = element.getElements(e -> (e instanceof CtInvocation)).stream()
				.map(CtInvocation.class::cast).collect(Collectors.toList());

		analyzeM1_eM2_M3_M4_SimilarMethod(element, context, parentClass, allMethods, invocations);
		analyzeM5(element, context, invocations, varsInScope);

		log.debug("------Total  Mx of " + ": " + cr.stopAndGetSeconds());

		analyzeLE1_AffectedVariablesUsed(varsAffected, element, context, parentClass, statements);
		log.debug("------Total le1 of " + ": " + cr.stopAndGetSeconds());
		analyzeLE2_AffectedVariablesInMethod(varsAffected, element, context, allMethods, invocationsFromClass);
		log.debug("------Total le2 of " + ": " + cr.stopAndGetSeconds());
		analyzeLE3_PrimitiveWithCompatibleNotUsed(varsAffected, varsInScope, element, context);
		log.debug("------Total le3  of " + ": " + cr.stopAndGetSeconds());
		analyzeLE4_BooleanVarNotUsed(varsAffected, varsInScope, element, context);
		log.debug("------Total le4 of " + ": " + cr.stopAndGetSeconds());
		analyzeLE5_BinaryInvolved(element, context);
		log.debug("------Total le5  of " + ": " + cr.stopAndGetSeconds());
		analyzeLE6_UnaryInvolved(element, context);
		log.debug("------Total le6 of " + ": " + cr.stopAndGetSeconds());
		analyzeLE7_VarDirectlyUsed(varsAffected, varsInScope, element, context);
		log.debug("------Total le7 of " + ": " + cr.stopAndGetSeconds());
		analyzeLE8_LocalVariablesVariablesUsed(varsAffected, element, context);
		log.debug("------Total le8 of " + ": " + cr.stopAndGetSeconds());

		analyzeC1_Constant(element, context, parentClass);
		log.debug("------Total c1 of " + ": " + cr.stopAndGetSeconds());
		analyzeC2_UseEnum(element, context, parentClass);
		log.debug("------Total c2 of " + ": " + cr.stopAndGetSeconds());

		analyzeAE1(element, context, allMethods, invocationsFromClass);
		log.debug("------Total ae1 of " + ": " + cr.stopAndGetSeconds());

		// Other features not enumerated
		analyzeAffectedWithCompatibleTypes(varsAffected, varsInScope, element, context);
		log.debug("------Total cp of " + ": " + cr.stopAndGetSeconds());
		analyzeParentTypes(element, context);
		log.debug("------Total py of " + ": " + cr.stopAndGetSeconds());
		analyze_UseEnumAndConstants(element, context);
		log.debug("------Total enum of " + ": " + cr.stopAndGetSeconds());
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
				writeGroupedByVar(context,
						((invocation.getExecutable() != null) ? invocation.getExecutable().getSimpleName()
								: invocation.toString()),
						CodeFeatures.M5_MI_WITH_COMPATIBLE_VAR_TYPE, currentInvocationWithCompVar,
						"FEATURES_METHOD_INVOCATIONS");

			}
		} catch (Exception e) {
		}
		context.put(CodeFeatures.M5_MI_WITH_COMPATIBLE_VAR_TYPE, hasMIcompatibleVar);

	}

	public List<CtVariableAccess> retrieveVariables(CtElement element) {

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

	private void analyzeC1_Constant(CtElement element, Cntx<Object> context, CtClass parentClass) {
		try {
			boolean hasSimilarLiterals = false;
			// Get all invocations inside the faulty element
			List<CtLiteral> literalsFromFaultyLine = element.getElements(e -> (e instanceof CtLiteral)).stream()
					.map(CtLiteral.class::cast).collect(Collectors.toList());

			if (literalsFromFaultyLine.size() > 0) {

				for (CtLiteral literalFormFaulty : literalsFromFaultyLine) {

					List<CtLiteral> literalsFromClass = parentClass.getElements(e -> (e instanceof CtLiteral)).stream()
							.map(CtLiteral.class::cast).collect(Collectors.toList());
					for (CtLiteral anotherLiteral : literalsFromClass) {
						if (// Compare types
						compareTypes(anotherLiteral.getType(), literalFormFaulty.getType())
								// Compare value
								&& !(anotherLiteral.getValue() != null && literalFormFaulty.getValue() != null
										&& anotherLiteral.getValue().equals(literalFormFaulty.getValue()))) {
							hasSimilarLiterals = true;
							break;
						}
					}

				}

			}
			context.put(CodeFeatures.C1_SAME_TYPE_CONSTANT, hasSimilarLiterals);
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

	public Cntx<?> retrieveCntx(ModificationPoint modificationPoint) {
		return retrieveCntx(modificationPoint.getCodeElement());
	}

	public Cntx<?> retrievePatchCntx(CtElement element) {
		Cntx<Object> patchcontext = new Cntx<>(determineKey(element));

		patchcontext.put(CodeFeatures.PATCH_CODE_ELEMENT, element.toString());

		CtElement stmt = element.getParent(CtStatement.class);
		if (stmt == null)
			stmt = element.getParent(CtMethod.class);
		patchcontext.put(CodeFeatures.PATCH_CODE_STATEMENT, (stmt != null) ? element.toString() : null);

		retrieveType(element, patchcontext);
		retrievePath(element, patchcontext);

		return patchcontext;
	}

	@SuppressWarnings("unused")
	public Cntx<?> retrieveBuggy(CtElement element) {

		Cntx<Object> context = new Cntx<>(determineKey(element));

		retrievePath(element, context);
		retrieveType(element, context);

		//
		context.put(CodeFeatures.CODE, element.toString());

		CtElement stmt = element.getParent(CtStatement.class);
		if (stmt == null)
			stmt = element.getParent(CtMethod.class);
		context.put(CodeFeatures.BUGGY_STATEMENT, (stmt != null) ? element.toString() : null);

		//
		Cntx<Object> buggyPositionCntx = new Cntx<>();
		retrievePosition(element, buggyPositionCntx);
		context.put(CodeFeatures.POSITION, buggyPositionCntx);

		return context;
	}

	@SuppressWarnings("unused")
	public Cntx<?> retrieveBuggyInfo(CtElement element) {

		Cntx<Object> context = new Cntx<>(determineKey(element));

		retrievePath(element, context);
		retrieveType(element, context);

		context.put(CodeFeatures.CODE, element.toString());

		Cntx<Object> buggyPositionCntx = new Cntx<>();
		retrievePosition(element, buggyPositionCntx);
		context.put(CodeFeatures.POSITION, buggyPositionCntx);

		return context;
	}

	@SuppressWarnings("unused")
	public Cntx<?> retrieveCntx(CtElement element) {
		Cntx<Object> context = new Cntx<>(determineKey(element));

		analyzeFeatures(element, context);

		return context;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void retrieveDM(CtElement element, Cntx<Object> context, List<CtVariable> varsInScope, CtClass parentClass) {

		List<CtLiteral> literals = VariableResolver.collectLiteralsNoString(parentClass);

		List<CtExpression> ctexpressions = new ArrayList<>();
		List<CtVariableRead> cteVarReadList = new ArrayList<>();
		for (CtVariable ctVariable : varsInScope) {

			CtVariableReadImpl vr = new CtVariableReadImpl<>();
			vr.setVariable(ctVariable.getReference());
			vr.setType(ctVariable.getType());
			ctexpressions.add(vr);
			cteVarReadList.add(vr);
		}

		for (CtLiteral ctLiteral : literals) {
			ctexpressions.add(ctLiteral);
		}

		StaSynthBuilder ib = new StaSynthBuilder();
		try {
			List<CtExpression> result = ib.synthesizer(ctexpressions, cteVarReadList);
			List<String> resultstring = result.stream().map(e -> e.toString()).collect(Collectors.toList());
			context.put(CodeFeatures.PSPACE, resultstring);
		} catch (Exception e) {
			e.printStackTrace();
			context.put(CodeFeatures.PSPACE, null);
		}
	}

	private void analyzeC2_UseEnum(CtElement element, Cntx<Object> context, CtClass parentClass) {
		try {
			boolean useEnum = false;

			if (parentClass == null)
				return;

			List<CtEnum> enums = parentClass.getElements(new TypeFilter<>(CtEnum.class));

			List<CtVariableRead> varAccessFromSusp = element.getElements(new TypeFilter<>(CtVariableRead.class));

			for (CtVariableRead varAccess : varAccessFromSusp) {

				if (varAccess.getVariable().getType() != null
						&& enums.contains(varAccess.getVariable().getType().getDeclaration())) {
					useEnum = true;
				}
			}

			context.put(CodeFeatures.C2_USES_ENUMERATION, useEnum);
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
										varInFaulty.getVariable().getSimpleName(),
										CodeFeatures.V4_FIRST_TIME_USED_AS_PARAMETER, true);
							} else {
								// already used as parameter
								int count = parameterFound.get(varInFaulty);
								writeDetailedInformationFromVariables(context,
										varInFaulty.getVariable().getSimpleName() + "_" + (count + 1),
										CodeFeatures.V4_FIRST_TIME_USED_AS_PARAMETER, false);
							}

							parameterFound.add(varInFaulty);

						}

					}
				}
				if (appearsInParams > 1) {
					hasOneVarAppearsMultiple = true;
				}
				writeDetailedInformationFromVariables(context, varInFaulty.getVariable().getSimpleName(),
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
	private void analyzeLE4_BooleanVarNotUsed(List<CtVariableAccess> varsAffectedInStatement,
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
	private void analyzeLE3_PrimitiveWithCompatibleNotUsed(List<CtVariableAccess> varsAffectedInStatement,
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
	 * is simply a boolean variable (i.e., not function call, equality comparison,
	 * etc.
	 * 
	 * @param varsAffectedInStatement
	 * @param varsInScope
	 * @param element
	 * @param context
	 */
	private void analyzeLE7_VarDirectlyUsed(List<CtVariableAccess> varsAffectedInStatement,
			List<CtVariable> varsInScope, CtElement element, Cntx<Object> context) {
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
			context.put(CodeFeatures.LE7_SIMPLE_VAR_IN_LOGIC, hasVarDirectlyUsed);
		} catch (Throwable e) {
			e.printStackTrace();
		}
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
				writeDetailedInformationFromVariables(context, variableAffected.getVariable().getSimpleName(),
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
	private void analyzeLE1_AffectedVariablesUsed(List<CtVariableAccess> varsAffected, CtElement element,
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

				boolean isInBinaryExpression = isLogicalExpressionInParent(variableAffected);

				if (!isInBinaryExpression)
					continue;

				// let's find other boolean expressions in the statements
				for (CtStatement aStatement : statements) {

					// let's find all binary expressions in the statement
					List<CtBinaryOperator> binaryOps = aStatement.getElements(e -> isLogicalExpression(e)).stream()
							.map(CtBinaryOperator.class::cast).collect(Collectors.toList());

					for (CtBinaryOperator ctBinaryOperator : binaryOps) {

						// retrieve all variables
						List<CtVariableAccess> varsInOtherExpressions = VariableResolver
								.collectVariableRead(ctBinaryOperator);
						for (CtVariableAccess varInAnotherExpression : varsInOtherExpressions) {
							if (!hasSameName(variableAffected, varInAnotherExpression)) {
								// Different name, so it's another variable

								// involve using variable whose type is same with v
								if (compareTypes(variableAffected.getVariable().getType(),
										varInAnotherExpression.getVariable().getType())) {
									foundSimilarVarUsed = true;
								}

							}

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

	/**
	 * // If the logical expression only uses local variables,whether all of the
	 * local variables have been used in other statements (exclude statements inside
	 * control flow structure) since the introduction
	 * 
	 * @param varsAffected
	 * @param element
	 * @param context
	 */
	@SuppressWarnings("rawtypes")
	private void analyzeLE8_LocalVariablesVariablesUsed(List<CtVariableAccess> varsAffected, CtElement element,
			Cntx<Object> context) {
		try {
			CtExecutable methodParent = element.getParent(CtExecutable.class);

			if (methodParent == null)
				return;

			List<CtStatement> statements = methodParent.getBody().getStatements();// methodParent.getElements(new
																					// LineFilter());

			// int similarUsedBefore = 0;
			boolean allLocalVariableUsed = true;
			// For each variable affected
			for (CtVariableAccess variableAffected : varsAffected) {

				boolean aVarUsed = false;

				if (variableAffected.getVariable().getType() != null
						&& !(variableAffected.getVariable().getDeclaration() instanceof CtLocalVariable)) {
					continue;
				}

				boolean isInBinaryExpression = isLogicalExpressionInParent(variableAffected);

				// For any variable involved in a logical expression,
				if (!isInBinaryExpression)
					continue;

				// For each assignment in the methid
				for (CtStatement aStatement : statements) {

					// ignoring control flow
					if (aStatement instanceof CtIf || aStatement instanceof CtLoop)
						continue;

					// ignoring statements after the faulty
					if (!isElementBeforeVariable(variableAffected, aStatement))
						continue;

					// let's collect the var access in the statement
					List<CtVariableAccess> varsReadInStatement = VariableResolver.collectVariableRead(aStatement);
					// if the var access in the right is the same that the affected
					for (CtVariableAccess varInStatement : varsReadInStatement) {
						if (hasSameName(variableAffected, varInStatement)) {
							aVarUsed = true;
						}
					}

				}
				// one variable is not used before the faulty
				if (!aVarUsed) {
					allLocalVariableUsed = false;
					break;
				}
			}

			context.put(CodeFeatures.LE_8_LOGICAL_WITH_USED_LOCAL_VARS, allLocalVariableUsed);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

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
	private void analyzeS1_AffectedVariablesUsed(List<CtVariableAccess> varsAffected, CtElement element,
			Cntx<Object> context, List<CtStatement> statements) {
		try {
			CtExecutable methodParent = element.getParent(CtExecutable.class);

			if (methodParent == null)
				// the element is not in a method.
				return;

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

				// For each assignment in the methid
				for (CtStatement aStatement : statements) {

					// ignoring control flow
					if (aStatement instanceof CtIf || aStatement instanceof CtLoop)
						continue;

					if (!isElementBeforeVariable(variableAffected, aStatement))
						continue;

					// let's collect the var access in the right part
					List<CtVariableAccess> varsInRightPart = VariableResolver.collectVariableRead(aStatement);
					// if the var access in the right is the same that the affected
					for (CtVariableAccess varInStatement : varsInRightPart) {
						if (hasSameName(variableAffected, varInStatement)
								&& !(varInStatement.getVariable().getSimpleName() + " != null")
										.equals(varInStatement.getParent().toString())) {
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
			context.put(CodeFeatures.NR_OBJECT_USED, usedObjects);
			context.put(CodeFeatures.NR_OBJECT_NOT_USED, notUsedObjects);

			context.put(CodeFeatures.NR_OBJECT_USED_LOCAL_VAR, usedObjectsLocal);
			context.put(CodeFeatures.NR_OBJECT_NOT_USED_LOCAL_VAR, notUsedObjectsLocal);

			context.put(CodeFeatures.NR_PRIMITIVE_USED_LOCAL_VAR, usedPrimitiveLocal);
			context.put(CodeFeatures.NR_PRIMITIVE_NOT_USED_LOCAL_VAR, notUsedPrimitiveLocal);

			context.put(CodeFeatures.S1_LOCAL_VAR_NOT_USED, (notUsedObjectsLocal) > 0);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void analyzeS2_S5_SametypewithGuard(List<CtVariableAccess> varsAffected, CtElement element,
			Cntx<Object> context, CtClass parentClass, List<CtStatement> statements) {
		try {
			CtExecutable faultyMethodParent = element.getParent(CtExecutable.class);

			if (parentClass == null)
				// the element is not in a method.
				return;

			boolean hasPrimitiveSimilarTypeWithGuard = false;
			boolean hasObjectSimilarTypeWithGuard = false;

			// For each variable affected
			for (CtVariableAccess variableAffected : varsAffected) {
				// for (CtStatement aStatement : statements) {

				// For each statement in the method (it includes the statements inside the
				// blocks (then, while)!)
				for (CtStatement aStatement : statements) {
					// for (CtVariableAccess variableAffected : varsAffected) {

					CtExecutable anotherStatmentMethodParent = aStatement.getParent(CtExecutable.class);

					if (anotherStatmentMethodParent.equals(faultyMethodParent)
							&& !isElementBeforeVariable(variableAffected, aStatement))
						continue;

					// let's collect the var access in the statement
					List<CtVariableAccess> varsFromStatement = VariableResolver
							.collectVariableReadIgnoringBlocks(aStatement);
					// if the var access is the same that the affected
					for (CtVariableAccess varInStatement : varsFromStatement) {
						// Has similar type but different name
						if (compareTypes(variableAffected.getVariable().getType(),
								varInStatement.getVariable().getType())
								&& !hasSameName(variableAffected, varInStatement)) {
							// Now, let's check if the parent is a guard
							// if (isGuard(getParentNotBlock(aStatement))) {
							if (isGuard(varInStatement, (aStatement))) {

								// it's ok, now let's check the type
								if (variableAffected.getType() != null) {

									if (variableAffected.getType().isPrimitive())
										hasPrimitiveSimilarTypeWithGuard = true;
									else
										hasObjectSimilarTypeWithGuard = true;
								}
							}

						}
					}
					// If we find both cases, we can stop
					if (hasPrimitiveSimilarTypeWithGuard && hasObjectSimilarTypeWithGuard)
						break;
				}
			}

			context.put(CodeFeatures.S2_SIMILAR_OBJECT_TYPE_WITH_GUARD, hasObjectSimilarTypeWithGuard);
			context.put(CodeFeatures.S5_SIMILAR_PRIMITIVE_TYPE_WITH_GUARD, hasPrimitiveSimilarTypeWithGuard);
		} catch (Throwable e) {
			e.printStackTrace();
		}
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
	private boolean isGuard(CtElement element) {

		// First, find the condition
		CtExpression condition = null;
		if (element instanceof CtIf) {

			CtIf guardCandidateIf = (CtIf) element;

			condition = guardCandidateIf.getCondition();

		} else if (element instanceof CtConditional) {
			CtConditional cond = (CtConditional) element;
			condition = cond.getCondition();

		}
		checkGuardCondition(condition);
		return false;
	}

	/**
	 * Return if the element is a guard
	 * 
	 * @param element
	 * @return
	 */
	private boolean isGuard(CtElement element, CtStatement parentStatement) {

		// Two cases: if and conditional
		CtExpression condition = null;
		CtConditional parentConditional = element.getParent(CtConditional.class);

		if (parentConditional != null) {// TODO, maybe force that the var must be in the condition, or not.
			CtConditional cond = (CtConditional) parentConditional;
			condition = cond.getCondition();
			return checkGuardCondition(condition);

		} else {
			CtElement parentElement = getParentNotBlock(parentStatement);
			// First, find the condition

			if (parentElement instanceof CtIf) {

				CtIf guardCandidateIf = (CtIf) parentElement;

				condition = guardCandidateIf.getCondition();

				boolean isConditionAGuard = checkGuardCondition(condition);
				return isConditionAGuard;
			}
		}
		return false;
	}

	/**
	 * Return if the Condition is a guard
	 * 
	 * @param condition
	 * @return
	 */
	public boolean checkGuardCondition(CtExpression condition) {
		if (condition != null) {
			List<CtBinaryOperator> binOp = condition.getElements(new TypeFilter<>(CtBinaryOperator.class));
			if (binOp != null && binOp.size() > 0) {

				for (CtBinaryOperator ctBinaryOperator : binOp) {
					if (ctBinaryOperator.getRightHandOperand().toString().equals("null")
							|| ctBinaryOperator.getLeftHandOperand().toString().equals("null")) {

						return true;
					}
				}
			}
			// If it's a unary, we keep the operand
			if (condition instanceof CtUnaryOperator) {
				condition = ((CtUnaryOperator) condition).getOperand();
			}
			// check if the if is a a boolean invocation
			if (condition instanceof CtInvocation) {

				// CtInvocation invocation = (CtInvocation) condition;
				// the method invocation must return a boolean, so not necessary to
				// check
				// if (invocation.getType() != null &&
				// invocation.getType().unbox().toString().equals("boolean"))
				return true;
			}

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
			CtBinaryOperator binop = (CtBinaryOperator) currentElement;
			if (binop.getKind().equals(BinaryOperatorKind.AND) || binop.getKind().equals(BinaryOperatorKind.OR)
					|| binop.getKind().equals(BinaryOperatorKind.EQ) || binop.getKind().equals(BinaryOperatorKind.NE)
			// || (binop.getType() != null &&
			// binop.getType().unbox().getSimpleName().equals("boolean"))
			)
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
	private void analyzeS1_AffectedAssigned(List<CtVariableAccess> varsAffected, CtElement element,
			Cntx<Object> context) {
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
				// For each assignment in the method
				for (CtAssignment assignment : assignments) {

					if (!isElementBeforeVariable(variableAffected, assignment))
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
			context.put(CodeFeatures.NR_VARIABLE_ASSIGNED, nrOfVarWithAssignment);
			context.put(CodeFeatures.NR_VARIABLE_NOT_ASSIGNED, nrOfVarWithoutAssignment);
			context.put(CodeFeatures.NR_FIELD_INCOMPLETE_INIT, hasIncomplete);
			context.put(CodeFeatures.NR_OBJECT_ASSIGNED_LOCAL, nrOfLocalVarWithAssignment);
			context.put(CodeFeatures.NR_OBJECT_NOT_ASSIGNED_LOCAL, nrOfLocalVarWithoutAssignment);

			// S1 is if NR_OBJECT_ASSIGNED_LOCAL > 0 then
			// if NR_VARIABLE_NOT_ASSIGNED = 0 then S1 = false else S1 = true
			// Else S1= false

			context.put(CodeFeatures.S1_LOCAL_VAR_NOT_ASSIGNED, (nrOfLocalVarWithoutAssignment > 0));
		} catch (Throwable e) {
			e.printStackTrace();
		}
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
	private void analyzeS4_AffectedFielfs(List<CtVariableAccess> varsAffected, CtElement element, Cntx<Object> context,
			CtClass parentClass) {
		try {
			CtMethod methodParent = element.getParent(CtMethod.class);
			if (parentClass == null)
				return;

			boolean hasFieldNeverUsedOutside = false;
			// For each variable affected in the faulty statement
			for (CtVariableAccess variableAffected : varsAffected) {

				// if it's a field
				if (variableAffected instanceof CtFieldAccess) {

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

					}
					// If the filed is never used
					if (!isFieldUsed)
						hasFieldNeverUsedOutside = true;

				}
			}
			context.put(CodeFeatures.S4_USED_FIELD, hasFieldNeverUsedOutside);
		} catch (Throwable e) {
			e.printStackTrace();
		}
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

				for (CtVariable aVarInScope : varsInScope) {
					if (!aVarInScope.getSimpleName().equals(aVarAffected.getVariable().getSimpleName())) {
						int dist = StringDistance.calculate(aVarInScope.getSimpleName(),
								aVarAffected.getVariable().getSimpleName());
						if (dist > 0 && dist < 3) {
							anyhasMinDist = true;

							if (compareTypes(aVarAffected.getType(), aVarInScope.getType())) {
								v2SimilarNameCompatibleType = true;
								v2VarSimilarNameCompatibleType = true;
								// to save computation
								// break;
							}
						}

					}
				}
				writeDetailedInformationFromVariables(context, aVarAffected.getVariable().getSimpleName(),
						CodeFeatures.V2_HAS_VAR_SIM_NAME_COMP_TYPE, (v2VarSimilarNameCompatibleType));

			}
			context.put(CodeFeatures.HAS_VAR_SIM_NAME, anyhasMinDist);
			context.put(CodeFeatures.V2_HAS_VAR_SIM_NAME_COMP_TYPE, v2SimilarNameCompatibleType);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * For each involved variable, is it constant?–can assumevariables whose
	 * identifier names are majorly capital lettersare constant variables
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
				writeDetailedInformationFromVariables(context, aVarAffected.getVariable().getSimpleName(),
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
	private void analyzeV1_V6(List<CtVariableAccess> varsAffected, CtElement element, Cntx<Object> context,
			List allMethods, List<CtInvocation> invocationsFromClass) {
		try {
			// For each involved variable, whether has method definitions or method calls
			// (in the fault class) that take the type of the involved variable as one of
			// its parameters and the return type of the method is type compatible with the
			// type of the involved variable
			boolean v1AnyVarCompatibleReturnAndParameterTypes = false;

			// For each involved variable, whether has methods in scope(method definitions
			// or method calls in the faulty class) thatreturn a type which is the same or
			// compatible with the typeof the involved variable.
			boolean v6AnyVarReturnCompatible = false;

			for (CtVariableAccess varAffected : varsAffected) {

				boolean v6CurrentVarReturnCompatible = false;
				boolean v1CurrentVarCompatibleReturnAndParameterTypes = false;

				if (checkMethodDeclarationWithParameterReturnCompatibleType(allMethods, varAffected.getType()) != null
						|| checkInvocationWithParameterReturnCompatibleType(invocationsFromClass,
								varAffected.getType()) != null) {
					v1AnyVarCompatibleReturnAndParameterTypes = true;
					v1CurrentVarCompatibleReturnAndParameterTypes = true;
				}

				if (checkMethodDeclarationWithReturnCompatibleType(allMethods, varAffected.getType()) != null
						|| checkInvocationWithReturnCompatibleType(invocationsFromClass,
								varAffected.getType()) != null) {
					v6AnyVarReturnCompatible = true;
					v6CurrentVarReturnCompatible = true;
				}

				writeDetailedInformationFromVariables(context, varAffected.getVariable().getSimpleName(),
						CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN,
						(v1CurrentVarCompatibleReturnAndParameterTypes));

				writeDetailedInformationFromVariables(context, varAffected.getVariable().getSimpleName(),
						CodeFeatures.V6_IS_METHOD_RETURN_TYPE_VAR, v6CurrentVarReturnCompatible);

			}

			context.put(CodeFeatures.V1_IS_TYPE_COMPATIBLE_METHOD_CALL_PARAM_RETURN,
					(v1AnyVarCompatibleReturnAndParameterTypes));

			context.put(CodeFeatures.V6_IS_METHOD_RETURN_TYPE_VAR, v6AnyVarReturnCompatible);

		} catch (Throwable e) {
			e.printStackTrace();
		}
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
	private void analyzeLE2_AffectedVariablesInMethod(List<CtVariableAccess> varsAffected, CtElement element,
			Cntx<Object> context, List allMethods, List<CtInvocation> invocationsFromClass) {
		try {

			boolean hasAnyLES2paramCompatibleWithBooleanReturn = false;

			for (CtVariableAccess varAffected : varsAffected) {

				if (!isParentBooleanExpression(varAffected))
					continue;

				boolean isCurrentVarLE2paramCompatibleWithBooleanReturn = false;

				if (// First, Let's analyze the method declaration
				checkBooleanMethodDeclarationWithTypeInParameter(allMethods, varAffected) != null
						// Second, let's inspect invocations
						|| checkBooleanInvocationWithParameterReturn(invocationsFromClass, varAffected) != null) {
					hasAnyLES2paramCompatibleWithBooleanReturn = true;
					isCurrentVarLE2paramCompatibleWithBooleanReturn = true;
				}

				writeDetailedInformationFromVariables(context, varAffected.getVariable().getSimpleName(),
						CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR,
						(isCurrentVarLE2paramCompatibleWithBooleanReturn));

			}

			context.put(CodeFeatures.LE2_IS_BOOLEAN_METHOD_PARAM_TYPE_VAR,
					(hasAnyLES2paramCompatibleWithBooleanReturn));

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
			List<CtInvocation> invocationsFromClass) {
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
							anAritmeticOperator.getType()) != null) {
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
			CtVariableAccess varAffected) {

		try {
			// For each invocation found in the class
			for (CtInvocation anInvocation : invocationsFromClass) {

				// Check types
				if (anInvocation.getType() != null && (anInvocation.getType().getSimpleName().equals("Boolean")
						|| anInvocation.getType().unbox().toString().equals("boolean"))) {

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

	public CtInvocation checkInvocationWithParameterReturnCompatibleType(List<CtInvocation> invocationsFromClass,
			CtTypeReference type) {
		// For each invocation found in the class
		for (CtInvocation anInvocation : invocationsFromClass) {
			// Compatible types
			if (compareTypes(type, anInvocation.getType())) {

				// For each argument in the invocation
				for (Object anObjArgument : anInvocation.getArguments()) {
					CtExpression anArgument = (CtExpression) anObjArgument;

					// retrieve Var access

					List<CtVariableAccess> varReadFromArguments = VariableResolver.collectVariableRead(anArgument);

					for (CtVariableAccess aVarReadFrmArgument : varReadFromArguments) {

						//
						if (compareTypes(type, aVarReadFrmArgument.getType())
								&& compareTypes(type, anInvocation.getType())) {
							return anInvocation;
						}
					}
				}
			}
		}
		return null;
	}

	public CtInvocation checkInvocationWithReturnCompatibleType(List<CtInvocation> invocationsFromClass,
			CtTypeReference type) {
		// For each invocation found in the class
		for (CtInvocation anInvocation : invocationsFromClass) {

			if (compareTypes(type, anInvocation.getType())) {
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
			return t1 != null && t2 != null && (t1.toString().equals(t2.toString()) || t1.equals(t2)
					|| t1.isSubtypeOf(t2) || t2.isSubtypeOf(t1));
		} catch (Exception e) {
			log.debug("Error comparing types");
			log.debug(e);
			return false;
		}

	}

	/**
	 * For the logical expression, whether there exists a boolean expression that
	 * starts with the "not" operator! (an exclamation mark)
	 * 
	 * @param element
	 * @param context
	 * @param parentContext
	 */
	private void analyzeLE6_UnaryInvolved(CtElement element, Cntx<Object> parentContext) {
		try {
			Cntx<Object> context = new Cntx<>();
			parentContext.put(CodeFeatures.UNARY_PROPERTIES, context);

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

			parentContext.put(CodeFeatures.LE6_HAS_NEGATION, containsNot);
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
	private void analyzeLE5_BinaryInvolved(CtElement element, Cntx<Object> parentContext) {
		try {
			Cntx<Object> context = new Cntx<>();
			parentContext.put(CodeFeatures.BIN_PROPERTIES, context);

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

			parentContext.put(CodeFeatures.LE5_BOOLEAN_EXPRESSIONS_IN_FAULTY,
					(containsAnd || containsBitand || containsBitor || containsBitxor || containsOr));

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

	/**
	 * Similar methods
	 * 
	 * @param element
	 * @param context
	 */
	private void analyzeM1_eM2_M3_M4_SimilarMethod(CtElement element, Cntx<Object> context, CtClass parentClass,
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

			for (CtInvocation invocation : invocations) {
				CtExecutable minvokedInAffected = invocation.getExecutable().getDeclaration();

				if (minvokedInAffected == null || !(minvokedInAffected instanceof CtMethod))
					continue;

				boolean m1methodHasSameName = false;
				boolean m2methodhasMinDist = false;
				boolean m3methodhasCompatibleParameterAndReturnWithOtherMethod = false;
				boolean m4methodHasCompatibleParameterAndReturnSameMethod = false;

				// Get the method that is invoked
				CtMethod affectedMethod = (CtMethod) minvokedInAffected;

				// Check parameters
				for (Object oparameter : affectedMethod.getParameters()) {
					CtParameter parameter = (CtParameter) oparameter;

					if (affectedMethod != null && compareTypes(affectedMethod.getType(), parameter.getType())) {
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
					if (anotherMethod == null || anotherMethod.getSignature().equals(affectedMethod.getSignature()))
						continue;

					if (anotherMethod.getSimpleName().equals(affectedMethod.getSimpleName())) {
						// It's override
						m1methodHasSameName = true;
						m1anyhasSameName = true;
					}
					// If the return types are compatibles
					if (anotherMethod.getType() != null && minvokedInAffected.getType() != null) {

						// Check if the method has the return type compatible with the affected
						// invocation
						boolean compatibleReturnTypes = compareTypes(anotherMethod.getType(),
								minvokedInAffected.getType());
						if (compatibleReturnTypes) {
							// Check name similarity:
							int dist = StringDistance.calculate(anotherMethod.getSimpleName(),
									minvokedInAffected.getSimpleName());
							if (dist > 0 && dist < 3) {
								m2anyhasMinDist = true;
								context.put(CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN, m2anyhasMinDist);
								m2methodhasMinDist = true;
							}

							// Check if the method has a parameter compatible with the affected invocation
							boolean hasSameParam = checkTypeInParameter(anotherMethod, minvokedInAffected);
							if (hasSameParam) {
								m3anyhasCompatibleParameterAndReturnWithOtherMethod = true;
								m3methodhasCompatibleParameterAndReturnWithOtherMethod = true;
							}
						}
					}
					// if the other method is not similar method for M3, let's find in the
					// invocation inside the .
					if (!m3methodhasCompatibleParameterAndReturnWithOtherMethod) {

						List<CtInvocation> invocationsFromAnotherMethod = anotherMethod
								.getElements(e -> (e instanceof CtInvocation)).stream().map(CtInvocation.class::cast)
								.collect(Collectors.toList());
						for (CtInvocation ctInvocation : invocationsFromAnotherMethod) {
							CtExecutable methodInvokedInAnotherMethod = ctInvocation.getExecutable().getDeclaration();

							if (methodInvokedInAnotherMethod != null) {

								if (compareTypes(anotherMethod.getType(), minvokedInAffected.getType())
										&& checkTypeInParameter(anotherMethod, minvokedInAffected)) {
									m3anyhasCompatibleParameterAndReturnWithOtherMethod = true;
									m3methodhasCompatibleParameterAndReturnWithOtherMethod = true;
								}

							}

						}

					}

				}
				writeDetailedInformationFromMethod(context, affectedMethod,
						CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY,
						m4methodHasCompatibleParameterAndReturnSameMethod);

				writeDetailedInformationFromMethod(context, affectedMethod, CodeFeatures.M1_OVERLOADED_METHOD,
						m1methodHasSameName);

				writeDetailedInformationFromMethod(context, affectedMethod,
						CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN, m2methodhasMinDist);

				writeDetailedInformationFromMethod(context, affectedMethod,
						CodeFeatures.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP,
						m3methodhasCompatibleParameterAndReturnWithOtherMethod);

			} // end invocation
			context.put(CodeFeatures.M1_OVERLOADED_METHOD, m1anyhasSameName);
			context.put(CodeFeatures.M2_SIMILAR_METHOD_WITH_SAME_RETURN, m2anyhasMinDist);
			context.put(CodeFeatures.M3_ANOTHER_METHOD_WITH_PARAMETER_RETURN_COMP,
					m3anyhasCompatibleParameterAndReturnWithOtherMethod);
			context.put(CodeFeatures.M4_PARAMETER_RETURN_COMPABILITY, m4anyhasCompatibleParameterAndReturnSameMethod);
		} catch (Throwable e) {
			e.printStackTrace();
		}
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

		if (ConfigurationProperties.getPropertyBool("write_composed_feature"))
			context.getInformation().put(property.name() + "_" + affectedMethod.getSignature(), value);
		writeGroupedByVar(context, affectedMethod.getSignature(), property, value, "FEATURES_METHODS");

	}

	private void writeDetailedInformationFromVariables(Cntx<Object> context, String key, CodeFeatures property,
			Boolean value) {

		if (ConfigurationProperties.getPropertyBool("write_composed_feature"))
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

					writeDetailedInformationFromVariables(context, aVariableAccess.getVariable().getSimpleName(),
							CodeFeatures.V8_VAR_PRIMITIVE, isPrimitive);
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
	private void analyzeS6_M5_Method_Method_Features(CtElement element, Cntx<Object> context) {
		try {
			CtMethod parentMethod = element.getParent(CtMethod.class);
			if (parentMethod != null) {
				// Return
				context.put(CodeFeatures.METHOD_RETURN_TYPE,
						(parentMethod.getType() != null) ? parentMethod.getType().getQualifiedName() : null);

				context.put(CodeFeatures.M6_RETURN_PRIMITIVE,
						(parentMethod.getType() != null) ? parentMethod.getType().isPrimitive() : null);
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

}
