package fr.inria.gitanalyzer.interfaces;

import java.util.List;

public interface FragmentAnalyzer extends CommitAnalyzer {

  /** Return a fragment that contains all new fragments of the commit.
   * Should be treated one by one.
   * @return Fragment containes all new fragments
   * @throws Exception If create fragments fails
   */
  public List<String> getNewFragments(Commit c) throws Exception;
  
  /** Return a fragment that contains all new fragments of the commit.
   * Should be treated one by one.
   * @return Fragment containes all new fragments
   * @throws Exception If create fragments fails
   */
  public List<String> getNewFragments(FileCommit c) throws Exception;
  

}
