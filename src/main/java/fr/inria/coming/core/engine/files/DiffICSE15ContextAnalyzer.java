package fr.inria.coming.core.engine.files;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.inria.astor.core.entities.Cntx;
import fr.inria.astor.core.entities.CntxResolver;
import fr.inria.coming.main.ComingProperties;
import gumtree.spoon.builder.Json4SpoonGenerator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.DiffImpl;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DiffICSE15ContextAnalyzer extends BugFixRunner {
	File out = null;

	public DiffICSE15ContextAnalyzer() {
		super();
		ComingProperties.properties.setProperty("maxdifftoanalyze", "5");

		out = new File("./outDiffAnalysis/");
		out.mkdirs();
	}

	Map<String, Diff> diffOfcommit = new HashMap();

	@Override
	public void processDiff(File fileModif, Diff diff) {
		List<Operation> ops = diff.getRootOperations();
		String key = fileModif.getParentFile().getName() + "_" + fileModif.getName();
		this.diffOfcommit.put(key, diff);
	}

	@Override
	protected boolean acceptFile(File fileModif) {
		File f = new File(out.getAbsolutePath() + File.separator + fileModif.getName() + ".json");
		// If the json file does not exist, we process it
		return !f.exists();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void atEndCommit(File difffile) {
		try {

			JSONObject statsjsonRoot = calculateCntxJSON(difffile.getName(), diffOfcommit);

			FileWriter fw = new FileWriter(out.getAbsolutePath() + File.separator + difffile.getName() + ".json");

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(statsjsonRoot.toJSONString());
			String prettyJsonString = gson.toJson(je);
			fw.write(prettyJsonString);

			fw.flush();
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		diffOfcommit.clear();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JSONObject calculateCntxJSON(String id, Map<String, Diff> operations) {
		JSONObject statsjsonRoot = new JSONObject();
		statsjsonRoot.put("diffid", id);
		JSONArray sublistJSon = new JSONArray();
		statsjsonRoot.put("info", sublistJSon);

		for (String modifiedFile : operations.keySet()) {

			Diff diff = operations.get(modifiedFile);
			List<Operation> ops = diff.getRootOperations();

			System.out.println("Diff file " + modifiedFile + " " + ops.size());
			for (Operation operation : ops) {
				CntxResolver cresolver = new CntxResolver();

				Cntx iContext = cresolver.retrieveCntx(operation.getSrcNode());
				iContext.setIdentifier(modifiedFile);

				JSONObject opContext = new JSONObject();
				opContext.put("key", modifiedFile);
				opContext.put("cntx", iContext.toJSON());
				opContext.put("ops", this.getJSONFromOperator(operation));

				calculateJSONAffectedMethod(diff, operation, opContext);
				calculateJSONAffectedElement(diff, operation, opContext);
				sublistJSon.add(opContext);
			}

		}
		return statsjsonRoot;
	}

	private void calculateJSONAffectedMethod(Diff diff, Operation operation, JSONObject opContext) {

		CtMethod methodOfOperation = operation.getNode().getParent(CtMethod.class);
		Json4SpoonGenerator jsongen = new Json4SpoonGenerator();

		Action affectedAction = operation.getAction();
		ITree affected = affectedAction.getNode();
		// jsongen.getJSONasJsonObject(

		ITree methodTreeNode = null;
		do {
			CtElement relatedCtElement = (CtElement) affected.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			if (methodOfOperation == relatedCtElement) {// same object
				methodTreeNode = affected;
			}
			affected = affected.getParent();
		} while (methodTreeNode == null && affected.getParent() != null);
		//
		if (methodTreeNode != null) {
			JsonObject jsonT = jsongen.getJSONasJsonObject(((DiffImpl) diff).getContext(), methodTreeNode,
					diff.getAllOperations());
			// TODO: refactor!!!
			org.json.simple.parser.JSONParser p = new JSONParser();
			try {
				JSONObject jsonTsimple = (JSONObject) p.parse(jsonT.toString());
				opContext.put("method", jsonTsimple);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

	}

	static List emptyList = new ArrayList();

	private void calculateJSONAffectedElement(Diff diff, Operation operation, JSONObject opContext) {

		operation.getNode();
		Json4SpoonGenerator jsongen = new Json4SpoonGenerator();

		JsonObject jsonT = jsongen.getJSONasJsonObject(((DiffImpl) diff).getContext(), operation.getAction().getNode(),
				emptyList);
		org.json.simple.parser.JSONParser p = new JSONParser();
		try {
			JSONObject jsonTsimple = (JSONObject) p.parse(jsonT.toString());
			opContext.put("ast_element", jsonTsimple);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
