package fr.inria.coming.spoon.core.dummies;

import java.util.ArrayList;
import java.util.Collection;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.core.engine.RevisionNavigationExperiment;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.extensionpoints.navigation.InOrderRevisionNavigation;

public class MyTestInput extends RevisionNavigationExperiment<Commit> {

	public MyTestInput() {
		// By default, in order.
		super(new InOrderRevisionNavigation<Commit>());
	}

	@Override
	protected FinalResult processEnd() {
		return null;
	}

	@Override
	public Collection<Commit> loadDataset() {
		return new ArrayList();
	}

	@Override
	public void processEndRevision(Commit element, RevisionResult resultAllAnalyzed) {

	}

}
