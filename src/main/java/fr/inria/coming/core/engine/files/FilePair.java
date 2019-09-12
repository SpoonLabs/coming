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

	@Override
	public String getPreviousName() {
		return name;
	}

	@Override
	public void setPreviousVersion(String previousContent) {
		this.previousVersion = previousContent;

	}

	@Override
	public void setNextVersion(String content) {
		this.postVersion = content;

	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public void setPreviousName(String previousName) {
		// TODO: maybe we would add a second field for the previous name

	}

}
