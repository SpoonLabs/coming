package fr.inria.coming.core.entities.interfaces;

/**
 * 
 * @author Matias Martinez
 *
 * @param <T>
 */
public interface IRevisionPair<T> {

	public T getPreviousVersion();

	public T getNextVersion();

	public String getName();

	public String getPreviousName();

	public void setPreviousVersion(T previousContent);

	public void setNextVersion(T content);

	public void setName(String name);

	public void setPreviousName(String previousName);

}
