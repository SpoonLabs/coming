package fr.inria.sacha.coming.jdt.treeGenerator;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import fr.inria.sacha.coming.analyzer.treeGenerator.TreeGenerator;
import fr.inria.sacha.coming.jdt.visitors.AbstractJdtVisitor;
import fr.labri.gumtree.tree.Tree;
/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class ASTTreeGenerator implements TreeGenerator {


	/**
	 * Code from
	 * http://www.programcreek.com/java-api-examples/index.php?api=org.
	 * eclipse.jdt.core.dom.ASTParser
	 * 
	 * @param content
	 * @param granularity
	 * @return
	 */
	
	@Override
	public Tree generateTree(String content) {
	
			ASTParser parser = ASTParser.newParser(AST.JLS3);

			parser.setSource(content.toCharArray());
			parser.setResolveBindings(true);
			ASTNode node = parser.createAST(new NullProgressMonitor());
			AbstractJdtVisitor visitor = getVisitor();
			
			node.accept(visitor);

			return visitor.getTree();
		

	}
	protected  abstract AbstractJdtVisitor getVisitor();
	


}
