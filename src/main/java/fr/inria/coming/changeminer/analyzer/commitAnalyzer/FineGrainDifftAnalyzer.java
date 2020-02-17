package fr.inria.coming.changeminer.analyzer.commitAnalyzer;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

import fr.inria.coming.changeminer.entity.GranuralityType;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.main.ComingProperties;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

/**
 * Commit analyzer: It searches fine grain changes.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class FineGrainDifftAnalyzer implements Analyzer<IRevision> {
//	private static final int MAX_FILE_SIZE = 1000000;

	Logger log = Logger.getLogger(FineGrainDifftAnalyzer.class.getName());

	protected AstComparator cdiff = null;

	protected GranuralityType granularity;

	protected boolean includeComments = false;

	/**
	 *
	 */
	public FineGrainDifftAnalyzer() {
		granularity = GranuralityType.valueOf(ComingProperties.getProperty("GRANULARITY"));
		this.includeComments = ComingProperties.getPropertyBoolean("processcomments");
		cdiff = new AstComparator(includeComments);
	}

	/**
	 * Analyze a commit finding instances of changes return a Map<FileCommit, List>
	 */
	@SuppressWarnings("rawtypes")
	public AnalysisResult analyze(IRevision revision) {

		List<IRevisionPair> javaFiles = revision.getChildren();

		Map<String, Diff> diffOfFiles = new HashMap<>();

		List<DiffRow> rows = null;

		log.info("\n*****\nCommit: " + revision.getName());

		for (IRevisionPair<String> fileFromRevision : javaFiles) {

			String left = fileFromRevision.getPreviousVersion();
			String right = fileFromRevision.getNextVersion();

			String leftName = fileFromRevision.getPreviousName();
			String rightName = fileFromRevision.getName();

			Diff diff = compare(left, right, leftName, rightName);
			if (diff != null) {
				diffOfFiles.put(fileFromRevision.getName(), diff);
			}

			DiffRowGenerator generator = DiffRowGenerator.create().showInlineDiffs(false).inlineDiffByWord(false)
					.ignoreWhiteSpaces(true).build();

			try {
				rows = generator.generateDiffRows(
						Arrays.stream(fileFromRevision.getPreviousVersion().split("\n")).collect(Collectors.toList()),
						Arrays.stream(fileFromRevision.getNextVersion().split("\n")).collect(Collectors.toList()));
			} catch (DiffException e) {
				e.printStackTrace();
			}

//			System.out.println("Diff of the revision");
//			for (DiffRow row : rows) {
//				switch (row.getTag()) {
//					case INSERT:
//						System.out.println("+ " + valueOf(row.getNewLine()));
//						break;
//					case DELETE:
//						System.out.println("- " + valueOf(row.getOldLine()));
//						break;
//					case CHANGE:
//						System.out.println("- " + valueOf(row.getOldLine()));
//						System.out.println("+ " + valueOf(row.getNewLine()));
//						break;
//				}
//			}

		}

		return (new DiffResult<IRevision, Diff>(revision, diffOfFiles, rows));
	}

	@Override
	public AnalysisResult analyze(IRevision input, RevisionResult previousResult) {
		// Not considered the previous results in this analyzer.
		return this.analyze(input);
	}

	public Diff compare(String left, String right) {
		return this.compare(left, right, "leftFile", "rightFile");
	}

	public Diff compare(String left, String right, GranuralityType granularity) {
		return this.compare(left, right, "leftFile", "rightFile");
	}

	public Diff compare(String left, String right, String leftName, String rightName) {
		if (!left.trim().isEmpty()) {

			List<Operation> operations;

			try {
				
				Diff diff = cdiff.compare(left, right, leftName, rightName);

				operations = diff.getRootOperations();

				if (operations == null
						|| operations.size() > ComingProperties.getPropertyInteger("MAX_AST_CHANGES_PER_FILE")
						|| operations.size() < ComingProperties.getPropertyInteger("MIN_AST_CHANGES_PER_FILE")) {
					log.debug(
							"FileRevision with Max number of Root AST Changes. Discating it. Total:" + operations.size()
									+ " max: " + ComingProperties.getPropertyInteger("MAX_AST_CHANGES_PER_FILE"));
					return null;
				}

				if (operations.size() > 0) {

					return diff;
				}
			} catch (Exception e) {
				log.error("Exception e: " + e);
				e.printStackTrace();

			}
		}
		return null;
	}

	public Diff getDiff(File left, File right) throws Exception {
		AstComparator localdiff = new AstComparator(ComingProperties.getPropertyBoolean("processcomments"));
		Diff d = localdiff.compare(left, right);
		return d;
	}

	public Diff getDiff(File left, File right, boolean includeComment) throws Exception {
		AstComparator localdiff = new AstComparator(includeComment);
		Diff d = localdiff.compare(left, right);
		return d;
	}

	private Future<Diff> getDiffInFuture(ExecutorService executorService, File left, File right) {

		Future<Diff> future = executorService.submit(() -> {
			AstComparator cdiff = new AstComparator(this.includeComments);
			Diff d = cdiff.compare(left, right);
			return d;
		});
		return future;
	}

	public Diff getdiffFuture(File left, File right) throws Exception {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Future<Diff> future = getDiffInFuture(executorService, left, right);

		Diff resukltDiff = null;
		try {
			resukltDiff = future.get(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) { // <-- possible error cases
			log.error("job was interrupted");
		} catch (ExecutionException e) {
			log.error("caught exception: " + e.getCause());
		} catch (TimeoutException e) {
			log.error("timeout");
		}

		executorService.shutdown();
		return resukltDiff;

	}

}