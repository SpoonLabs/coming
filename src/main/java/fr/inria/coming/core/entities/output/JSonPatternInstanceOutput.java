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
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.main.ComingProperties;
import gumtree.spoon.diff.operations.Operation;

public class JSonPatternInstanceOutput implements IOutput {

	@Override
	public void generateFinalOutput(FinalResult finalResult) {

		System.out.println("JSON output");
		JsonObject root = new JsonObject();
		JsonArray instances = new JsonArray();
		for (Object commit : finalResult.getAllResults().keySet()) {

			RevisionResult rv = (RevisionResult) finalResult.getAllResults().get(commit);

			if (rv == null)
				continue;

			PatternInstancesFromRevision result = (PatternInstancesFromRevision) rv
					.getResultFromClass(PatternInstanceAnalyzer.class);

			for (PatternInstancesFromDiff pi : result.getInfoPerDiff()) {
				if (pi.getInstances().size() > 0) {
					String cid = commit.toString();
					if (commit instanceof Commit) {
						Commit c = (Commit) commit;
						cid = c.getName() + " " + c.getFullMessage();
					}

					System.out.println("\n--------\ncommit with instance:\n " + cid);
					System.out.println(pi.getInstances());
					for (ChangePatternInstance instancePattern : pi.getInstances()) {

						JsonObject instance = new JsonObject();

						instance.addProperty("revision", cid.toString());
						instance.addProperty("pattern_name", (instancePattern.getPattern().getName()));
						JsonArray ops = new JsonArray();

						for (PatternAction pa : instancePattern.getActionOperation().keySet()) {
							Operation op = instancePattern.getActionOperation().get(pa);
							JsonObject opjson = new JsonObject();
							opjson.addProperty("action", pa.getAction().toString());
							opjson.addProperty("entity", pa.getAffectedEntity().toString());
							opjson.add("op", getJSONFromOperator(op));
							ops.add(opjson);
							opjson.addProperty("code", op.getNode().toString());
							opjson.addProperty("location", op.getNode().getPath().toString());
						}

						instance.add("ops", ops);
						instances.add(instance);
					}

				}
				root.add("instances", instances);
			}
		}

		File fout = new File(ComingProperties.getProperty("output"));
		fout.mkdirs();
		try {
			FileWriter fw = new FileWriter(fout.getAbsolutePath() + File.separator + "instances_found" + ".json");

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

	@SuppressWarnings("unchecked")
	protected JsonObject getJSONFromOperator(Operation operation) {
		JsonObject op = new JsonObject();
		op.addProperty("operator", operation.getAction().getName());
		op.addProperty("src",
				(operation.getSrcNode() != null) ? operation.getSrcNode().getClass().getSimpleName() : "null");
		op.addProperty("dst",
				(operation.getDstNode() != null) ? operation.getDstNode().getParent().getClass().getSimpleName()
						: "null");

		op.addProperty("srcparent",
				(operation.getSrcNode() != null) ? operation.getSrcNode().getClass().getSimpleName() : "null");
		op.addProperty("dstparent",
				(operation.getDstNode() != null) ? operation.getDstNode().getParent().getClass().getSimpleName()
						: "null");
		return op;
	}
}
