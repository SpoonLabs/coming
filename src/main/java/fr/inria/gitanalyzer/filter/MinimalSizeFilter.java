package fr.inria.gitanalyzer.filter;

import fr.inria.gitanalyzer.interfaces.Commit;
import fr.inria.gitanalyzer.interfaces.FragmentAnalyzer;



/**
 * A filter on the commit size.
 * It defines a minimal count value of fragments inside a commit to validate it.
 *
 */
public class MinimalSizeFilter extends AbstractFilter{
	
	private int limit;
	private FragmentAnalyzer fragmentAnalyzer;
	
	
	/**
	 * Initialize the filter with the value of required number of fragment per commit
	 * And stack a existing filter over.
	 * @param parentFilter The existing filter to add over this one
	 * @param limit The required size of the fragment list of a commit
	 */
	public MinimalSizeFilter(int limit, IFilter parentFilter, FragmentAnalyzer fragmentAnalyzer){
		super(parentFilter);
    if (fragmentAnalyzer == null) {
      throw new IllegalArgumentException();
    }
    this.fragmentAnalyzer = fragmentAnalyzer;
		this.limit = limit;
	}

	
	/**
	 * Check if the specified commit pass through the minimal requirement
	 * @return <tt>true</tt> if the fragments count is at least equal to the defined limit, else <tt>false</tt>
	 */
	@Override
	public boolean acceptCommit(Commit c) {
		if (super.acceptCommit(c)){
			try {
				if (this.fragmentAnalyzer
				    .getNewFragments(c)
				    .size() >= this.limit)
				return true;
				
			} catch (Exception e) {
				System.err.println("Error while checking the quantity of fragments inside of the commit " + c.getName());
				e.printStackTrace();
			}
		}
		return false;
	}


}
