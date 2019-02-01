package fr.inria.coming.core.entities.output;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.utils.MapCounter;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

public class JSonChangeFrequencyOutput implements IOutput {
	MapCounter<String> counter = new MapCounter<>();
	MapCounter<String> counterParent = new MapCounter<>();

	@SuppressWarnings("rawtypes")
	@Override
	public void generateFinalOutput(FinalResult finalResult) {

		if (finalResult == null)
			return;

		Map<IRevision, RevisionResult> results = finalResult.getAllResults();

		for (RevisionResult revresult : results.values()) {

			AnalysisResult ar = revresult.getResultFromClass(FineGrainDifftAnalyzer.class);
			if (ar != null) {
				DiffResult dr = (DiffResult) ar;
				for (Object value : dr.getDiffOfFiles().values()) {

					Diff singleDiff = (Diff) value;
					for (Operation operation : singleDiff.getRootOperations()) {
						counter.add(operation.getNode().getClass().getSimpleName());
						counterParent.add(
								operation.getAction().getName() + "_" + operation.getNode().getClass().getSimpleName()
										+ "_" + operation.getNode().getParent().getClass().getSimpleName());

					}
				}
			}

		}
		// System.out.println("counter: " + counter.sorted());

		JsonObject root = new JsonObject();

		addStats(root, "frequency", counter.sorted());
		addStats(root, "frequencyParent", counterParent.sorted());

		Map prob = counter.getProbabilies();
		Map probParent = counterParent.getProbabilies();

		addStats(root, "probability", prob);
		addStats(root, "probabilityParent", probParent);

		FileWriter fw;
		try {
			String fileName = ComingProperties.getProperty("output") + File.separator + "change_frequency.json";
			fw = new FileWriter(fileName);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(root.toString());
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

	@SuppressWarnings({ "unused", "unchecked" })
	private void addStats(JsonObject root, String key1, Map sorted) {
		JsonArray frequencyArray = new JsonArray();
		root.add(key1, frequencyArray);
		for (Object key : sorted.keySet()) {
			Object v = sorted.get(key);
			JsonObject singlediff = new JsonObject();

			singlediff.addProperty("c", key.toString().replace("Ct", "").replace("Impl", ""));
			singlediff.addProperty("f", v.toString());
			frequencyArray.add(singlediff);
		}
	}
}
