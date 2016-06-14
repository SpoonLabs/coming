package fr.inria.sacha.coming.analyzer.commitAnalyzer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.inria.sacha.coming.analyzer.DiffResult;
import fr.inria.sacha.coming.analyzer.DiffEngineFacade;
import fr.inria.sacha.coming.analyzer.Parameters;
import fr.inria.sacha.coming.entity.GranuralityType;
import fr.inria.sacha.coming.util.ConfigurationProperties;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.gitanalyzer.interfaces.CommitAnalyzer;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
import fr.labri.gumtree.actions.model.Action;

/**
 * Commit analyzer: It searches fine grain changes.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class FineGrainChangeCommitAnalyzer implements CommitAnalyzer {

	Logger log = Logger
			.getLogger(FineGrainChangeCommitAnalyzer.class.getName());

	protected GranuralityType granularity;

	
	IChangesProcessor processor; 
	
	public FineGrainChangeCommitAnalyzer(SimpleChangeFilter filter, GranuralityType granularity) {

		this.processor = filter;
		this.granularity = granularity;

	}

	public FineGrainChangeCommitAnalyzer(SimpleChangeFilter filter) {

		this.processor = filter;
		
		granularity = GranuralityType.valueOf(ConfigurationProperties
				.getProperty("GRANULARITY"));
	}
	
	/**
	 * 
	 * @param typeLabel
	 *            node label to mine
	 * @param operationType
	 *            operation type to mine
	 */
	public FineGrainChangeCommitAnalyzer() {

		granularity = GranuralityType.valueOf(ConfigurationProperties
				.getProperty("GRANULARITY"));
	}


	/**
	 * Analyze a commit finding instances of changes return a Map<FileCommit,
	 * List>
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object analyze(Commit commit) {
				
		// Retrieve a list of file affected by the commit
		List<FileCommit> javaFiles = commit.getJavaFileCommits();
		
		//System.out.println("files "+javaFiles);
		int nChanges = 0;


		// The result is divided by File from the commit.
		Map<FileCommit, List> changeInstancesInCommit = new HashMap<FileCommit, List>();

		for (FileCommit fileCommit : javaFiles) {
			if (fileCommit.getCompletePath().toLowerCase().contains("test")
					|| fileCommit.getCompletePath().toLowerCase()
							.endsWith("package-info.java")) {

				continue;
			}

			String left = fileCommit.getPreviousVersion();
			String right = fileCommit.getNextVersion();

			if (!left.trim().isEmpty()) {

				List<Action> actions;

				try {
					DiffResult diff =   this.compareContent(left, right, granularity);
					//todo
					actions = //diff.getAllActions();
						diff.getRootActions();

					String name = commit.getName();
//					System.out.println(name + actions);
					if (actions == null
							|| actions.size() > Parameters.MAX_AST_CHANGES_PER_FILE
							|| actions.size() < Parameters.MIN_AST_CHANGES_PER_FILE) {
						continue;
					}

					if (actions.size() > 0) {
						
						List<Action> filterActions = processCommit(diff);
						nChanges += filterActions.size();

						if (filterActions.size() > 0) 
							changeInstancesInCommit.put(fileCommit,
									filterActions); 
					}
				} catch (Exception e) {
					throw new RuntimeException(e);

				}
			}
		}

		return changeInstancesInCommit;
	}

	private List<Action> processCommit(DiffResult diff) {
		
		if(this.processor == null)
			return diff.getRootActions();
		
		return this.processor.process(diff);
	}

	public DiffResult compareContent(String left, String right, GranuralityType granularity) throws Exception {
	
		DiffEngineFacade cdiff = new DiffEngineFacade();
		return cdiff.compareContent(left, right, granularity);

	}


}