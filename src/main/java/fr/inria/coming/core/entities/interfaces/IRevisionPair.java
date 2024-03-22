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

	/**
	 * @return the file path of the next version
	 */
	public String getNextName();

	public String getPreviousName();

	public void setPreviousVersion(T previousContent);

	public void setNextVersion(T content);

	public void setNextName(String name);

	public void setPreviousName(String previousName);

}
