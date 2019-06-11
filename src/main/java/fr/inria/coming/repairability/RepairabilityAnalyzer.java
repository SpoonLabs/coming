package fr.inria.coming.repairability;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;

public class RepairabilityAnalyzer implements Analyzer {
    /**
     * Analyze the input and return the results
     *
     * @param input           input to be analyzer
     * @param previousResults results of previous analysis that can be used in case
     *                        of doing a chain of analysis
     * @return result of the analysis
     */
    @Override
    public AnalysisResult analyze(IRevision input, RevisionResult previousResults) {
        return null;
    }
}
