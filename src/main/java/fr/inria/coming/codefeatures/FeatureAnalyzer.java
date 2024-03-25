package fr.inria.coming.codefeatures;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import add.entities.RepairPatterns;
import add.features.detector.repairpatterns.AbstractPatternDetector;
import add.features.detector.repairpatterns.CodeMovingDetector;
import add.features.detector.repairpatterns.ConditionalBlockDetector;
import add.features.detector.repairpatterns.ConstantChangeDetector;
import add.features.detector.repairpatterns.CopyPasteDetector;
import add.features.detector.repairpatterns.ExpressionFixDetector;
import add.features.detector.repairpatterns.MissingNullCheckDetector;
import add.features.detector.repairpatterns.RepairPatternDetector;
import add.features.detector.repairpatterns.SingleLineDetector;
import add.features.detector.repairpatterns.WrapsWithDetector;
import add.features.detector.repairpatterns.WrongReferenceDetector;
import add.main.Config;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.gumtreediff.actions.EditScript;
import com.google.gson.Gson;
import fr.inria.coming.core.engine.files.FileDiff;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.HunkDifftAnalyzer;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.HunkDiff;
import fr.inria.coming.core.entities.HunkPair;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.output.JSonPatternInstanceOutput;
import fr.inria.coming.main.ComingProperties;
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
		JsonArray filesArray = new JsonArray();
		DiffResult diffResut = (DiffResult) resultFromDiffAnalysis;


		for (Object nameFile : diffResut.getDiffOfFiles().keySet()) {
			System.out.println("Analyzing file: " + nameFile);
			Diff diff = (Diff) diffResut.getDiffOfFiles().get(nameFile);

			List<Operation> ops = diff.getRootOperations();

			JsonObject file = new JsonObject();

			filesArray.add(file);
			file.addProperty("file_name", nameFile.toString());
			putCodeFromHunk(previousResults, nameFile, file);
			JsonArray changesArray = new JsonArray();
			file.add("features", changesArray);

			for (Operation operation : ops) {
				CtElement affectedCtElement = getLeftElement(operation);

				if (affectedCtElement != null) {
					Cntx iContext = cresolver.analyzeFeatures(affectedCtElement);
					if (iContext != null) {
						JsonObject jsonFeature = iContext.toJSON();

						if (ComingProperties.getPropertyBoolean("addchangeinfoonfeatures")) {
							JsonObject opjson = JSonPatternInstanceOutput.getJSONFromOperator(operation);
							jsonFeature.add("ast_info", opjson);
						}
						
						changesArray.add(jsonFeature);
					}
				}
				
			}
			
			FeaturesResult p4jfeatures = (FeaturesResult) new P4JFeatureAnalyzer().analyze(revision, previousResults);
			if(p4jfeatures!=null) {
				changesArray.add(p4jfeatures.getFeatures());
			}
			
			try {
				// generate unified diff
				File tempFile = File.createTempFile("add_", ".diff");
				try (FileWriter sb = new FileWriter(tempFile)) {
					for (IRevisionPair<String> fileFromRevision : revision.getChildren()) {
						String previousVersion = fileFromRevision.getPreviousVersion();
						String nextVersion = fileFromRevision.getNextVersion();
						List<String> strings = UnifiedDiffUtils.generateUnifiedDiff(fileFromRevision.getPreviousName(), fileFromRevision.getNextName(), Arrays.asList(previousVersion.split("\n")), DiffUtils.diff(previousVersion, nextVersion, null), 0);
						sb.append(String.join("\n", strings));
					}
				}

				// Analyze the diff and extract all the patterns of ADD
				Config config = new Config();
				config.setDiffPath(tempFile.getAbsolutePath());
				config.setBuggySourceDirectoryPath(revision.getFolder());
				RepairPatterns analyze =analyze(diff, config);

				changesArray.add(new Gson().fromJson(analyze.toJson().toString(), JsonObject.class));
				tempFile.delete();

				if (revision instanceof FileDiff) {
					// TODO: generalize the implementation of RepairPatternFeatureAnalyzer and P4JFeatureAnalyzer with IRevision.getChildren instead of hard coding FileDiff
					//add more features
					JsonObject patternJson = RepairPatternFeatureAnalyzer.analyze(revision, diff, nameFile.toString());
					changesArray.add(patternJson);
				}
				
			} catch (Exception e) {
				new RuntimeException("Unable to compute ADD analysis", e);
			}
		}
		
		
		JsonObject root = new JsonObject();
		root.addProperty("id", revision.getName());
		root.add("files", filesArray);

		return (new FeaturesResult(revision, root));

	}

	public RepairPatterns analyze(Diff editScript, Config config) {
		RepairPatterns repairPatterns = new RepairPatterns();
		List<Operation> operations = editScript.getRootOperations();
		List<AbstractPatternDetector> detectors = new ArrayList();
		detectors.add(new MissingNullCheckDetector(operations));
		//detectors.add(new SingleLineDetector(config, operations));
		detectors.add(new ConditionalBlockDetector(operations));
		detectors.add(new WrapsWithDetector(operations));
		detectors.add(new CopyPasteDetector(operations));
		detectors.add(new ConstantChangeDetector(operations));
		detectors.add(new CodeMovingDetector(operations));
		detectors.add(new ExpressionFixDetector(operations));
		detectors.add(new WrongReferenceDetector(operations));
		Iterator var3 = detectors.iterator();

		while(var3.hasNext()) {
			AbstractPatternDetector detector = (AbstractPatternDetector)var3.next();
			detector.detect(repairPatterns);
		}

		return repairPatterns;
	}


	public void putCodeFromHunk(RevisionResult previousResults, Object nameFile, JsonObject file) {
		AnalysisResult resultsHunk = previousResults.get(HunkDifftAnalyzer.class.getSimpleName());
		if (resultsHunk != null) {
			DiffResult<Commit, HunkDiff> hunkresults = (DiffResult<Commit, HunkDiff>) resultsHunk;
			HunkDiff hunks = hunkresults.getDiffOfFiles().get(nameFile);
			if (hunks != null && hunks.getHunkpairs() != null)
				if (hunks.getHunkpairs().size() == 1) {
					HunkPair hunkp = hunks.getHunkpairs().get(0);
					String patch = hunkp.getLeft() + "<EOS>" + hunkp.getRight();
					file.addProperty("pairs", patch);
				} else {
					JsonArray pairsArray = new JsonArray();

					for (HunkPair hunkp : hunks.getHunkpairs()) {
						String patch = hunkp.getLeft() + "<EOS>" + hunkp.getRight();
						pairsArray.add(patch);

					}
					file.add("pairs", pairsArray);
				}

		}
	}

	@SuppressWarnings("unchecked")

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
		try {
			CtStatement parentLine = affectedCtElement.getParent(FILTER);
			if (parentLine != null)
				return parentLine;
		} catch (Exception e) {
			log.error("Problems getting parents of line: " + affectedCtElement);
		}
		// by default, we return the affected element
		return affectedCtElement;

	}

}