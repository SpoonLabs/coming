package fr.inria.coming.core.filter;

/**
 * A filter which accepts only commits with a supposed bugfix purpose
 *
 */
public class BugfixOnlyFilter extends KeyWordsTitleFilter {
	
	private final static String [] keywords = 
			new String[] {"fix","solve","correct", "patch", "bug"};
	
	public BugfixOnlyFilter(){
		super(keywords);
	}
	
	public BugfixOnlyFilter(IFilter parentFilter){
		super(parentFilter, keywords);
	}
}