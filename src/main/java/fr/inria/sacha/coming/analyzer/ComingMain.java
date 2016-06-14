package fr.inria.sacha.coming.analyzer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fr.inria.sacha.coming.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.SimpleChangeFilter;
import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.coming.entity.ChangeDistillerEntityType;
import fr.inria.sacha.coming.util.ConsoleOutput;
import fr.inria.sacha.coming.util.XMLOutput;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
/**
 * 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class ComingMain {

	public static void main(String[] args) {
		
		
		String message = "USAGE --parameters-- [projectLocation] [entity] [action] [message (optional)] ; to get the entities use -e, to get the actions use -a";
		
		if(args == null ){
				System.out.println(message);
				return;
		}
			
		if(args.length < 3)
			return ;
		
		Parameters.setUpProperties();
		Parameters.printParameters();
	
	    RepositoryInspector c = new RepositoryInspector();
	    String projectLocation = args[0];
	    String entity = args[1];
	    String action = args[2];
	    String messageHeuristic = (args.length == 4)? args[3]: "";
	    
	    FineGrainChangeCommitAnalyzer analyzer = new FineGrainChangeCommitAnalyzer(
	    		new SimpleChangeFilter(entity, ActionType.valueOf(action)));

	    Map<FileCommit, List> instancesFound = c.analize(
	    		projectLocation
	    		, "master", 
	    		analyzer
	    		, messageHeuristic);
	    ConsoleOutput.printResultDetails(instancesFound);
	    XMLOutput.print(instancesFound);
	}
	
}
