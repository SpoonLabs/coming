package fr.inria.coming.spoon.diffanalyzer;

import org.junit.Test;

import diffanalyzer.DiffICSE15ContextAnalyzer;
import fr.inria.coming.main.ConfigurationProperties;

public class DiffICSE2015Test {

	@Test
	public void testICSE2015() throws Exception {
		DiffICSE15ContextAnalyzer analyzer = new DiffICSE15ContextAnalyzer();
		analyzer.run(ConfigurationProperties.getProperty("icse15difffolder"));
	}

}
