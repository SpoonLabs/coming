package fr.inria.sacha.coming.analyzer.treeGenerator;

import java.util.ArrayList;
import java.util.List;

import fr.inria.sacha.coming.entity.GranuralityType;
/**
 * 
 * @author Matias Martinez
 *
 */
public class TreeGeneratorRegistry {
	
	public static List<TreeGenerator> generators = new ArrayList<TreeGenerator>();
	
	public TreeGeneratorRegistry(){
		
	}
	public static void addGenerator(TreeGenerator tg){
		generators.add(tg);
	}
	
	public static TreeGenerator getGenerator(GranuralityType granularity){

		if(generators.isEmpty())
			throw new IllegalArgumentException("Any TreeGenerator was registered. E.g.: TreeGeneratorRegistry.add(myTreeGenerator) ");
		
		for (TreeGenerator treeGen : generators) {
			if(treeGen.acceptGranularity(granularity)){
				return treeGen;
			}
		}
		return null;
	} 
	
}
