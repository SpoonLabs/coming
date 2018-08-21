package fr.inria.coming.core.filter;

import fr.inria.coming.core.interfaces.IFilter;

/**
 * A filter which accepts only commits with a supposed bugfix purpose
 *
 */
public class BugfixKeywordsFilter extends KeyWordsTitleFilter {
	
	private final static String [] keywords = 
			new String[] {"fix","solve","correct", "patch", "bug"};
	
	public BugfixKeywordsFilter(){
		super(keywords);
	}
	
	public BugfixKeywordsFilter(IFilter parentFilter){
		super(parentFilter, keywords);
	}
}