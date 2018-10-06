package fr.inria.coming.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.HunkDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.entity.ActionType;
import fr.inria.coming.changeminer.entity.EntityTypeSpoon;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.core.engine.RevisionNavigationExperiment;
import fr.inria.coming.core.engine.git.GITRepositoryInspector;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.core.entities.output.JSonPatternInstanceOutput;
import fr.inria.coming.core.entities.output.StdOutput;
import fr.inria.coming.core.extensionpoints.PlugInLoader;
import fr.inria.coming.core.extensionpoints.changepattern.PatternFileParser;
import fr.inria.coming.core.filter.commitmessage.BugfixKeywordsFilter;
import fr.inria.coming.core.filter.commitmessage.KeyWordsMessageFilter;
import fr.inria.coming.core.filter.diff.NbHunkFilter;
import fr.inria.coming.core.filter.files.CommitSizeFilter;
import fr.inria.coming.core.filter.files.ContainTestFilterFilter;

/**
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class ComingMain {

	Logger log = Logger.getLogger(FineGrainDifftAnalyzer.class.getName());

	static Options options = new Options();

	CommandLineParser parser = new BasicParser();

	static {
		//
		options.addOption("location", true, "location of the project");
		options.addOption("resultoutput", true, "location of the output");
		options.addOption("mode", true, "execution Mode. ");
		options.addOption("input", true, "Input. ");
		options.addOption("outputprocessor", true, "result outout processors");
		options.addOption("output", true, "output folder");
		// Pattern mining
		options.addOption("pattern", true, "Location the file pattern ");
		options.addOption("patternparser", true, "Class name of the pattern parser (By default XML)");
		options.addOption("entitytype", true, "entity type to mine");
		options.addOption("entityvalue", true, "entity value to mine");
		options.addOption("action", true, "action");
		options.addOption("parenttype", true, "parent type");
		options.addOption("parentlevel", true,
				"parent level: numbers of AST node where the parent is located. 1 means inmediate parent");

		options.addOption("hunkanalysis", true, "include analysis of hunks");

		options.addOption("showactions", false, "show all actions");
		options.addOption("showentities", false, "show all entities");

		// Revision filter
		options.addOption("filter", true, "Names of filter");
		options.addOption("filtervalue", true, "Values");

		// In case of git
		options.addOption("branch", true, "branch");
		options.addOption("message", true, "comming message");
		options.addOption("parameters", true, "Parameters, divided by " + File.pathSeparator);
	}

	public static void main(String[] args) {
		ComingMain cmain = new ComingMain();

		try {
			cmain.run(args);
		} catch (Exception e) {
			System.err.println("Error initializing Coming with args" + Arrays.toString(args));
			System.err.println(e);
			e.printStackTrace();
		}
	}

	RevisionNavigationExperiment<?> experiment = null;

	public FinalResult run(String[] args) throws Exception {

		ComingProperties.reset();
		CommandLine cmd = null;
		this.experiment = null;
		try {
			cmd = parser.parse(options, args);
		} catch (UnrecognizedOptionException e) {
			System.out.println("Error: " + e.getMessage());
			help();
			return null;
		}
		if (cmd.hasOption("help")) {
			help();
			return null;
		}

		if (cmd.hasOption("showactions")) {
			System.out.println("---");
			System.out.println("Actions available: ");
			for (ActionType a : ActionType.values()) {
				System.out.println(a);
			}
			System.out.println("---");
			return null;
		}

		if (cmd.hasOption("showentities")) {
			System.out.println("---");
			System.out.println("Entities Type Available:");
			System.out.println("---");
			for (EntityTypeSpoon et : EntityTypeSpoon.values()) {
				System.out.println(et);
			}
			System.out.println("---");
			return null;
		}
		for (Option option : cmd.getOptions()) {

			if (cmd.hasOption(option.getOpt())) {
				String value = cmd.getOptionValue(option.getOpt());
				ComingProperties.properties.setProperty(option.getOpt(), value);
			}

		}

		if (cmd.hasOption("parameters")) {
			String[] pars = cmd.getOptionValue("parameters").split(File.pathSeparator);
			for (int i = 0; i < pars.length; i = i + 2) {
				String key = pars[i];
				String value = pars[i + 1];
				ComingProperties.properties.setProperty(key, value);

			}
		}

		String mode = ComingProperties.getProperty("mode");
		String input = ComingProperties.getProperty("input");

		if (input == null || input.equals("git")) {
			experiment = new GITRepositoryInspector();
		} else if (input.equals("file")) {
			// TODO:
		} else {
			// extension point
			experiment = loadInputEngine(input);
		}

		if ("diff".equals(mode)) {
			experiment.getAnalyzers().clear();
			experiment.getAnalyzers().add(new FineGrainDifftAnalyzer());
		} else if ("mineinstance".equals(mode)) {
			experiment.getAnalyzers().clear();
			experiment.getAnalyzers().add(new FineGrainDifftAnalyzer());

			ChangePatternSpecification pattern = loadPattern();
			experiment.getAnalyzers().add(new PatternInstanceAnalyzer(pattern));

			// JSON output
			experiment.getOutputProcessors().add(new JSonPatternInstanceOutput());

		} else {
			// TODO: LOAD Analyzers from command

		}

		// output
		if (ComingProperties.getPropertyBoolean("hunkanalysis")) {
			experiment.getAnalyzers().add(0, new HunkDifftAnalyzer());
		}

		// TODO: maybe move to git engine
		experiment.setFilters(createFilters());

		String outputs = ComingProperties.getProperty("outputprocessor");
		if (outputs == null) {
			experiment.getOutputProcessors().add(0, new StdOutput());
		} else {
			loadOutputProcessors(outputs);
		}

		// EXECUTION
		FinalResult result = experiment.analyze();

		// RESULTS:
		for (IOutput out : experiment.getOutputProcessors()) {
			out.generateOutput(result);
		}

		return result;
	}

	private void loadOutputProcessors(String output) {
		String[] outputs = output.split(File.pathSeparator);

		for (String singlefoutput : outputs) {
			try {
				Object outLoaded = PlugInLoader.loadPlugin(singlefoutput, IOutput.class);
				if (outLoaded != null) {
					experiment.getOutputProcessors().add((IOutput) outLoaded);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private RevisionNavigationExperiment<?> loadInputEngine(String input) {

		Object loaded;
		try {
			loaded = PlugInLoader.loadPlugin(input, RevisionNavigationExperiment.class);
			if (loaded != null) {
				return (RevisionNavigationExperiment<?>) loaded;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("We could not load input: " + input);
		return null;
	}

	private List<IFilter> createFilters() {
		List<IFilter> filters = new ArrayList<IFilter>();
		String filterProperty = ComingProperties.getProperty("filter");
		if (filterProperty == null || filterProperty.isEmpty())
			return filters;

		String[] filter = filterProperty.split(File.pathSeparator);

		for (String singlefilter : filter) {

			if ("bugfix".equals(singlefilter)) {
				filters.add(new BugfixKeywordsFilter());
			} else if ("keywords".equals(singlefilter)) {
				filters.add(new KeyWordsMessageFilter(ComingProperties.getProperty("filtervalue")));
			} else if ("numberhunks".equals(singlefilter))
				filters.add(new NbHunkFilter());
			else if ("maxfiles".equals(singlefilter))
				filters.add(new CommitSizeFilter());
			else if ("withtest".equals(singlefilter))
				filters.add(new ContainTestFilterFilter());
			else {
				IFilter filterLoaded = loadFilter(singlefilter);
				if (filterLoaded != null) {
					filters.add(filterLoaded);
				}
			}

		}
		return filters;
	}

	private IFilter loadFilter(String singlefilter) {
		try {
			Object filter = PlugInLoader.loadPlugin(singlefilter, IFilter.class);
			if (filter != null) {
				return (IFilter) filter;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private ChangePatternSpecification loadPattern() {
		String patternProperty = ComingProperties.getProperty("pattern");

		if (patternProperty != null) {
			// Load pattern from file
			File fl = new File(patternProperty);
			if (fl.exists()) {
				PatternFileParser patternParser = loadPatternParser();
				ChangePatternSpecification patternParsed = patternParser.parse(fl);
				return patternParsed;
			} else {
				throw new IllegalAccessError("The pattern file given as input does not exist " + fl.getAbsolutePath());
			}

		} else {
			// Simple pattern
			String actionProperty = ComingProperties.getProperty("action");
			String entityTypeProperty = ComingProperties.getProperty("entitytype");
			String entityValueProperty = ComingProperties.getProperty("entityvalue");
			String parentTypeProperty = ComingProperties.getProperty("parenttype");
			Integer parentlevelProperty = ComingProperties.getPropertyInteger("parentlevel");

			ActionType at = ActionType.valueOf(actionProperty);

			if (at != null && (entityTypeProperty != null || entityValueProperty != null)) {
				ChangePatternSpecification cpattern = new ChangePatternSpecification();
				PatternEntity affectedEntity = new PatternEntity(entityTypeProperty, entityValueProperty);
				PatternAction pa = new PatternAction(affectedEntity, at);
				if (parentTypeProperty != null) {
					PatternEntity parentEntity = new PatternEntity(parentTypeProperty);
					affectedEntity.setParent(parentEntity, parentlevelProperty);
				}
				cpattern.addChange(pa);
				return cpattern;
			} else {
				throw new IllegalAccessError("The pattern is not well specified: missing entitytype or entityvalue");
			}

		}

		// return null;
	}

	private PatternFileParser loadPatternParser() {
		String parser = ComingProperties.getProperty("patternparser");
		if (parser == null) {
			return new PatternXMLParser();
		} else {
			// TODO:
		}
		return null;
	}

	private static void help() {

		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.out.println("More options and default values at 'configuration.properties' file");

		System.exit(0);

	}

	public RevisionNavigationExperiment<?> getExperiment() {
		return experiment;
	}

	public void setExperiment(RevisionNavigationExperiment<?> experiment) {
		this.experiment = experiment;
	}

}
