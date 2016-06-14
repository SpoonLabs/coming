package fr.inria.sacha.remining.coming.dependencyanalyzer.entity;

import java.rmi.NoSuchObjectException;

import spoon.reflect.reference.CtTypeReference;

/**
 * Describes a JAVA dependency
 * 
 * @author Romain Philippon
 *
 */
public class Dependency {
	/**
	 * Is the type reference given by spoon 
	 */
	protected CtTypeReference<?> dependency;
	/**
	 * Is the dependency Squalified name
	 */
	protected String qualifiedName;
	
	public Dependency(CtTypeReference<?> dependency) {
		this.setDependency(dependency);
		this.qualifiedName = null;
	}
	
	public Dependency(String dependencyName) {
		this.qualifiedName = dependencyName;
		this.dependency = null;
	}
	
	public String getQualifiedDependencyName() {
		if(this.qualifiedName == null)
			return this.dependency.getQualifiedName();
		else
			return this.qualifiedName;
	}
	
	public CtTypeReference<?> getDependency() throws NoSuchObjectException {
		if(this.dependency != null) {
			return this.dependency;
		}
		else {
			throw new NoSuchObjectException("No dependency of CtTypeReference initialized - Be sure to give this type reference before use the method");
		}
	}
	
	public void setDependency(CtTypeReference<?> newDependency) {
		this.dependency = newDependency;
		this.qualifiedName = this.dependency.getQualifiedName();
	}
	
	@Override
	public int hashCode() { 
	    return -1;
	}

	@Override
	public boolean equals(Object o) {
		if(o.getClass() == this.getClass()) {
			try {
				return ((Dependency) o).getDependency().equals(this.dependency);
			}
			catch(NoSuchObjectException nsoe) {
				return ((Dependency) o).getQualifiedDependencyName().equals(this.qualifiedName);
			}
		}
		else {
			return false;
		}
	}
}
