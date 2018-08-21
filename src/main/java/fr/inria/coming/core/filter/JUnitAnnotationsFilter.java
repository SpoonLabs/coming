package fr.inria.coming.core.filter;

import fr.inria.coming.core.interfaces.IFilter;

/**
 * Filter ignoring a fragment when its a test method prefix (like '@setup','@before',...)
 * This filter apply only on fragment level
 */
public class JUnitAnnotationsFilter extends AbstractFilter{
	
	private final static String [] predicates = 
		new String[] {"@Test", "@After", "@AfterClass",
		"@Before","@BeforeClass", "@Rule", "@Ignore" };
	
	public JUnitAnnotationsFilter(){
		super();
	}
	
	public JUnitAnnotationsFilter(IFilter parentFilter){
		super(parentFilter);
	}
	
	@Override
	public boolean acceptFragment(String fragment) {
		if (super.acceptFragment(fragment)) {
			
			for (String predicat : predicates) {
				if (predicat.contains(fragment)) return false;
			}
			return true;
		}
		else return false;
		
	}



}
