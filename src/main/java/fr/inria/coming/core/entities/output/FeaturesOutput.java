package fr.inria.coming.core.entities.output;

import java.io.File;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.codefeatures.FeatureAnalyzer;
import fr.inria.coming.codefeatures.FeaturesResult;
import fr.inria.coming.codefeatures.P4JFeatureAnalyzer;
import fr.inria.coming.core.entities.AnalysisResult;
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

		log.debug("JSON output");
		// JsonObject root = new JsonObject();
		// JsonArray instances = new JsonArray();
		for (Object commit : finalResult.getAllResults().keySet()) {

			RevisionResult rv = (RevisionResult) finalResult.getAllResults().get(commit);

			if (rv == null)
				continue;

			FeaturesResult result = (FeaturesResult) rv.getResultFromClass(FeatureAnalyzer.class);
			save(result);
		}

	}
	
	   private static Comparator<String> getComparator()
	    {
	        Comparator<String> c = new Comparator<String>()
	        {
	            public int compare(String o1, String o2)
	            {
	                return o1.compareTo(o2);
	            }
	        };

	        return c;
	    }
	
	public static void sort(JsonElement e) {
        if (e.isJsonNull())
        {
            return;
        }

        if (e.isJsonPrimitive())
        {
            return;
        }

        if (e.isJsonArray())
        {
            JsonArray a = e.getAsJsonArray();
            for (Iterator<JsonElement> it = a.iterator(); it.hasNext();)
            {
                sort(it.next());
            }
            return;
        }

        if (e.isJsonObject())
        {
            Map<String, JsonElement> tm = new TreeMap<String, JsonElement>(getComparator());
            for (Entry<String, JsonElement> en : e.getAsJsonObject().entrySet())
            {
                tm.put(en.getKey(), en.getValue());
            }

            for (Entry<String, JsonElement> en : tm.entrySet())
            {
                e.getAsJsonObject().remove(en.getKey());
                e.getAsJsonObject().add(en.getKey(), en.getValue());
                sort(en.getValue());
            }
            return;
        }
    }

	public JsonElement save(FeaturesResult result) {
		if (result == null) {
			log.debug("No Code Change feature captured");
			return null;
		}
		
		JsonElement file = result.getFeatures();

		FileWriter fw;
		try {
			// Create the output dir
			File fout = new File(ComingProperties.getProperty("output"));
			fout.mkdirs();
			String fileName = fout.getAbsolutePath() + File.separator +"features_" + result.getAnalyzed().getName()
					+ ".json";
			fw = new FileWriter(fileName);
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(file.toString());
			sort(je);
			String prettyJsonString = gson.toJson(je);
			log.debug("\nJSON Code Change feature: (file stored at " + fileName + ")\n");
			fw.write(prettyJsonString);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return file;
	}

	@Override
	public void generateRevisionOutput(RevisionResult resultAllAnalyzed) {	
		FeaturesResult comingFeatures = (FeaturesResult) resultAllAnalyzed.getResultFromClass(FeatureAnalyzer.class);					
		save(comingFeatures);
		
	}

}
