package fr.inria.coming.core.entities.interfaces;

public interface IRevisionPair<T> {

	public T getPreviousVersion();

	public T getNextVersion();

	public String getName();

}
