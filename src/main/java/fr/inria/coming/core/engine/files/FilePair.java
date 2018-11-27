package fr.inria.coming.core.engine.files;

import fr.inria.coming.core.entities.interfaces.IRevisionPair;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FilePair implements IRevisionPair<String> {

	protected String previousVersion;
	protected String postVersion;
	protected String name;

	public FilePair(String previous, String post, String name) {
		super();
		this.previousVersion = previous;
		this.postVersion = post;
		this.name = name;
	}

	@Override
	public String getPreviousVersion() {
		return previousVersion;
	}

	@Override
	public String getNextVersion() {
		return postVersion;
	}

	@Override
	public String getName() {
		return name;
	}

}
