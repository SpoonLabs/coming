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
import fr.inria.coming.main.ComingProperties;
import fr.inria.prophet4j.feature.Feature;
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
		String filename = "";
		if (diffResut.getDiffOfFiles().size()!=0) {
			filename =  diffResut.getDiffOfFiles().keySet().iterator().next().toString();		
		}else {
			filename = diffResut.getAnalyzed().toString();
		}

		if (resultFromDiffAnalysis == null) {
			System.err.println("Error Diff must be executed before");
			throw new IllegalArgumentException("Error: missing diff");
		}

		// determine source and target file path
		String path = revision.getFolder();
		Map<String, File> filePaths = null;
		if(path!=null) {
			filePaths = processFilesPair(new File(path),"");
		} else {
			return null;
		}
		JsonObject jsonfile = extractFeatures(filePaths);
		return (new FeaturesResult(revision,jsonfile));
	}
	
	
	public AnalysisResult analyze(IRevision revision, String targetFile) {
		String path = revision.getFolder();
		Map<String, File> filePaths = null;
		if(path!=null) {
			filePaths = processFilesPair(new File(path),targetFile);
		} else {
			return null;
		}		
		JsonObject jsonfile = extractFeatures(filePaths);
		return (new FeaturesResult(revision,jsonfile));
	}
		
	public JsonObject extractFeatures(Map<String, File> filePaths) {
		File src = filePaths.get("src");
		File target = filePaths.get("target");
		Option option = new Option();
		option.featureOption = FeatureOption.ORIGINAL;
		//We set the first parameter of CodeDiffer as False to not allow the code generation at buggy location
		//By default, coming extracts simple P4J features, so the cross sets to false
		Boolean cross = ComingProperties.getPropertyBoolean("cross");
		CodeDiffer codeDiffer = new CodeDiffer(false, option,cross);
		//Get feature matrix
		List<FeatureMatrix> featureMatrix = codeDiffer.runByGenerator(src, target);
		//Get feature vector
		JsonObject jsonfile = null;
		if(cross) {
			jsonfile = genVectorsCSV(option,target,featureMatrix);
		} else {
			jsonfile = getSimleP4JJSON(option,target,featureMatrix,true);
		}
		return jsonfile;
	}
	

	public JsonObject getSimleP4JJSON(Option option, File target, List<FeatureMatrix> featureMatrix, Boolean numericalIndixator) {
		
	        JsonObject jsonfile = new JsonObject();
	        
	        for (FeatureVector featureVector : featureMatrix.get(0).getFeatureVectors()) {
                List<FeatureCross> featureCrosses = featureVector.getNonSortedFeatureCrosses();
                
                for (FeatureCross featureCross : featureCrosses) {
            			List<Feature> simpleP4JFeatures= featureCross.getSimpleP4JFeatures();
	                	OriginalFeatureCross ofc = (OriginalFeatureCross) featureCross;
	                	for(Feature f: simpleP4JFeatures) {
	                		Boolean positive = ofc.containFeature(f);
	                		if(numericalIndixator) {
		                		jsonfile.addProperty("P4J_"+ofc.getCrossType()+"_"+f, positive?"1":"0");
	                		}else {
		                		jsonfile.addProperty("P4J_"+ofc.getCrossType()+"_"+f, positive?"true":"false");
	                		}
	                	}

                }
	        }
	        return jsonfile;

	}

	public Map processFilesPair(File pairFolder,String targetFile) {
		Map<String, File> pathmap = new HashMap();

		for (File fileModif : pairFolder.listFiles()) {

			if (".DS_Store".equals(fileModif.getName()))
				continue;
			
			if(targetFile!="") {
				if (!fileModif.getPath().contains(targetFile)) {
					continue;
				}
			}

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