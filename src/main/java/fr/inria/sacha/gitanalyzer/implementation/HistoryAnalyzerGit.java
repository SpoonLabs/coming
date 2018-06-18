package fr.inria.sacha.gitanalyzer.implementation;

import fr.inria.sacha.gitanalyzer.interfaces.CommitAnalyzer;
import fr.inria.sacha.gitanalyzer.interfaces.Filter;
import fr.inria.sacha.gitanalyzer.interfaces.HistoryAnalyzer;

public class HistoryAnalyzerGit implements HistoryAnalyzer {

	private CommitAnalyzer analyzer;
	private Filter filter;
	private String path;

	@Override
	public void run() {
		
	}

	@Override
	public void setCommitAnalyzer(CommitAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	@Override
	public void setCommitFilter(Filter filter) {
		this.filter = filter;
	}

	@Override
	public void setGitRepoPath(String path) {
		this.path = path;
	}

}
