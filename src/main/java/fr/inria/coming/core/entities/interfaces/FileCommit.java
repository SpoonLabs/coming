package fr.inria.coming.core.entities.interfaces;

public interface FileCommit extends IRevisionPair<String> {

	String getPreviousVersion();

	String getNextVersion();

	String getFileName();

	String getCompletePath();

	String getPreviousFileName();

	String getNextFileName();

	Commit getCommit();

}
