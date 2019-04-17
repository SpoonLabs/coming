package fr.inria.coming.core.entities;

import fr.inria.coming.core.entities.interfaces.IRevisionPair;

/**
 * 
 * @author Matias Martinez
 *
 */
@Deprecated
public class RevisionPair<T> implements IRevisionPair<T> {

	protected T previousVersion;
	protected T nextVersion;

	protected String name;

	public T getPreviousVersion() {
		return previousVersion;
	}

	public void setPreviousVersion(T previousVersion) {
		this.previousVersion = previousVersion;
	}

	public T getNextVersion() {
		return nextVersion;
	}

	public void setNextVersion(T getNextVersion) {
		this.nextVersion = getNextVersion;
	}

	public RevisionPair(String name, T previousVersion, T nextVersion) {
		super();
		this.previousVersion = previousVersion;
		this.nextVersion = nextVersion;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getPreviousName() {
		return name;
	}

}
