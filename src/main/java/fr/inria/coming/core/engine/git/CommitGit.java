package fr.inria.coming.core.engine.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.gitective.core.BlobUtils;

import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.core.entities.interfaces.RepositoryP;
import fr.inria.coming.main.ComingProperties;

public class CommitGit implements Commit {

	private RepositoryP repo;
	private RevCommit revCommit;
	static public String[] extensionToConsider = new String[] { ".java" };

	static {
		String extension_to_consider = ComingProperties.getProperty("extensions_to_consider");

		if (extension_to_consider != null) {
			extensionToConsider = extension_to_consider.split(File.pathSeparator);
		}
	}

	public CommitGit(RepositoryP repository, RevCommit revCmt) {
		this.repo = repository;
		this.revCommit = revCmt;

	}

	@Override
	public List<FileCommit> getFileCommits() {
		List<FileCommit> resultFileCommits = new ArrayList<FileCommit>();

		RevWalk rw = new RevWalk(this.repo.getRepository());
		try {
			TreeWalk tw = new TreeWalk(this.repo.getRepository());
			tw.reset();
			tw.setRecursive(true);
			tw.addTree(revCommit.getTree());

			if (revCommit.getParentCount() == 0) {
				// initial commit, is not a diff with a prev version
				String filePrevVersion = "";
				while (tw.next()) {
				}
				tw.release();
				return resultFileCommits;
			} else {
				for (RevCommit rc : revCommit.getParents()) {
					tw.addTree(rc.getTree());
				}
				tw.setFilter(new MyTreeFilter());

				DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
				df.setRepository(this.repo.getRepository());
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);

				for (int i = 0; i < revCommit.getParentCount(); i++) {
					RevCommit parent = rw.parseCommit(this.revCommit.getParent(i).getId());
					List<DiffEntry> diffs = df.scan(parent.getTree(), this.revCommit.getTree());
					// --
					/*
					 * RenameDetector rd = new RenameDetector(this.repo.getRepository());
					 * rd.addAll(diffs); List<DiffEntry> lde = rd.compute(); for (DiffEntry de :
					 * lde) { if (de.getScore() >= rd.getRenameScore()) {
					 * ("score "+de.getScore()); System.out.println("file: " +
					 * de.getOldPath() + " copied/moved to: " + de.getNewPath()); } }
					 */
					// --
					for (DiffEntry diff : diffs) {

						//System.err.println(diff.getChangeType());

						if (diff.getChangeType().equals(ChangeType.DELETE)) {
							// Martin removed the support for removed files
							// because the prev § next assumption is blurry later in code
						}
						if (diff.getChangeType().equals(ChangeType.MODIFY)) {

							String previousCommitName = this.revCommit.getParent(0).getName();
							String filePrevVersion = getFileContent(this.revCommit.getParent(0).getId(),
									diff.getOldPath());

							// To retrieve file name
							final String fileNextVersion = getFileContent(this.revCommit.getId(), diff.getNewPath());
							if (fileNextVersion == null || fileNextVersion.length() == 0) {
								System.err.println(diff);
								throw new RuntimeException("Empty file content for " + diff.getNewPath());
							}
							File src = File.createTempFile(previousCommitName+"_","_s.java");
							File target = new File(src.getAbsolutePath().replace("_s.java", "_t.java"));
							try(FileOutputStream fs = new FileOutputStream(src)) {
								IOUtils.write(filePrevVersion, fs);
							}
							try(FileOutputStream fs = new FileOutputStream(target)){
								IOUtils.write(fileNextVersion, fs);
							}
							if (filePrevVersion!="") {
								FileCommit file = new FileCommitGit(src.getAbsolutePath(), filePrevVersion, target.getAbsolutePath(), fileNextVersion, this);
								resultFileCommits.add(file);
							}

						}
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			rw.dispose();
		}

		return resultFileCommits;
	}

	@Override
	public List<FileCommit> getJavaFileCommits() {
		List<FileCommit> files = getFileCommits();
		return filter(files, ".java");
	}

	public List<FileCommit> filter(List<FileCommit> files, String extension) {
		List<FileCommit> javaFiles = new ArrayList<FileCommit>();

		for (FileCommit fileCommit : files) {
			if (fileCommit.getNextName().endsWith(extension) || fileCommit.getPreviousName().endsWith(extension))
				javaFiles.add(fileCommit);
		}

		return javaFiles;
	}

	@Override
	public List<FileCommit> getFileCommits(String extension) {
		List<FileCommit> files = getFileCommits();
		return filter(files, extension);

	}

	@Override
	public String getName() {

		return this.revCommit.getName();
	}

	@Override
	public List<String> getParents() {

		List<String> parents = new ArrayList();
		for (RevCommit rc : this.revCommit.getParents()) {
			parents.add(rc.getName());
		}

		return parents;
	}

	@Override
	public List<String> getBranches() {

		List<String> branches = new ArrayList();
		// https://www.eclipse.org/forums/index.php/t/280339/
		RevWalk walk = new RevWalk(repo.getRepository());
		for (Map.Entry<String, Ref> e : repo.getRepository().getAllRefs().entrySet())

			if (e.getKey().startsWith(Constants.R_HEADS) || e.getKey().startsWith(Constants.R_REMOTES))
				try {
					RevCommit parseCommit = walk.parseCommit(e.getValue().getObjectId());
					RevCommit base = walk.parseCommit(this.revCommit.toObjectId());

					if (walk.isMergedInto(base, parseCommit)) {
						branches.add(e.getValue().getName());
					}

				} catch (Exception e1) {

					e1.printStackTrace();
				}

		return branches;
	}

	@Override
	public PersonIdent getAuthorInfo() {
		return this.revCommit.getAuthorIdent();

	}

	@Override
	public boolean containsJavaFile() {
		List<FileCommit> javaFiles = getJavaFileCommits();
		return !javaFiles.isEmpty();
	}

	@Override
	public String getShortMessage() {
		return this.revCommit.getShortMessage();
	}

	public String getFullMessage() {
		return this.revCommit.getFullMessage();
	}

	@Override
	public int getRevCommitTime() {
		return this.revCommit.getCommitTime();
	}

	@Override
	public String getRevDate() {
		PersonIdent authorIdent = revCommit.getAuthorIdent();
		Date authorDate = authorIdent.getWhen();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		return ft.format(authorDate);
	}

	private String getFileContent(ObjectId idRevCommit, String pathOfFile) {
		Repository jgitRepo = this.repo.getRepository();

		// Retrieve ID of files
		ObjectId fileId = BlobUtils.getId(jgitRepo, idRevCommit, pathOfFile);

		// Retrieve the files content
		String file = "";

		// If the file is new or has been deleted
		if (fileId != null) {
			file = BlobUtils.getContent(jgitRepo, fileId);
		}

		return file;
	}

	public RepositoryP getRepository() {
		return repo;
	}

	public void setRepository(RepositoryP repo) {
		this.repo = repo;
	}

	@Override
	public List<IRevisionPair> getChildren() {
		if (extensionToConsider == null || extensionToConsider.length == 0) {
			List<IRevisionPair> li = new ArrayList<>(this.getFileCommits());
			return li;
		} else {

			List<IRevisionPair> li = new ArrayList<>();
			for (String ext : extensionToConsider) {
				li.addAll(this.getFileCommits(ext));
			}

			return li;
		}

	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getFolder() {
		// TODO Auto-generated method stub
		return null;
	}

}

class MyTreeFilter extends TreeFilter {

	@Override
	public TreeFilter clone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean include(TreeWalk walker) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int n = walker.getTreeCount();
		if (n == 1) {
			return true;
		}

		int m = walker.getRawMode(0);
		int i = 1;

		while (i < n) {
			if (walker.getRawMode(i) == m && walker.idEqual(i, 0)) {
				return false;
			}
			i++;
		}
		return true;
	}

	@Override
	public boolean shouldBeRecursive() {
		throw new UnsupportedOperationException();
	}

}
