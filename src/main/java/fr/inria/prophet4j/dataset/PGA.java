package fr.inria.prophet4j.dataset;

import fr.inria.prophet4j.utility.Option;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import fr.inria.prophet4j.utility.Structure.FeatureMatrix;
import fr.inria.prophet4j.utility.Structure.Sample;
import fr.inria.prophet4j.utility.CodeDiffer;
import fr.inria.prophet4j.learner.FeatureLearner;
import tech.sourced.siva.IndexEntry;
import tech.sourced.siva.SivaReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://github.com/src-d/datasets/tree/master/PublicGitArchive/pga
// https://pga.sourced.tech
// https://stedolan.github.io/jq/manual/
// https://github.com/src-d/siva-java
// https://github.com/eclipse/jgit
// https://github.com/centic9/jgit-cookbook
// refactor this class if necessary
public class PGA {
    // pga list -u github.com/src-d/ -f json | jq -r 'select(.fileCount > 100) | .sivaFilenames[]' | pga get -i -o ~
    // pga list -l java -f json | jq -r 'select(.commitsCount > 10000) | select(.commitsCount < 10100) | select(.langsFilesCount[.langs | index("Java")]==(.langsFilesCount | max)) | .sivaFilenames[]' | pga get -i -o ~
    /*
    pga list -l java -f json | jq -r 'select(.commitsCount > 10000) | select(.commitsCount < 10100) | select(.langsFilesCount[.langs | index("Java")]==(.langsFilesCount | max)) | .url, .commitsCount, .sivaFilenames'
    https://github.com/swagger-api/swagger-codegen
    10021
    [
      "d8c0f69ad42c803d2363cfcfb7d138aa3933be61.siva"
    ]
    https://github.com/pegasus-isi/pegasus
    10032
    [
      "327b2cd10cd27b8872ab59b4ab2f51c94113aea5.siva",
      "701b812e8ccff5c4d0ae2023818bdff573e3d09c.siva",
      "873c2c47d8752d22d908da1425925c5653bdb95a.siva",
      "8f1051d4d0926510ad3f629628e7d57140919fff.siva",
      "eab5465f9662eab52c93e8834ab66f17a570e638.siva"
    ]
    https://github.com/camunda/camunda-bpm-platform
    10022
    [
      "b5ec00079c4bb1b4eb5e55c3a590a76fde5ac477.siva"
    ]
    https://github.com/apache/logging-log4j2
    10036
    [
      "10fb9656a916d1c0ff57c28d7dcbfcb5bd313278.siva"
    ]
     */
    private final String PROPHET4J_DIR = "src/main/resources/prophet4j/";
    private final String SIVA_FILES_DIR = PROPHET4J_DIR + "siva_files/";
    private final String SIVA_UNPACKED_DIR = PROPHET4J_DIR + "siva_unpacked/";
    private final String SIVA_COMMITS_DIR = PROPHET4J_DIR + "siva_commits/";
    private final String SIVA_VECTORS_DIR = PROPHET4J_DIR + "siva_vectors/";
    private final String SIVA_PARAMETERS_DIR = PROPHET4J_DIR + "siva_parameters/";
    private static final Logger logger = LogManager.getLogger(PGA.class.getName());

    private void unpack() {
        logger.log(Level.INFO, "unpacking siva files");
        try {
            String sampleFile = SIVA_FILES_DIR + "10fb9656a916d1c0ff57c28d7dcbfcb5bd313278.siva";
            SivaReader sivaReader = new SivaReader(new File(sampleFile));
            List<IndexEntry> index = sivaReader.getIndex().getFilteredIndex().getEntries();
            for (IndexEntry indexEntry : index) {
                InputStream entry = sivaReader.getEntry(indexEntry);
                Path outPath = Paths.get(SIVA_UNPACKED_DIR.concat(indexEntry.getName()));
                FileUtils.copyInputStreamToFile(entry, new File(outPath.toString()));
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.toString(), e);
        }
    }

    private DiffEntry diffFile(Repository repo, String oldCommit, String newCommit, String path) throws IOException, GitAPIException {
//        Config config = new Config();
//        config.setBoolean("diff", null, "renames", true);
//        DiffConfig diffConfig = config.get(DiffConfig.KEY);
        Git git = new Git(repo);
        List<DiffEntry> diffList = git.diff().
                setOldTree(prepareTreeParser(repo, oldCommit)).
                setNewTree(prepareTreeParser(repo, newCommit)).
//                setPathFilter(FollowFilter.create(path, diffConfig)).
        call();
        if (diffList.size() == 0)
            return null;
        if (diffList.size() > 1)
            throw new RuntimeException("invalid diff");
        return diffList.get(0);
    }

