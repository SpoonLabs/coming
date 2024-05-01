package fr.inria.coming.codefeatures;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.prophet4j.feature.enhanced.EnhancedFeatureCross;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.prophet4j.feature.Feature;
import fr.inria.prophet4j.feature.FeatureCross;
import fr.inria.prophet4j.utility.CodeDiffer;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import fr.inria.prophet4j.utility.Structure.FeatureMatrix;
import fr.inria.prophet4j.utility.Structure.FeatureVector;
import fr.inria.prophet4j.utility.Structure.ParameterVector;

/**
 * Computes the P4J features for a given file pair (does not work with repo)
 * @author He Ye
 *
 */
public class P4JFeatureAnalyzer implements Analyzer<IRevision> {

	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {

		AnalysisResult resultFromDiffAnalysis = previousResults.getResultFromClass(FineGrainDifftAnalyzer.class);
		DiffResult diffResut = (DiffResult) resultFromDiffAnalysis;
		String filename = revision.getName();
//		if (diffResut.getDiffOfFiles().size()!=0) {
//			filename =  diffResut.getDiffOfFiles().keySet().iterator().next().toString();
//		}else {
//			filename = diffResut.getAnalyzed().toString();
//		}

		if (resultFromDiffAnalysis == null) {
			System.err.println("Error Diff must be executed before");
			throw new IllegalArgumentException("Error: missing diff");
		}

		JsonObject jsonfile = new JsonObject();
		for (IRevisionPair pair: revision.getChildren()) {
			// determine source and target file path
			JsonObject jsonpair = extractFeatures(fileSrcTgtPaths(pair));
			jsonfile.add(pair.getPreviousName(), jsonpair);
		}
		return (new FeaturesResult(revision,jsonfile));
	}

	public Map<String, File> fileSrcTgtPaths(IRevisionPair s) {

		Map<String, File> filePaths = new HashMap<>();
		final File src = new File(s.getPreviousName());
		if (!src.exists()) {
			throw new IllegalArgumentException("The source file not exist! "+src);
		}
		filePaths.put("src", src);
		final File tgt = new File(s.getNextName());
		if (!tgt.exists()) {
			throw new IllegalArgumentException("The source file not exist!");
		}
		filePaths.put("target", tgt);
		return filePaths;
	}



	public JsonObject extractFeatures(Map<String, File> filePaths) {
		File src = filePaths.get("src");
		if (src==null) {
			return null;
		}
		File target = filePaths.get("target");
		if (src == null || target == null) {
			log.error("The source or target file not exist!");
			return null;
		}
		Option option = new Option();
		option.featureOption = FeatureOption.ENHANCED;

		CodeDiffer codeDiffer = new CodeDiffer(false, option);
		//Get feature matrix
		List<FeatureMatrix> featureMatrix = codeDiffer.runByGenerator(src, target);
		//Get feature vector
		JsonObject jsonfile = null;
		//	csvfile = genVectorsCSV(option,target,featureMatrix);
		jsonfile = genVectorsJSON(option,target,featureMatrix);
		return jsonfile;
	}


	public JsonObject genVectorsJSON(Option option, File patchedFile, List<FeatureMatrix> featureMatrices) {
		 
		 	String[] pathStr = patchedFile.getAbsolutePath().split("/");
		 	String fileName = pathStr[pathStr.length-1];
		 	fileName = fileName.replace(".java", "");
		 	
	        ParameterVector parameterVector = new ParameterVector(option.featureOption);
            List<String> valueList = null;
	        List<String> header = new ArrayList<>();
	        List<String> values = new ArrayList<>();
	        JsonObject jsonfile = new JsonObject();


		for (FeatureVector featureVector : featureMatrices.get(0).getFeatureVectors()) {
			List<FeatureCross> featureCrosses = featureVector.getNonSortedFeatureCrosses();
			for (FeatureCross featureCross : featureCrosses) {
				List<Feature> simpleP4JFeatures= featureCross.getFeatures();
				for(Feature f: simpleP4JFeatures) {
					Boolean positive = featureCross.containFeature(f);
					jsonfile.addProperty("P4J_"+f, positive?"true":"false");
				}

			}
		}

		//Initial all vector  as 0.
	        for (int idx = 0; idx < parameterVector.size(); idx++) {
	            FeatureCross featureCross;
	            featureCross = new EnhancedFeatureCross(idx);
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
                
                if(valueList==null) {
                	return null;
                }
                
                String valueStr="";
                String head="";
                for (int idx = 0; idx < parameterVector.size(); idx++) {
			jsonfile.addProperty(header.get(idx), valueList.get(idx));
			valueStr+=valueList.get(idx)+",";  	 
			head+=idx+",";
    	        }
                
                //write to csv file.          
                valueStr = fileName+","+valueStr.substring(0,valueStr.length()-1);
                head = "id,"+head.substring(0,head.length()-1);

                try {
			Path path = Paths.get("test.csv");
			boolean exists = Files.exists(path);  
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("test.csv", true)));
			if(!exists) {
			    out.println(head);
			    out.flush();
			} 
			out.println(valueStr);
			out.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

                
                
            }	               
	       return jsonfile;	        
	    }
}
