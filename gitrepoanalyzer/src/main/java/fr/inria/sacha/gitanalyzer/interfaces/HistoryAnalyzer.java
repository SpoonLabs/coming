package fr.inria.sacha.gitanalyzer.interfaces;

public interface HistoryAnalyzer {

  void setGitRepoPath(String path);
  
  void setCommitAnalyzer(CommitAnalyzer analyzer);

  void setCommitFilter(Filter filter);
  
  void run();
}
