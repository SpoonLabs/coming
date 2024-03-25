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
	protected String postName;
	protected String previousName;

	public FilePair(String previous, String post, String previousName, String postName) {
		super();
		this.previousVersion = previous;
		this.postVersion = post;
		this.previousName = previousName;
		this.postName = postName;
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
	// TODO this should return a file
	// because the convention on the "name" is unclear
	// a file name? or a valid file path?
	public String getNextName() {
		return postName;
	}

	@Override
	public String getPreviousName() {
		return previousName;
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
	public void setNextName(String name) {
		this.postName = name;

	}

	@Override
	public void setPreviousName(String previousName) {
		this.previousName = previousName;

	}

}
