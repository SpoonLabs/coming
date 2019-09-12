package fr.inria.coming.core.engine.git;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.entity.CommitFinalResult;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.core.engine.RevisionNavigationExperiment;
import fr.inria.coming.core.entities.RevisionDataset;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.RepositoryP;
import fr.inria.coming.core.extensionpoints.navigation.InOrderRevisionNavigation;
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

	protected Logger log = Logger.getLogger(GITRepositoryInspector.class.getName());

	RevisionDataset<Commit> history = null;

	public GITRepositoryInspector() {
		// By default, in order.
		super(new InOrderRevisionNavigation<Commit>());
		this.allResults = new CommitFinalResult();
	}

	@Override
	public FinalResult processEnd() {
		super.processEnd();
		return this.allResults;
	}

	@Override
	public RevisionDataset<Commit> loadDataset() {

		String repositoryPath = ComingProperties.getProperty("location");
		String branch = ComingProperties.getProperty("branch");

		RepositoryP repo = new RepositoryPGit(repositoryPath, branch);

		history = new RevisionDataset<>(repo.history());

		return history;
	}

}
