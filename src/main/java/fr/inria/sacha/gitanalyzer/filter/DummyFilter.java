package fr.inria.sacha.gitanalyzer.filter;

import java.util.List;

import fr.inria.sacha.gitanalyzer.interfaces.Commit;


/**
 * An use of the Empty Object Pattern to avoid cases of NullPointer 
 * This "filter" is cool, it agrees with everything.
 */
public class DummyFilter implements IFilter {

	@Override
	public boolean acceptCommit(Commit c) {
		return true;
	}

	@Override
	public boolean acceptFragment(String fragment) {
		return true;
	}

}
