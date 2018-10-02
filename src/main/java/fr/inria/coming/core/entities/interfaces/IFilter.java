package fr.inria.coming.core.entities.interfaces;

/**
 * A filter to establish if a Commit, a list of Fragments or a lone fragment
 * matches with specifics criteria
 *
 */
public interface IFilter<T> {
	boolean accept(T c);

}
