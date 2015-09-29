package fr.inria.sacha.coming.jdt.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;

import fr.labri.gumtree.tree.Tree;



public class AbstractJdtVisitor  extends ASTVisitor {

	protected Tree root;

	public Tree getTree() {
		return root;
	}

}
