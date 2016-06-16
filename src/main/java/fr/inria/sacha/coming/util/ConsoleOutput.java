package fr.inria.sacha.coming.util;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.inria.sacha.coming.analyzer.Parameters;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Update;


public class ConsoleOutput {

	public static Logger log = Logger.getLogger(ConsoleOutput.class.getName());


	/**
	 *
	 * @param result
	 */
	public static void printResultDetails(Map<Commit, List> result) {

		Parameters.printParameters();
		
		log.info("End of processing: Result " + result.size());
		for (Object o : result.keySet()) {
			if (! (o instanceof Commit)) continue;
			for (Object o2 : ((Commit)o).getFileCommits()) {
			FileCommit fc = (FileCommit)o2;
			log.info("Commit: " + fc.getCommit().getName()+", "+fc.getCommit().getFullMessage().replace('\n', ' ') + ", file " + fc.getFileName());
			if (! (result.get(fc) instanceof List)) continue;
			List<Action> actionsfc = result.get(fc);
			log.info(" , instances  "
					+ actionsfc.size());
			System.out.println("file: "+fc.getFileName());
			System.out.println("Modifications: "+fc.getFileName());
			

			for (Action action : actionsfc) {
				//--
				if(action instanceof Update) {
					Update up = (Update) action;
					//System.out.println(up);
					System.out.println(up.getNode().getLabel() );
					System.out.println(up.getValue() );
			
				}
				else {
					System.out.println(action.getNode().getLabel());
				}
				System.out.println("-");
			}

			System.out.println("---");
		}
		}
	}
	
}
