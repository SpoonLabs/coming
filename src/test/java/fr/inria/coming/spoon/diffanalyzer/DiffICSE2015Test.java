package fr.inria.coming.spoon.diffanalyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import diffanalyzer.DiffICSE15ContextAnalyzer;
import fr.inria.coming.main.ConfigurationProperties;
import gumtree.spoon.AstComparator;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtPath;

public class DiffICSE2015Test {

	@Test
	public void testICSE2015() throws Exception {
		DiffICSE15ContextAnalyzer analyzer = new DiffICSE15ContextAnalyzer();
		analyzer.run(ConfigurationProperties.getProperty("icse15difffolder"));
	}

	@Test
	public void test_Path_from_Spoon() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/main/resources/diffcases/1139461/WildcardQuery/1139461_WildcardQuery_0_s.java");

		CtType<?> astLeft = diff.getCtType(fl);

		assertNotNull(astLeft);
		assertEquals("WildcardQuery", astLeft.getSimpleName());

		CtPath pathLeft = astLeft.getPath();
		assertNotNull(pathLeft);

	}

}