    private AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        RevWalk walk = new RevWalk(repository);
        RevCommit commit = walk.parseCommit(repository.resolve(objectId));
        RevTree tree = walk.parseTree(commit.getTree().getId());

        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        ObjectReader reader = repository.newObjectReader();
        treeParser.reset(reader, tree.getId());

        walk.dispose();

        return treeParser;
    }

    private void runDiff(Repository repo, String oldCommit, String newCommit, String path) throws IOException, GitAPIException {
        // Diff README.md between two commits. The file is named README.md in
        // the new commit (5a10bd6e), but was named "jgit-cookbook README.md" in
        // the old commit (2e1d65e4).
        DiffEntry diff = diffFile(repo, oldCommit, newCommit, path);

        // Display the diff
        System.out.println("Showing diff of " + path);
        DiffFormatter formatter = new DiffFormatter(System.out);
        formatter.setRepository(repo);
        //noinspection ConstantConditions
        formatter.format(diff);
    }

    private void listDiff(Repository repository, Git git, String oldCommit, String newCommit) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        System.out.println("Found: " + diffs.size() + " differences");
        for (DiffEntry diff : diffs) {
            System.out.println("Diff: " + diff.getChangeType() + ": " +
                    (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
        }
    }

    private CommitDiffer filterDiff(Repository repository, Git git, String oldCommitName, String newCommitName, Option option) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommitName))
                .setNewTree(prepareTreeParser(repository, newCommitName))
                .call();

        CommitDiffer commitDiffer = new CommitDiffer();
        System.out.println("Found: " + diffs.size() + " differences");
        for (DiffEntry diff : diffs) {
            if (diff.getChangeType().equals(DiffEntry.ChangeType.MODIFY)) {
                String oldPath = diff.getOldPath();
                String newPath = diff.getNewPath();
                if (oldPath.endsWith(".java") && newPath.endsWith(".java")) {
                    // exclude some commits for renamed files todo improve
                    if (oldPath.equals(newPath)) {
                        commitDiffer.addDiffer(oldCommitName, newCommitName, oldPath, newPath, option);
                    } else {
                        System.out.println("oldPath is different from newPath");
                    }
                }
            }
        }
        return commitDiffer;
    }

    private void obtainDiff(Repository repository, RevCommit commit, List<String> paths) throws IOException, GitAPIException {
        // and using commit's tree find the path
        RevTree tree = commit.getTree();
        System.out.println("Having tree: " + tree);

        // now try to find a specific file
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        for (String path : paths) {
            String filePath = SIVA_COMMITS_DIR + commit.getName() + "/" + path;
            File file = new File(filePath);
            if (!file.exists()) {
                treeWalk.setFilter(PathFilter.create(path));
                if (!treeWalk.next()) {
                    throw new IllegalStateException("Did not find expected file '" + path + "'");
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);
                // and then one can the loader to read the file
//                loader.copyTo(System.out);
                loader.copyTo(FileUtils.openOutputStream(file));
            }
        }
    }

    private class Differ {
        String oldFilePath;
        String newFilePath;
        String vectorFilePath;

        Differ(String oldCommitName, String newCommitName, String oldPath, String newPath, Option option) {
            super();
            oldFilePath = SIVA_COMMITS_DIR + oldCommitName + "/" + oldPath;
            newFilePath = SIVA_COMMITS_DIR + newCommitName + "/" + newPath;
            vectorFilePath = SIVA_VECTORS_DIR + option.featureOption.toString() + "/" + oldCommitName + "~" + newCommitName + "/" + newPath;
        }
    }

    private class CommitDiffer {
        List<Differ> differs = new ArrayList<>();
        Map<String, ArrayList<String>> paths = new HashMap<>();

        void addDiffer(String oldCommitName, String newCommitName, String oldPath, String newPath, Option option) {
            differs.add(new Differ(oldCommitName, newCommitName, oldPath, newPath, option));
            if (!paths.containsKey(oldCommitName)) {
                paths.put(oldCommitName, new ArrayList<>());
            }
            paths.get(oldCommitName).add(oldPath);
            if (!paths.containsKey(newCommitName)) {
                paths.put(newCommitName, new ArrayList<>());
            }
            paths.get(newCommitName).add(newPath);
        }

        ArrayList<String> getPaths(String commitName) {
            return paths.size() > 0 ? paths.get(commitName) : new ArrayList<>();
        }
    }

    public void handleCommits(Option option) throws IOException, GitAPIException {
        // if siva-unpacked files do not exist then uncommented the next line
        boolean existUnpackDir = new File(SIVA_UNPACKED_DIR).exists();
        boolean existCommitsDir = new File(SIVA_COMMITS_DIR).exists();
        if (!existUnpackDir) {
            unpack();
        }
        int progressAll, progressNow = 0;
        // prepare the whole dataport-set
        List<Differ> differs = new ArrayList<>();
        File repoDir = new File(SIVA_UNPACKED_DIR);
        // now open the resulting repository with a FileRepositoryBuilder
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(repoDir)
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
        System.out.println("Having repository: " + repository.getDirectory());

        Git git = new Git(repository);
        Iterable<RevCommit> commits = git.log().all().call();

        int countCommits = 0;
        int countDiffers = 0;
        RevCommit lastCommit = null;
        for (RevCommit commit : commits) {
            System.out.println("LogCommit: " + commit);
            if (lastCommit != null) {
                // why runDiff() for some commits returns "java.lang.RuntimeException: invalid diff"? (tested on the very first one case)
//                runDiff(repository, lastCommit.getName(), commit.getName(), "README.md");
//                listDiff(repository, git, lastCommit.getName(), commit.getName());
                CommitDiffer commitDiffer = filterDiff(repository, git, lastCommit.getName(), commit.getName(), option);
                // obtain oldFile and newFile (save files to disk)
                if (!existCommitsDir) {
                    obtainDiff(repository, lastCommit, commitDiffer.getPaths(lastCommit.getName()));
                    obtainDiff(repository, commit, commitDiffer.getPaths(commit.getName()));
                }
                // add dataport into the whole dataport-set
                differs.addAll(commitDiffer.differs);
                countDiffers += commitDiffer.differs.size();
            }
            lastCommit = commit;
            countCommits++;
            // remove 3 lines below to obtainRepairCandidates on all commits (around 10k commits)
            if (countCommits >= 10) {
                break;
            }
            /* 10036 != 12813 why? I guess because "we store all references (including all pull requests) from different repositories that share the same initial commit – root"
            https://github.com/apache/logging-log4j2
            10036["10fb9656a916d1c0ff57c28d7dcbfcb5bd313278.siva"]
            */
        }
        System.out.println(countCommits + " Commits");
        System.out.println(countDiffers + " Differs");
        progressAll = countDiffers;
//        runDiff(repository, "5fddbeb678bd2c36c5e5c891ab8f2b143ced5baf", "5d7303c49ac984a9fec60523f2d5297682e16646", "README.md");
        CodeDiffer codeDiffer = new CodeDiffer(true, option);
        List<String> filePaths = new ArrayList<>();
        for (Differ differ : differs) {
            File vectorFile = new File(differ.vectorFilePath);
            System.out.println("================");
            System.out.println(differ.vectorFilePath);
            if (!vectorFile.exists()) {
                File oldFile = new File(differ.oldFilePath);
                File newFile = new File(differ.newFilePath);
                List<FeatureMatrix> featureMatrices = codeDiffer.runByGenerator(oldFile, newFile);
                // we should have more than one FeatureMatrix when CodeDiffer's "byGenerator" is true
                if (featureMatrices.size() == 0) {
                    continue;
                }
                if (featureMatrices.get(0).getFeatureVectors().size() == 0) {
                    // diff.commonAncestor() returns null value
                    progressNow += 1;
                    continue;
                }
                new Sample(vectorFile.getPath()).saveFeatureMatrices(featureMatrices);
            }
            filePaths.add(differ.vectorFilePath);
            progressNow += 1;
            System.out.println(progressNow + " / " + progressAll);
        }
        new FeatureLearner(option).run(filePaths);
        // clean up here to not keep using more and more disk-space for these samples
//        FileUtils.deleteDirectory(repoDir.getParentFile());
    }
}
