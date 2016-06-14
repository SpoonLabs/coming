package fr.inria.sacha.remining.coming.dependencyanalyzer;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
import fr.inria.sacha.gitanalyzer.object.RepositoryPGit;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Class;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Class.ClassType;
import fr.inria.sacha.remining.coming.dependencyanalyzer.spoonanalyzer.Analyzer;
import fr.inria.sacha.remining.coming.dependencyanalyzer.util.io.ResourceFile;
import fr.inria.sacha.remining.coming.dependencyanalyzer.util.io.XMLOutputResFile;
import fr.inria.sacha.remining.coming.dependencyanalyzer.util.tool.DepTool;


public class DependencyanalyzerTest {
	@Test
	public void test() throws Exception {
		XMLOutputResFile result = DependencyAnalyzerMain.main("repogit4testv0", null);
		result.save();
		assertEquals(11, result.getRootNodeCommitList().getChildren().size());
		assertEquals("ab71649c481971a9ad54f04797f5fd9cb133789b", result.getRootNodeCommitList().getChildren().get(0).getAttribute("number").getValue());
		assertEquals(2, result.getRootNodeCommitList().getChildren().get(0).getChild("file").getChild("added-class").getChild("dependencies").getChildren().size());
		assertEquals(9, result.getRootNodeCommitList().getChildren().get(8).getChild("file").getChild("added-class").getChild("dependencies").getChildren().size());
	}	
	
}
