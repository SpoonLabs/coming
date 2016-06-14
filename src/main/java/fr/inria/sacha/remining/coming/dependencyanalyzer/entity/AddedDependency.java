package fr.inria.sacha.remining.coming.dependencyanalyzer.entity;

import spoon.reflect.reference.CtTypeReference;

/**
 * Describes dependency which has been added to a class
 * 
 * @author Romain Philippon
 *
 */
public final class AddedDependency extends Dependency {

	public AddedDependency(CtTypeReference<?> dependency) {
		super(dependency);
	}
	
	public AddedDependency(String dependencyName) {
		super(dependencyName);
	}
}
