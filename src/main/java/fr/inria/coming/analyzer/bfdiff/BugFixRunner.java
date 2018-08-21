package fr.inria.coming.analyzer.bfdiff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Ignore;

import fr.inria.coming.analyzer.DiffEngineFacade;
import fr.inria.coming.entity.GranuralityType;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class BugFixRunner {

	@SuppressWarnings("unchecked")
	public void run(String path) throws Exception {

		MapCounter<String> counter = new MapCounter<>();
		MapCounter<String> counterParent = new MapCounter<>();
		JSONObject root = new JSONObject();
		JSONArray firstArray = new JSONArray();
		root.put("diffs", firstArray);
		int error = 0;
		int zero = 0;
		int withactions = 0;
		File dir = new File(path);

		int diffanalyzed = 0;
		for (File difffile : dir.listFiles()) {

			if (difffile.isFile() || difffile.listFiles() == null)
				continue;

			if (diffanalyzed % 100 == 0) {
				System.out.println(diffanalyzed + "/" + dir.listFiles().length);
			}

			JSONObject jsondiff = new JSONObject();
			firstArray.add(jsondiff);
			jsondiff.put("diffid", difffile.getName());
			JSONArray filesArray = new JSONArray();
			jsondiff.put("files", filesArray);

			for (File fileModif : difffile.listFiles()) {
				int i_hunk = 0;
				do {

					String pathname = fileModif.getAbsolutePath() + File.separator + difffile.getName() + "_"
							+ fileModif.getName() + "_" + i_hunk;
					File s = new File(pathname + "_s.java");
					if (!s.exists()) {
						break;
					}

					JSONObject file = new JSONObject();
					filesArray.add(file);
					file.put("name", fileModif.getName());
					JSONArray changesArray = new JSONArray();
					file.put("changes", changesArray);

					// System.out.println("-> " + s);

					File t = new File(pathname + "_t.java");
					i_hunk++;
					try {

						Diff diff = getdiff(s, t);
						JSONObject singlediff = new JSONObject();
						changesArray.add(singlediff);
						// singlediff.put("filename", fileModif.getName());
						singlediff.put("rootop", diff.getRootOperations().size());
						JSONArray operationsArray = new JSONArray();

						singlediff.put("operations", operationsArray);
						singlediff.put("allop", diff.getAllOperations().size());

						if (diff.getAllOperations().size() > 0) {
							withactions++;
							for (Operation operation : diff.getRootOperations()) {

								// System.out.println("-op->" + operation);
								counter.add(operation.getNode().getClass().getSimpleName());
								counterParent.add(operation.getAction().getName() + "_"
										+ operation.getNode().getClass().getSimpleName() + "_"
										+ operation.getNode().getParent().getClass().getSimpleName());

								JSONObject op = new JSONObject();
								op.put("operator", operation.getAction().getName());
								op.put("src", (operation.getSrcNode() != null)
										? operation.getSrcNode().getClass().getSimpleName() : "null");
								op.put("dst", (operation.getDstNode() != null)
										? operation.getDstNode().getParent().getClass().getSimpleName() : "null");

								op.put("srcparent", (operation.getSrcNode() != null)
										? operation.getSrcNode().getClass().getSimpleName() : "null");
								op.put("dstparent", (operation.getDstNode() != null)
										? operation.getDstNode().getParent().getClass().getSimpleName() : "null");

								operationsArray.add(op);
							}

						} else {
							zero++;
						}

					} catch (Throwable e) {
						System.out.println("error with " + s);
						// System.out.println("error with " + t);
						e.printStackTrace();
						error++;
						// System.exit(1);
					}
				} while (true);
			}
			diffanalyzed++;
			// if (diffanalyzed == 5) {
			// System.out.println("max-break");
			// break;
			// }
		}

		Map sorted = counter.sorted();
		System.out.println("\n***\nSorted:" + sorted);
		///

		addStats(root, "frequency", sorted);
		addStats(root, "frequencyParent", counterParent.sorted());
		Map prob = counter.getProbabilies();
		addStats(root, "probability", prob);
		Map probParent = counterParent.getProbabilies();
		addStats(root, "probabilityParent", probParent);

		root.put("diffwithactions", withactions);
		root.put("diffzeroactions", zero);
		root.put("differrors", error);

		System.out.println("\n***\nProb: " + counter.getProbabilies());
		System.out.println("Withactions " + withactions);
		System.out.println("Zero " + zero);
		System.out.println("Error " + error);

		System.out.println("JSON: \n" + root);
		FileWriter fw = new FileWriter("./outputanalysis" + (new Date()).toString() + ".json");
		fw.write(root.toJSONString());
		fw.flush();
		fw.close();
	}

	public Diff getdiff(File left, File right) throws Exception {

		DiffEngineFacade cdiff = new DiffEngineFacade();
		// Diff d = cdiff.compareContent(left, right, GranuralityType.SPOON);
		Diff d = cdiff.compareFiles(left, right, GranuralityType.SPOON);
		// System.out.println("-->" + d.getAllOperations().size());
		return d;
	}

	private void addStats(JSONObject root, String key1, Map sorted) {
		JSONArray frequencyArray = new JSONArray();
		root.put(key1, frequencyArray);
		for (Object key : sorted.keySet()) {
			Object v = sorted.get(key);
			JSONObject singlediff = new JSONObject();
			singlediff.put("c", key);
			singlediff.put("f", v);
			frequencyArray.add(singlediff);
		}
	}

	// Buggy Array exception
	@Ignore
	public String read(File file) {
		String s = "";
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				s += (line);

			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("One arg: folder path");
		}
		String path = args[0];
		BugFixRunner runner = new BugFixRunner();
		runner.run(path);
	}
}
