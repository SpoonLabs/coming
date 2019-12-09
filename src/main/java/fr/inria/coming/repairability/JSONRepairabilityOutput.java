package fr.inria.coming.repairability;

import com.github.difflib.text.DiffRow;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromDiff;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.output.JSonPatternInstanceOutput;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.repairability.models.InstanceStats;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import org.apache.log4j.Logger;

import java.io.File;

public class JSONRepairabilityOutput extends JSonPatternInstanceOutput {

	Logger log = Logger.getLogger(FineGrainDifftAnalyzer.class.getName());

	public void getInstancesOfRevision(RevisionResult revisionResult, JsonArray instances) {

		String revisionIdentifier = null;
		if (revisionResult.getRelatedRevision() != null) {
			revisionIdentifier = revisionResult.getRelatedRevision().getName();
		}

		PatternInstancesFromRevision result = null;
		result = (PatternInstancesFromRevision) revisionResult.getResultFromClass(RepairabilityAnalyzer.class);
		for (PatternInstancesFromDiff pi : result.getInfoPerDiff()) {
			if (pi.getInstances().size() > 0) {

				Diff diff = pi.getDiff();

				JsonObject instance = new JsonObject();

				instance.addProperty("revision", revisionIdentifier.toString());

				log.info("\n--------\ncommit with instance:\n " + revisionIdentifier);
//              System.out.println("\n--------\ncommit with instance:\n " + revisionIdentifier);
//              log.info(pi.getInstances());
//              System.out.println(pi.getInstances());

				JsonArray repair_tools = new JsonArray();
				for (ChangePatternInstance instancePattern : pi.getInstances()) {

					JsonObject repair = new JsonObject();
					repair.addProperty("tool-name",
							(instancePattern.getPattern().getName().split(File.pathSeparator)[0]));
					repair.addProperty("pattern-name", (instancePattern.getPattern().getName()));
					repair.addProperty("Unified_Diff_of-files:", "Starts Below...");

//                  System.out.println("result.getRow_list()");
//                  System.out.println(result.getRow_list());
					for (DiffRow row : result.getRow_list()) {
						switch (row.getTag()) {
						case INSERT:
							repair.addProperty("INSERT:", row.getNewLine());
							break;
						case DELETE:
							repair.addProperty("DELETE:", row.getOldLine());
							break;
						case CHANGE:
							repair.addProperty("CHANGE_old:", row.getOldLine());
							repair.addProperty("CHANGE_new:", row.getNewLine());
							break;
						}
					}

					JsonArray ops = new JsonArray();

					for (PatternAction pa : instancePattern.getActionOperation().keySet()) {
						Operation op = instancePattern.getActionOperation().get(pa);
						JsonObject opjson = new JsonObject();
						opjson.addProperty("pattern_action", pa.getAction().toString());
						opjson.add("pattern_entity", getJSONFromEntity(pa.getAffectedEntity()));
						opjson.add("concrete_change", getJSONFromOperator(op));

						if (op.getNode().getPosition() != null && op.getNode().getPosition().isValidPosition()) {
							try {
								opjson.addProperty("line", op.getNode().getPosition().getLine());
							} catch (UnsupportedOperationException e) {
								e.printStackTrace();
								opjson.addProperty("line", -1);
							}
							if (op.getNode().getPosition().getFile() != null) {
								opjson.addProperty("file", op.getNode().getPosition().getFile().getAbsolutePath());
							}
						}

						if (isRootOperation(op, diff)) {
							InstanceStats instanceStats = getOperationStats(op);
							opjson.add("stats", getJSONFromInstanceStats(instanceStats));
						}
						ops.add(opjson);
					}

					repair.add("instance_detail", ops);
					repair_tools.add(repair);

				}
				instance.add("repairability", repair_tools);
				instances.add(instance);

			}
		}
	}

	private boolean isRootOperation(Operation op, Diff diff) {
		for (Operation diffOp : diff.getRootOperations()) {
			if (diffOp.getAction().equals(op.getAction())) {
				return true;
			}
		}
		return false;
	}

	private InstanceStats getOperationStats(Operation operation) {
		InstanceStats stats = new InstanceStats();
		if (operation.getSrcNode() != null) {
			stats.setSrcEntityTypes(operation.getSrcNode().getReferencedTypes());
			stats.setNumberOfSrcEntities(operation.getSrcNode().getElements(null).size());
		}
		if (operation.getDstNode() != null) {
			stats.setDstEntityTypes(operation.getDstNode().getReferencedTypes());
			stats.setNumberOfDstEntities(operation.getDstNode().getElements(null).size());
		}
		return stats;
	}
}
