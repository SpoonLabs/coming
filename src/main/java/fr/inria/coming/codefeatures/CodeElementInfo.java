package fr.inria.coming.codefeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.inria.coming.utils.VariableResolver;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.LineFilter;

public class CodeElementInfo {

	public CtElement element;

	public Cntx<Object> context;

	public List<CtVariable> varsInScope;

	public CtClass parentClass;

	public List<CtStatement> statements;

	public List allMethods;

	public List<CtInvocation> invocationsFromClass;

	public List<CtConstructorCall> constructorcallsFromClass;

	public CtElement elementToStudy;

	public List<CtVariableAccess> varsAffected;

	public List<CtInvocation> invocations; // invocations from the element under study

	public List<CtConstructorCall> constructorcalls; // constructors from the element under study

	public List<CtLiteral> literalsFromFaultyLine;

	public List<CtTypeAccess> typeaccess;

	public List<CtExpression> logicalExpressions = new ArrayList();

	public List<CtExpression> desirableExpressions = new ArrayList();

	public List<CtBinaryOperator> binoperators = new ArrayList();

	public CodeElementInfo(CtElement elementoriginal, List<CtExpression> allExpressions,
			List<CtExpression> allrootlogicalexpers, List<CtBinaryOperator> allBinOperators) {

		this.element = elementoriginal;
		this.desirableExpressions = allExpressions;
		this.logicalExpressions = allrootlogicalexpers;
		this.binoperators = allBinOperators;

		setContext();
		setVarsInScope();
		setParentClass();
		setStatementList();
		setMethodList();
		setInvocationsFromClass();
		setConstructorcallsFromClass();
		setElementToStudy();
		setVarsAffected();
		setInvocations();
		setConstructorcalls();
		setLiteralsFromFaultyLine();
		setTypeaccess();

	}

	private void setContext() {
		context = new Cntx<>(determineKey(element));
		context.getInformation().put("FEATURES_VARS", new Cntx<>());
		context.getInformation().put("FEATURES_TYPEACCESS", new Cntx<>());
		context.getInformation().put("FEATURES_METHOD_INVOCATION", new Cntx<>());

	}

	private Cntx<Object> getContext() {
		return this.context;
	}

	private Object determineKey(CtElement element) {
		String key = null;
		if (element.getPosition() != null && element.getPosition().getFile() != null) {
			key = element.getPosition().getFile().getName().toString();
		} else {
			key = element.getShortRepresentation();
		}
		return key;
	}

	private void setVarsInScope() {
		varsInScope = VariableResolver.searchVariablesInScope(element);
	}

	private List<CtVariable> getVarsInScope() {
		return this.varsInScope;
	}

	private void setParentClass() {

		if (element instanceof CtClass)
			parentClass = (CtClass) element;
		else
			parentClass = element.getParent(CtClass.class);
	}

	private CtClass getParentClass() {

		return this.parentClass;
	}

	private void setStatementList() {

		if (parentClass != null)
			statements = parentClass.getElements(new LineFilter());
		else
			statements = null;
	}

	private List<CtStatement> getStatementList() {

		return this.statements;
	}

	private void setMethodList() {

		if (parentClass != null)
			allMethods = getAllMethodsFromClass(parentClass);
		else
			allMethods = null;
	}

	private List getAllMethodsFromClass(CtClass parentClass) {

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

	private List getMethodList() {

		return this.allMethods;
	}

	private void setInvocationsFromClass() {

		if (parentClass != null)
			invocationsFromClass = parentClass.getElements(e -> (e instanceof CtInvocation)).stream()
					.map(CtInvocation.class::cast).collect(Collectors.toList());
		else
			invocationsFromClass = null;
	}

	private List<CtInvocation> getInvocationsFromClass() {

		return this.invocationsFromClass;
	}

	private void setConstructorcallsFromClass() {

		if (parentClass != null)
			constructorcallsFromClass = parentClass.getElements(e -> (e instanceof CtConstructorCall)).stream()
					.map(CtConstructorCall.class::cast).collect(Collectors.toList());
		else
			constructorcallsFromClass = null;
	}

	private List<CtConstructorCall> getConstructorcallsFromClass() {

		return this.constructorcallsFromClass;
	}

	private void setElementToStudy() {

		elementToStudy = retrieveElementToStudy(element);
	}

	private CtElement retrieveElementToStudy(CtElement element) {

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

	private CtElement getElementToStudy() {

		return this.elementToStudy;
	}

	private void setVarsAffected() {

		varsAffected = VariableResolver.collectVariableAccess(elementToStudy, false);
	}

	private List<CtVariableAccess> getVarsAffected() {

		return this.varsAffected;
	}

	private void setInvocations() {
		if (elementToStudy != null)
			invocations = elementToStudy.getElements(e -> (e instanceof CtInvocation)).stream()
					.map(CtInvocation.class::cast).collect(Collectors.toList());
		else
			invocations = null;
	}

	private List<CtInvocation> getInvocations() {

		return this.invocations;
	}

	private void setConstructorcalls() {
		if (elementToStudy != null)
			constructorcalls = elementToStudy.getElements(e -> (e instanceof CtConstructorCall)).stream()
					.map(CtConstructorCall.class::cast).collect(Collectors.toList());
		else
			constructorcalls = null;
	}

	private List<CtConstructorCall> getConstructorcalls() {

		return this.constructorcalls;
	}

	private void setLiteralsFromFaultyLine() {

		if (elementToStudy != null)
			literalsFromFaultyLine = elementToStudy.getElements(e -> (e instanceof CtLiteral)).stream()
					.map(CtLiteral.class::cast).collect(Collectors.toList());
		else
			literalsFromFaultyLine = null;
	}

	private List<CtLiteral> getLiteralsFromFaultyLine() {

		return this.literalsFromFaultyLine;
	}

	private void setTypeaccess() {

		if (elementToStudy != null)
			typeaccess = elementToStudy.getElements(e -> (e instanceof CtTypeAccess)).stream()
					.map(CtTypeAccess.class::cast).collect(Collectors.toList());
		else
			typeaccess = null;
	}

	private List<CtTypeAccess> getTypeaccess() {

		return this.typeaccess;
	}

	private List<CtExpression> getLogicalExpressions() {

		return this.logicalExpressions;
	}

	private List<CtExpression> getDesirableExpressions() {

		return this.desirableExpressions;
	}

	private List<CtBinaryOperator> getAllBinaryOperators() {

		return this.binoperators;
	}
}
