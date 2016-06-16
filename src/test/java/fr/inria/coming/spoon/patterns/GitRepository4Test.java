package fr.inria.coming.spoon.patterns;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import net.lingala.zip4j.core.ZipFile;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import fr.inria.sacha.coming.analyzer.Parameters;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
import fr.inria.sacha.spoon.diffSpoon.SpoonGumTreeBuilder;

import com.github.gumtreediff.actions.model.Action;

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
			
			URL resource = getClass().getResource(getRepoName());
			File fl = new File(resource
					.getFile());

			ZipFile zipFile = new ZipFile(fl.getAbsolutePath());

			temp = Files.createTempDirectory("tempRepo4Test");
			temp.toFile().deleteOnExit();
			zipFile.extractAll(temp.toString());

			repoPath = temp.toString() + File.separator + "repogit4testv0";

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@After
	public void end() throws IOException {
		FileUtils.deleteDirectory(temp.toFile());

	}
	
	
	public static boolean containsCommit(Map<Commit, List> instancesFound,
			String commit) {
		for (Commit c : instancesFound.keySet()) {
				if (c.getName().equals(commit))
					return true;
			
		}
		return false;
	}

	protected boolean containsCommit(Map<Commit, 
			List> instancesFound,
			String commit, String typeLabel) {
		for (Commit c : instancesFound.keySet()) {
			for (FileCommit fc : c.getFileCommits()) {//TODO: Matias: FC not used??
				List<Action> actions = instancesFound.get(c);
				for (Action action : actions) {
					String type = SpoonGumTreeBuilder.gtContext.getTypeLabel(action.getNode().getType());
					if (type != null && type.equals(typeLabel))
						return true;

				}
			}
		}
		return false;
	}
	
}
