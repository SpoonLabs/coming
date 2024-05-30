package fr.inria.coming.codefeatures.codeanalyze;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import com.github.gumtreediff.tree.Tree;
import fr.inria.coming.codefeatures.Cntx;
import fr.inria.coming.codefeatures.CodeElementInfo;
import fr.inria.coming.codefeatures.CodeFeatures;
import fr.inria.coming.utils.VariableResolver;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.LineFilter;
import spoon.reflect.visitor.filter.TypeFilter;

import static fr.inria.coming.codefeatures.codeanalyze.BinaryOperatorAnalyzer.getSafeStringRepr;

public abstract class AbstractCodeAnalyzer {

	protected CodeElementInfo elementinfo;

	AbstractCodeAnalyzer(CodeElementInfo inputinfo) {
		this.elementinfo = inputinfo;
	}

	public abstract void analyze();

	public void writeGroupedInfo(Cntx<Object> context, String key, CodeFeatures property, Boolean value, String type) {

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

	public String adjustIdentifyInJson(CtElement spoonElement) {

		if (spoonElement.getAllMetadata().containsKey("gtnode")) {
			Tree gumtreeObject = (Tree) spoonElement.getMetadata("gtnode");

			return gumtreeObject.getLabel();
		} else {
			return spoonElement.getShortRepresentation();
		}
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
	 */
	@SuppressWarnings("rawtypes")
	public boolean analyze_AffectedAssigned(List<CtVariableAccess> varsAffected, CtElement element) {
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
			int nrOfVarWithAssignment = 0;
			int nrOfVarWithoutAssignment = 0;

			int nrOfLocalVarWithAssignment = 0;
			int nrOfLocalVarWithoutAssignment = 0;

			// For each variable affected
			for (CtVariableAccess variableAffected : varsAffected) {

				boolean varHasAssig = false;

				CtStatement parent = variableAffected.getParent(new LineFilter());

				if (isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;
				// For each assignment in the method
				for (CtAssignment assignment : assignments) {

					if (!isElementBeforeVariable(variableAffected, assignment))
						continue;

					if (isStatementInControl(parent, assignment) || parent == assignment)
						continue;

					if (assignment.getAssigned().toString().equals(variableAffected.getVariable().getSimpleName())) {
						varHasAssig = true;
						break;
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
								&& !"null".equals(ctLocalVariable.getDefaultExpression().toString())) {
							varHasAssig = true;
							break;
						}
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

				if (nrOfLocalVarWithoutAssignment > 0)
					break;
			}

			return nrOfLocalVarWithoutAssignment > 0;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Return if the element is a guard
	 * 
	 * @param element
	 * @return
	 */
	public boolean isNormalGuard(CtElement element, CtStatement parentStatement) {

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

				if (whethereffectiveguard(guardCandidateIf, parentStatement)) {
					condition = guardCandidateIf.getCondition();
					boolean isConditionAGuard = checkNormalGuardCondition(condition);
					return isConditionAGuard;
				}
			}
		}
		return false;
	}

	public boolean isNullCheckGuard(CtElement element, CtStatement parentStatement) {

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

				if (whethereffectiveguard(guardCandidateIf, parentStatement)) {
					condition = guardCandidateIf.getCondition();
					boolean isConditionAGuard = checkNullCheckGuardCondition(condition);
					return isConditionAGuard;
				}
			}
		}
		return false;
	}

	public boolean isElementBeforeVariable(CtVariableAccess variableAffected, CtElement element) {

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

	public boolean isStatementInControl(CtStatement targetstatement, CtStatement statementtocompare) {
		CtElement parentelement = targetstatement.getParent();
		int layer = 0;
		CtElement parent;
		parent = statementtocompare;
		do {
			parent = parent.getParent();
			layer++;
		} while (parent != parentelement && parent != null);

		if (layer > 1 && parent != null)
			return true;
		else
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
					if (getSafeStringRepr(ctBinaryOperator.getRightHandOperand()).equals("null")
							|| getSafeStringRepr(ctBinaryOperator.getLeftHandOperand()).equals("null")) {

						return false;
					}
				}
			}

			return true;
		}
		return false;
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

