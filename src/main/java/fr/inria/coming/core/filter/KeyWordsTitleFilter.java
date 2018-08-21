package fr.inria.coming.core.filter;

import fr.inria.coming.core.interfaces.Commit;



/**
 * A filter to search keywords ONLY in the first line of a commit message
 *
 */
public class KeyWordsTitleFilter extends AbstractFilter {
	
	
	protected String [] predicates;
	
	/**
	 * Take an array of keywords for which we expect to find at least one in the commit
	 * @param keywords The array of keyword uses as predicates
	 */
	public KeyWordsTitleFilter(String... keywords) {
		super();
		predicates = keywords;
	}
	
	public KeyWordsTitleFilter(IFilter parentFilter, String... keywords){
		super(parentFilter);
		this.predicates = keywords;
	}
	
	@Override
	public boolean acceptCommit(Commit c) {
		if (super.acceptCommit(c))
		{	
			String title = c.getShortMessage().toLowerCase();
			for (String predicate : predicates) {
				if (title.contains(predicate))
					return true;
			}
		}
		return false;
	}
}
