package fr.inria.coming.changeminer.analyzer.commitAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.analyzer.DiffEngineFacade;
import fr.inria.coming.changeminer.analyzer.Parameters;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.filters.SimpleChangeFilter;
import fr.inria.coming.changeminer.entity.GranuralityType;
import fr.inria.coming.changeminer.util.ConfigurationProperties;
import fr.inria.coming.core.interfaces.Commit;
import fr.inria.coming.core.interfaces.CommitAnalyzer;
import fr.inria.coming.core.interfaces.FileCommit;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

/**
 * Commit analyzer: It searches fine grain changes.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class FineGrainChangeCommitAnalyzer implements CommitAnalyzer {

	Logger log = Logger.getLogger(FineGrainChangeCommitAnalyzer.class.getName());

	protected GranuralityType granularity;

	IChangesProcessor processor;

	public FineGrainChangeCommitAnalyzer(SimpleChangeFilter filter, GranuralityType granularity) {

		this.processor = filter;
		this.granularity = granularity;

	}

	public FineGrainChangeCommitAnalyzer(SimpleChangeFilter filter) {

		this.processor = filter;

		granularity = GranuralityType.valueOf(ConfigurationProperties.getProperty("GRANULARITY"));
	}

	/**
	 * 
	 * @param typeLabel
	 *            node label to mine
	 * @param operationType
	 *            operation type to mine
	 */
	public FineGrainChangeCommitAnalyzer() {
		this.processor = null;
		granularity = GranuralityType.valueOf(ConfigurationProperties.getProperty("GRANULARITY"));
	}

	/**
	 * Analyze a commit finding instances of changes return a Map<FileCommit,
	 * List>
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object analyze(Commit commit) {

		// Retrieve a list of file affected by the commit
		List<FileCommit> javaFiles = commit.getJavaFileCommits();

		// System.out.println("files "+javaFiles);
		int nChanges = 0;

		// The result is divided by File from the commit.
		List info = new ArrayList();

		System.out.println("\n*****\nCommit: " + commit.getName());

		for (FileCommit fileCommit : javaFiles) {
			if (fileCommit.getCompletePath().toLowerCase().contains("test")
					|| fileCommit.getCompletePath().toLowerCase().endsWith("package-info.java")) {

				continue;
			}

			String left = fileCommit.getPreviousVersion();
			String right = fileCommit.getNextVersion();

			if (!left.trim().isEmpty()) {

				List<Operation> operations;

				try {
					Diff diff = this.compareContent(left, right, granularity);
					// todo
					operations = // diff.getAllActions();
							// diff.getAllOperations();
							diff.getRootOperations();
					String name = commit.getName();
					// System.out.println(name + actions);
					if (operations == null || operations.size() > Parameters.MAX_AST_CHANGES_PER_FILE
							|| operations.size() < Parameters.MIN_AST_CHANGES_PER_FILE) {
						continue;
					}

					if (operations.size() > 0) {

						List<Operation> filterActions = processCommit(diff);
						nChanges += filterActions.size();

						if (filterActions.size() > 0)
							info.addAll(filterActions);
					}
				} catch (Exception e) {
					log.error("Exception e: " + e);
					e.printStackTrace();
					throw new RuntimeException(e);

				}
			}
		}

		HashMap res = new HashMap();
		res.put(commit, info);
		return res;
	}

	private List<Operation> processCommit(Diff diff) {

		if (this.processor == null)
			return diff.getRootOperations();

		return this.processor.process(diff);
	}

	public Diff compareContent(String left, String right, GranuralityType granularity) throws Exception {

		DiffEngineFacade cdiff = new DiffEngineFacade();
		return cdiff.compareContent(left, right, granularity);

	}

}