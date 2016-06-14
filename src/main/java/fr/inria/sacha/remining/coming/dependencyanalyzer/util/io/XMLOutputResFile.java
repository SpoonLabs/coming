package fr.inria.sacha.remining.coming.dependencyanalyzer.util.io;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import fr.inria.sacha.coming.entity.ActionType;
import fr.inria.sacha.gitanalyzer.interfaces.Commit;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.AddedDependency;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Class;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.DeletedDependency;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Dependency;

/**
 * Exports analysis results to XML format
 * 
 * @author Romain Philippon
 *
 */
public class XMLOutputResFile {
	
	/**
	 * Gives the document's root XML node 
	 */
	private final static String ROOT_NODE_NAME = new String("analysis");
	/**
	 * Gives the history XML node name containing all commit changes
	 */
	private final static String ROOT_NODE_COMMIT_LIST_NAME = new String("history");
	/**
	 * Gives the commit XML node containing all class changes
	 */
	private final static String COMMIT_NODE_NAME = new String("commit");
	/**
	 * Gives the node XML name corresponding to an added class
	 */
	private final static String ADDED_CLASS_NODE_NAME = new String("added-class");
	/**
	 * Gives the node XML name corresponding to a deleted class
	 */
	private final static String DELETED_CLASS_NODE_NAME = new String("deleted-class");
	/**
	 * Gives the node XML name corresponding to an updated class
	 */
	private final static String UPDATED_CLASS_NODE_NAME = new String("updated-class");
	/**
	 * Gives the node XML name corresponding to a new class dependency
	 */
	private final static String ADDED_DEPENDENCY_NODE_NAME = new String("added-dependency");
	/**
	 * Gives the node XML name corresponding to a class dependency which has not changed
	 */
	private final static String SAME_DEPENDENCY_NODE_NAME = new String("same-dependency");
	/**
	 * Gives the node XML name corresponding to an deleted class dependency
	 */
	private final static String DELETED_DEPENDENCY_NODE_NAME = new String("deleted-dependency");
	
	/**
	 * Is a reference to the XML document's root
	 */
	private Document rootXML;
	/**
	 * Is a reference to the XML root node
	 */
	private Element rootNode;
	/**
	 * Is a reference to the commit list XML node
	 */
	private Element rootNodeCommitList;
	/**
	 * Is a reference to the git repository name XML node
	 */
	private Element gitRepoNameNode;
	/**
	 * Is a reference to the number of commits XML node
	 */
	private Element totalNumberCommitInGitRepo;
	/**
	 * Is a reference to the current commit XML node
	 */
	private Element currentCommitNode;
	/**
	 * Is a reference to the current file commit XML node
	 */
	private Element fileCommitNode;
	
	/**
	 * Indicates the analysis' number of results
	 */
	private int numberResult;
	/**
	 * Tells if the analyzed repository is or not a GitHub repository
	 */
	private boolean hasGithubUrl;
	
	/**
	 * Build an object of this class
	 * @param hasGithubUrl indicates if the analyzed repository is a GitHub repository
	 */
	public XMLOutputResFile(boolean hasGithubUrl) {
		this.rootNode = new Element(ROOT_NODE_NAME);
		this.rootXML = new Document(this.rootNode);
		this.rootNodeCommitList = new Element(ROOT_NODE_COMMIT_LIST_NAME);
		this.currentCommitNode = new Element(COMMIT_NODE_NAME);
		this.numberResult = 0;
		this.hasGithubUrl = hasGithubUrl;
	}
	
	/**
	 * Sets the number of commits in the total number of commits XML node 
	 * @param numberOfCommit corresponds to the total number of commit in the analyzed repository
	 */
	public void setNumberOfCommitInRepository(int numberOfCommit) {
		this.totalNumberCommitInGitRepo = new Element("number-of-commit");
		this.totalNumberCommitInGitRepo.setText(Integer.toString(numberOfCommit));		
	}
	
	/**
	 * Puts the Git repository name in the repository name XML node
	 * @param gitRepositoryName is the analyzed repository's name 
	 */
	public void setGitRepositoryName(String gitRepositoryName) {
		this.gitRepoNameNode = new Element("git-repository");
		this.gitRepoNameNode.setText(gitRepositoryName);		
	}
	
	/**
	 * Adds the previous commit XML node in commit list XML node and add a new commit XML node
	 * @param commit is the commit instance that represents a Git commit
	 */
	public void addAnalyzedCommit(Commit commit) {
		if((this.hasGithubUrl && this.currentCommitNode.getChildren().size() > 1) || (!this.hasGithubUrl && this.currentCommitNode.getChildren().size() >= 1)) { 
			// the commit node does not contain only a github-url node
			this.rootNodeCommitList.addContent(this.currentCommitNode);
		}
		
		this.currentCommitNode = new Element(COMMIT_NODE_NAME);
		this.currentCommitNode.setAttribute("number", commit.getName());
	}
	
	/**
	 * Adds a GitHub url commit XML node in the current commit XML node
	 * @param githubRepoUrl is the Github repository url
	 * @param SHA1NumberCommit is the SHA-1 commit number
	 */
	public void addURLGithubCommit(String githubRepoUrl, String SHA1NumberCommit) {
		String githubCommitUrl = githubRepoUrl;
		
		if(!githubCommitUrl.endsWith("/")) {
			githubCommitUrl = githubCommitUrl.concat("/");
		}
		
		githubCommitUrl = githubCommitUrl.concat("commit/");
		githubCommitUrl = githubCommitUrl.concat(SHA1NumberCommit);
		
		this.currentCommitNode.addContent(new Element("github-url").setText(githubCommitUrl));
	}
	
