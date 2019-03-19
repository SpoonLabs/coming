package fr.inria.coming.core.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionDataset;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.core.entities.interfaces.RevisionOrder;
import fr.inria.coming.main.ComingProperties;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class RevisionNavigationExperiment<R extends IRevision> {

	protected RevisionOrder<R> navigationStrategy = null;
	protected List<Analyzer> analyzers = new ArrayList<>();
	protected List<IFilter> filters = null;
	protected List<IOutput> outputProcessors = new ArrayList<>();

	protected FinalResult<R> allResults = null;

	public RevisionNavigationExperiment() {
		allResults = new FinalResult<>();
	}

	public RevisionNavigationExperiment(RevisionOrder<R> navigationStrategy) {
		super();
		this.navigationStrategy = navigationStrategy;
	}

	public RevisionOrder<R> getNavigationStrategy() {
		return navigationStrategy;
	}

	public void setNavigationStrategy(RevisionOrder<R> navigationStrategy) {
		this.navigationStrategy = navigationStrategy;
	}

	public abstract RevisionDataset<R> loadDataset();

	public List<Analyzer> getAnalyzers() {
		return this.analyzers;
	}

	public void setAnalyzers(List<Analyzer> analyzers) {
		this.analyzers = analyzers;
	}

	@SuppressWarnings("unchecked")
	public void processEndRevision(R element, RevisionResult resultAllAnalyzed) {

		if (ComingProperties.getPropertyBoolean("save_result_revision_analysis")) {
			allResults.put(element, resultAllAnalyzed);
		}
		if (ComingProperties.getPropertyBoolean("outputperrevision")) {

			for (IOutput out : this.getOutputProcessors()) {
				out.generateRevisionOutput(resultAllAnalyzed);
			}
		}
	}

	protected FinalResult processEnd() {
		if (ComingProperties.getPropertyBoolean("save_result_revision_analysis")) {

			for (IOutput out : this.getOutputProcessors()) {
				out.generateFinalOutput(this.allResults);
			}

			return this.allResults;
		} else
			return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FinalResult analyze() {

		RevisionDataset data = loadDataset();
		Iterator it = this.getNavigationStrategy().orderOfNavigation(data);

		int i = 1;

		List<Analyzer> analyzers = this.getAnalyzers();

		int size = data.size();

		for (Iterator<R> iterator = it; iterator.hasNext();) {

			R oneRevision = iterator.next();

			System.out.println("\n***********\nAnalyzing " + i + "/" + size);
			if (!accept(oneRevision)) {
				continue;
			}

			RevisionResult resultAllAnalyzed = new RevisionResult(oneRevision);
			for (Analyzer analyzer : analyzers) {

				AnalysisResult resultAnalyzer = analyzer.analyze(oneRevision, resultAllAnalyzed);
				resultAllAnalyzed.put(analyzer.getClass().getSimpleName(), resultAnalyzer);
				if (resultAnalyzer == null || !resultAnalyzer.sucessful())
					break;
			}

			processEndRevision(oneRevision, resultAllAnalyzed);

			i++;
			if (i > ComingProperties.getPropertyInteger("maxrevision"))
				break;
		}

		return processEnd();
	}

	protected boolean accept(R element) {
		if (this.getFilters() == null)
			return true;

		boolean accepted = true;
		for (IFilter iFilter : this.getFilters()) {

			accepted &= iFilter.accept(element);
		}
		return accepted;
	};

	public List<IFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<IFilter> filters) {
		this.filters = filters;
	}

	public List<IOutput> getOutputProcessors() {
		return outputProcessors;
	}

	public void setOutputProcessors(List<IOutput> outputProcessors) {
		this.outputProcessors = outputProcessors;
	}

}
