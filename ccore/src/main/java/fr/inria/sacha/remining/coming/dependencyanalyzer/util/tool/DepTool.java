package fr.inria.sacha.remining.coming.dependencyanalyzer.util.tool;

import java.rmi.NoSuchObjectException;
import java.util.HashSet;
import java.util.Set;

import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Dependency;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.AddedDependency;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.DeletedDependency;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.SameDependency;

/**
 * Is a class providing methods to do operations on dependencies instances
 * @author Romain Philippon
 *
 */
public class DepTool {
	/**
	 * Computes a diff operation (like Linux diff) between two sets of dependencies 
	 * @param before is a set of dependencies in a previous state
	 * @param after is a set of dependencies in a next state
	 * @return the diff result containing istances of SameDependencies, DeletedDependencies and AddedDependencies
	 */
	public static Set<Dependency> diff(Set<Dependency> before, Set<Dependency> after) {
		HashSet<Dependency> result = new HashSet<Dependency>();
		
		/* COMPUTES THE SAME AND THE DELETED DEPENDENCIES */
		for(Dependency dependency : before) {

			if(after.contains(dependency)) { // dependencies which have not changed
//				try {
//					result.add(new SameDependency(dependency.getDependency()));
//				}
//				catch(NoSuchObjectException nsoe) {
//					result.add(new SameDependency(dependency.getQualifiedDependencyName()));
//				}
			}
			else { // dependencies deleted
				System.err.println(dependency);
				try {
					result.add(new DeletedDependency(dependency.getDependency()));
				} catch(NoSuchObjectException nsoe) {
					result.add(new DeletedDependency(dependency.getQualifiedDependencyName()));
				}
			}
		}
		
		/* COMPUTES ALL NEW DEPENDENCIES */
		after.removeAll(before);
		
		for(Dependency dependency : after) {
			try {
				result.add(new AddedDependency(dependency.getDependency()));
			}
			catch(NoSuchObjectException nsoe) {
				result.add(new AddedDependency(dependency.getQualifiedDependencyName()));
			}
		}
		
		return result;
	}
}