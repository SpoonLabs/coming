package fr.inria.coming.core.entities.output;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

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
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.repairability.models.InstanceStats;
import gumtree.spoon.diff.operations.Operation;
import org.json.simple.JSONObject;

import static fr.inria.coming.codefeatures.codeanalyze.BinaryOperatorAnalyzer.getStringRepr;

public class JSonPatternInstanceOutput implements IOutput {

	@Override
	public void generateFinalOutput(FinalResult finalResult) {

		JsonObject root = new JsonObject();
		JsonArray instances = new JsonArray();
		root.add("instances", instances);
		JsonArray analyzedRevisions = new JsonArray();
		root.add("analyzed_revisions", analyzedRevisions);

		for (Object revision_commit : finalResult.getAllResults().keySet()) {
			analyzedRevisions.add(revision_commit.toString());

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

				for (ChangePatternInstance instancePattern : pi.getInstances()) {

					JsonObject instance = new JsonObject();

					instance.addProperty("revision", revisionIdentifier.toString());
					instance.addProperty("pattern_name", (instancePattern.getPattern().getName()));
					JsonArray ops = new JsonArray();

					for (PatternAction pa : instancePattern.getActionOperation().keySet()) {
						Operation op = instancePattern.getActionOperation().get(pa);
						JsonObject opjson = new JsonObject();
						opjson.addProperty("pattern_action", pa.getAction().toString());
						opjson.add("pattern_entity", getJSONFromEntity(pa.getAffectedEntity()));
						opjson.add("concrete_change", getJSONFromOperator(op));

						if (op.getNode().getPosition() != null) {
							final String repoPath = new File(ComingProperties.getProperty("location")).getAbsolutePath();
							final String relativePath = op.getNode().getPosition().getFile().getAbsolutePath().replace(new File(System.getProperty("user.dir")).getAbsolutePath(), "");
							opjson.addProperty("file", repoPath+File.separator+ relativePath);
							opjson.addProperty("line", op.getNode().getPosition().getLine());
						}
						ops.add(opjson);
					}

					instance.add("instance_detail", ops);
					instances.add(instance);
				}

			}
		}
	}

	protected JsonElement getJSONFromEntity(PatternEntity affectedEntity) {

		if (affectedEntity == null)
			return null;
		JsonObject jsonEntity = new JsonObject();
		jsonEntity.addProperty("entity_type", affectedEntity.getEntityType());
		jsonEntity.addProperty("entity_new value", affectedEntity.getNewValue());
		jsonEntity.addProperty("entity_old value", affectedEntity.getOldValue());
		jsonEntity.addProperty("entity_role", affectedEntity.getRoleInParent());
		if (affectedEntity.getParentPatternEntity() != null)
			jsonEntity.add("entity_parent", getJSONFromEntity(affectedEntity.getParentPatternEntity().getParent()));
		else
			jsonEntity.addProperty("entity_parent", "null");

		return jsonEntity;
	}

	@SuppressWarnings("unchecked")
	public static JsonObject getJSONFromOperator(Operation operation) {
		JsonObject op = new JsonObject();
		op.addProperty("operator", operation.getAction().getName());
		op.addProperty("src_type",
				(operation.getSrcNode() != null) ? clean(operation.getSrcNode().getClass().getSimpleName()) : "null");
		op.addProperty("dst_type",
				(operation.getDstNode() != null) ? clean(operation.getDstNode().getClass().getSimpleName()) : "null");

		op.addProperty("src", (operation.getSrcNode() != null) ? operation.getSrcNode().toString() : "null");
		op.addProperty("dst", (operation.getDstNode() != null) ? operation.getDstNode().toString() : "null");

		op.addProperty("src_parent_type",
				(operation.getSrcNode() != null) ? clean(operation.getSrcNode().getParent().getClass().getSimpleName())
						: "null");
		op.addProperty("dst_parent_type",
				(operation.getDstNode() != null) ? clean(operation.getDstNode().getParent().getClass().getSimpleName())
						: "null");

		op.addProperty("src_parent",
				(operation.getSrcNode() != null) ? getStringRepr(operation.getSrcNode().getParent()) : "null");
		op.addProperty("dst_parent",
				(operation.getDstNode() != null) ? getStringRepr(operation.getDstNode().getParent()) : "null");

		return op;
	}

	public static JsonObject getJSONFromInstanceStats(InstanceStats stats){
		JsonObject is = new JsonObject();
		is.addProperty("number_of_src_entities", stats.getNumberOfSrcEntities());
		is.addProperty("number_of_dst_entities", stats.getNumberOfDstEntities());
		is.addProperty("src_entity_types", stats.getSrcEntityTypes().toString());
		is.addProperty("dst_entity_types", stats.getDstEntityTypes().toString());
		return is;
	}

	private static String clean(String simpleName) {

		return simpleName.substring(2, simpleName.length() - 4);
	}

	@Override
	public void generateRevisionOutput(RevisionResult resultRevision) {
		JsonObject root = new JsonObject();
		JsonArray instances = new JsonArray();
		root.add("instances", instances);
		getInstancesOfRevision(resultRevision, instances);
		saveToJsonFile(root, ("instances_found_rev_" + resultRevision.getRelatedRevision().getName()));

	}
}
