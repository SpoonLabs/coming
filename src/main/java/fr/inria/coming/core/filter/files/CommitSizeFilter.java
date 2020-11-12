package fr.inria.coming.core.filter.files;

import java.util.List;

import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.filter.AbstractChainedFilter;
import fr.inria.coming.main.ComingProperties;

public class CommitSizeFilter extends AbstractChainedFilter<Commit> {

	public CommitSizeFilter() {
		super();

	}

	public CommitSizeFilter(IFilter parentFilter) {
		super(parentFilter);

	}

	@Override
	public boolean accept(Commit commit) {

		if (super.accept(commit)) {

			// Retrieve a list of file affected by the commit
			List<FileCommit> javaFiles = commit.getJavaFileCommits();
			int countJava = 0, nTests = 0;

			for (FileCommit fileCommit : javaFiles) {
				if (!fileCommit.getNextName().toLowerCase().endsWith("package-info.java"))
					countJava++;
				if (fileCommit.getNextName().toLowerCase().contains("test"))
					nTests++;

			}

			if (countJava > ComingProperties.getPropertyInteger("max_files_per_commit")) {
				// System.out.println("Commit not accepted, many files in the commit");
				// log.info("-----");
				return false;
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
