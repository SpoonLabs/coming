package fr.inria.sacha.coming.analyzer;

import java.io.File;

import org.apache.log4j.Logger;

import fr.inria.sacha.coming.entity.GranuralityType;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;

/**
 * Facade for GumTree functionality. Fine granularity comparison between two
 * files according to a given granularity (JDT, CD, Spoon). It uses GT Matching
 * algorithm.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */

public class DiffEngineFacade {

	private Logger log = Logger.getLogger(DiffEngineFacade.class.getName());

	public Diff compareContent(String contentL, String contentR, GranuralityType granularity) throws Exception {

		AstComparator comparator = new AstComparator();
		return comparator.compare(contentL, contentR);
		// final SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
		// Diff diffEngine = new DiffImpl(contentL, contentR);

		// return diffEngine.compare(contentL, contentR);

	};

	public Diff compareFiles(File contentL, File contentR, GranuralityType granularity) throws Exception {
		AstComparator comparator = new AstComparator();
		return comparator.compare(contentL, contentR);

	};

}
