package fr.inria.coming.changeminer.entity;

import java.util.Map;

import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;

/**
 * 
 * @author Matias Martinez
 *
 */
public class CommitFinalResult extends FinalResult<Commit> {

	public CommitFinalResult(Map<Commit, RevisionResult> allResults) {
		super(allResults);
	}

}
