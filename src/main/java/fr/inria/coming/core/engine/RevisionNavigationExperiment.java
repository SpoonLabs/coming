package fr.inria.coming.core.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public abstract class RevisionNavigationExperiment<Data extends IRevision> {

	protected RevisionOrder<Data> navigationStrategy = null;
	protected List<Analyzer> analyzers = new ArrayList<>();
	protected List<IFilter> filters = null;
	protected List<IOutput> outputProcessors = new ArrayList<>();

	protected Map<Data, RevisionResult> allResults = new HashMap<>();

	public RevisionNavigationExperiment() {
	}

	public RevisionNavigationExperiment(RevisionOrder<Data> navigationStrategy) {
		super();
		this.navigationStrategy = navigationStrategy;
	}

	public RevisionOrder<Data> getNavigationStrategy() {
		return navigationStrategy;
	}

	public void setNavigationStrategy(RevisionOrder<Data> navigationStrategy) {
		this.navigationStrategy = navigationStrategy;
	}

	public abstract RevisionDataset<Data> loadDataset();

	public List<Analyzer> getAnalyzers() {
		return this.analyzers;
	}

	public void setAnalyzers(List<Analyzer> analyzers) {
		this.analyzers = analyzers;
	}

	@SuppressWarnings("unchecked")
	public void processEndRevision(Data element, RevisionResult resultAllAnalyzed) {

		if (ComingProperties.getPropertyBoolean("save_result_revision_analysis")) {
			allResults.put(element, resultAllAnalyzed);
		}

	}

	protected FinalResult processEnd() {

		return new FinalResult(allResults);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FinalResult analyze() {

		RevisionDataset data = loadDataset();
		Iterator it = this.getNavigationStrategy().orderOfNavigation(data);

		int i = 0;

		List<Analyzer> analyzers = this.getAnalyzers();

		for (Iterator<Data> iterator = it; iterator.hasNext();) {

			Data element = iterator.next();

			if (!accept(element)) {
				continue;
			}

			RevisionResult resultAllAnalyzed = new RevisionResult();
			for (Analyzer analyzer : analyzers) {

				AnalysisResult resultAnalyzer = analyzer.analyze(element, resultAllAnalyzed);
				resultAllAnalyzed.put(analyzer.getClass().getSimpleName(), resultAnalyzer);
				if (resultAnalyzer == null || !resultAnalyzer.sucessful())
					break;
			}

			processEndRevision(element, resultAllAnalyzed);

			i++;
			if (i > ComingProperties.getPropertyInteger("maxrevision"))
				break;
		}

		return processEnd();
	}

	protected boolean accept(Data element) {
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
