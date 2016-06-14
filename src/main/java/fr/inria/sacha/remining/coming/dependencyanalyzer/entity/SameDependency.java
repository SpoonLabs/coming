package fr.inria.sacha.remining.coming.dependencyanalyzer.entity;

import spoon.reflect.reference.CtTypeReference;

/**
 * Represents a class dependency which has not been changed
 * @author Romain Philippon
 *
 */
public class SameDependency extends Dependency {

	public SameDependency(CtTypeReference<?> dependency) {
		super(dependency);
	}
	
	public SameDependency(String dependencyName) {
		super(dependencyName);
	}
}
