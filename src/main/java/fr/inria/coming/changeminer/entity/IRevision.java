package fr.inria.coming.changeminer.entity;

import java.util.List;

import fr.inria.coming.core.entities.interfaces.IRevisionPair;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface IRevision {

	public List<IRevisionPair> getChildren();

	public String getName();
	
	public String getFolder();
}
