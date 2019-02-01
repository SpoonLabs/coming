package fr.inria.coming.codefeatures;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.LineFilter;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FeatureAnalyzer implements Analyzer<IRevision> {

	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	private static final LineFilter FILTER = new LineFilter();

	protected CodeFeatureDetector cresolver = new CodeFeatureDetector();

	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {

		AnalysisResult resultFromDiffAnalysis = previousResults.getResultFromClass(FineGrainDifftAnalyzer.class);

		if (resultFromDiffAnalysis == null) {
			System.err.println("Error Diff must be executed before");
			throw new IllegalArgumentException("Error: missing diff");
		}

		DiffResult diffResut = (DiffResult) resultFromDiffAnalysis;
		List<Cntx> allContext = new ArrayList<>();

		for (Object value : diffResut.getDiffOfFiles().values()) {

			Diff diff = (Diff) value;

			List<Operation> ops = diff.getRootOperations();

			for (Operation operation : ops) {
				CtElement affectedCtElement = getLeftElement(operation);

				if (affectedCtElement != null) {
					Cntx iContext = cresolver.analyzeFeatures(affectedCtElement);
					allContext.add(iContext);
				}

			}
		}

		return new FeaturesResult(revision, allContext);
	}

	@SuppressWarnings("unchecked")
	public JsonArray processFilesPair(File pairFolder) {
		Map<String, Diff> diffOfcommit = new HashMap();

		JsonArray filesArray = new JsonArray();
		for (File fileModif : pairFolder.listFiles()) {
			int i_hunk = 0;

			if (".DS_Store".equals(fileModif.getName()))
				continue;

			String pathname = fileModif.getAbsolutePath() + File.separator + pairFolder.getName() + "_"
					+ fileModif.getName();

			File previousVersion = new File(pathname + "_s.java");
			if (!previousVersion.exists()) {
				pathname = pathname + "_" + i_hunk;
				previousVersion = new File(pathname + "_s.java");
				if (!previousVersion.exists())
					continue;
			}

			File postVersion = new File(pathname + "_t.java");
			i_hunk++;

			JsonObject file = new JsonObject();
			try {
				filesArray.add(file);
				file.addProperty("file_name", fileModif.getName());
				JsonArray changesArray = new JsonArray();
				file.add("features", changesArray);

				AstComparator comparator = new AstComparator();

				Diff diff = comparator.compare(previousVersion, postVersion);
				if (diff == null) {
					file.addProperty("status", "differror");
					continue;
				}

				log.info("--diff: " + diff);

				List<Operation> ops = diff.getRootOperations();
				String key = fileModif.getParentFile().getName() + "_" + fileModif.getName();
				diffOfcommit.put(key, diff);

				for (Operation operation : ops) {
					CtElement affectedCtElement = getLeftElement(operation);

					if (affectedCtElement != null) {
						Cntx iContext = cresolver.analyzeFeatures(affectedCtElement);
						changesArray.add(iContext.toJSON());
					}
				}

			} catch (Throwable e) {
				log.error("error with " + previousVersion);
				log.error(e);
				file.addProperty("status", "exception");
			}

		}
		return filesArray;
	}

	/**
	 * Get the element that is modified
	 * 
	 * @param operation
	 * @return
	 */
	public CtElement getLeftElement(Operation operation) {

		CtElement affectedCtElement = null;

		if (operation instanceof MoveOperation) {

			// Element to move in source
			CtElement affectedMoved = operation.getSrcNode();

			affectedCtElement = affectedMoved;

		} else if (operation instanceof InsertOperation) {

			CtElement oldLocation = ((InsertOperation) operation).getParent();

			affectedCtElement = oldLocation;

		} else if (operation instanceof DeleteOperation) {

			CtElement oldLocation = operation.getSrcNode();

			affectedCtElement = oldLocation;

		} else if (operation instanceof UpdateOperation) {

			CtElement oldLocation = operation.getSrcNode();

			affectedCtElement = oldLocation;
		}
		// Let's find the parent statement
		CtStatement parentLine = affectedCtElement.getParent(FILTER);
		if (parentLine != null)
			return parentLine;
		// by default, we return the affected element
		return affectedCtElement;

	}

}
