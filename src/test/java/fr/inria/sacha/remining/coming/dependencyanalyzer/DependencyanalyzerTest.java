package fr.inria.sacha.remining.coming.dependencyanalyzer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.sacha.remining.coming.dependencyanalyzer.util.io.XMLOutputResFile;


public class DependencyanalyzerTest {
	@Test
	public void test() throws Exception {
		XMLOutputResFile result = DependencyAnalyzerMain.main("repogit4testv0", null);
		assertEquals(11, result.getRootNodeCommitList().getChildren().size());
		assertEquals("ab71649c481971a9ad54f04797f5fd9cb133789b", result.getRootNodeCommitList().getChildren().get(0).getAttribute("number").getValue());
		assertEquals(2, result.getRootNodeCommitList().getChildren().get(0).getChild("file").getChild("added-class").getChild("dependencies").getChildren().size());
		assertEquals(9, result.getRootNodeCommitList().getChildren().get(8).getChild("file").getChild("added-class").getChild("dependencies").getChildren().size());
	}	
	
}
