package fr.inria.coming.spoon.core.dummies;

import fr.inria.coming.core.entities.interfaces.IFilter;

public class MyTestFilter<T> implements IFilter<T> {

	@Override
	public boolean accept(T c) {
		System.out.println("Attention:Test filter");
		return false;
	}

}
