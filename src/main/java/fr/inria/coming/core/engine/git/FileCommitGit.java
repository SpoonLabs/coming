package fr.inria.coming.core.engine.git;

import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.FileCommit;

public class FileCommitGit implements FileCommit {

	private Commit commit;
	private String previousFileName;
	private String nextFileName;
	private String previousVersion;
	private String nextVersion;
	private String previousCommitName;

	public FileCommitGit(String previousFileName, String previousVersion, String nextFileName, String nextVersion,
			Commit commit) {
		this.commit = commit;
		this.previousVersion = previousVersion;
		this.nextVersion = nextVersion;
		this.previousFileName = previousFileName;
		this.nextFileName = nextFileName;
	}

	public FileCommitGit(String previousFileName, String previousVersion, String nextFileName, String nextVersion,
			Commit commit, String previousCommitName) {
		this.commit = commit;
		this.previousVersion = previousVersion;
		this.nextVersion = nextVersion;
		this.previousFileName = previousFileName;
		this.nextFileName = nextFileName;
		this.previousCommitName = previousCommitName;
	}

	@Override
	public Commit getCommit() {
		return this.commit;
	}

	@Override
	public String getPreviousName() {
		return this.previousFileName;
	}

	@Override
	public String getNextVersion() {
		return this.nextVersion;
	}

	@Override
	public String getPreviousVersion() {
		return this.previousVersion;
	}

	public String getPreviousCommitName() {
		return previousCommitName;
	}

	public String toString() {
		return this.previousFileName+"->"+this.nextFileName;
	}

	@Override
	public String getNextName() {
		return this.nextFileName;
	}

	public void setPreviousVersion(String previousVersion) {
		this.previousVersion = previousVersion;
	}

	public void setNextVersion(String nextVersion) {
		this.nextVersion = nextVersion;
	}


	@Override
	public void setNextName(String name) {
		this.nextFileName = name;

	}

	@Override
	public void setPreviousName(String previousName) {
		this.previousFileName = previousName;

	}
}
