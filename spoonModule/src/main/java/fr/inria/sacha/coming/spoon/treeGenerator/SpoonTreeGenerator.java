package fr.inria.sacha.coming.spoon.treeGenerator;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.declaration.CtType;
import fr.inria.sacha.coming.analyzer.treeGenerator.TreeGenerator;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.labri.gumtree.tree.Tree;
/**
 * 
 * @author Matias Martinez
 *
 */
public class SpoonTreeGenerator implements TreeGenerator {

	@Override
	public Tree generateTree(String content)  throws Exception {
		SpoonDiffCalculator ds = new SpoonDiffCalculator(true);
		CtType ctType = ds.getSpoonType(content);
		return ds.getTree(ctType);
	}

	@Override
	public boolean acceptGranularity(fr.inria.sacha.coming.entity.GranuralityType gtype) {
		return GranuralityType.SPOON.equals(gtype);
	
	}

	@Override
	public List<String> getTreeNodeTypes() {
		List<String> types = new ArrayList<String>();
		

		return null;
	}

}
