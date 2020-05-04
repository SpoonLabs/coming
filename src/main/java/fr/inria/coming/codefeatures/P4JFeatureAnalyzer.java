package fr.inria.coming.codefeatures;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.prophet4j.feature.FeatureCross;
import fr.inria.prophet4j.feature.extended.ExtendedFeatureCross;
import fr.inria.prophet4j.feature.original.OriginalFeatureCross;
import fr.inria.prophet4j.learner.RepairEvaluator;
import fr.inria.prophet4j.utility.CodeDiffer;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Support;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import fr.inria.prophet4j.utility.Option.RankingOption;
import fr.inria.prophet4j.utility.Structure.FeatureMatrix;
import fr.inria.prophet4j.utility.Structure.FeatureVector;
import fr.inria.prophet4j.utility.Structure.ParameterVector;

/**
 *
 * @author He Ye
 *
 */
public class P4JFeatureAnalyzer implements Analyzer<IRevision> {

	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	protected CodeFeatureDetector cresolver = new CodeFeatureDetector();

	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {

		AnalysisResult resultFromDiffAnalysis = previousResults.getResultFromClass(FineGrainDifftAnalyzer.class);
		DiffResult diffResut = (DiffResult) resultFromDiffAnalysis;
		String filename =   diffResut.getDiffOfFiles().keySet().iterator().next().toString();		

		if (resultFromDiffAnalysis == null) {
			System.err.println("Error Diff must be executed before");
			throw new IllegalArgumentException("Error: missing diff");
		}

		// determine source and target file path
		String path = revision.getFolder();
		Map<String, File> filePaths = processFilesPair(new File(path));
		File src = filePaths.get("src");
		File target = filePaths.get("target");

		Option option = new Option();
		option.featureOption = FeatureOption.ORIGINAL;
		//We set the first parameter of CodeDiffer as False to not allow the code generation at buggy location
		//The second false indicates cross features
		Boolean cross = false;
		CodeDiffer codeDiffer = new CodeDiffer(false, option,cross);
		//Get feature matrix
		List<FeatureMatrix> featureMatrix = codeDiffer.runByGenerator(src, target);
		//Get feature vector
		JsonObject jsonfile = null;
		if(cross) {
			jsonfile = genVectorsCSV(option,target,featureMatrix);
		} else {
			jsonfile = getNonCrossJSON(option,target,featureMatrix);
		}
		
		JsonArray filesArray = new JsonArray();		
		JsonObject file = new JsonObject();
		JsonArray changesArray = new JsonArray();
		changesArray.add(jsonfile);	
		file.addProperty("file_name", filename);
		file.add("features", changesArray);
		filesArray.add(file);
		JsonObject root = new JsonObject();
		root.addProperty("id", revision.getName());
		root.add("files", filesArray);

		return (new FeaturesResult(revision, root));

	}

	private JsonObject getNonCrossJSON(Option option, File target, List<FeatureMatrix> featureMatrix) {
		 ParameterVector parameterVector = new ParameterVector(option.featureOption);
	        JsonObject jsonfile = new JsonObject();
	       
	        for (FeatureVector featureVector : featureMatrix.get(0).getFeatureVectors()) {
                List<FeatureCross> featureCrosses = featureVector.getNonSortedFeatureCrosses();
                for (FeatureCross featureCross : featureCrosses) {
	                	OriginalFeatureCross ofc = (OriginalFeatureCross) featureCross;
	                	jsonfile.addProperty(ofc.getCrossType(), ofc.getFeatures().toString());

                }
	        }
	        return jsonfile;

	}

	public Map processFilesPair(File pairFolder) {
		Map<String, File> pathmap = new HashMap();

		for (File fileModif : pairFolder.listFiles()) {

			if (".DS_Store".equals(fileModif.getName()))
				continue;

			String pathname = fileModif.getAbsolutePath() + File.separator + pairFolder.getName() + "_"
					+ fileModif.getName();

			File previousVersion = new File(pathname + "_s.java");
			if (!previousVersion.exists()) {
				log.error("The source file " + previousVersion.getPath() + " not exist!");
			} else {
				pathmap.put("src", previousVersion);
				File postVersion = new File(pathname + "_t.java");
				pathmap.put("target", postVersion);

			}

		}
		return pathmap;

	}
	
	
	 public JsonObject genVectorsCSV(Option option, File patchedFile, List<FeatureMatrix> featureMatrices) {
	        ParameterVector parameterVector = new ParameterVector(option.featureOption);
            List<String> valueList = null;
	        List<String> header = new ArrayList<>();
	        List<String> values = new ArrayList<>();
	        JsonObject jsonfile = new JsonObject();

	        //Initial all vector  as 0.
	        for (int idx = 0; idx < parameterVector.size(); idx++) {
	            FeatureCross featureCross;
	            featureCross = new OriginalFeatureCross(idx);
                header.add(featureCross.getFeatures().toString());
	            values.add("0");
	        }
	     
	        //update value vector based on featureMatrices
	        if (featureMatrices.size() == 1) {
                for (FeatureVector featureVector : featureMatrices.get(0).getFeatureVectors()) {
                	valueList = new ArrayList<>(values);
                    List<FeatureCross> featureCrosses = featureVector.getFeatureCrosses();
                    for (FeatureCross featureCross : featureCrosses) {
                        valueList.set(featureCross.getId(), "1");
                    }
                }
                
                for (int idx = 0; idx < parameterVector.size(); idx++) {
    	        			jsonfile.addProperty(header.get(idx), valueList.get(idx));
    	        		}
            }
             
	               
	       return jsonfile;
	        
	    }
	
	
	

}