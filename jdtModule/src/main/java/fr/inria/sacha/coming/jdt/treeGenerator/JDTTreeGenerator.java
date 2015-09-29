package fr.inria.sacha.coming.jdt.treeGenerator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.coming.jdt.visitors.AbstractJdtVisitor;
import fr.inria.sacha.coming.jdt.visitors.JdtVisitor;

/**
 * 
 * @author Matias Martinez
 *
 */
public class JDTTreeGenerator extends ASTTreeGenerator {

	@Override
	protected AbstractJdtVisitor getVisitor() {
		
		return new JdtVisitor();
	}

	@Override
	public boolean acceptGranularity(GranuralityType gtype) {
		
		return GranuralityType.JDT.equals(gtype) || GranuralityType.SPOON.equals(gtype) ;
	}

	@Override
	public List<String> getTreeNodeTypes() {
		List<String> types = new ArrayList<String>();
		for (Field astField : ASTNode.class.getFields()) {
			types.add(astField.getName());
		}
		return types;
	}

}
