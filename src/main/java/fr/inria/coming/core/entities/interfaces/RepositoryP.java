package fr.inria.coming.core.entities.interfaces;

import java.util.List;

import org.eclipse.jgit.lib.Repository;

// Based on a Git repository, contain's all commit of the repository
public interface RepositoryP {
	
	/** Get the history of the repo
	 * @return the list of the commit of the repo
	 */
	public List<Commit> history();

	
	/** Get the JGit repository
	 * @return the JGit repository
	 */
	public Repository getRepository();

}
