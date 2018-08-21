package fr.inria.coming.core.filter;

import fr.inria.coming.core.interfaces.Commit;
import fr.inria.coming.core.interfaces.IFilter;



/**
 * A model of filter allowing filter stacking
 *
 */
public abstract class AbstractChainedFilter implements IFilter {

	protected IFilter parentFilter;
	
	/**
	 * Simply define a parent filter for this filter
	 * @param parentFilter
	 */
	public AbstractChainedFilter(IFilter parentFilter){
		this.parentFilter = parentFilter;
	}
	
	
	/**
	 * Define a filter which don't have any parent
	 */
	public AbstractChainedFilter(){
		this.parentFilter = new DummyFilter(); // empty object for a safe use of filters chain
	}
	
	/**
	 * Stack a unique filter on this one. 
	 * Only one filter can be stack at a time (but the stacked filter can have a stacked filter)
	 * @param filter The filter to stack on this one
	 */
	protected void setStackedFilter(IFilter filter){
		this.parentFilter = filter;
	}
	

	/**
	 * Get the success or fail status of the stacked filter on the commit
	 */
	@Override
	public boolean acceptCommit(Commit c) {
		return this.parentFilter.acceptCommit(c);
	}


	/**
	 * Get the success or fail status of the stacked filter on the alone fragment
	 */
	@Override
	public boolean acceptFragment(String fragment) {
		return this.parentFilter.acceptFragment(fragment);
	}

}
