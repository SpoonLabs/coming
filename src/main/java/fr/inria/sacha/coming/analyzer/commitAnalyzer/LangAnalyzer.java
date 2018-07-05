package fr.inria.sacha.coming.analyzer.commitAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.gitanalyzer.interfaces.CommitAnalyzer;
import fr.inria.sacha.gitanalyzer.interfaces.RepositoryP;
import fr.inria.sacha.gitanalyzer.object.CommitGit;
import fr.inria.sacha.gitanalyzer.object.RepositoryPGit;

/**
 * 
 * @author Matias Martinez
 *
 */
public class LangAnalyzer implements CommitAnalyzer {
	Logger log = Logger.getLogger(LangAnalyzer.class.getName());

	public String output = "/tmp/";
	public String prefix = "v";
	public String cloc_path = "/usr/local/bin/cloc";

	List<CommitInfo> commitsProcessed = new ArrayList<>();

	@SuppressWarnings("rawtypes")
	public List<CommitInfo> navigateRepo(String repositoryPath, String masterBranch) {

		RepositoryP repo = new RepositoryPGit(repositoryPath, masterBranch);
		this.commitsProcessed.clear();

		// For each commit of a repository
		List<Commit> history = repo.history();
		int i = 0;

		for (Commit c : history) {

			this.analyze(c);

			i++;
		}

		log.info("\n commits analyzed " + i);
		return this.commitsProcessed;
	}

	@Override
	public Object analyze(Commit commit) {

		CommitGit c = (CommitGit) commit;
		String repositoryPath = c.getRepository().getRepository().getDirectory().getAbsolutePath();
		log.debug("Commit ->:  " + c.getName());
		try {
			run(repositoryPath, "git reset --hard master".split(" "));
			File diro = new File(output + prefix + c.getName());
			diro.mkdirs();
			run(repositoryPath,
					("git --work-tree=" + diro.getAbsolutePath() + " checkout " + c.getName() + " .").split(" "));

			List<String> ls = run(repositoryPath, new String[] { cloc_path, diro.getAbsolutePath() });
			Map<String, Integer[]> langcommit = getLanguages(ls);
			this.commitsProcessed.add(new CommitInfo(c.getName(), langcommit));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return commitsProcessed;
	}

	private List<String> run(String repositoryPath, String[] command) throws Exception {
		Process p = null;
		// ProcessBuilder pb = new ProcessBuilder("/bin/bash");
		ProcessBuilder pb = new ProcessBuilder();

		pb.directory(new File(repositoryPath));
		pb.command(command);
		p = pb.start();

		// p.waitFor(2, TimeUnit.SECONDS);
		p.waitFor();
		java.util.List<String> ls = readOutput(p);

		p.destroy();
		return ls;

	}

	public static List<String> readOutput(Process p) throws IOException {
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

		List<String> lines = new ArrayList<>();
		String s = null;
		while ((s = stdInput.readLine()) != null) {

			lines.add(s);
		}

		stdInput.close();

		return lines;
	}

	public static List<String> readError(Process p) throws IOException {

		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		List<String> lines = new ArrayList<>();
		String s = null;
		while ((s = stdError.readLine()) != null) {

			lines.add(s);
		}

		stdError.close();

		return lines;
	}

	// files blank comment code
	public Map<String, Integer[]> getLanguages(List<String> lines) {
		Map<String, Integer[]> stats = new HashMap<>();
		boolean activate = false;
		for (String l : lines) {
			if (!activate && l.startsWith("Language")) {
				activate = true;
				// we activate and go the next line
				continue;
			} else {
				if (activate && l.startsWith("SUM")) {
					activate = false;
					continue;
				}
			}
			if (!activate || l.startsWith("--"))
				continue;
			String[] aline = l.split(" ");
			// FORMAT: Language files blank comment code
			Integer[] result = new Integer[4];
			int i = 0;
			String name = "";
			for (String a : aline) {
				if (a.trim().isEmpty())
					continue;
				if (i == 0) {
					name = a;
					i++;
				} else {

					try {
						int statistic = Integer.parseInt(a);
						result[i - 1] = statistic;
						i++;
					} catch (NumberFormatException e) {
						// nothing
					}
				}
			}
			stats.put(name, result);
		}
		return stats;
	}

	public class CommitInfo {
		String commitid;
		Map<String, Integer[]> stats;

		public CommitInfo(String commitid, Map<String, Integer[]> stats) {
			super();
			this.commitid = commitid;
			this.stats = stats;
		}

		public String getCommitid() {
			return commitid;
		}

		public void setCommitid(String commitid) {
			this.commitid = commitid;
		}

		public Map<String, Integer[]> getStats() {
			return stats;
		}

		public void setStats(Map<String, Integer[]> stats) {
			this.stats = stats;
		}

		@Override
		public String toString() {

			String s = "ci->" + commitid + ":\n";
			for (String lang : this.stats.keySet()) {
				s += "--" + lang + ":\n";
				s += " code:" + this.stats.get(lang)[3] + "\n";// make an enum
			}
			return s;
		}

		@SuppressWarnings("unchecked")
		public JSONObject toJSON() {

			JSONObject root = new JSONObject();
			root.put("commitid", commitid);

			JSONArray languages = new JSONArray();
			root.put("languages", languages);

			for (String lang : this.stats.keySet()) {
				JSONObject language = new JSONObject();
				languages.add(language);
				language.put("langname", lang);
				// files blank comment code
				language.put("files", this.stats.get(lang)[0]);
				language.put("blank", this.stats.get(lang)[1]);
				language.put("comment", this.stats.get(lang)[2]);
				language.put("code", this.stats.get(lang)[3]);

			}
			return root;
		}

	}

	public List<CommitInfo> getCommitsProcessed() {
		return commitsProcessed;
	}

	public void setCommitsProcessed(List<CommitInfo> commitsProcessed) {
		this.commitsProcessed = commitsProcessed;
	}

	@SuppressWarnings("unchecked")
	public JSONObject resultToJSON() {

		JSONObject root = new JSONObject();
		JSONArray commits = new JSONArray();
		root.put("commits", commits);

		for (CommitInfo commitInfo : commitsProcessed) {
			commits.add(commitInfo.toJSON());
		}
		return root;
	}

}
