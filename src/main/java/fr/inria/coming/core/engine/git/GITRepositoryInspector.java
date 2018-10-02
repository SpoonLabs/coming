package fr.inria.coming.core.engine.git;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.gumtreediff.actions.model.Action;

import fr.inria.coming.changeminer.entity.CommitFinalResult;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.core.engine.RevisionNavigationExperiment;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.CommitAnalyzer;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.entities.interfaces.RepositoryP;
import fr.inria.coming.core.extensionpoints.navigation.InOrderRevisionNavigation;
import fr.inria.coming.core.filter.DummyFilter;
import fr.inria.coming.main.ComingProperties;

/**
 *
 * This class navigates the history of a project: for each commit...
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 *
 */
public class GITRepositoryInspector extends RevisionNavigationExperiment<Commit> {

	Logger log = Logger.getLogger(GITRepositoryInspector.class.getName());

	public GITRepositoryInspector() {
		// By default, in order.
		super(new InOrderRevisionNavigation<Commit>());
	}

	public FinalResult analize(String repositoryPath, CommitAnalyzer commitAnalyzer) {

		return this.analize(repositoryPath, commitAnalyzer, new DummyFilter());
	}

	public FinalResult analize(String repositoryPath, CommitAnalyzer commitAnalyzer, IFilter filter) {
		ComingProperties.properties.setProperty("location", repositoryPath);
		return this.analyze();
	}

	@Override
	protected FinalResult processEnd() {

		return new CommitFinalResult(allResults);
	}

	Map<Commit, RevisionResult> allResults = new HashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	public void processEndRevision(Commit element, RevisionResult resultAllAnalyzed) {

		allResults.put(element, resultAllAnalyzed);

	}

	/**
	 *
	 * @param result
	 */
	public void printResult(Map<FileCommit, List> result) {

		log.info("End of processing: Result " + result.size());
		for (FileCommit fc : result.keySet()) {
			List<Action> actionsfc = result.get(fc);
			log.info("Commit " + fc.getCommit().getName() + ", " + fc.getCommit().getFullMessage().replace('\n', ' ')
					+ ", file " + fc.getFileName() + " , instances  " + actionsfc.size());
		}
	}

	@Override
	public Collection<Commit> loadDataset() {

		String repositoryPath = ComingProperties.getProperty("location");
		String branch = ComingProperties.getProperty("branch");

		RepositoryP repo = new RepositoryPGit(repositoryPath, branch);

		// For each commit of a repository
		List<Commit> history = repo.history();

		return history;
	}

}
