package fr.inria.coming.changeminer.analyzer;

import java.io.File;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.entity.GranuralityType;
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

	}

	public Diff compareContent(String contentL, String contentR, String nameLeft, String nameRight) throws Exception {

		AstComparator comparator = new AstComparator();
		return comparator.compare(contentL, contentR, nameLeft, nameRight);

	}

	public Diff compareFiles(File contentL, File contentR, GranuralityType granularity) throws Exception {
		AstComparator comparator = new AstComparator();
		return comparator.compare(contentL, contentR);

	}

}
