package fr.inria.sacha.remining.coming.dependencyanalyzer.entity;

import spoon.reflect.reference.CtTypeReference;

/**
 * Describes dependency which has been deleted from a class
 * 
 * @author Romain Philippon
 *
 */
public final class DeletedDependency extends Dependency {

	public DeletedDependency(CtTypeReference<?> dependency) {
		super(dependency);
	}
	
	public DeletedDependency(String dependencyName) {
		super(dependencyName);
	}
}
