package fr.inria.sacha.coming.analyzer.filter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.RangeDifference;

import comparison.Fragmentable;
import comparison.FragmentableComparator;
import comparison.LineComparator;
import fr.inria.sacha.coming.analyzer.Parameters;
import fr.inria.sacha.gitanalyzer.filter.AbstractFilter;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;

public class SyntacticDiffFilter extends AbstractFilter {

	FragmentableComparator comparator = new LineComparator(); // new JavaTokenComparator();
	
	public SyntacticDiffFilter(){
		super();
	};
	
	public SyntacticDiffFilter(CommitSizeFilter sizeFilter) {
		super(sizeFilter);
	}
	@Override
	public boolean acceptCommit(Commit commit) {

		if (super.acceptCommit(commit)) {
			
			List<FileCommit> javaFiles = commit.getJavaFileCommits();
			
			for (FileCommit fileCommit : javaFiles) {
				if (fileCommit.getCompletePath().toLowerCase().contains("test") ||
						fileCommit.getCompletePath().toLowerCase().endsWith("package-info.java")) {
					continue;
				}
				
				List<RangeDifference> nChangesFile = getNumberChanges(
						fileCommit.getPreviousVersion(),
						fileCommit.getNextVersion());

				// First filter by hunks.
				if (nChangesFile.size() == 0) {
					return false;
				}
				if (nChangesFile.size() > Parameters.MAX_HUNKS_PER_FILECOMMIT) {
					return false;
				}
								
			//
			}
		return true;
			
		}
		
		return false;
	
	}
		/**
		 *
		 * @param previousVersion
		 * @param nextVersion
		 * @return
		 */
		protected List<RangeDifference> getNumberChanges(String previousVersion, String nextVersion) {
			List<RangeDifference> ranges = new ArrayList<RangeDifference>();

			Fragmentable fPreviousVersion = comparator.createFragmentable(previousVersion);
			Fragmentable fNextVersion = comparator.createFragmentable(nextVersion);
			RangeDifference[] results = comparator.compare(fPreviousVersion, fNextVersion);

			for (RangeDifference diff : results) {
				if (diff.kind() != RangeDifference.NOCHANGE) {
					int length = diff.rightEnd() - diff.rightStart();
					if (length <= Parameters.MAX_LINES_PER_HUNK)
						ranges.add(diff);
				
				}
			}

			return ranges;
		}
}
