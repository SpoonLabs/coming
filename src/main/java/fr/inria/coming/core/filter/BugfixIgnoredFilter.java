package fr.inria.coming.core.filter;

import fr.inria.coming.core.interfaces.Commit;
import fr.inria.coming.core.interfaces.IFilter;



public class BugfixIgnoredFilter extends KeyWordsTitleFilter{
	
	private final static String [] keywords = 
			new String[] {"fix","solve","correct", "patch" };
	
	/**
	 * Initialize a filter with pre-defined predicates to allow only not bugfixing commits
	 */
	public BugfixIgnoredFilter(){
		super(keywords);
	}
	
	public BugfixIgnoredFilter(IFilter parentFilter){
		super(parentFilter, keywords);
	}

	@Override
	public boolean acceptCommit(Commit c) {
		if (this.parentFilter.acceptCommit(c))
		{
			String message = c.getShortMessage().toLowerCase();
			for (String predicate : predicates) {
				if (message.contains(predicate))
					return false;
			}
			return true;
			
		}
		else return false;
	}

}
