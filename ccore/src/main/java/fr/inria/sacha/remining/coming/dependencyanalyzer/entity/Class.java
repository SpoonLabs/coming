package fr.inria.sacha.remining.coming.dependencyanalyzer.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Gathers essential informations about a JAVA class
 * 
 * @author Romain PHILIPPON
 */
public class Class {
	/**
	 * Tells the class kind
	 */
	private ClassType type;
	/**
	 * Corresponds to the class name
	 */
	private String name;
	/**
	 * Gives all class dependencies according to the attribute & return method type
	 */
	private Set<Dependency> dependencies;
	
	public Class(String name, ClassType type, Set<Dependency> dependencies){
		this.type = type;
		this.name = name;
		this.dependencies = dependencies;
	}
	
	public ClassType getType() {
		return type;
	}

	public void setType(ClassType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Set<Dependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	/**
	 * Indicates which kind of class is an instance of Class
	 * 
	 * @author Romain Philippon
	 *
	 */
	public static enum ClassType {
		REGULAR, ANONYMOUS, STATIC;
	}
}
