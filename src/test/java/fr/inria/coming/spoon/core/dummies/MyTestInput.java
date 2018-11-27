package fr.inria.coming.spoon.core.dummies;

import java.util.ArrayList;

import fr.inria.coming.core.engine.RevisionNavigationExperiment;
import fr.inria.coming.core.entities.RevisionDataset;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.extensionpoints.navigation.InOrderRevisionNavigation;

public class MyTestInput extends RevisionNavigationExperiment<Commit> {

	public MyTestInput() {
		// By default, in order.
		super(new InOrderRevisionNavigation<Commit>());
	}

	@Override
	public RevisionDataset<Commit> loadDataset() {
		return new RevisionDataset(new ArrayList());
	}

}
