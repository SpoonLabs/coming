package diffanalyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import fr.inria.astor.core.entities.Cntx;
import fr.inria.astor.core.entities.CntxResolver;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DiffICSE15ContextAnalyzer extends BugFixRunner {
	File out = null;

	public DiffICSE15ContextAnalyzer() {
		super();
		// ConfigurationProperties.properties.setProperty("maxdifftoanalyze", "5");

		out = new File("./outDiffAnalysis/");
		out.mkdirs();
	}

	Map<String, List<Operation>> opsOfcommit = new HashMap();

	@Override
	public void processDiff(File fileModif, Diff diff) {
		// System.out.println("Processing diff");
		List<Operation> ops = diff.getRootOperations();
		String key = fileModif.getParentFile().getName() + "_" + fileModif.getName();
		this.opsOfcommit.put(key, ops);

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

		JSONObject statsjsonRoot = new JSONObject();
		statsjsonRoot.put("diffid", difffile.getName());
		JSONArray sublistJSon = new JSONArray();
		statsjsonRoot.put("info", sublistJSon);

		for (String key : this.opsOfcommit.keySet()) {

			List<Operation> ops = this.opsOfcommit.get(key);
			for (Operation operation : ops) {
				CntxResolver cresolver = new CntxResolver();

				Cntx iContext = cresolver.retrieveCntx(operation.getSrcNode());
				iContext.setIdentifier(key);

				JSONObject opContext = new JSONObject();
				opContext.put("key", key);
				opContext.put("cntx", iContext.toJSON());
				opContext.put("ops", this.getJSONFromOperator(operation));

				sublistJSon.add(opContext);
			}

		}
		opsOfcommit.clear();
		try {

			FileWriter fw = new FileWriter(out.getAbsolutePath() + File.separator + difffile.getName() + ".json");

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(statsjsonRoot.toJSONString());
			String prettyJsonString = gson.toJson(je);
			fw.write(prettyJsonString);

			fw.flush();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
