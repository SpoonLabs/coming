package fr.inria.coming.analyzer.filter;

import java.util.List;

import fr.inria.coming.analyzer.Parameters;
import fr.inria.gitanalyzer.filter.AbstractFilter;
import fr.inria.gitanalyzer.filter.IFilter;
import fr.inria.gitanalyzer.interfaces.Commit;
import fr.inria.gitanalyzer.interfaces.FileCommit;

public class ContainTestFilterFilter extends AbstractFilter {

	public ContainTestFilterFilter() {
		super();
	
	}

	public ContainTestFilterFilter(IFilter parentFilter) {
		super(parentFilter);
		
	}

	@Override
	public boolean acceptCommit(Commit commit) {

		if (super.acceptCommit(commit)) {

			// Retrieve a list of file affected by the commit
			List<FileCommit> javaFiles = commit.getJavaFileCommits();
			int countJava = 0, nTests = 0;

			for (FileCommit fileCommit : javaFiles) {
				if (!fileCommit.getCompletePath().toLowerCase().endsWith("package-info.java"))
					countJava++;
				if (fileCommit.getCompletePath().toLowerCase().contains("test"))
					nTests++;

			}
			
			if (Parameters.ONLY_COMMIT_WITH_TEST_CASE && nTests == 0){
				return false; 
			}
			//Finally, we accept the commit.
			return true;
		}
		return false;
	}

}
