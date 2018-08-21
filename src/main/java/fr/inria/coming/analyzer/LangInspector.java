package fr.inria.coming.analyzer;

import java.util.List;

import fr.inria.coming.analyzer.commitAnalyzer.LangAnalyzer;
import fr.inria.coming.analyzer.commitAnalyzer.LangAnalyzer.CommitInfo;

/**
 * 
 * @author Matias Martinez
 *
 */
public class LangInspector {

	public static int PARAM_GIT_PATH = 0;
	public static int PARAM_MASTER_BRANCH = 1;

	public static void main(String[] args) {

		String repositoryPath, masterBranch;

		repositoryPath = args[PARAM_GIT_PATH];
		masterBranch = args[PARAM_MASTER_BRANCH];

		LangAnalyzer analyzer = new LangAnalyzer();

		List<CommitInfo> ci = (List<CommitInfo>) analyzer.navigateRepo(repositoryPath, masterBranch);
		System.out.println("Results: ");
		for (CommitInfo commitInfo : ci) {
			System.out.println("--> " + commitInfo);
		}
	}

}
