package fr.inria.coming.changeminer.analyzer.filter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.RangeDifference;

import comparison.Fragmentable;
import comparison.FragmentableComparator;
import comparison.LineComparator;
import fr.inria.coming.changeminer.analyzer.Parameters;
import fr.inria.coming.core.filter.AbstractFilter;
import fr.inria.coming.core.interfaces.Commit;
import fr.inria.coming.core.interfaces.FileCommit;
import fr.inria.coming.core.interfaces.IFilter;

/** filters on the number of hunks per file */
public class NbHunkFilter extends AbstractFilter {

	FragmentableComparator comparator = new LineComparator(); // new JavaTokenComparator();
	private int max_included;
	private int min_included;
	
	public NbHunkFilter(int min_included, int max_included){
		super();
		this.max_included = max_included;
		this.min_included = min_included;
	};
	
	public NbHunkFilter(int min_included, int max_included, IFilter other) {
		super(other);
		this.max_included = max_included;
		this.min_included = min_included;
	}
	
	@Override
	public boolean acceptCommit(Commit commit) {

		if (super.acceptCommit(commit)) {
			int nbHunks = 0;
			List<FileCommit> javaFiles = commit.getJavaFileCommits();
			
			for (FileCommit fileCommit : javaFiles) {
				
				List<RangeDifference> hunks = getNumberChanges(
						fileCommit.getPreviousVersion(),
						fileCommit.getNextVersion());
				nbHunks+=hunks.size();
			}


			if (nbHunks >= min_included && nbHunks <= max_included ) {	
				return true;
			}
			
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
