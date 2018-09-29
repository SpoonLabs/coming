package fr.inria.coming.core.filter.commitmessage;

import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.filter.AbstractChainedFilter;

/**
 * A filter to search keywords in the commit message
 *
 */
public class KeyWordsMessageFilter extends AbstractChainedFilter<Commit> {

	protected String[] predicates;

	/**
	 * Take an array of keywords for which we expect to find at least one in the
	 * commit
	 * 
	 * @param keywords The array of keyword uses as predicates
	 */
	public KeyWordsMessageFilter(String... keywords) {
		super();
		predicates = keywords;
	}

	public KeyWordsMessageFilter(IFilter parentFilter, String... keywords) {
		super(parentFilter);
		this.predicates = keywords;
	}

	@Override
	public boolean accept(Commit c) {
		if (super.accept(c)) {
			String title = c.getShortMessage() + " " + c.getFullMessage();
			for (String predicate : predicates) {
				if (title.contains(predicate)) {
					return true;
				}
			}
		}
		return false;
	}
}
