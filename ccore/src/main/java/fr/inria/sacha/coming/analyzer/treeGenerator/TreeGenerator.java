package fr.inria.sacha.coming.analyzer.treeGenerator;


import java.util.List;

import fr.inria.sacha.coming.entity.GranuralityType;
import fr.labri.gumtree.tree.Tree;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface TreeGenerator {

	
	public Tree generateTree(String content) throws Exception;
	public boolean acceptGranularity(GranuralityType gtype);
	public List<String> getTreeNodeTypes();
}
