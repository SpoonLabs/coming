package fr.inria.sacha.coming.analyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.inria.sacha.coming.analyzer.treeGenerator.TreeGenerator;
import fr.inria.sacha.coming.analyzer.treeGenerator.TreeGeneratorRegistry;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.labri.gumtree.actions.ActionGenerator;
import fr.labri.gumtree.actions.model.Action;
import fr.labri.gumtree.matchers.CompositeMatchers;
import fr.labri.gumtree.matchers.Mapping;
import fr.labri.gumtree.matchers.MappingStore;
import fr.labri.gumtree.matchers.Matcher;
import fr.labri.gumtree.matchers.MatcherFactory;
import fr.labri.gumtree.tree.Tree;
import fr.labri.gumtree.tree.TreeUtils;

/**
 * Facade for GumTree functionality.
 * Fine granularity comparison between two files according to a given
 * granularity (JDT, CD, Spoon). 
 * It uses GT Matching algorithm.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */

public class DiffEngineFacade {

	private Logger log = Logger.getLogger(DiffEngineFacade.class.getName());

	private  boolean defaultOnlyRoots = true;

	private Tree tl = null;
	private Tree tr = null;
	
	private ActionClassifier classifier = new ActionClassifier();
	
	//TreeGeneratorRegistry registry = new TreeGeneratorRegistry();
	
	
	/**
	 * Code from
	 * http://www.programcreek.com/java-api-examples/index.php?api=org.
	 * eclipse.jdt.core.dom.ASTParser
	 * 
	 * @param content
	 * @param granularity
	 * @return
	 * @throws Exception 
	 */
	public Tree createTree(String content, GranuralityType granularity) throws Exception {
		
		TreeGenerator treeGen = TreeGeneratorRegistry.getGenerator(granularity);
		if (treeGen == null)
			return null;
		
		return treeGen.generateTree(content);
	
	}
	public Tree createTree(String content, TreeGenerator treeGen) throws Exception {
		
		if (treeGen == null)
			return null;
		
		return treeGen.generateTree(content);
	
	}
	
	public DiffResult getActions(Tree tl, Tree tr){
		
		//DiffSpoon diffEngine = new DiffSpoon();
		DiffResult result = /*diffEngine.*/compare(tl, tr);
		return result;
		
	}
	public void prepare(Tree node){
		node.refresh();
		TreeUtils.postOrderNumbering(node);
		TreeUtils.computeHeight(node);
		TreeUtils.computeDigest(node);
	}
	
	public DiffResult compare(Tree rootSpoonLeft, Tree rootSpoonRight) {
		
		List<Action> actions = null;

		
		Set<Mapping> mappings = null;
		MappingStore mappingsComp = null;

		
		prepare(rootSpoonLeft);
		prepare(rootSpoonRight);
		
		//---
		/*logger.debug("-----Trees:----");
		logger.debug("left tree:  " + rootSpoonLeft.toTreeString());
		logger.debug("right tree: " + rootSpoonRight.toTreeString());
*/
		// --
		//Matcher matcher = new GumTreeMatcher(rootSpoonLeft, rootSpoonRight);
		MatcherFactory f = new CompositeMatchers.GumTreeMatcherFactory();
		Matcher matcher = f.newMatcher(rootSpoonLeft, rootSpoonRight);
		
		//new 
		matcher.match();
		//
		mappings = matcher.getMappingSet();
		mappingsComp = new MappingStore(mappings);

		ActionGenerator gt = new ActionGenerator(rootSpoonLeft, rootSpoonRight,	matcher.getMappings());
		gt.generate();
		actions = DiffResult.getAllFilterDuplicate(gt.getActions());//gt.getActions();

		ActionClassifier gtfac = new ActionClassifier();
		
		List<Action> rootActions = gtfac.getRootActions(mappings, actions);
		
		return new DiffResult(actions, rootActions);
	}

	
	
	public List<Action> getActions(Tree tl, Tree tr, boolean onlyRoot) {
		List<Action> actions;
				
		DiffResult result = getActions(tl, tr);
		
		if (onlyRoot) {
			actions = result.getRootActions();
		}else{
			actions = result.getAllActions();
		}
		return actions;
	}

	public DiffResult compareContent(String contentL, String contentR,
			GranuralityType granularity) throws Exception {
		

			tl = this.createTree(contentL, granularity);

			tr = this.createTree(contentR, granularity);

			if(tl == null || tr == null){
				log.debug("Imposible to generate an ast");
				return null;
			}
			
			return  getActions(tl, tr);

	};

	
	public List<Action> compareFiles(File fjaval, File fjavar,
			GranuralityType granularity, boolean onlyRoot) {

		TreeGenerator treeGen = TreeGeneratorRegistry.getGenerator(granularity);
		
		List<Action> actions = null;
		try {

			
			tl = treeGen.generateTree(readFileContent(fjaval));

			tr =  treeGen.generateTree(readFileContent(fjavar));

			actions = getActions(tl, tr, onlyRoot);
			return actions;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}

	}

	
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2)
			throw new IllegalArgumentException("wrong # of parameters");

		DiffEngineFacade c = new DiffEngineFacade();
		List<Action> actions = c.compareFiles(new File(args[0]), new File(
				args[1]), GranuralityType.CD, true);

		for (Action action : actions) {
			System.out.println("-->" + action);
		}

	}

	
	private static String readFileContent(File f) throws IOException {
		FileReader reader = new FileReader(f);
		char[] chars = new char[(int) f.length()];
		reader.read(chars);
		String content = new String(chars);
		reader.close();
		return content;
	}
	
	
	private File createTempFile(String name, String content) throws IOException {

		File temp = File.createTempFile(name, ".tmp");

		BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
		bw.write(content);
		bw.close();

		return temp;

	}
	
}
