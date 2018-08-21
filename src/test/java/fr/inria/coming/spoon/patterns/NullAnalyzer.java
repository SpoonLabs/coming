package fr.inria.coming.spoon.patterns;

import java.util.HashMap;

import fr.inria.gitanalyzer.interfaces.Commit;
import fr.inria.gitanalyzer.interfaces.CommitAnalyzer;

public class NullAnalyzer implements CommitAnalyzer {
	@Override
	public Object analyze(Commit commit) {
		HashMap hashMap = new HashMap();
		hashMap.put(commit, "bar");// the result shound not be empty
		return hashMap;
	}
}