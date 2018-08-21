package fr.inria.gitanalyzer.filter;

import fr.inria.gitanalyzer.interfaces.Commit;
import fr.inria.gitanalyzer.interfaces.FragmentAnalyzer;


public class MaximalSizeFilter extends AbstractFilter{
	private int limit;
	protected FragmentAnalyzer fragmentAnalyzer;
	

	/**
	 * Initialize the filter with a maximal allowed number of fragment per commit (included limit)
	 * The filter don't have any stacked other filter
	 * @param limit The maximal quantity of fragments
	 */
	public MaximalSizeFilter(int limit, FragmentAnalyzer fragmentAnalyzer){
		super();
		this.limit = limit;
		this.fragmentAnalyzer = fragmentAnalyzer;
	}
	
	
	/**
	 * Initialize the filter with a maximal allowed number of fragment per commit (included limit)
	 * This filter is stacked with a previous one.
	 * @param parentFilter The other filter to stack
	 * @param limit The maximal quantity of fragments (included)
	 */
	public MaximalSizeFilter(int limit, IFilter parentFilter, FragmentAnalyzer fragmentAnalyzer){
		super(parentFilter);
		this.limit = limit;
	}

	
	/**
	 * Check if the specified commit pass the requirement of maximal size
	 * @return <tt>true</tt> if the fragments count is at least equal to the defined limit, else <tt>false</tt>
	 */
	
	// FIXME Suite au refactoring
	@Override
	public boolean acceptCommit(Commit c) {
		if (super.acceptCommit(c)){
		
			try {
				if (this.fragmentAnalyzer.getNewFragments(c).size() <= this.limit)
				return true;
				
			} catch (Exception e) {
				System.err.println("Error to check the number of fragments inside of the commit " + c.getName());
				System.err.println(e.getMessage());
				return false;
			}
			
		}
		return false;
	}


}
