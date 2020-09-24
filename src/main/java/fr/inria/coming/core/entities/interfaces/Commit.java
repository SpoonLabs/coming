package fr.inria.coming.core.entities.interfaces;

import java.util.List;

import org.eclipse.jgit.lib.PersonIdent;

import fr.inria.coming.changeminer.entity.IRevision;

public interface Commit extends IRevision {

	/**
	 * Return a list of FileCommit affected by commit
	 * 
	 * @return list of pFileCommit
	 */
	public List<FileCommit> getFileCommits();

	public List<FileCommit> getJavaFileCommits();

	public List<FileCommit> getFileCommits(String extension);

	/**
	 * Get the name of the commit (SHA-1)
	 * 
	 * @return the commint name (SHA-1 code)
	 */
	public String getName();

	public boolean containsJavaFile();

	public String getShortMessage();

	public String getFullMessage();

	public int getRevCommitTime();

	public String getRevDate();

	public List<String> getParents();

	public List<String> getBranches();

	public PersonIdent getAuthorInfo();

}
