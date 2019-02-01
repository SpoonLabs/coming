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

public class PatternInstanceAnalyzer implements Analyzer {

	ChangePatternSpecification patternToMine = null;

	public PatternInstanceAnalyzer() {
		loadPattern();
	}

	public PatternInstanceAnalyzer(ChangePatternSpecification patternToMine) {
		super();
		this.patternToMine = patternToMine;
	}

	public void loadPattern() {
		// TODO:
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

		for (Object value : diffResut.getDiffOfFiles().values()) {

			Diff singleDiff = (Diff) value;
			DetectorChangePatternInstanceEngine instanceDetector = new DetectorChangePatternInstanceEngine();
			List<ChangePatternInstance> instances = instanceDetector.findPatternInstances(this.patternToMine,
					singleDiff);

			PatternInstancesFromDiff resultDiff = new PatternInstancesFromDiff(input, instances, singleDiff);
			instancesAll.add(resultDiff);

		}

		PatternInstancesFromRevision revisionResult = new PatternInstancesFromRevision(input, instancesAll);

		return revisionResult;
	}

	public ChangePatternSpecification getPatternToMine() {
		return patternToMine;
	}

	public void setPatternToMine(ChangePatternSpecification patternToMine) {
		this.patternToMine = patternToMine;
	}

}
