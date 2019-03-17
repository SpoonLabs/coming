package fr.inria.coming.core.entities.output;

import java.io.File;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromDiff;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.main.ComingProperties;
import gumtree.spoon.diff.operations.Operation;

public class JSonPatternInstanceOutput implements IOutput {

	@Override
	public void generateFinalOutput(FinalResult finalResult) {

		System.out.println("JSON output Final Results");
		JsonObject root = new JsonObject();
		JsonArray instances = new JsonArray();
		root.add("instances", instances);
		for (Object revision_commit : finalResult.getAllResults().keySet()) {

			RevisionResult revisionResult = (RevisionResult) finalResult.getAllResults().get(revision_commit);

			if (revisionResult == null)
				continue;

			getInstancesOfRevision(revisionResult, instances);
		}

		saveToJsonFile(root, "all_instances_found");
	}

	public void saveToJsonFile(JsonObject root, String filename) {
		File fout = new File(ComingProperties.getProperty("output"));
		fout.mkdirs();
		try {
			FileWriter fw = new FileWriter(fout.getAbsolutePath() + File.separator + filename + ".json");

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(root.toString());
			String prettyJsonString = gson.toJson(je);
			fw.write(prettyJsonString);

			fw.flush();
			fw.close();
			System.out.println("Output saved in " + fout.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getInstancesOfRevision(RevisionResult revisionResult, JsonArray instances) {

		String revisionIdentifier = null;
		if (revisionResult.getRelatedRevision() != null) {
			revisionIdentifier = revisionResult.getRelatedRevision().getName();
		}

		PatternInstancesFromRevision result = (PatternInstancesFromRevision) revisionResult
				.getResultFromClass(PatternInstanceAnalyzer.class);

		for (PatternInstancesFromDiff pi : result.getInfoPerDiff()) {
			if (pi.getInstances().size() > 0) {

				System.out.println("\n--------\ncommit with instance:\n " + revisionIdentifier);
				System.out.println(pi.getInstances());
				for (ChangePatternInstance instancePattern : pi.getInstances()) {

					JsonObject instance = new JsonObject();

					instance.addProperty("revision", revisionIdentifier.toString());
					instance.addProperty("pattern_name", (instancePattern.getPattern().getName()));
					JsonArray ops = new JsonArray();

					for (PatternAction pa : instancePattern.getActionOperation().keySet()) {
						Operation op = instancePattern.getActionOperation().get(pa);
						JsonObject opjson = new JsonObject();
						opjson.addProperty("action", pa.getAction().toString());
						opjson.addProperty("entity", pa.getAffectedEntity().toString());
						opjson.add("op", getJSONFromOperator(op));
						opjson.addProperty("code", op.getNode().toString());
						opjson.addProperty("location", op.getNode().getPath().toString());
						ops.add(opjson);
					}

					instance.add("ops", ops);
					instances.add(instance);
				}

			}
		}
	}

	@SuppressWarnings("unchecked")
	public static JsonObject getJSONFromOperator(Operation operation) {
		JsonObject op = new JsonObject();
		op.addProperty("operator", operation.getAction().getName());
		op.addProperty("src",
				(operation.getSrcNode() != null) ? operation.getSrcNode().getClass().getSimpleName() : "null");
		op.addProperty("dst",
				(operation.getDstNode() != null) ? operation.getDstNode().getClass().getSimpleName() : "null");

		op.addProperty("srcparent",
				(operation.getSrcNode() != null) ? operation.getSrcNode().getParent().getClass().getSimpleName()
						: "null");
		op.addProperty("dstparent",
				(operation.getDstNode() != null) ? operation.getDstNode().getParent().getClass().getSimpleName()
						: "null");
		return op;
	}

	@Override
	public void generateRevisionOutput(RevisionResult resultRevision) {
		System.out.println("JSON output Final Results");
		JsonObject root = new JsonObject();
		JsonArray instances = new JsonArray();
		root.add("instances", instances);
		getInstancesOfRevision(resultRevision, instances);
		saveToJsonFile(root, ("instances_found_rev_" + resultRevision.getRelatedRevision().getName()));

	}
}
