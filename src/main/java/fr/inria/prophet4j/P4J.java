package fr.inria.prophet4j;

import fr.inria.prophet4j.utility.CodeDiffer;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.DataOption;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import fr.inria.prophet4j.utility.Option.PatchOption;
import fr.inria.prophet4j.utility.Option.LearnerOption;
import fr.inria.prophet4j.utility.Structure;
import fr.inria.prophet4j.utility.Support;

import java.io.File;
import java.util.List;

// this is the API class of Prophet4J
public class P4J {
    private CodeDiffer codeDiffer;
    private Structure.ParameterVector parameterVector;

    public P4J() {
        Option option = new Option();
        option.dataOption = DataOption.BUG_DOT_JAR_MINUS_MATH;
        option.patchOption = PatchOption.BUG_DOT_JAR_MINUS_MATH;
        option.featureOption = FeatureOption.ORIGINAL;
        option.learnerOption = LearnerOption.CROSS_ENTROPY;

        this.codeDiffer = new CodeDiffer(false, option);
        this.parameterVector = new Structure.ParameterVector(option.featureOption);

        String parameterFilePath = Support.getFilePath(Support.DirType.PARAMETER_DIR, option) + "ParameterVector";
        this.parameterVector.load(parameterFilePath);
    }

    public double computeOverfittingScore(File buggyFile, File patchedFile) {
        List<Structure.FeatureMatrix> featureMatrices = codeDiffer.runByGenerator(buggyFile, patchedFile);
        if (featureMatrices.size() == 1) {
            return featureMatrices.get(0).score(parameterVector);
        }
        return Double.POSITIVE_INFINITY;
    }
}
