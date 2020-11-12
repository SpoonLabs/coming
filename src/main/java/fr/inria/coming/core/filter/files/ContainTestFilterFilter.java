package fr.inria.coming.core.filter.files;

import java.util.List;

import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.filter.AbstractChainedFilter;
import fr.inria.coming.main.ComingProperties;

public class ContainTestFilterFilter extends AbstractChainedFilter<Commit> {

	public ContainTestFilterFilter() {
		super();

	}

	public ContainTestFilterFilter(IFilter parentFilter) {
		super(parentFilter);

	}

	@Override
	public boolean accept(Commit commit) {

		if (super.accept(commit)) {

			// Retrieve a list of file affected by the commit
			List<FileCommit> javaFiles = commit.getJavaFileCommits();
			int nTests = 0;

			for (FileCommit fileCommit : javaFiles) {

				if (fileCommit.getNextName().toLowerCase().contains("test"))
					nTests++;
			}

			if (ComingProperties.getPropertyBoolean("ONLY_COMMIT_WITH_TEST_CASE") && nTests == 0) {
				return false;
			}
			// Finally, we accept the commit.
			return true;
		}
		return false;
	}

}
