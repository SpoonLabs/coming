package fr.inria.coming.spoon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.json.simple.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import fr.inria.coming.spoon.patterns.GitRepository4Test;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.LangAnalyzer;
import fr.inria.sacha.coming.analyzer.commitAnalyzer.LangAnalyzer.CommitInfo;
import fr.inria.sacha.coming.util.ConfigurationProperties;

/**
 * 
 * @author Matias Martinez
 *
 */
@Ignore
public class LangInspectorTest extends GitRepository4Test {

	@Test
	public void testLangInspectorTestRepo() {

		LangAnalyzer analyzer = new LangAnalyzer();

		List<CommitInfo> ci = (List<CommitInfo>) analyzer.navigateRepo(this.repoPath, "master");

		assertTrue(ci.size() > 0);
		System.out.println("Results: ");
		for (CommitInfo commitInfo : ci) {
			System.out.println("--> " + commitInfo);
		}

		JSONObject json = analyzer.resultToJSON();
		assertNotNull(json);

		System.out.println(json);

		CommitInfo cfe = getCommit(ci, "fe76");

		assertEquals(290, (int) cfe.getStats().get("Java")[3]);

		CommitInfo c60b5 = getCommit(ci, "60b5");
		assertTrue(c60b5.getStats().isEmpty());
		assertEquals(0, c60b5.getNrCommit());

		CommitInfo cab71 = getCommit(ci, "ab71");

		assertEquals(84, (int) cab71.getStats().get("Java")[3]);
		assertEquals(1, cab71.getNrCommit());

		CommitInfo cab4120 = getCommit(ci, "4120");

		assertEquals(85, (int) cab4120.getStats().get("Java")[3]);
		assertEquals(3, cab4120.getNrCommit());

		CommitInfo cabc6b1 = getCommit(ci, "c6b1");

		assertEquals(294, (int) cabc6b1.getStats().get("Java")[3]);

	}

	@Test
	public void testLangInspectorKTutorialsRepo() {
		// https://github.com/enbandari/Kotlin-Tutorials
		/// Users/matias/develop/testkotlin/Kotlin-Tutorials
		// d5a487298d55197329acaa2bbfcdd922555457c6
		LangAnalyzer analyzer = new LangAnalyzer();

		String repositoryPath = ConfigurationProperties.getProperty("TEST_REPO_KOTLIN-TUTORIALS");
		if (repositoryPath == null || !(new File(repositoryPath)).exists()) {
			System.out.println("Path not found " + repositoryPath);
			return;
		}
		List<CommitInfo> ci = (List<CommitInfo>) analyzer.navigateRepo(repositoryPath, "master");
		System.out.println("Results: ");
		for (CommitInfo commitInfo : ci) {
			System.out.println("--> " + commitInfo);
		}

		// f7c91f0c148164adf83ab37cb6813ce2241f28d5
		CommitInfo cabcf7c91 = getCommit(ci, "f7c91");

		assertEquals(2, (int) cabcf7c91.getStats().get("Markdown")[3]);

		assertEquals(1, cabcf7c91.getStats().keySet().size());

		// e1ba4d4b26588bc313154e49eb7bcaf0d244239f
		CommitInfo ce1ba4 = getCommit(ci, "e1ba4");

		assertEquals(37, (int) ce1ba4.getStats().get("Markdown")[3]);
		assertEquals(10, (int) ce1ba4.getStats().get("Kotlin")[3]);
		assertEquals(2, ce1ba4.getStats().keySet().size());

		JSONObject json = analyzer.resultToJSON();
		assertNotNull(json);

		System.out.println(json);

	}

	private CommitInfo getCommit(List<CommitInfo> ci, String commitid) {
		return ci.stream().filter(e -> e.getCommitid().startsWith(commitid)).findAny().get();
	}

}
