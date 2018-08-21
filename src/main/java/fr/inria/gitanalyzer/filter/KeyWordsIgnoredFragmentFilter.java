package fr.inria.gitanalyzer.filter;


/**
 * A filter to ignore fragment containing one of the specified keywords
 *
 */
public class KeyWordsIgnoredFragmentFilter extends AbstractFilter {

	private String [] keywords;
	
	public KeyWordsIgnoredFragmentFilter(String... keywords){
		super();
		this.keywords = keywords;
	}
	
	public KeyWordsIgnoredFragmentFilter(IFilter parentFilter, String... keywords){
		super(parentFilter);
		this.keywords = keywords;
	}
	
	
	/**
	 * Check that the fragment don't contain any words from the specified keywords list
	 */
	@Override
	public boolean acceptFragment(String fragment){
		
		if (super.acceptFragment(fragment)){
			
			if(fragment.trim().isEmpty())
				return false;
			
			for (String keyword : this.keywords) {
				//if (fragment.contains(keyword))
				if (fragment.trim().startsWith(keyword))
					return false;
			}
			return true;
		}
		else return false;
	}
	
}
