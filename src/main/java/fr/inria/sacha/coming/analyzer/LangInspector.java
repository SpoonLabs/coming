package fr.inria.sacha.coming.analyzer;

import java.util.List;

import fr.inria.sacha.coming.analyzer.commitAnalyzer.LangAnalyzer;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.LangAnalyzer.CommitInfo;

/**
 * 
 * @author Matias Martinez
 *
 */
public class LangInspector {

	public static int PARAM_GIT_PATH = 0;
	public static int PARAM_MASTER_BRANCH = 1;
	public static int PARAM_LABEL = 2;
	public static int PARAM_OP_TYPE = 3;

	public static void main(String[] args) {

		String repositoryPath, masterBranch, label, optype;

		repositoryPath = args[PARAM_GIT_PATH];
		// optype = args[PARAM_OP_TYPE];
		// label = args[PARAM_LABEL];
		masterBranch = "master";// args[PARAM_MASTER_BRANCH];

		LangAnalyzer analyzer = new LangAnalyzer();

		List<CommitInfo> ci = (List<CommitInfo>) analyzer.navigateRepo(repositoryPath, masterBranch);
		System.out.println("Results: ");
		for (CommitInfo commitInfo : ci) {
			System.out.println("--> " + commitInfo);
		}
	}

}
