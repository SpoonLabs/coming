package fr.inria.coming.core.filter;

import fr.inria.coming.core.entities.interfaces.IFilter;

/**
 * An use of the Empty Object Pattern to avoid cases of NullPointer This
 * "filter" is cool, it agrees with everything.
 */
public class DummyFilter<T> implements IFilter<T> {

	@Override
	public boolean accept(T c) {
		return true;
	}

}
