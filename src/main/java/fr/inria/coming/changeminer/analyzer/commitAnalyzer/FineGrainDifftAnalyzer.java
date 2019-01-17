package fr.inria.coming.changeminer.analyzer.commitAnalyzer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.analyzer.DiffEngineFacade;
import fr.inria.coming.changeminer.entity.GranuralityType;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.main.ComingProperties;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

/**
 * Commit analyzer: It searches fine grain changes.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class FineGrainDifftAnalyzer implements Analyzer<IRevision> {

	Logger log = Logger.getLogger(FineGrainDifftAnalyzer.class.getName());

	protected GranuralityType granularity;

	/**
	 * 
	 * @param typeLabel     node label to mine
	 * @param operationType operation type to mine
	 */
	public FineGrainDifftAnalyzer() {
		granularity = GranuralityType.valueOf(ComingProperties.getProperty("GRANULARITY"));
	}

	/**
	 * Analyze a commit finding instances of changes return a Map<FileCommit, List>
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public AnalysisResult<IRevision> analyze(IRevision revision) {

		List<IRevisionPair> javaFiles = revision.getChildren();

		Map<String, Diff> diffOfFiles = new HashMap<>();

		System.out.println("\n*****\nCommit: " + revision.getName());

		for (IRevisionPair<String> fileFromRevision : javaFiles) {

			String left = fileFromRevision.getPreviousVersion();
			String right = fileFromRevision.getNextVersion();

			Diff diff = compare(left, right);
			if (diff != null) {
				diffOfFiles.put(fileFromRevision.getName(), diff);
			}
		}

		return new DiffResult<IRevision, Diff>(revision, diffOfFiles);
	}

	@Override
	public AnalysisResult analyze(IRevision input, RevisionResult previousResult) {
		// Not considered the previous results in this analyzer.
		return this.analyze(input);
	}

	public Diff compare(String left, String right) {
		if (!left.trim().isEmpty()) {

			List<Operation> operations;

			try {
				Diff diff = this.compareContent(left, right, granularity);

				operations = diff.getRootOperations();

				// TODO: Move to filter?
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

	public Diff compareContent(String left, String right, GranuralityType granularity) throws Exception {

		DiffEngineFacade cdiff = new DiffEngineFacade();
		return cdiff.compareContent(left, right, granularity);

	}

	public Diff getDiff(File left, File right) throws Exception {

		DiffEngineFacade cdiff = new DiffEngineFacade();
		Diff d = cdiff.compareFiles(left, right, GranuralityType.SPOON);
		return d;
	}

	private Future<Diff> getDiffInFuture(ExecutorService executorService, File left, File right) {

		Future<Diff> future = executorService.submit(() -> {
			DiffEngineFacade cdiff = new DiffEngineFacade();
			Diff d = cdiff.compareFiles(left, right, GranuralityType.SPOON);
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
			System.out.println("job was interrupted");
		} catch (ExecutionException e) {
			System.out.println("caught exception: " + e.getCause());
		} catch (TimeoutException e) {
			System.out.println("timeout");
		}

		executorService.shutdown();
		return resukltDiff;

	}

}