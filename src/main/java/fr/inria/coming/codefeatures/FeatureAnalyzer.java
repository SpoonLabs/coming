package fr.inria.coming.codefeatures;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.LineFilter;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FeatureAnalyzer {
	private static final LineFilter FILTER = new LineFilter();

	@SuppressWarnings("unchecked")
	public JsonArray processDiff(File difffile) {
		Map<String, Diff> diffOfcommit = new HashMap();
		CodeFeatureDetector cresolver = new CodeFeatureDetector();
		JsonArray filesArray = new JsonArray();
		for (File fileModif : difffile.listFiles()) {
			int i_hunk = 0;

			if (".DS_Store".equals(fileModif.getName()))
				continue;

			String pathname = fileModif.getAbsolutePath() + File.separator + difffile.getName() + "_"
					+ fileModif.getName();

			File previousVersion = new File(pathname + "_s.java");
			if (!previousVersion.exists()) {
				pathname = pathname + "_" + i_hunk;
				previousVersion = new File(pathname + "_s.java");
				if (!previousVersion.exists())
					// break;
					continue;
			}

			JsonObject file = new JsonObject();
			filesArray.add(file);
			file.addProperty("file_name", fileModif.getName());
			JsonArray changesArray = new JsonArray();
			file.add("features", changesArray);

			File postVersion = new File(pathname + "_t.java");
			i_hunk++;
			try {
				AstComparator comparator = new AstComparator();

				Diff diff = comparator.compare(previousVersion, postVersion);
				if (diff == null) {
					file.addProperty("status", "differror");
					continue;
				}

				System.out.println("--diff: " + diff);

				List<Operation> ops = diff.getRootOperations();
				String key = fileModif.getParentFile().getName() + "_" + fileModif.getName();
				diffOfcommit.put(key, diff);

				for (Operation operation : ops) {
					CtElement affectedCtElement = getLeftElement(operation);
					//
					if (affectedCtElement != null) {
						Cntx iContext = cresolver.retrieveCntx(affectedCtElement);
						changesArray.add(iContext.toJSON());
					}
				}

			} catch (Throwable e) {
				System.out.println("error with " + previousVersion);
				e.printStackTrace();
				file.addProperty("status", "exception");
			}

		}
		return filesArray;
	}

	/**
	 * Get the element that is modified
	 * 
	 * @param operation
	 * @return
	 */
	public CtElement getLeftElement(Operation operation) {

		CtElement affectedCtElement = null;

		if (operation instanceof MoveOperation) {

			// Element to move in source
			CtElement affectedMoved = operation.getSrcNode();

			affectedCtElement = affectedMoved;

		} else if (operation instanceof InsertOperation) {

			CtElement oldLocation = ((InsertOperation) operation).getParent();

			affectedCtElement = oldLocation;

		} else if (operation instanceof DeleteOperation) {

			CtElement oldLocation = operation.getSrcNode();

			affectedCtElement = oldLocation;

		} else if (operation instanceof UpdateOperation) {

			CtElement oldLocation = operation.getSrcNode();

			affectedCtElement = oldLocation;
		}
		// Let's find the parent statement
		CtStatement parentLine = affectedCtElement.getParent(FILTER);
		if (parentLine != null)
			return parentLine;
		// by default, we return the affected element
		return affectedCtElement;

	}

}
