package fr.inria.coming.changeminer.analyzer.commitAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.difflib.text.DiffRow;
import fr.inria.coming.core.entities.*;
import org.apache.log4j.Logger;
import org.eclipse.compare.rangedifferencer.RangeDifference;

import fr.inria.coming.changeminer.entity.GranuralityType;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.core.filter.diff.syntcomparison.Fragmentable;
import fr.inria.coming.core.filter.diff.syntcomparison.FragmentableComparator;
import fr.inria.coming.core.filter.diff.syntcomparison.LineComparator;
import fr.inria.coming.main.ComingProperties;

/**
 *
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class HunkDifftAnalyzer implements Analyzer<IRevision> {
	FragmentableComparator comparator = new LineComparator();

	Logger log = Logger.getLogger(HunkDifftAnalyzer.class.getName());

	protected GranuralityType granularity;

	/**
	 * 
	 * @param typeLabel     node label to mine
	 * @param operationType operation type to mine
	 */
	public HunkDifftAnalyzer() {
		granularity = GranuralityType.valueOf(ComingProperties.getProperty("GRANULARITY"));
	}

	/**
	 * Analyze a commit finding instances of changes return a Map<FileCommit, List>
	 */
	@SuppressWarnings("rawtypes")
	public AnalysisResult analyze(IRevision revision) {

		List<IRevisionPair> javaFiles = revision.getChildren();

		Map<String, HunkDiff> diffOfFiles = new HashMap<>();

		for (IRevisionPair<String> fileFromRevision : javaFiles) {

			HunkDiff hunks = getNumberChanges(fileFromRevision.getPreviousVersion(), fileFromRevision.getNextVersion());

			if (hunks != null) {
				diffOfFiles.put(fileFromRevision.getNextName(), hunks);
			}
		}
		// TODO: refactor
		List<DiffRow> rows = null;

		return (new DiffResult<IRevision, HunkDiff>(revision, diffOfFiles,rows));
	}

	@Override
	public AnalysisResult analyze(IRevision input, RevisionResult previousResult) {
		// Not considered the previous results in this analyzer.
		return this.analyze(input);
	}

	protected HunkDiff getNumberChanges(String previousVersion, String nextVersion) {
		HunkDiff ranges = new HunkDiff();

		Fragmentable fPreviousVersion = comparator.createFragmentable(previousVersion);
		Fragmentable fNextVersion = comparator.createFragmentable(nextVersion);
		RangeDifference[] results = comparator.compare(fPreviousVersion, fNextVersion);

		for (RangeDifference diffInfo : results) {
			if (diffInfo.kind() != RangeDifference.NOCHANGE /* && diffInfo.kind() != RangeDifference.ANCESTOR */) {
				// TODO: for the moment, ignoring here hunk filtering
				// int length = diff.rightEnd() - diff.rightStart();
				// if (length <= ComingProperties.getPropertyInteger("max_lines_per_hunk"))
				ranges.add(diffInfo);
				String left = "";
				String right = "";

				if (diffInfo.ancestorStart() == 0 && diffInfo.ancestorEnd() == 0
						|| diffInfo.ancestorStart() == 1 && diffInfo.ancestorLength() == 0)
					continue;

				for (int i = diffInfo.ancestorStart(); i < diffInfo.ancestorEnd(); i++) {
					left += fPreviousVersion.getFragment(i) + "\n";
				}

				for (int i = diffInfo.rightStart(); i < diffInfo.rightEnd(); i++) {

					right += fNextVersion.getFragment(i) + "\n";
				}

				ranges.getHunkpairs().add(new HunkPair(left, right));

			}
		}

		return ranges;
	}

}
