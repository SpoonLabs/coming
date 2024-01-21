package fr.inria.coming.core.engine.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.RepositoryP;

public class RepositoryPGit implements RepositoryP {

	private Repository repository;

	private List<Commit> commits;
	private String masterBranch;
	private Collection<String> filter;

	public RepositoryPGit(String pathOfRepo, String branch) {
		this(pathOfRepo, branch, null);
	}

	/**
	 * Init a Git repository navigation
	 * 
	 * @param pathOfRepo
	 *            The path of the git repository
	 * @param comp
	 *            The comparator used to set the grain of fragments
	 * @param branch
	 *            The branch to analyze
	 */
	public RepositoryPGit(String pathOfRepo, String branch, Collection<String> filter) {
		if (!new File(pathOfRepo).exists()) {
			throw new RuntimeException("repository path (arg --location) does not exist");
		}
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		this.filter = filter;
		String path = pathOfRepo;
		if (!path.endsWith("/"))
			path = path + "/";
		path = path + ".git";
		try {
			repository = builder.setGitDir(new File(path)).readEnvironment().findGitDir().build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		commits = new ArrayList<Commit>();
		masterBranch = branch;
		try {
			loadCommits();
		} catch (RevisionSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AmbiguousObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Load all commits of the repository
	 * 
	 * @throws RevisionSyntaxException
	 * @throws AmbiguousObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	private void loadCommits()
			throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		RevWalk revWalk = new RevWalk(repository);
		ObjectId from = repository.resolve(masterBranch);
		revWalk.markStart(revWalk.parseCommit(from));
		if (this.filter == null || this.filter.isEmpty())
			revWalk.setTreeFilter(TreeFilter.ALL);
		else {
			TreeFilter filter = AndTreeFilter.create(PathFilterGroup.createFromStrings(this.filter),
					TreeFilter.ANY_DIFF);
			revWalk.setTreeFilter(filter);
		}
		revWalk.sort(RevSort.REVERSE, true);

		for (RevCommit c : revWalk) {
			// detectRenames(c.getTree());
			Commit commit = new CommitGit(this, c);
			commits.add(commit);
		}
		Git git = new Git(repository);
		List<Ref> call;
		try {
			call = git.branchList().setListMode(ListMode.ALL).call();
			// call = git.init().setDirectory(new File("")).call();

			for (Ref ref : call) {
				// System.out.println("Branch: " + ref + " " + ref.getName() +
				// " " + ref.getObjectId().getName());
			}

			List<Ref> call2 = new Git(repository).tagList().call();
			for (Ref ref : call2) {

				// System.out.println("Tag: " + ref + " " + ref.getName() + " "
				// + ref.getObjectId().getName());
			}
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void detectRenames(RevTree revTree)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		TreeWalk tw = new TreeWalk(repository);
		tw.setRecursive(true);
		tw.addTree(revTree);
		tw.addTree(new FileTreeIterator(repository));
		RenameDetector rd = new RenameDetector(repository);
		rd.addAll(DiffEntry.scan(tw));

		List<DiffEntry> lde = rd.compute(/* tw.getObjectReader(), null */);
		for (DiffEntry de : lde) {
			if (de.getScore() >= rd.getRenameScore()) {
				System.out.println("file: " + de.getOldPath() + " copied/moved to: " + de.getNewPath() + " ");
			}
		}
	}

	@Override
	public List<Commit> history() {
		return this.commits;
	}

	@Override
	public Repository getRepository() {
		return this.repository;
	}
}
