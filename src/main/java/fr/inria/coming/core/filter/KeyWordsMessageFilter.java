package fr.inria.coming.core.filter;

import fr.inria.coming.core.interfaces.Commit;
import fr.inria.coming.core.interfaces.IFilter;




/**
 * A filter to search keywords in the  commit message
 *
 */
public class KeyWordsMessageFilter extends AbstractFilter {
	
	
	protected String [] predicates;
	
	/**
	 * Take an array of keywords for which we expect to find at least one in the commit
	 * @param keywords The array of keyword uses as predicates
	 */
	public KeyWordsMessageFilter(String... keywords) {
		super();
		predicates = keywords;
	}
	
	public KeyWordsMessageFilter(IFilter parentFilter, String... keywords){
		super(parentFilter);
		this.predicates = keywords;
	}
	
	@Override
	public boolean acceptCommit(Commit c) {
		if (super.acceptCommit(c))
		{
			String title = c.getShortMessage() +" "+c.getFullMessage();
			for (String predicate : predicates) {
				if (title.contains(predicate)) {
					return true;
				}
			}
		}
		return false;
	}
}
