package fr.inria.coming.repairability.models;

import fr.inria.coming.utils.ASTInfoResolver;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ASTData {
	private static final String NAME_SEPARATOR = "###";

	private Set<String> executableInvocations;
	private Set<String> variablesAndLiterals;

	public ASTData(CtElement rootNode) {
		executableInvocations = new HashSet<>();
		variablesAndLiterals = new HashSet<>();

		List<CtElement> allElements = rootNode.getElements(null);
		for (CtElement element : allElements) {
			if (element instanceof CtAbstractInvocation) {
				executableInvocations.add(getExecutableQualifiedSignature(element));
			} else if (element instanceof CtVariableAccess || element instanceof CtLiteral) {
				variablesAndLiterals.add(ASTInfoResolver.getCleanedName(element));
			} else if (element instanceof CtMethod) {
				executableInvocations.add(getExecutableQualifiedSignature(element));
			} else if (element instanceof CtVariable) {
				variablesAndLiterals.add(((CtVariable) element).getReference().toString());
			}
		}
	}

	public boolean canElixirGenerateNode(CtElement mappedElement, CtElement newNode) {
		Set<String> validInvocationsAsArguments = new HashSet<>();
		if (mappedElement != null && mappedElement instanceof CtAbstractInvocation) {
			List<CtExpression> arguments = ((CtAbstractInvocation) mappedElement).getArguments();
			for (CtExpression argument : arguments) {
				if (argument instanceof CtAbstractInvocation) {
					validInvocationsAsArguments.add(argument.toString());
				}
			}
		}
		if (newNode instanceof CtAbstractInvocation) {
			if (!executableInvocations.contains(getExecutableQualifiedSignature(newNode)))
				return false;
			List<CtExpression> arguments = ((CtAbstractInvocation) newNode).getArguments();
			for (CtExpression argument : arguments) {
				if (argument.toString().equals("null"))
					continue;
				if (validInvocationsAsArguments.contains(argument.toString()))
					continue;
				if (!(argument instanceof CtVariableAccess || argument instanceof CtLiteral)
						|| !variablesAndLiterals.contains(ASTInfoResolver.getCleanedName(argument))) {
					return false;
				}
			}
			return true;
		} else if (newNode instanceof CtVariableAccess || newNode instanceof CtLiteral) {
			return variablesAndLiterals.contains(ASTInfoResolver.getCleanedName(newNode));
		}
		return false;
	}

	private String getExecutableQualifiedSignature(CtElement element) {
		if (element instanceof CtAbstractInvocation) {
			CtAbstractInvocation invocation = (CtAbstractInvocation) element;
			return invocation.getExecutable().getDeclaringType() == null ? "null" : invocation.getExecutable().getDeclaringType().toString()
					+ NAME_SEPARATOR + invocation.getExecutable().getSignature();
		} else if (element instanceof CtMethod) {
			CtMethod method = (CtMethod) element;
			return method.getDeclaringType().getQualifiedName().toString() + NAME_SEPARATOR + method.getSignature();
		}

		return null;
	}

	public Set<String> getExecutableInvocations() {
		return executableInvocations;
	}

	public void setExecutableInvocations(Set<String> executableInvocations) {
		this.executableInvocations = executableInvocations;
	}

	public Set<String> getVariablesAndLiterals() {
		return variablesAndLiterals;
	}

	public void setVariablesAndLiterals(Set<String> variablesAndLiterals) {
		this.variablesAndLiterals = variablesAndLiterals;
	}
}
