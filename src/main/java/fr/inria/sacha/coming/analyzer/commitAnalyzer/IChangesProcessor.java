package fr.inria.sacha.coming.analyzer.commitAnalyzer;

import java.util.List;

import fr.inria.sacha.spoon.diffSpoon.CtDiff;
import com.github.gumtreediff.actions.model.Action;

public interface IChangesProcessor {

	/**
	 * Method called at the beginning of the process
	 */
	public void init();
	
	/**
	 * Process the differences of each commits
	 * @param diff
	 * @return
	 */
	public List<Action> process(CtDiff  diff);

	/**
	 * Method called at the ending of the process 
	 */
	public void end();
	
}
