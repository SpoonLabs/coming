package fr.inria.coming.core.entities.output;

import java.io.File;
import java.io.FileWriter;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.codefeatures.FeatureAnalyzer;
import fr.inria.coming.codefeatures.FeaturesResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.main.ComingProperties;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FeaturesOutput implements IOutput {
	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	@Override
	public void generateFinalOutput(FinalResult finalResult) {

		System.out.println("JSON output");
		// JsonObject root = new JsonObject();
		// JsonArray instances = new JsonArray();
		for (Object commit : finalResult.getAllResults().keySet()) {

			RevisionResult rv = (RevisionResult) finalResult.getAllResults().get(commit);

			if (rv == null)
				continue;

			FeaturesResult result = (FeaturesResult) rv.getResultFromClass(FeatureAnalyzer.class);
			JsonElement file = result.getFeatures();

			FileWriter fw;
			try {
				String fileName = ComingProperties.getProperty("output") + File.separator + "features_"
						+ result.getAnalyzed() + ".json";
				fw = new FileWriter(fileName);
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonParser jp = new JsonParser();
				JsonElement je = jp.parse(file.toString());
				String prettyJsonString = gson.toJson(je);
				System.out.println("\nJSON Code Change Frequency: (file stored at " + fileName + ")\n");
				System.out.println(prettyJsonString);
				fw.write(prettyJsonString);

				fw.flush();
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void generateRevisionOutput(RevisionResult resultAllAnalyzed) {

	}

}
