package fr.inria.coming.changeminer.analyzer.commitAnalyzer;

import java.util.List;

import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

public interface IChangesProcessor {

	/**
	 * Method called at the beginning of the process
	 */
	public void init();

	/**
	 * Process the differences of each commits
	 * 
	 * @param diff
	 * @return
	 */
	public List<Operation> process(Diff diff);

	/**
	 * Method called at the ending of the process
	 */
	public void end();

}
