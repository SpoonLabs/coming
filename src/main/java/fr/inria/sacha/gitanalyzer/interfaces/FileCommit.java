package fr.inria.sacha.gitanalyzer.interfaces;

public interface FileCommit {

  String getPreviousVersion();
  
  String getNextVersion();

  String getFileName();

  String getCompletePath();

  String getPreviousFileName();
  
  String getNextFileName();

  Commit getCommit();

}
