package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.ArrayList;
import java.util.List;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import gumtree.spoon.diff.Diff;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SpreadPatternInstanceAnalyzer implements Analyzer {

	List<ChangePatternSpecification> patternsToMine = new ArrayList();

	public SpreadPatternInstanceAnalyzer() {
		loadPattern();
	}

	public SpreadPatternInstanceAnalyzer(ChangePatternSpecification patternToMine) {
		super();

		this.patternsToMine.add(patternToMine);
	}

	public SpreadPatternInstanceAnalyzer(List<ChangePatternSpecification> patternToMine) {
		super();

		this.patternsToMine = patternToMine;
	}

	public void loadPattern() {
	}

	@Override
	public AnalysisResult analyze(IRevision input, RevisionResult previousResults) {

		AnalysisResult resultFromDiffAnalysis = previousResults.getResultFromClass(FineGrainDifftAnalyzer.class);

		if (resultFromDiffAnalysis == null) {
			System.err.println("Error Diff must be executed before");
			throw new IllegalArgumentException("Error: missing diff");
		}

		DiffResult diffResut = (DiffResult) resultFromDiffAnalysis;
		List<PatternInstancesFromDiff> instancesAll = new ArrayList<>();

		MegaDiff megadiff = new MegaDiff();
		for (Object file : diffResut.getDiffOfFiles().keySet()) {

			Object value = diffResut.getDiffOfFiles().get(file);
			Diff singleDiff = (Diff) value;
			megadiff.merge(singleDiff);
		}

		DetectorChangePatternInstanceEngine instanceDetector = new DetectorChangePatternInstanceEngine();

		List<ChangePatternInstance> instances = new ArrayList<>();
		for (ChangePatternSpecification changePatternSpecification : patternsToMine) {
			instances.addAll(instanceDetector.findPatternInstances(changePatternSpecification, megadiff));
		}
		PatternInstancesFromDiff resultDiff = new PatternInstancesFromDiff(input, instances, megadiff, "megadiff");
		instancesAll.add(resultDiff);

		PatternInstancesFromRevision revisionResult = new PatternInstancesFromRevision(input, instancesAll);

		return revisionResult;
	}

	public List<ChangePatternSpecification> getPatternsToMine() {
		return patternsToMine;
	}

	public void setPatternsToMine(List<ChangePatternSpecification> patternToMine) {
		this.patternsToMine = patternToMine;
	}

}
