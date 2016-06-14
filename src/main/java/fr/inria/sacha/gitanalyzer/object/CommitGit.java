package fr.inria.sacha.gitanalyzer.object;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.gitective.core.BlobUtils;

import fr.inria.sacha.gitanalyzer.implementation.FileCommitGit;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
import fr.inria.sacha.gitanalyzer.interfaces.RepositoryP;



public class CommitGit implements Commit {
	
	private RepositoryP repo;
	private RevCommit revCommit;

	public CommitGit(RepositoryP repository, RevCommit revCmt) {
		this.repo = repository;
		this.revCommit = revCmt;
	}
	
	@Override
	public List<FileCommit> getFileCommits() {
		List<FileCommit> ret = new ArrayList<FileCommit>();

		RevWalk rw = new RevWalk(this.repo.getRepository());
		try {
			TreeWalk tw = new TreeWalk(this.repo.getRepository());
			tw.reset();
			tw.setRecursive(true);
			tw.addTree(revCommit.getTree());

			if (revCommit.getParentCount() == 0) {
				while (tw.next()) {
					// To retrieve file name
					String fileNextVersion = getFileContent(this.revCommit
							.getId(), tw.getPathString());
					FileCommit file = new FileCommitGit("", "", tw
							.getPathString(), fileNextVersion, this);
					ret.add(file);
				}
				tw.release();
				return ret;
			} else {
				for (RevCommit rc : revCommit.getParents()) {
					tw.addTree(rc.getTree());
				}
				tw.setFilter(new MyTreeFilter());

				List<String> tmp = new ArrayList<String>();
				while (tw.next()) {
					tmp.add(tw.getPathString());
				}

				DiffFormatter df = new DiffFormatter(
						DisabledOutputStream.INSTANCE);
				df.setRepository(this.repo.getRepository());
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);

				for (int i = 0; i < revCommit.getParentCount(); i++) {
					RevCommit parent = rw.parseCommit(this.revCommit.getParent(
							i).getId());
					List<DiffEntry> diffs = df.scan(parent.getTree(),
							this.revCommit.getTree());
					//--
					/*RenameDetector rd = new RenameDetector(this.repo.getRepository());
					rd.addAll(diffs);
					List<DiffEntry> lde = rd.compute();
					for (DiffEntry de : lde) {
					    if (de.getScore() >= rd.getRenameScore()) {
					    	System.out.println("score "+de.getScore());
					        System.out.println("file: " + de.getOldPath() + " copied/moved to: " + de.getNewPath());
					    }
					}*/
					//--
					for (DiffEntry diff : diffs) {
						//
						//System.out.println(diff.getChangeType()+" "+diff.getNewPath());
						
						//
						if (!diff.getChangeType().equals(ChangeType.DELETE)) {
							if (tmp.contains(diff.getNewPath())) {
							//	System.out.println("score "+diff.getScore() + " "+ diff.getNewPath());
								String previousCommitName = this.revCommit.getParent(0).getName();
								String filePrevVersion = getFileContent(
										this.revCommit.getParent(0).getId(),
										diff.getOldPath());
								String fileNextVersion = getFileContent(
										this.revCommit.getId(), diff
												.getNewPath());
								FileCommit file = new FileCommitGit(diff
										.getOldPath(), filePrevVersion, diff
										.getNewPath(), fileNextVersion, this,previousCommitName);
								ret.add(file);
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

		return ret;
	}

	@Override
	public List<FileCommit> getJavaFileCommits() {
		List<FileCommit> files = getFileCommits();
		List<FileCommit> javaFiles = new ArrayList<FileCommit>();
		
		for (FileCommit fileCommit : files) {
			if (fileCommit.getFileName().endsWith(".java"))
				javaFiles.add(fileCommit);
		}
		
		return javaFiles;
	}

	@Override
	public String getName() {
		return this.revCommit.getName();
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
	
	public String getFullMessage(){
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
}

class MyTreeFilter extends TreeFilter {

	@Override
	public TreeFilter clone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean include(TreeWalk walker) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
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
