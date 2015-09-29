package fr.inria.sacha.coming.util.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import net.lingala.zip4j.core.ZipFile;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import fr.inria.sacha.coming.analyzer.Parameters;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
import fr.labri.gumtree.actions.model.Action;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class GitRepository4Test {

	protected static String repoPath;
	protected Path temp;
	
	public String getRepoName(){
		return "/repogit4testv0.zip";
	}
	
	@Before
	public void setUpGitRepo4Test() {
		try {

			Parameters.setUpProperties();
			
			File fl = new File(getClass().getResource(getRepoName())
					.getFile());

			ZipFile zipFile = new ZipFile(fl.getAbsolutePath());

			temp = Files.createTempDirectory("tempRepo4Test");
			temp.toFile().deleteOnExit();
			zipFile.extractAll(temp.toString());

			repoPath = temp.toString() + File.separator + "repogit4testv0";

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void end() throws IOException {
		FileUtils.deleteDirectory(temp.toFile());

	}
	
	
	public static boolean containsCommit(Map<FileCommit, List> instancesFound,
			String commit) {
		for (FileCommit fc : instancesFound.keySet()) {
			if (fc.getCommit().getName().equals(commit))
				return true;
		}
		return false;
	}

	protected boolean containsCommit(Map<FileCommit, 
			List> instancesFound,
			String commit, String typeLabel) {
		for (FileCommit fc : instancesFound.keySet()) {
			if (fc.getCommit().getName().equals(commit)) {
				List<Action> actions = instancesFound.get(fc);
				for (Action action : actions) {
					if (action.getNode().getTypeLabel().equals(typeLabel))
						return true;

				}
			}
		}
		return false;
	}
	
}
