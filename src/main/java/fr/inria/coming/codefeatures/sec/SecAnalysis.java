package fr.inria.coming.codefeatures.sec;

import java.util.List;
import java.util.Map;

import com.github.gumtreediff.tree.ITree;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.utils.MapList;
import fr.inria.coming.utils.OperationClassifier;
import gumtree.spoon.builder.Json4SpoonGenerator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.DiffImpl;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SecAnalysis {

	public static JsonObject createJSonOfOperation(MapList<Operation, Operation> operationHierarchy,
			Operation operation, Diff iDiff) {
		JsonObject change = new JsonObject();
		if (operation.getSrcNode() != null) {
			change.addProperty("change_type", operation.getAction().getName());
			change.addProperty("entity_type",
					operation.getSrcNode().getClass().getSimpleName().replace("Ct", "").replace("Impl", ""));

			// code source
			change.addProperty("content", operation.getSrcNode().toString());

			//
			Json4SpoonGenerator jsongen = new Json4SpoonGenerator();

			if (operation instanceof InsertOperation) {
				JsonObject ast = jsongen.getJSONasJsonObject(((DiffImpl) iDiff).getContext(),
						operation.getAction().getNode());
				change.add("ast_node_inserted", ast);
			} else if (operation instanceof DeleteOperation) {
				JsonObject ast = jsongen.getJSONasJsonObject(((DiffImpl) iDiff).getContext(),
						operation.getAction().getNode());
				change.add("ast_node_deleted", ast);
			} else if (operation instanceof UpdateOperation) {
				JsonObject ast = jsongen.getJSONasJsonObject(((DiffImpl) iDiff).getContext(),
						operation.getAction().getNode());
				change.add("ast_node_updated_previous", ast);

				ITree dest = iDiff.getMappingsComp().getDst(operation.getAction().getNode());
				JsonObject ast_dst = jsongen.getJSONasJsonObject(((DiffImpl) iDiff).getContext(), dest);
				change.add("ast_node_updated_post", ast_dst);

			} else if (operation instanceof MoveOperation) {
				JsonObject ast = jsongen.getJSONasJsonObject(((DiffImpl) iDiff).getContext(),
						operation.getAction().getNode());
				change.add("ast_node_moved", ast);
			}

			JsonArray subchanges = getSubChanges(operationHierarchy, operation, iDiff);
			change.add("sub-changes", subchanges);

		}
		return change;
	}

	public static JsonArray getSubChanges(MapList<Operation, Operation> operationHierarchy, Operation operation,
			Diff iDiff) {
		JsonArray subChanges = new JsonArray();
		if (operationHierarchy.containsKey(operation)) {

			List<Operation> operations = operationHierarchy.get(operation);
			for (Operation soonOperation : operations) {
				JsonObject subchange = createJSonOfOperation(operationHierarchy, soonOperation, iDiff);
				subChanges.add(subchange);
			}
		}

		return subChanges;
	}

	public static JsonObject computeJSonOfRev(IRevision rev, DiffResult<IRevision, Diff> result,
			Map<String, VulInstance> vtypes) {

		JsonObject rootRevision = new JsonObject();
		rootRevision.addProperty("revision_id", rev.getName());
		rootRevision.addProperty("vulnerability_type", vtypes.get(rev.getName()).getType());
		rootRevision.addProperty("project", vtypes.get(rev.getName()).getProject());
		rootRevision.addProperty("cve", vtypes.get(rev.getName()).getCVE());

		int numbersOfFiles = result.getDiffOfFiles().keySet().size();

		rootRevision.addProperty("number_files_affected", numbersOfFiles);
		int nrChangesInRevision = 0;
		JsonArray files = new JsonArray();
		rootRevision.add("files", files);

		for (String fileName : result.getDiffOfFiles().keySet()) {
			Diff iDiff = result.getDiffOfFiles().get(fileName);
			processDiff(files, fileName, iDiff);

			nrChangesInRevision += iDiff.getRootOperations().size();
		}

		rootRevision.addProperty("number_changes_in_diff", nrChangesInRevision);

		return rootRevision;
	}

	public static void processDiff(JsonArray files, String fileName, Diff iDiff) {
		JsonObject fileJson = new JsonObject();
		fileJson.addProperty("filename", fileName);
		files.add(fileJson);
		JsonArray changes = new JsonArray();

		fileJson.add("changes", changes);

		// Splits operations, creating an hierarchy

		MapList<Operation, Operation> operationHierarchy = OperationClassifier.getOperationHierarchy(iDiff);

		// Get Root Operations
		List<Operation> ops = iDiff.getRootOperations();
		for (Operation operation : ops) {

			JsonObject change = createJSonOfOperation(operationHierarchy, operation, iDiff);

			changes.add(change);

		}
	}
}
