package fr.inria.sacha.coming.jdt.treeGenerator;

import java.util.ArrayList;
import java.util.List;

import fr.inria.sacha.coming.entity.EntityType;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.coming.jdt.visitors.AbstractJdtVisitor;
import fr.inria.sacha.coming.jdt.visitors.CDVisitJDTTree;
/**
 * 
 * @author Matias Martinez
 *
 */
public class CDTreeGenerator extends ASTTreeGenerator {

	@Override
	public boolean acceptGranularity(GranuralityType gtype) {
	
		return GranuralityType.CD.equals(gtype);
	}

	@Override
	protected AbstractJdtVisitor getVisitor() {
		return new CDVisitJDTTree();
	}

	@Override
	public List<String> getTreeNodeTypes() {
		List<String> types = new ArrayList<String>();
		for(EntityType et : EntityType.values()){
			types.add(et.name());
		}
		
		return types;
	}

}
