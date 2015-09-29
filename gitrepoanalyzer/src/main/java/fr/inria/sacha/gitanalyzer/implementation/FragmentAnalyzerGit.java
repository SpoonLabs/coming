package fr.inria.sacha.gitanalyzer.implementation;

import java.util.ArrayList;
import java.util.List;

import comparison.Fragmentable;
import comparison.FragmentableComparator;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
import fr.inria.sacha.gitanalyzer.interfaces.FragmentAnalyzer;


public class FragmentAnalyzerGit implements FragmentAnalyzer {
	
	public static boolean analyzeTest = false;
	
	private FragmentableComparator comparator;
	
	public FragmentAnalyzerGit(FragmentableComparator fragmentComparator) {
		this.comparator = fragmentComparator;
	}

	@Override
	public List<String> getNewFragments(Commit c) throws Exception {
		List<String> newFragments = new ArrayList<String>();
		
		// Retrieve a list of file affected by the commit
		List<FileCommit> javaFiles = c.getJavaFileCommits();
		
		for (FileCommit fileCommit : javaFiles)
			newFragments.addAll(getNewFragments(fileCommit));

		return newFragments;
	}

	@Override
	public Object analyze(Commit commit) {
	  throw new RuntimeException();
	}

	@Override
	public List<String> getNewFragments(FileCommit fileCommit) throws Exception {
		List<String> newFragments = new ArrayList<String>();
		
		String name =  fileCommit.getFileName().replace(".java", "");
		if(!name.toLowerCase().contains("test")){
			Fragmentable fPreviousVersion = comparator.createFragmentable(fileCommit.getPreviousVersion());
			Fragmentable fNextVersion = comparator.createFragmentable(fileCommit.getNextVersion());
			newFragments.addAll(comparator.getAfterDifferences(fPreviousVersion,
				fNextVersion));
		};

		return newFragments;
	}
}
