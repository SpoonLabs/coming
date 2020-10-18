package fr.inria.coming.codefeatures;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import add.main.Config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import add.entities.RepairPatterns;
import add.features.detector.repairpatterns.RepairPatternDetector;

import org.apache.log4j.Logger;
import org.junit.Test;

import fr.inria.coming.main.ComingProperties;
import fr.inria.prophet4j.utility.CodeDiffer;
import fr.inria.prophet4j.utility.Option;
import fr.inria.prophet4j.utility.Option.FeatureOption;
import fr.inria.prophet4j.utility.Structure.FeatureMatrix;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;

import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;

public class RepairnatorFeatures {

	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());
    private final String UNKNOWN = "unknown";
    private final String CORRECT = "correct";
    private final String OVERFITTING = "overfitting";

	
	public String getLabel(File pairFolder) throws Exception {
		
		//Obtain feature in libsvm format
		String features = extractFeatures(pairFolder);
		if("".equals(features)) {
			return UNKNOWN;
		}
		//Load test data in libsvm format
		File tempFile = File.createTempFile("test", ".txt");
		try (FileWriter sb = new FileWriter(tempFile)) {
			sb.append(features);
		}
		//Load model
		DMatrix dtest = new DMatrix(tempFile.getAbsolutePath());
		String odsModel = this.getClass().getClassLoader().getResource("ODSTraining/ODSmodel.bin").getPath();  
		Booster booster = XGBoost.loadModel(odsModel);
		
		//Predict
		float[][] predicts = booster.predict(dtest);
		float probability = predicts[0][0];
		tempFile.delete();

		
		//Return label		
		String thresholdString = ComingProperties.getProperty("overfitting-threshold");
		float threshold = Float.parseFloat(thresholdString);

		if (probability > threshold) {
			return OVERFITTING;
		} else {		
			return CORRECT;
		}
		
	}
	
	@Test
	public void trainModel(File pairFolder) throws Exception {
		
		String odsModel = this.getClass().getClassLoader().getResource("ODSTraining/ODSmodel.bin").getPath();  

		String trainingFile = this.getClass().getClassLoader().getResource("ODSTraining/Training-libsvm.txt").getPath();  

		DMatrix trainMat = new DMatrix(trainingFile);
		
		Map<String, Object> params = new HashMap<String, Object>() {
			  {
			    put("eta", 0.3);
			    put("max_depth", 6);
			    put("gamma", 1);
			    put("objective", "binary:logistic");
			    put("eval_metric", "logloss");
			  }
			};
			
		Map<String, DMatrix> watches = new HashMap<String, DMatrix>() {
				{
				    put("train", trainMat);
		      }
		};		
							
		int nround = 20;
		Booster booster = XGBoost.train(trainMat, params, nround, watches, null, null);			
		booster.saveModel(odsModel);

	}
	
	
	public String extractFeatures(File pairFolder) throws Exception {
		
		try {
		String features = "";
		
		// Get the src and target file paths
		Map<String, File> srcTargetPair = getSrcTargetPair(pairFolder);
		File previousVersion = srcTargetPair.get("src");
		File postVersion = srcTargetPair.get("target");


		// Compute the diff with Gumtree
		AstComparator comparator = new AstComparator();
		Diff diff = comparator.compare(previousVersion, postVersion);

		// Case: No diff detected, we return empty feature string
		if (diff.getRootOperations().size() == 0) {
			System.out.print("diff is null: " + previousVersion.getAbsolutePath());
			return features;
		}

		// Extract code description features (P4J)
		Option option = new Option();
		option.featureOption = FeatureOption.ORIGINAL;
		Boolean cross = ComingProperties.getPropertyBoolean("cross");
		String codefeature = ComingProperties.getProperty("codeDescriptionFeatures");
		String[] codefeatureList = codefeature.split(",");
		CodeDiffer codeDiffer = new CodeDiffer(false, option, cross);

		List<FeatureMatrix> featureMatrix = codeDiffer.runByGenerator(previousVersion, postVersion);
		JsonObject jsonfile = new P4JFeatureAnalyzer().getSimleP4JJSON(option, postVersion, featureMatrix, true);

		for (String feature : codefeatureList) {
			for (Entry<String, JsonElement> extractedFeatureSet : jsonfile.entrySet()) {
				if (feature.contains(extractedFeatureSet.getKey())) {
					features += extractedFeatureSet.getValue() + ",";
				}
			}
		}
		features = features.replace("\"", "");

		// Extract repair pattern features (ADD)
		String repairFeature = ComingProperties.getProperty("repairPatternFeatures");
		String[] repairFeatureList = repairFeature.split(",");
		File tempFile = File.createTempFile("add_", ".diff");
		Config config = new Config();
		config.setDiffPath(tempFile.getAbsolutePath());
		config.setBuggySourceDirectoryPath(pairFolder.getAbsolutePath());
		RepairPatternDetector patternDetector = new RepairPatternDetector(config, diff);
		RepairPatterns analyze = patternDetector.analyze();

		JsonObject repairfeatures = new Gson().fromJson(analyze.toJson().toString(), JsonObject.class);
		JsonObject repairPatterns = (JsonObject) repairfeatures.get("repairPatterns");

		for (String feature : repairFeatureList) {
			for (Entry<String, JsonElement> extractedFeatureSet : repairPatterns.entrySet()) {
				if (feature.contains(extractedFeatureSet.getKey())) {
					features += extractedFeatureSet.getValue() + ",";
				}
			}
		}
		tempFile.delete();
		
		
		//To use XGBoost4J, we convert csv format features obtain above to libsvm format.		
		String[] featureList = features.split(",");
		String libsvmFeature="";
		for(int i=0; i <featureList.length;i++ ){
			if(!"0".equals(featureList[i])) {
				libsvmFeature=libsvmFeature+(i+1)+":"+featureList[i]+" ";
			}
		}		
		return libsvmFeature;
		
		} catch( Exception e ){
			return "";
		}
	}

	private Map<String, File> getSrcTargetPair(File pairFolder) {
		Map<String, File> pathmap = new HashMap<String, File>();

		for (File fileModif : pairFolder.listFiles()) {
			if (!fileModif.getName().contains(".DS")) {
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

		}
		return pathmap;
	}

}
