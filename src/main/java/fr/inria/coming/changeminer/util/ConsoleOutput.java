package fr.inria.coming.changeminer.util;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Update;

import fr.inria.coming.changeminer.analyzer.Parameters;
import fr.inria.coming.core.interfaces.Commit;
import fr.inria.coming.core.interfaces.FileCommit;
import gumtree.spoon.diff.operations.Operation;

public class ConsoleOutput {

	public static Logger log = Logger.getLogger(ConsoleOutput.class.getName());

	/**
	 *
	 * @param result
	 */
	public static void printResultDetails(Map<Commit, List<Operation>> result) {

		Parameters.printParameters();

		log.info("End of processing: Result " + result.size());
		for (Commit o : result.keySet()) {

			for (FileCommit o2 : ((Commit) o).getFileCommits()) {
				FileCommit fc = (FileCommit) o2;
				log.info("Commit: " + fc.getCommit().getName() + ", "
						+ fc.getCommit().getFullMessage().replace('\n', ' ') + ", file " + fc.getFileName());

				List<Operation> actionsfc = result.get(fc);
				if (actionsfc == null)
					continue;
				System.out.println();
				System.out.println(" , instances  " + actionsfc.size());
				System.out.println("file: " + fc.getFileName());
				System.out.println("Modifications: " + fc.getFileName());

				for (Operation op : actionsfc) {
					Action action = op.getAction();
					// --
					if (action instanceof Update) {
						Update up = (Update) action;
						// System.out.println(up);
						System.out.println(up.getNode().getLabel());
						System.out.println(up.getValue());

					} else {
						System.out.println(action.getNode().getLabel());
					}
					System.out.println("-");
				}

				System.out.println("---");
			}
		}
	}

}
