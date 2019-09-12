package fr.inria.coming.spoon.patterns;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import gumtree.spoon.diff.operations.Operation;
import net.lingala.zip4j.core.ZipFile;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class GitRepository4Test {

	public static Commit getCommit(Map<Commit, List<Operation>> instancesFound, String commit) {
		for (Commit c : instancesFound.keySet()) {
			if (c.getName().equals(commit)) {
				// return true;
				List<Operation> ops = instancesFound.get(c);
				if ((ops != null && !ops.isEmpty())) {
					return c;
				}

			}

		}
		return null;
	}

	public static boolean containsCommit(Map<Commit, List<Operation>> instancesFound, String commit) {
		for (Commit c : instancesFound.keySet()) {
			if (c.getName().equals(commit)) {
				// return true;
				List<Operation> ops = instancesFound.get(c);
				return (ops != null && !ops.isEmpty());
			}

		}
		return false;
	}

	protected boolean containsCommit(Map<Commit, List<Operation>> instancesFound, String commit, String typeLabel) {
		for (Commit c : instancesFound.keySet()) {
			for (FileCommit fc : c.getFileCommits()) {
				if (fc.getCommit().getName().equals(commit)) {
					List<Operation> actions = instancesFound.get(c);
					for (Operation action : actions) {
						String type = action.getNode().getClass().getSimpleName();
						// SpoonGumTreeBuilder.gtContext.getTypeLabel(action.getNode().getType());
						if (type != null && type.substring(2, type.length() - 4).equals(typeLabel))
							return true;

					}
				}
			}
		}
		return false;
	}

}