	/**
	 * Adds a commit date XML in the current commit XML node
	 * @param commitDate
	 */
	public void addCommitDate(String commitDate) {
		this.currentCommitNode.addContent(new Element("date").setText(commitDate));
	}
	
	/**
	 * Adds a new filecommit XML node
	 * @param fileName is the filename which have changes
	 */
	public void addCommitFile(String fileName) {
		this.fileCommitNode = new Element("file");
		fileCommitNode.setAttribute("name", fileName);		
	}
	
	/**
	 * Adds a class XML node in the current filecommit XML node
	 * @param classFound is the class found during the analysis
	 * @param action tells if the class has been added, modified or deleted
	 */
	@SuppressWarnings("incomplete-switch")
	public void addClass(Class classFound, ActionType action) {
		/* CLASS NODE BUILDING */
		Element classNode = null;

		switch(action) {
			case INS : classNode = this.addAddedClassNode(classFound.getName()); break;
			case DEL : classNode = this.addDeletedClassNode(classFound.getName()); break;
			case UPD: classNode = this.addUpdatedClassNode(classFound.getName()); break;
			default: fileCommitNode.addContent(new Element("type-action").setText("unknown"));
		}
				
		/* DEPENDENCY PART */
		Element dependenciesNode = new Element("dependencies");
		
		for(Dependency dependency : classFound.getDependencies()) {			
			switch(action) {
				// new class
				case INS: 
					dependenciesNode.addContent(this.addAddedDependencyNode(dependency)); 
				break;
				// deleted class
				case DEL: 
					dependenciesNode.addContent(this.addDeletedDependencyNode(dependency)); 
				break;
				// class update
				case UPD:
					// new dependency
					if(dependency.getClass() == AddedDependency.class) {
						dependenciesNode.addContent(this.addAddedDependencyNode(dependency)); 
					}
					// deleted dependency
					else if(dependency.getClass() == DeletedDependency.class) {
						dependenciesNode.addContent(this.addDeletedDependencyNode(dependency));
					}
					// same dependency
					else {
						dependenciesNode.addContent(this.addSameDependencyNode(dependency));
					}
				break;
			}
		}
		
		classNode.addContent(dependenciesNode);
		
		this.fileCommitNode.addContent(classNode);
		
		this.currentCommitNode.addContent(fileCommitNode);
		this.numberResult++;
	}
	
	/**
	 * Is a helper method which builds a new class XML node
	 * @param className is the class which has been added
	 * @return an XML element with the attribute name containing the class name
	 */
	private Element addAddedClassNode(String className) {
		return new Element(ADDED_CLASS_NODE_NAME).setAttribute("name", className);
	}
	
	/**
	 * Is a helper method which builds a deleted class XML node
	 * @param className is the class which has been deleted
	 * @return an XML element with the attribute name containing the class name
	 */
	private Element addDeletedClassNode(String className) {
		return new Element(DELETED_CLASS_NODE_NAME).setAttribute("name", className);
	}
	
	/**
	 * Is a helper method which builds an updated class XML node
	 * @param className is the class which has been updated
	 * @return an XML element with the attribute name containing the class name
	 */
	private Element addUpdatedClassNode(String className) {
		return new Element(UPDATED_CLASS_NODE_NAME).setAttribute("name", className);
	}
	
	/**
	 * Is a helper method which builds a new class dependency XML node
	 * @param className is the class dependency which has been added
	 * @return an XML element containing the qualified dependency name
	 */
	private Element addAddedDependencyNode(Dependency dependency) {
		return new Element(ADDED_DEPENDENCY_NODE_NAME).setText(dependency.getQualifiedDependencyName());
	}
	
	/**
	 * Is a helper method which builds a class dependency XML node which has not been changed
	 * @param className is the class dependency which has not been changed
	 * @return an XML element containing the qualified dependency name
	 */
	private Element addSameDependencyNode(Dependency dependency) {
		return new Element(SAME_DEPENDENCY_NODE_NAME).setText(dependency.getQualifiedDependencyName());
	}
	
	/**
	 * Is a helper method which builds a deleted class dependency XML node
	 * @param className is the class dependency which has been deleted
	 * @return an XML element containing the qualified dependency name
	 */
	private Element addDeletedDependencyNode(Dependency dependency) {
		return new Element(DELETED_DEPENDENCY_NODE_NAME).setText(dependency.getQualifiedDependencyName());
	}
	
	/**
	 * Saves the XML document, the filename follows this format : dd_MM_yyyy.xml
	 * @throws IOException when this method can't save he result on a hard disk
	 */
	public void save() throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
		Calendar cal = Calendar.getInstance();
		
		String XMLFilename = dateFormat.format(cal.getTime()) +".xml";
		
		this.rootNodeCommitList.setAttribute("number-of-result", Integer.toString(this.numberResult));
		
		this.rootNode.addContent(this.gitRepoNameNode);
		this.rootNode.addContent(this.totalNumberCommitInGitRepo);
		this.rootNode.addContent(this.rootNodeCommitList);
		
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(this.rootXML, new FileWriter(XMLFilename));
	}
	
	/**
	 * Displays the XML document on standard output
	 */
	public void display() {
		 XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
		 
	     try {
			sortie.output(this.rootXML, System.out);
		} catch (IOException e) {
			System.out.println("Impossible to display the result xml file");
		}
	}

	public Element getRootNodeCommitList() {
		return rootNodeCommitList;
	}
}
