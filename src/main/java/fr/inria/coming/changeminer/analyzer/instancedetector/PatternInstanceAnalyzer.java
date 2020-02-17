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
public class PatternInstanceAnalyzer implements Analyzer {

	List<ChangePatternSpecification> patternsToMine = new ArrayList();

	public PatternInstanceAnalyzer() {
		loadPattern();
	}

	public PatternInstanceAnalyzer(ChangePatternSpecification patternToMine) {
		super();

		this.patternsToMine.add(patternToMine);
	}

	public PatternInstanceAnalyzer(List<ChangePatternSpecification> patternToMine) {
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

		for (Object file : diffResut.getDiffOfFiles().keySet()) {

			Object value = diffResut.getDiffOfFiles().get(file);

			Diff singleDiff = (Diff) value;
			DetectorChangePatternInstanceEngine instanceDetector = new DetectorChangePatternInstanceEngine();

			List<ChangePatternInstance> instances = new ArrayList<>();
			for (ChangePatternSpecification changePatternSpecification : patternsToMine) {
				if(input.getName().contentEquals("da9188ff65a5ea0c68a60ab431f77226997d9972"))
					System.out.println(input.getName());
				instances.addAll(instanceDetector.findPatternInstances(changePatternSpecification, singleDiff));
			}
			PatternInstancesFromDiff resultDiff = new PatternInstancesFromDiff(input, instances, singleDiff,
					file.toString());
			instancesAll.add(resultDiff);

		}

		PatternInstancesFromRevision revisionResult = new PatternInstancesFromRevision(input, instancesAll,diffResut.getRow_list());

		return (revisionResult);
	}

	public List<ChangePatternSpecification> getPatternsToMine() {
		return patternsToMine;
	}

	public void setPatternsToMine(List<ChangePatternSpecification> patternToMine) {
		this.patternsToMine = patternToMine;
	}

}
