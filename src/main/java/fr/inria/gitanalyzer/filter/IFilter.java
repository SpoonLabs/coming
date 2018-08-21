package fr.inria.gitanalyzer.filter;

import fr.inria.gitanalyzer.interfaces.Commit;

/**
 * A filter to establish if a Commit, a list of Fragments or a lone fragment matches with specifics criteria
 *
 */
public interface IFilter {
	boolean acceptCommit(Commit c);
	boolean acceptFragment(String fragment);
}
