package fr.inria.coming.core.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.core.entities.interfaces.RevisionOrder;

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

	public abstract Collection<Data> loadDataset();

	public List<Analyzer> getAnalyzers() {
		return this.analyzers;
	}

	public void setAnalyzers(List<Analyzer> analyzers) {
		this.analyzers = analyzers;
	}

	/**
	 * Map<String, ResultRevision> analyzer
	 * 
	 * @param element
	 * @param resultAllAnalyzed
	 */
	public abstract void processEndRevision(Data element, RevisionResult resultAllAnalyzed);

	protected abstract FinalResult processEnd();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FinalResult analyze() {

		Collection data = loadDataset();
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
				if (!resultAnalyzer.sucessful())
					break;
			}

			processEndRevision(element, resultAllAnalyzed);

			i++;
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
