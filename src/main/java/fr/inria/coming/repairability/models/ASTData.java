package fr.inria.coming.repairability.models;

import fr.inria.coming.utils.ASTInfoResolver;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ASTData {
	private static final String NAME_SEPARATOR = "###";

	private String[] PREDEFINED_METHODS_AND_LITERALS_ARR = { "-1", "0", "1", "size()", "length()", "isEmpty()",
			"length" };
	private List<String> PREDEFINED_METHODS_AND_LITERALS = Arrays.asList(PREDEFINED_METHODS_AND_LITERALS_ARR);

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
				variablesAndLiterals
						.add(ASTInfoResolver.getCleanedName(((CtVariable) element).getReference().toString()));
				if (element instanceof CtField) {
					variablesAndLiterals.add(ASTInfoResolver.getCleanedName(((CtField) element).getSimpleName()));
				}
			}
		}
	}

	public boolean canNPEfixGenerateExpression(CtExpression exp) {
		if (exp == null)
			return true;

		String expStr = exp.toString();
		if (PREDEFINED_METHODS_AND_LITERALS.contains(expStr)
				|| PREDEFINED_METHODS_AND_LITERALS.contains(ASTInfoResolver.getCleanedName(exp))
				|| ASTInfoResolver.getCleanedName(exp).startsWith("null"))
			return true;
		if (exp instanceof CtVariableAccess) {
			return variablesAndLiterals.contains(ASTInfoResolver.getCleanedName(exp));
		}
		return false;
	}

	public boolean canNPEfixGenerateExpression(String exp) {
		if (PREDEFINED_METHODS_AND_LITERALS.contains(exp)
				|| PREDEFINED_METHODS_AND_LITERALS.contains(exp)
				|| exp.startsWith("null"))
			return true;
		return variablesAndLiterals.contains(exp);
	}

	public boolean canNopolGenerateCondition(CtElement condition) {
		List<CtElement> elementsInConditional = condition.getElements(null);
		for (CtElement element : elementsInConditional) {
			String elementAsString = "";
			if (element instanceof CtAbstractInvocation) {
				elementAsString = getExecutableQualifiedSignature(element);
			} else if (element instanceof CtVariableAccess || element instanceof CtLiteral) {
				elementAsString = ASTInfoResolver.getCleanedName(element);
			} else if (element instanceof CtMethod) {
				elementAsString = getExecutableQualifiedSignature(element);
			} else if (element instanceof CtVariable) {
				elementAsString = ASTInfoResolver.getCleanedName(((CtVariable) element).getReference().toString());
			} else {
				continue;
			}

			// nopol might use a field or method of an object that is not used in the src
			String[] parts = elementAsString.split("\\.");
			elementAsString = parts.length == 0 ? elementAsString : parts[parts.length - 1];
			parts = elementAsString.split(NAME_SEPARATOR);
			elementAsString = parts.length == 0 ? elementAsString : parts[parts.length - 1];

			if (element.toString().equals("null") || PREDEFINED_METHODS_AND_LITERALS.contains(elementAsString)) {
				continue;
			}

			boolean isFromVariablesAndLiterals = false;
			for (String str : variablesAndLiterals) {
				if (str.equals(elementAsString)) {
					isFromVariablesAndLiterals = true;
					break;
				}
				parts = str.split("\\.");
				str = parts.length == 0 ? str : parts[parts.length - 1];
				if (str.equals(elementAsString)) {
					isFromVariablesAndLiterals = true;
					break;
				}
			}

			if (isFromVariablesAndLiterals) {
				continue;
			}

			boolean isFromExecutables = false;
			for (String str : executableInvocations) {
				if (str.equals(elementAsString)) {
					isFromExecutables = true;
					break;
				}
				parts = str.split("\\.");
				str = parts.length == 0 ? str : parts[parts.length - 1];
				parts = str.split(NAME_SEPARATOR);
				str = parts.length == 0 ? str : parts[parts.length - 1];
				if (str.equals(elementAsString)) {
					isFromExecutables = true;
					break;
				}
			}

			if (!isFromExecutables) {
				return false;
			}
		}
		return true;
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
			return invocation.getExecutable().getDeclaringType() == null ? "null"
					: invocation.getExecutable().getDeclaringType().toString() + NAME_SEPARATOR
							+ invocation.getExecutable().getSignature();
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

	public boolean canArjaFindVarsAndMethods(CtElement target) {
		List<CtElement> allElements = target.getElements(null);

		for (CtElement element : allElements) {
			if (element instanceof CtAbstractInvocation) {
				if (!executableInvocations.contains(getExecutableQualifiedSignature(element)))
					return false;
			} else if (element instanceof CtVariableAccess) {
				if (!variablesAndLiterals.contains(ASTInfoResolver.getCleanedName(element)))
					return false;
			} else if (element instanceof CtVariable) {
				if (!variablesAndLiterals
						.contains(ASTInfoResolver.getCleanedName(((CtVariable) element).getReference().toString())))
					return false;
			}
		}

		return true;
	}
}
