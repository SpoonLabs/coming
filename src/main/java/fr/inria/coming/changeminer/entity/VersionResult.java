package fr.inria.coming.changeminer.entity;

/**
 * 
 * @author Matias Martinez
 *
 * @param <I>
 * @param <R>
 */
public class VersionResult<I, R> {

	public I versionIdentifier;
	public R versionResult;

	public VersionResult(I versionIdentifier, R versionResult) {
		super();
		this.versionIdentifier = versionIdentifier;
		this.versionResult = versionResult;
	}

	public I getVersionIdentifier() {
		return versionIdentifier;
	}

	public void setVersionIdentifier(I versionIdentifier) {
		this.versionIdentifier = versionIdentifier;
	}

	public R getVersionResult() {
		return versionResult;
	}

	public void setVersionResult(R versionResult) {
		this.versionResult = versionResult;
	}

}
