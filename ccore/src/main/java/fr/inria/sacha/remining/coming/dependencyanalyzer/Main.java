package fr.inria.sacha.remining.coming.dependencyanalyzer;

import java.io.IOException;
import java.util.List;

import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.gitanalyzer.interfaces.FileCommit;
import fr.inria.sacha.gitanalyzer.object.RepositoryPGit;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Class;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Class.ClassType;
import fr.inria.sacha.remining.coming.dependencyanalyzer.spoonanalyzer.Analyzer;
import fr.inria.sacha.remining.coming.dependencyanalyzer.util.io.ResourceFile;
import fr.inria.sacha.remining.coming.dependencyanalyzer.util.io.XMLOutputResFile;
import fr.inria.sacha.remining.coming.dependencyanalyzer.util.tool.DepTool;

/**
 * 
 * Launches a dependency analysis for each class on a Git JAVA project
 * 
 * @author Romain Philippon
 *
 */
public class Main {
	
	public static void main(String[] args) throws IOException {
		String previousVersion, nextVersion;
		String message = "USAGE --parameters-- projectLocation [remoteRepositoryGitHubUrl]";
		XMLOutputResFile xml = null;
		String githubRepoUrl = null;
		boolean hasGithubUrl = false;
		Analyzer dependencyAnalyzer = new Analyzer();
		ResourceFile rFile = null;
		Class classFound, classFoundBefore, classFoundAfter;
		classFound = classFoundBefore = classFoundAfter = null;
		
		/* HANDLES COMMAND PARAMETERS */
		if(args == null){
				System.out.println(message);
				return;
		}
		else {
			if(args.length == 2) {
				githubRepoUrl = args[1];
				hasGithubUrl = true;
			}
		}
		
		xml = new XMLOutputResFile(hasGithubUrl);
	    String projectLocation = args[0];
	    
	    /* GET ALL COMMITS FROM LOCAL REPOSITORY */
	    RepositoryPGit gitRepository = new RepositoryPGit(projectLocation, "master");
	    List<Commit> allGitCommit = gitRepository.history();
	    
	    System.out.println("Git project analyzed : "+ projectLocation);
	    System.out.println("Contains "+ allGitCommit.size() +" commit(s)");
	    
	    xml.setGitRepositoryName(projectLocation);
	    xml.setNumberOfCommitInRepository(allGitCommit.size());
	    
    	/* ANALYSIS COMMIT ONE BY ONE */
	    int i=0;
    	for(Commit commit : allGitCommit) {
    		System.out.print(i++ + " ");
    		List<FileCommit> javaCommitFiles = commit.getJavaFileCommits();
    		
    		for(FileCommit fileCommit : javaCommitFiles) {
    			if(!fileCommit.getFileName().equals(new String("package-info.java"))) // excludes package information files from analysis
    			{
	    			previousVersion = fileCommit.getPreviousVersion();
	    			nextVersion = fileCommit.getNextVersion();
	    			
	    			/* ADDED CLASSES */
	    			if(previousVersion.isEmpty()) {    				
	    				rFile = new ResourceFile(fileCommit.getFileName(), nextVersion);
	    				try {
	    					classFound = dependencyAnalyzer.analyze(rFile);
	    					
	    					if(!classFound.getDependencies().isEmpty()) {
	    						if(githubRepoUrl != null)
    								xml.addURLGithubCommit(githubRepoUrl, commit.getName());
	    					
	    						xml.addAnalyzedCommit(commit);
	    		        		xml.addCommitDate(commit.getRevDate());
	    		        		xml.addCommitFile(fileCommit.getFileName());
	    						xml.addClass(classFound, ActionType.INS);
	    					} 
						} 
	    				catch (Exception e) {
	    					System.err.println("oops added  "+rFile.getPath());
	    					e.printStackTrace();
//                                                     throw new RuntimeException(e);
						}
	        		}
	    			/* DELETED CLASSES */
	    			else if(nextVersion.isEmpty()) {
    					rFile = new ResourceFile(fileCommit.getFileName(), previousVersion);

	    				try {
							classFound = dependencyAnalyzer.analyze(rFile);
							
							if(!classFound.getDependencies().isEmpty()) {
								if(githubRepoUrl != null)
    								xml.addURLGithubCommit(githubRepoUrl, commit.getName());
							
								xml.addAnalyzedCommit(commit);
				        		xml.addCommitDate(commit.getRevDate());
				        		xml.addCommitFile(fileCommit.getFileName());
		    					xml.addClass(classFound, ActionType.DEL);
							}
	    				}
	    				catch (Exception e) {
                                                    throw new RuntimeException(e);
	    				}
	    			}
	    			/* UPDATED CLASSES */
	    			else {
	    				try {
	    					// analysis for version before commit
	    					rFile = new ResourceFile(fileCommit.getFileName(), previousVersion);
							classFoundBefore = dependencyAnalyzer.analyze(rFile);
	    					
		    				// analysis for version after commit
	    					rFile = new ResourceFile(fileCommit.getFileName(), nextVersion);
							classFoundAfter = dependencyAnalyzer.analyze(rFile);
							
							if (classFoundBefore!=null) {
							// get correct dependency update
							if(githubRepoUrl != null)
								xml.addURLGithubCommit(githubRepoUrl, commit.getName());
							
							xml.addAnalyzedCommit(commit);
			        		xml.addCommitDate(commit.getRevDate());
			        		xml.addCommitFile(fileCommit.getFileName());
	    					xml.addClass(new Class(classFoundBefore.getName(), ClassType.REGULAR, DepTool.diff(classFoundBefore.getDependencies(), classFoundAfter.getDependencies())), ActionType.UPD);
							}
	    				}
	    				catch (Exception e) {
	    					System.err.println("oops updated"+rFile);

//                                                    throw new RuntimeException(e);
	    				}
	    			}
    			}
    		}
    	}
    	
    	System.out.println("End analysis");
    	
    	try {
			xml.save();
			System.out.println("Results are saved in a xml file");
		} 
    	catch (IOException ioe) {
			xml.display();
		}
	}
}