	public static boolean whethereffectiveguard(CtIf ifstatement, CtStatement targetstatement) {
		CtBlock thenBlock = ifstatement.getThenStatement();
		CtBlock elseBlock = ifstatement.getElseStatement();

		if (thenBlock != null) {
			List<CtStatement> thenstatements = thenBlock.getStatements();
			if (thenstatements.size() > 0 && thenstatements.get(0) == targetstatement)
				return true;
		}

		if (elseBlock != null) {
			List<CtStatement> elsestatements = elseBlock.getStatements();
			if (elsestatements.size() > 0 && elsestatements.get(0) == targetstatement)
				return true;
		}

		return false;
	}

	public boolean checkNullCheckGuardCondition(CtExpression condition) {
		if (condition != null) {
			List<CtBinaryOperator> binOp = condition.getElements(new TypeFilter<>(CtBinaryOperator.class));
			if (binOp != null && binOp.size() > 0) {

				for (CtBinaryOperator ctBinaryOperator : binOp) {
					if (!getSafeStringRepr(ctBinaryOperator.getRightHandOperand()).equals("null")
							&& !getSafeStringRepr(ctBinaryOperator.getLeftHandOperand()).equals("null")) {

						return false;
					}
				}

				return true;
			}

			return false;
		}
		return false;
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
	 * @param statements
	 */
	@SuppressWarnings("rawtypes")
	public boolean analyze_AffectedVariablesUsed(List<CtVariableAccess> varsAffected, CtElement element,
			List<CtStatement> statements) {

		try {
			CtExecutable methodParent = element.getParent(CtExecutable.class);

			if (methodParent == null)
				// the element is not in a method.
				return false;
			statements = methodParent.getElements(new LineFilter());

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

				if (isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;

				// For each assignment in the methid
				for (CtStatement aStatement : statements) {

					List<CtVariableAccess> varsInRightPart;

					if (!isElementBeforeVariable(variableAffected, aStatement))
						continue;

					if (isStatementInControl(parent, aStatement) || parent == aStatement)
						continue;

					if (aStatement instanceof CtIf || aStatement instanceof CtLoop) {
						varsInRightPart = VariableResolver.collectVariableRead(retrieveElementToStudy(aStatement));
					} else
						varsInRightPart = VariableResolver.collectVariableRead(aStatement);

					// if the var access in the right is the same that the affected
					for (CtVariableAccess varInStatement : varsInRightPart) {
						if (hasSameName(variableAffected, varInStatement)) {
							aVarUsed = true;
							break;
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

				if (notUsedObjectsLocal > 0)
					break;
			}

			return (notUsedObjectsLocal) > 0;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean hasSameName(CtVariableAccess variableAffected, CtVariableAccess varInStatement) {
		return varInStatement.getVariable().getSimpleName().equals(variableAffected.getVariable().getSimpleName())
				|| varInStatement.equals(variableAffected);
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
		} else if (element instanceof CtForEach) {
			return (((CtForEach) element).getExpression());
		} else if (element instanceof CtSwitch) {
			return (((CtSwitch) element).getSelector());
		} else
			return (element);
	}

	public boolean[] analyze_SametypewithGuard(List<CtVariableAccess> varsAffected, CtElement element,
			CtClass parentClass, List<CtStatement> statements) {

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

				if (isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;

				// For each statement in the method (it includes the statements inside the
				// blocks (then, while)!)
				for (CtStatement aStatement : statements) {
					// for (CtVariableAccess variableAffected : varsAffected) {

					if (parent == aStatement)
						continue;

					List<CtVariableAccess> varsFromStatement;

					if (aStatement instanceof CtIf || aStatement instanceof CtLoop) {
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
							// && !hasSameName(variableAffected, varInStatement)) {
							// Now, let's check if the parent is a guard
							// if (isGuard(getParentNotBlock(aStatement))) {
							if (isNormalGuard(varInStatement, (aStatement))) {

								// it's ok, now let's check the type
								if (variableAffected.getType() != null) {
									// for primitive type variables, we require it to be the same global variable
									if (variableAffected.getType().isPrimitive() && varInStatement.getVariable()
											.getSimpleName().equals(variableAffected.getVariable().getSimpleName()))
										hasPrimitiveSimilarTypeWithNormalGuard = true;
									else
										hasObjectSimilarTypeWithNormalGuard = true;
								}
							}

							if (isNullCheckGuard(varInStatement, (aStatement))) {

								// it's ok, now let's check the type
								if (variableAffected.getType() != null) {

									if (variableAffected.getType().isPrimitive() && varInStatement.getVariable()
											.getSimpleName().equals(variableAffected.getVariable().getSimpleName()))
										hasPrimitiveSimilarTypeWithNullGuard = true;
									else
										hasObjectSimilarTypeWithNullGuard = true;
								}
							}

						}
					}
					// If we find both cases, we can stop
					if (hasPrimitiveSimilarTypeWithNormalGuard && hasObjectSimilarTypeWithNormalGuard
							&& hasPrimitiveSimilarTypeWithNullGuard && hasObjectSimilarTypeWithNullGuard)
						break;
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}

		boolean[] expressionvalue = new boolean[4];
		expressionvalue[0] = hasObjectSimilarTypeWithNormalGuard;
		expressionvalue[1] = hasPrimitiveSimilarTypeWithNormalGuard;
		expressionvalue[2] = hasObjectSimilarTypeWithNullGuard;
		expressionvalue[3] = hasPrimitiveSimilarTypeWithNullGuard;

		return expressionvalue;

	}

	public static boolean compareTypes(CtTypeReference t1, CtTypeReference t2) {
		try {

			return t1 != null && t2 != null
					&& (t1.toString().equals(t2.toString())
							|| t1.toString().toLowerCase().endsWith(t2.toString().toLowerCase())
							|| t2.toString().toLowerCase().endsWith(t1.toString().toLowerCase()) || t1.equals(t2)
							|| t1.isSubtypeOf(t2) || t2.isSubtypeOf(t1));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * // If the faulty statement involves object reference to field (i.e., use
	 * object type class field), do there exist certain field(s) that have never
	 * been referenced in other methods of the faulty class.
	 * 
	 * @param varsAffected
	 * @param element
	 * @param parentClass
	 */
	@SuppressWarnings("rawtypes")
	public boolean analyze_AffectedFielfs(List<CtVariableAccess> varsAffected, CtElement element, CtClass parentClass) {
		try {
			CtMethod methodParent = element.getParent(CtMethod.class);
			if (parentClass == null || methodParent == null)
				return false;

			List<CtStatement> statements = methodParent.getElements(new LineFilter());

			boolean hasFieldNeverUsedOutside = false;
			// For each variable affected in the faulty statement
			for (CtVariableAccess variableAffected : varsAffected) {

				CtStatement parent = variableAffected.getParent(new LineFilter());

				if (isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;

				// if it's a field
				if (variableAffected instanceof CtFieldAccess) {

					if (variableAffected.getVariable().getType() == null
							|| variableAffected.getVariable().getType().isPrimitive())
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
								break;
							}
						}

						if (isFieldUsed)
							break;
					}

					boolean aVarUsedInFaultyMethod = false;

					for (CtStatement aStatement : statements) {

						List<CtVariableAccess> varsInRightPart;

						if (!isElementBeforeVariable(variableAffected, aStatement))
							continue;

						if (isStatementInControl(parent, aStatement) || parent == aStatement)
							continue;

						if (aStatement instanceof CtIf || aStatement instanceof CtLoop) {
							varsInRightPart = VariableResolver.collectVariableRead(retrieveElementToStudy(aStatement));
						} else
							varsInRightPart = VariableResolver.collectVariableRead(aStatement);

						// if the var access in the right is the same that the affected
						for (CtVariableAccess varInStatement : varsInRightPart) {
							if (hasSameName(variableAffected, varInStatement)) {
								aVarUsedInFaultyMethod = true;
								break;
							}
						}
						if (aVarUsedInFaultyMethod)
							break;
					}
					// If the filed is never used on other methods and never used before the faulty
					// statement in the faulty method
					if (!isFieldUsed && !aVarUsedInFaultyMethod)
						hasFieldNeverUsedOutside = true;

					if (hasFieldNeverUsedOutside)
						break;
				}
			}

			return hasFieldNeverUsedOutside;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean analyze_AffectedFieldAssigned(List<CtVariableAccess> varsAffected, CtElement element,
			CtClass parentClass) {

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

				if (isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;

				// if it's a field
				if (variableAffected instanceof CtFieldAccess) {

					if (variableAffected.getVariable().getType() == null
							|| variableAffected.getVariable().getType().isPrimitive())
						continue;

					boolean isFieldAssigned = false;

					for (CtAssignment assignment : assignments) {

						CtMethod methodParentAssign = assignment.getParent(CtMethod.class);

						if (methodParentAssign != null && methodParentAssign.equals(methodParent)) {

							if (!isElementBeforeVariable(variableAffected, assignment))
								continue;

							if (isStatementInControl(parent, assignment) || parent == assignment)
								continue;

							if (assignment.getAssigned().toString()
									.equals(variableAffected.getVariable().getSimpleName())) {
								isFieldAssigned = true;
								break;
							}
						} else {

							if (assignment.getAssigned().toString()
									.equals(variableAffected.getVariable().getSimpleName())) {
								isFieldAssigned = true;
								break;
							}
						}

						if (isFieldAssigned)
							break;
					}

					if (!isFieldAssigned) {

						for (CtField specificField : allfields) {

							if (specificField.getReference().getSimpleName()
									.equals(variableAffected.getVariable().getSimpleName())
									&& specificField.getDefaultExpression() != null
									&& !"null".equals(specificField.getDefaultExpression().toString()))
								isFieldAssigned = true;
							break;
						}
					}

					// If the filed is never used on other methods and never used before the faulty
					// statement in the faulty method
					if (!isFieldAssigned)
						hasFieldNeverAssigned = true;

					if (hasFieldNeverAssigned)
						break;
				}
			}

			return hasFieldNeverAssigned;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return false;
	}

	// S7: If the faulty statement involves object reference (either local or class
	// field),
	// do there exist certain referenced variable(s) for which the last time they
	// appear in the faulty class
	// (before the faulty statement and exclude statements in control structure) are
	// left-hand side of assignment.
	// S8: same, but primitive type variables
	public boolean[] analyze_AffectedObjectLastAppear(List<CtVariableAccess> varsAffected, CtElement element,
			List<CtStatement> statements) {

		try {
			CtExecutable methodParent = element.getParent(CtExecutable.class);

			if (methodParent == null)
				// the element is not in a method.
				return null;

			statements = methodParent.getElements(new LineFilter());

			int objectsLastAssign = 0;
			int objectsLastUse = 0;
			int primitiveLastAssign = 0;
			int primitiveLastUse = 0;

			// For each variable affected
			for (CtVariableAccess variableAffected : varsAffected) {

				boolean aVarAppearLastAssign = false;
				// boolean foundSimilarVarUsedBefore = false;

				CtStatement parent = variableAffected.getParent(new LineFilter());

				if (isNormalGuard(variableAffected, (parent)) || isNullCheckGuard(variableAffected, (parent)))
					continue;

				// For each assignment in the methid
				for (CtStatement aStatement : statements) {

					List<CtVariableAccess> varsInRightPart;

					if (!isElementBeforeVariable(variableAffected, aStatement))
						continue;

					if (isStatementInControl(parent, aStatement) || parent == aStatement)
						continue;

					if (aStatement instanceof CtIf || aStatement instanceof CtLoop) {
						varsInRightPart = VariableResolver.collectVariableRead(retrieveElementToStudy(aStatement));
					} else
						varsInRightPart = VariableResolver.collectVariableRead(aStatement);

					// if the var access in the right is the same that the affected
					for (CtVariableAccess varInStatement : varsInRightPart) {
						if (hasSameName(variableAffected, varInStatement)) {
							aVarAppearLastAssign = false;
						}
					}

					if (aStatement instanceof CtAssignment) {
						CtAssignment assignment = (CtAssignment) aStatement;
						if (assignment.getAssigned().toString()
								.equals(variableAffected.getVariable().getSimpleName())) {
							aVarAppearLastAssign = true;
						}
					}

					if (aStatement instanceof CtLocalVariable) {
						CtLocalVariable ctLocalVariable = (CtLocalVariable) aStatement;

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

				if (objectsLastAssign > 0 && primitiveLastAssign > 0)
					break;
			}

			boolean[] expressionfeatures = new boolean[2];

			expressionfeatures[0] = (objectsLastAssign > 0);
			expressionfeatures[1] = (primitiveLastAssign > 0);

			return expressionfeatures;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		boolean[] expressionfeaturesdefault = new boolean[2];
		expressionfeaturesdefault[0] = false;
		expressionfeaturesdefault[1] = false;

		return expressionfeaturesdefault;
	}

	public boolean[] analyze_SamerMethodWithGuardOrTrywrap(CtElement element, CtClass parentClass,
			List<CtInvocation> allinvocationsFromClass, List<CtInvocation> invocationstostudy,
			List<CtConstructorCall> allconstructorcallsFromClass, List<CtConstructorCall> constructorcallstostudy) {

		try {

			boolean S9anyhasNormalGuard = false;
			boolean S10anyhasNULLGuard = false;

			for (CtInvocation invocation : invocationstostudy) {

				CtStatement parent = invocation.getParent(new LineFilter());

				if (isNormalGuard(invocation, (parent)) || isNullCheckGuard(invocation, (parent)))
					continue;

				// For each method in the class
				for (CtInvocation specificinvocation : allinvocationsFromClass) {

					if (invocation.equals(specificinvocation))
						continue;

					if (invocation.getExecutable().getSimpleName()
							.equals(specificinvocation.getExecutable().getSimpleName())) {

						CtStatement specificparent = specificinvocation.getParent(new LineFilter());

						if (isNormalGuard(specificinvocation, (specificparent)))
							S9anyhasNormalGuard = true;

						if (isNullCheckGuard(specificinvocation, (specificparent)))
							S10anyhasNULLGuard = true;
					}

					if (S9anyhasNormalGuard && S10anyhasNULLGuard)
						break;
				}

				if (S9anyhasNormalGuard && S10anyhasNULLGuard)
					break;
			} // end invocation

			if (!(S9anyhasNormalGuard && S10anyhasNULLGuard)) {
				for (CtConstructorCall constructorcall : constructorcallstostudy) {

					CtStatement parent = constructorcall.getParent(new LineFilter());

					if (isNormalGuard(constructorcall, (parent)) || isNullCheckGuard(constructorcall, (parent)))
						continue;

					// For each method in the class
					for (CtConstructorCall specificconstructorcall : allconstructorcallsFromClass) {

						if (constructorcall.equals(specificconstructorcall))
							continue;

						if (getSimplenameForConstructorCall(constructorcall)
								.equals(getSimplenameForConstructorCall(specificconstructorcall))) {

							CtStatement specificparent = specificconstructorcall.getParent(new LineFilter());

							if (isNormalGuard(specificconstructorcall, (specificparent)))
								S9anyhasNormalGuard = true;

							if (isNullCheckGuard(specificconstructorcall, (specificparent)))
								S10anyhasNULLGuard = true;
						}

						if (S9anyhasNormalGuard && S10anyhasNULLGuard)
							break;
					}

					if (S9anyhasNormalGuard && S10anyhasNULLGuard)
						break;
				}
			}

			boolean S12anyhasTryCatch = false;

			for (CtInvocation invocation : invocationstostudy) {

				CtStatement parent = invocation.getParent(new LineFilter());

				CtTry potentionalTryCatch = invocation.getParent(CtTry.class);

				if (potentionalTryCatch != null && whethereffectivetrycatch(potentionalTryCatch, parent))
					continue;

				// For each method in the class
				for (CtInvocation specificinvocation : allinvocationsFromClass) {

					if (invocation.equals(specificinvocation))
						continue;

					if (invocation.getExecutable().getSimpleName()
							.equals(specificinvocation.getExecutable().getSimpleName())) {

						CtStatement parentspecific = specificinvocation.getParent(new LineFilter());

						CtTry potentionalTryCatchspecific = specificinvocation.getParent(CtTry.class);

						if (potentionalTryCatchspecific != null
								&& whethereffectivetrycatch(potentionalTryCatchspecific, parentspecific))
							S12anyhasTryCatch = true;
					}

					if (S12anyhasTryCatch)
						break;
				}

				if (S12anyhasTryCatch)
					break;
			} // end invocation

			if (!S12anyhasTryCatch) {
				for (CtConstructorCall constructorcall : constructorcallstostudy) {

					CtStatement parent = constructorcall.getParent(new LineFilter());

					CtTry potentionalTryCatch = constructorcall.getParent(CtTry.class);

					if (potentionalTryCatch != null && whethereffectivetrycatch(potentionalTryCatch, parent))
						continue;

					// For each method in the class
					for (CtConstructorCall specificconstructorcall : allconstructorcallsFromClass) {

						if (constructorcall.equals(specificconstructorcall))
							continue;

						if (getSimplenameForConstructorCall(constructorcall)
								.equals(getSimplenameForConstructorCall(specificconstructorcall))) {

							CtStatement parentspecific = specificconstructorcall.getParent(new LineFilter());

							CtTry potentionalTryCatchspecific = specificconstructorcall.getParent(CtTry.class);

							if (potentionalTryCatchspecific != null
									&& whethereffectivetrycatch(potentionalTryCatchspecific, parentspecific))
								S12anyhasTryCatch = true;
						}

						if (S12anyhasTryCatch)
							break;
					}

					if (S12anyhasTryCatch)
						break;
				}
			}

			boolean[] expressionfeatures = new boolean[3];

			expressionfeatures[0] = S9anyhasNormalGuard;
			expressionfeatures[1] = S10anyhasNULLGuard;
			expressionfeatures[2] = S12anyhasTryCatch;

			return expressionfeatures;

		} catch (Throwable e) {
			e.printStackTrace();
		}

		boolean[] expressionfeaturesdefault = new boolean[3];

		expressionfeaturesdefault[0] = false;
		expressionfeaturesdefault[1] = false;
		expressionfeaturesdefault[2] = false;

		return expressionfeaturesdefault;
	}

	public static boolean whethereffectivetrycatch(CtTry trystatement, CtStatement targetstatement) {

		CtBlock tryblock = trystatement.getBody();

		if (tryblock != null) {
			List<CtStatement> trystatements = tryblock.getStatements();
			// if(trystatements.size()>0 && trystatements.get(0)==targetstatement)
			if (trystatements.size() > 0)
				return true;
		}

		return false;
	}

	/**
	 * Check if a method declaration has a parameter compatible and return with the
	 * cttype as argument
	 * 
	 * @param allMethods
	 * @param typeToMatch
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

	public CtInvocation checkInvocationWithParameterReturnCompatibleType(List<CtInvocation> invocationsFromClass,
			CtTypeReference type, CtClass parentclass) {

		List<CtBinaryOperator> binaryOperatorInClass = parentclass.getElements(e -> (e instanceof CtBinaryOperator))
				.stream().map(CtBinaryOperator.class::cast).collect(Collectors.toList());

		for (CtInvocation anInvocation : invocationsFromClass) {

			List<CtTypeReference> inferredpotentionaltypes = new ArrayList<CtTypeReference>();
			inferredpotentionaltypes.clear();

			CtTypeReference inferredtype = null;
			if (anInvocation.getType() == null) {
				for (CtBinaryOperator certainbinary : binaryOperatorInClass) {
					if (certainbinary.getLeftHandOperand() instanceof CtInvocation) {

						CtInvocation anotherinvocation = (CtInvocation) certainbinary.getLeftHandOperand();
						if (anotherinvocation.getExecutable().getSignature()
								.equals(anInvocation.getExecutable().getSignature())
								&& certainbinary.getRightHandOperand().getType() != null) {
							inferredtype = certainbinary.getRightHandOperand().getType();
							inferredpotentionaltypes.add(inferredtype);
							break;
						}
					}

					if (certainbinary.getRightHandOperand() instanceof CtInvocation) {
						CtInvocation anotherinvocation = (CtInvocation) certainbinary.getRightHandOperand();
						if (anotherinvocation.getExecutable().getSignature()
								.equals(anInvocation.getExecutable().getSignature())
								&& certainbinary.getLeftHandOperand().getType() != null) {
							inferredtype = certainbinary.getLeftHandOperand().getType();
							inferredpotentionaltypes.add(inferredtype);
							break;
						}
					}
				}

			} else
				inferredtype = anInvocation.getType();

			if (compareTypes(type, inferredtype) || compareInferredTypes(type, inferredpotentionaltypes)) {

				for (Object anObjArgument : anInvocation.getArguments()) {
					CtExpression anArgument = (CtExpression) anObjArgument;

					if (compareTypes(type, anArgument.getType())) {
						return anInvocation;
					}
				}
			}
		}

		return null;
	}

	public static boolean compareInferredTypes(CtTypeReference t1, List<CtTypeReference> potentionaltypes) {

		for (int i = 0; i < potentionaltypes.size(); i++) {
			if (compareTypes(t1, potentionaltypes.get(i)))
				return true;
		}

		return false;
	}

	public CtMethod checkMethodDeclarationWithParemetrCompatibleType(List allMethods, CtTypeReference typeToMatch) {

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

	public CtInvocation checkInvocationWithParemetrCompatibleType(List<CtInvocation> invocationsFromClass,
			CtTypeReference type) {

		for (CtInvocation anInvocation : invocationsFromClass) {

			for (Object anObjArgument : anInvocation.getArguments()) {
				CtExpression anArgument = (CtExpression) anObjArgument;

				if (compareTypes(type, anArgument.getType())) {
					return anInvocation;
				}
			}
		}

		return null;
	}

	public static String[] getLiteralTypeAndValue(CtLiteral inputLiteral) {

		String[] literaltypeandvalue = new String[2];

		if (inputLiteral.toString().trim().startsWith("'")) {
			literaltypeandvalue[0] = "char";
			literaltypeandvalue[1] = inputLiteral.getValue().toString();
		} else if (inputLiteral.toString().trim().startsWith("\"")) {
			literaltypeandvalue[0] = "string";
			literaltypeandvalue[1] = inputLiteral.getValue().toString();
		} else if (inputLiteral.toString().indexOf("null") != -1) {
			literaltypeandvalue[0] = "null";
			literaltypeandvalue[1] = "null";
		} else {
			if (inputLiteral.getValue().toString().equals("true")
					|| inputLiteral.getValue().toString().equals("false")) {
				literaltypeandvalue[0] = "boolean";
				literaltypeandvalue[1] = inputLiteral.getValue().toString();
			} else {
				literaltypeandvalue[0] = "numerical";
				literaltypeandvalue[1] = inputLiteral.getValue().toString();
			}
		}

		return literaltypeandvalue;
	}

	public List<CtTypeReference> inferPotentionalTypes(CtInvocation ainvocation, CtClass parentclass) {

		List<CtTypeReference> inferredpotentionaltypes = new ArrayList<CtTypeReference>();

		try {
			List<CtBinaryOperator> binaryOperatorInClass = parentclass.getElements(e -> (e instanceof CtBinaryOperator))
					.stream().map(CtBinaryOperator.class::cast).collect(Collectors.toList());

			inferredpotentionaltypes.clear();

			CtTypeReference inferredtype = null;
			if (ainvocation.getType() == null) {
				for (CtBinaryOperator certainbinary : binaryOperatorInClass) {
					if (certainbinary.getLeftHandOperand() instanceof CtInvocation) {

						CtInvocation anotherinvocation = (CtInvocation) certainbinary.getLeftHandOperand();
						if (anotherinvocation.getExecutable().getSignature()
								.equals(ainvocation.getExecutable().getSignature())
								&& certainbinary.getRightHandOperand().getType() != null) {

							inferredtype = certainbinary.getRightHandOperand().getType();
							inferredpotentionaltypes.add(inferredtype);
							break;
						}
					}

					if (certainbinary.getRightHandOperand() instanceof CtInvocation) {
						CtInvocation anotherinvocation = (CtInvocation) certainbinary.getRightHandOperand();
						if (anotherinvocation.getExecutable().getSignature()
								.equals(ainvocation.getExecutable().getSignature())
								&& certainbinary.getLeftHandOperand().getType() != null) {
							inferredtype = certainbinary.getLeftHandOperand().getType();
							inferredpotentionaltypes.add(inferredtype);
							break;
						}
					}
				}
			}
		} catch (Exception ex) {
		}

		return inferredpotentionaltypes;
	}

	public boolean whetherhasobjective(List<CtTypeReference> inferredtypes) {

		for (int index = 0; index < inferredtypes.size(); index++) {

			if (!inferredtypes.get(index).isPrimitive()) {
				return true;
			}
		}
		return false;
	}

	public String getSimplenameForConstructorCall(CtConstructorCall call) {

		String[] namespace = call.getType().getQualifiedName().split("\\(")[0].split("\\.");
		String constructorname = namespace[namespace.length - 1];
		return constructorname;
	}
}
