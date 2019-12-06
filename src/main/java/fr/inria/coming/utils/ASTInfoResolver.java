package fr.inria.coming.utils;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.ParentNotInitializedException;

import java.util.ArrayList;
import java.util.List;

public class ASTInfoResolver {

	public static CtElement getRootNode(CtElement node) {
		return getPathToRootNode(node).get(0);
	}

	public static List<CtElement> getPathToRootNode(CtElement element) {
		CtElement par = null;
		try {
			par = element.getParent();
			if (par == null || par instanceof CtPackage || element == par) {
				List<CtElement> res = new ArrayList<>();
				res.add(element);
				return res;
			}
			List<CtElement> pathToParent = getPathToRootNode(par);
			pathToParent.add(element);
			return pathToParent;
		} catch (ParentNotInitializedException e) {
			return new ArrayList<>();
		}
	}

	// the returned list also includes the current node.
	public static List<CtElement> getNSubsequentParents(CtElement node, int n) {
		List<CtElement> lst = new ArrayList<CtElement>();
		
		lst.add(node);

		CtElement curPar = node.getParent();
		for (int i = 0; i < n; i++) {
			if (curPar == null)
				break;
			lst.add(curPar);

			curPar = curPar.getParent();
		}

		return lst;
	}

	public static CtElement getFirstAncestorOfType(CtElement node, CtEntityType type) {
		String typeStr = type.toString();

		List<CtElement> pathToRoot = getPathToRootNode(node);
		for (int i = pathToRoot.size() - 1; i >= 0; i--) {
			CtElement parent = pathToRoot.get(i);
			if (EntityTypesInfoResolver.getInstance()
					.isAChildOf(EntityTypesInfoResolver.getNodeLabelFromCtElement(parent), typeStr)) {
				return parent;
			}
		}
		return null;
	}

	public static String getCleanedName(CtElement element) {
		String elementName = element.toString();
		return getCleanedName(elementName);
	}

	public static String getCleanedName(String elementName) {
		while (elementName.startsWith("(") && elementName.endsWith(")")) {
			elementName = elementName.substring(1, elementName.length() - 1);
		}
		if (elementName.startsWith("this.")) {
			elementName = elementName.substring("this.".length());
		}
		return elementName;
	}
}
