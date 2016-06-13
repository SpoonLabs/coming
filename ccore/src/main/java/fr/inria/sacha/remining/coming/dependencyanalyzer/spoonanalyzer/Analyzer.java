package fr.inria.sacha.remining.coming.dependencyanalyzer.spoonanalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Class;
import fr.inria.sacha.remining.coming.dependencyanalyzer.util.io.ResourceFile;
import spoon.Launcher;
import spoon.OutputType;
import spoon.compiler.Environment;
import spoon.compiler.SpoonCompiler;
import spoon.compiler.SpoonResource;
import spoon.processing.ProcessingManager;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;
import spoon.support.compiler.FileSystemFile;

/**
 * Handles dependency analysis of multiple JAVA class
 * @author Romain Philippon
 *
 */
public class Analyzer {
	/**
	 * Is the spoon context which runs the analysis
	 */
	private Launcher spoon;
	/**
	 * Is the spoon environment which configure the spoon context
	 */
	private Environment environment;
	/**
	 * Is the spoon factory used to generate for each analysis a spoon compiler 
	 */
	private Factory factory;
	/**
	 * Is the dependency processor for the current dependency analysis
	 */
	private DependencyProcessor<?> processor;
	
	public Analyzer() {
		this.disableSpoonLog();
	}
	
	/**
	 * Analyzes class dependencies for each class located in the file passed as parameter
	 * @param rFile is the resource file to analyze
	 * @return a class instance with the result of the dependency analysis
	 * @throws Exception is raised if the spoon compiler failed to compile the resource file
	 */
	public Class analyze(ResourceFile rFile) throws Exception {		
		/* INIT ANALYSYS */
		this.prepareNewCompilation();
		LinkedList<SpoonResource> inputSource = new LinkedList<SpoonResource>();
		inputSource.add(new FileSystemFile(rFile.toFile()));
		
		/* SPOON COMPILATION FOR ANALYSIS PROCESS */
//		this.spoon.run(new String[]{ "-i ", rFile.getPath(), "-x"});
		this.spoon.addInputResource(rFile.getPath());
		this.processor = new DependencyProcessor();
		this.spoon.addProcessor(this.processor);
		this.spoon.run();
//		this.spoon.run(new String[]{ "-i ", rFile.getPath(), "-x"});
//				this.buildNewCompiler(), 
//				"UTF-8",  // encoding
//				false, // pre-compilation
//				OutputType.NO_OUTPUT, // output type
//				new File("spooned"), // output directory
//				new ArrayList<String>(), // processor types
//				true,  // compilation
//				null, // destination directory
//				true, // build only out-dated files
//				"target/classes", // source class path
//				null, // no template class path
//				inputSource, // input sources
//				new ArrayList<SpoonResource>() // list of spoon resources
//		);
//		
//		/* ANALYSIS PROCESS */
//		this.buildNewManager().process();
		return this.processor.getAnalyzedClass();
	}
	
	/**
	 * Initializes a spoon context for each analysis
	 */
	private void prepareNewCompilation() {
		this.spoon = new Launcher();
		this.factory = spoon.getFactory();
		this.environment = this.factory.getEnvironment();
		this.environment.setNoClasspath(true);
		this.environment.setComplianceLevel(7);
	}
	
	/**
	* Disables spoon logs
	*/
	@SuppressWarnings("unchecked")
	private void disableSpoonLog() {
		List<Logger> loggers = Collections.list(LogManager.getCurrentLoggers());
		loggers.add(LogManager.getRootLogger());
		
		for (Logger logger : loggers) {
			logger.setLevel(Level.OFF);
		}
	}
}
