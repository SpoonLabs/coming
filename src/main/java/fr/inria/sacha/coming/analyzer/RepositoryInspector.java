package fr.inria.sacha.coming.analyzer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.inria.sacha.coming.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.SimpleChangeFilter;
import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.gitanalyzer.filter.DummyFilter;
import fr.inria.sacha.gitanalyzer.filter.IFilter;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.gitanalyzer.interfaces.CommitAnalyzer;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
import fr.inria.sacha.gitanalyzer.interfaces.RepositoryP;
import fr.inria.sacha.gitanalyzer.object.RepositoryPGit;
import com.github.gumtreediff.actions.model.Action;
/**
 *
 *This class navigates the history of a project: for each commit...
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 *
 */
public class RepositoryInspector {

	Logger log = Logger.getLogger(RepositoryInspector.class.getName());

	public static int PARAM_GIT_PATH = 0;
	public static int PARAM_MASTER_BRANCH = 1;
	public static int PARAM_LABEL = 2;
	public static int PARAM_OP_TYPE = 3;

	public RepositoryInspector() {}
	/**
	 * By default, the main uses the "FineGrainChangeCommitAnalyzer"
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String repositoryPath, masterBranch, label, optype;

		repositoryPath = args[PARAM_GIT_PATH];
		optype = args[PARAM_OP_TYPE];
		label = args[PARAM_LABEL];
		masterBranch = args[PARAM_MASTER_BRANCH];
	
		RepositoryInspector c = new RepositoryInspector();
		FineGrainChangeCommitAnalyzer analyzer = new FineGrainChangeCommitAnalyzer(
				new SimpleChangeFilter(label,ActionType.valueOf(optype)));

		c.analize(repositoryPath, masterBranch, analyzer, null);
	}


	public Map<Commit, List> analize(String repositoryPath, CommitAnalyzer commitAnalyzer) {
		return this.analize(repositoryPath, "master", commitAnalyzer, new DummyFilter());
	}

	public Map<Commit, List> analize(String repositoryPath, CommitAnalyzer commitAnalyzer, IFilter filter) {
		return this.analize(repositoryPath, "master", commitAnalyzer, filter);
	}
	
	@SuppressWarnings("rawtypes")
	public Map<Commit, List> analize(String repositoryPath, String masterBranch, CommitAnalyzer commitAnalyzer, IFilter filter) {

		RepositoryP repo = new RepositoryPGit(repositoryPath, masterBranch);
			
		System.out.println(filter);
		
		// For each commit of a repository
		List<Commit> history = repo.history();
		int i = 0;

		Map<Commit, List> allInstances = new HashMap<Commit, List>();
		for (Commit c : history) {
			//log.debug((i++)+"/"+history.size());
			if (filter.acceptCommit(c)) {
				
				Map<Commit, List> resultCommit = (Map) commitAnalyzer.analyze(c);
				if (resultCommit != null && !resultCommit.isEmpty())
					allInstances.putAll(resultCommit);
			} else {
			//	log.info("\n commits not accepted "+ c.getName());
			}

			i++;
		}

		//System.out.println("Result "+ fineGrainAnalyzer.withPattern + " "+ fineGrainAnalyzer.withoutPattern +" "+ fineGrainAnalyzer.withError);
		log.info("\n commits analyzed "+ i);
		return allInstances;
	}




	
	/**
	 *
	 * @param result
	 */
	public void printResult(Map<FileCommit, List> result) {
	
		log.info("End of processing: Result " + result.size());
		for (FileCommit fc : result.keySet()) {
			List<Action> actionsfc = result.get(fc);
			log.info("Commit " + fc.getCommit().getName()+", "+fc.getCommit().getFullMessage().replace('\n', ' ') + ", file " + fc.getFileName() + " , instances  "
					+ actionsfc.size());
		}
	}
}
