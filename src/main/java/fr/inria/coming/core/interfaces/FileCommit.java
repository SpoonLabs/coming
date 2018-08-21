package fr.inria.coming.core.interfaces;

public interface FileCommit {

  String getPreviousVersion();
  
  String getNextVersion();

  String getFileName();

  String getCompletePath();

  String getPreviousFileName();
  
  String getNextFileName();

  Commit getCommit();

}
