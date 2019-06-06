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
import org.apache.commons.cli.ParseException;
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
import fr.inria.coming.codefeatures.FeatureAnalyzer;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.engine.RevisionNavigationExperiment;
import fr.inria.coming.core.engine.callback.IntermediateResultProcessorCallback;
import fr.inria.coming.core.engine.files.FileNavigationExperiment;
import fr.inria.coming.core.engine.filespair.FilesPairNavigation;
import fr.inria.coming.core.engine.git.GITRepositoryInspector;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.core.entities.output.FeaturesOutput;
import fr.inria.coming.core.entities.output.JSonChangeFrequencyOutput;
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
		options.addOption(
				Option.builder("location").argName("path").hasArg().desc("analyse the content in \'path\'").build());

		options.addOption(Option.builder("mode").argName("mineinstance | diff | features").hasArg()
				.desc("the mode of execution of the analysis").build());

		options.addOption(Option.builder("input").argName("git(default) | files | filespair").hasArg().desc(
				"format of the content present in the given -path. git implies that the path is a git repository. files implies the path contains .patch files ")
				.build());

		options.addOption(Option.builder("output").argName("path").hasArg()
				.desc("dump the output of the analysis in the given path").build());

		options.addOption(Option.builder("outputprocessor").argName("classname").hasArg()
				.desc("output processors for result").build());

		// Pattern mining
		options.addOption(Option.builder("pattern").argName("path").hasArg()
				.desc("path of the pattern file to be used when the -mode is \'mineinstance\'").build());

		options.addOption(Option.builder("patternparser").argName("classname").hasArg()
				.desc("parser to be used for parsing the file specified -pattern. Default is XML").build());

		options.addOption("entitytype", true, "entity type to be mine");
		options.addOption("entityvalue", true, "the value of the entity  mentioned in -entitytype");

		options.addOption(Option.builder("action").argName("INS | DEL | UPD | MOV | PER | ANY").hasArg()
				.desc("tye of action to be mined").build());

		options.addOption("parenttype", true, "parent type of the nodes to be considered");
		options.addOption("parentlevel", true,
				"numbers of AST node where the parent is located. 1 implies immediate parent");

		options.addOption("hunkanalysis", true, "include analysis of hunks");

		options.addOption("showactions", false, "show all actions");
		options.addOption("showentities", false, "show all entities");

		// Revision filter
		options.addOption("filter", true, "name of the filter");
		options.addOption("filtervalue", true, "values of the filter  mentioned in -filter");

		// In case of git
		options.addOption(Option.builder("branch").argName("branch name").hasArg()
				.desc("In case of -input=\'git\', use this branch name. Default is master.").build());
		options.addOption("message", true, "comming message"); // TODO: What's this?
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

	RevisionNavigationExperiment<?> navigatorEngine = null;

	@SuppressWarnings("rawtypes")
	public FinalResult run(String[] args) throws Exception {

		boolean created = createEngine(args);
		if (!created)
			return null;

		return start();
	}

	public FinalResult start() {
		if (navigatorEngine == null)
			throw new IllegalAccessError("error: initialize the engine first");

		FinalResult result = navigatorEngine.analyze();

		return result;
	}

	public boolean createEngine(String[] args) throws ParseException {
		ComingProperties.reset();
		CommandLine cmd = null;
		this.navigatorEngine = null;
		try {
			cmd = parser.parse(options, args);
		} catch (UnrecognizedOptionException e) {
			System.out.println("Error: " + e.getMessage());
			help();
			return false;
		}
		if (cmd.hasOption("help")) {
			help();
			return false;
		}

		if (cmd.hasOption("showactions")) {
			System.out.println("---");
			System.out.println("Actions available: ");
			for (ActionType a : ActionType.values()) {
				System.out.println(a);
			}
			System.out.println("---");
			return false;
		}

		if (cmd.hasOption("showentities")) {
			System.out.println("---");
			System.out.println("Entities Type Available:");
			System.out.println("---");
			for (EntityTypeSpoon et : EntityTypeSpoon.values()) {
				System.out.println(et);
			}
			System.out.println("---");
			return false;
		}
		for (Option option : cmd.getOptions()) {

			if (cmd.hasOption(option.getOpt())) {
				String value = cmd.getOptionValue(option.getOpt());
				ComingProperties.properties.setProperty(option.getOpt(), value);
			}

		}

		if (cmd.hasOption("parameters")) {
			String[] pars = cmd.getOptionValue("parameters").split(":");
			if (pars.length % 2 != 0) {
				throw new RuntimeException("The number of input parameters must be even.");
			}

			for (int i = 0; i < pars.length; i = i + 2) {
				String key = pars[i];
				String value = pars[i + 1];
				ComingProperties.properties.setProperty(key, value);

			}
		}

		String mode = ComingProperties.getProperty("mode");
		String input = ComingProperties.getProperty("input");

		// CONFIGURATION:
		loadInput(input);

		loadModelAnalyzers(mode);

		loadFilters();

		loadOutput();

		return true;
	}

	private void loadFilters() {
		navigatorEngine.setFilters(createFilters());
	}

	private void loadOutput() {
		String outputs = ComingProperties.getProperty("outputprocessor");
		if (outputs == null) {
			navigatorEngine.getOutputProcessors().add(0, new StdOutput());
		} else {
			loadOutputProcessors(outputs);
		}
	}

	private void loadInput(String input) {
		if (input == null || input.equals("git")) {
			navigatorEngine = new GITRepositoryInspector();
		} else if (input.equals("files")) {
			navigatorEngine = new FileNavigationExperiment();
		} else if (input.equals("filespair")) {
			navigatorEngine = new FilesPairNavigation();
		} else {
			// extension point
			navigatorEngine = loadInputEngine(input);
		}
	}

	private void loadModelAnalyzers(String modes) {

		String[] modesp = modes.split(":");

		for (String mode : modesp) {

			if ("diff".equals(mode)) {
				navigatorEngine.getAnalyzers().clear();
				navigatorEngine.getAnalyzers().add(new FineGrainDifftAnalyzer());
				navigatorEngine.getOutputProcessors().add(new JSonChangeFrequencyOutput());
			} else if ("mineinstance".equals(mode)) {
				navigatorEngine.getAnalyzers().clear();
				navigatorEngine.getAnalyzers().add(new FineGrainDifftAnalyzer());

				List<ChangePatternSpecification> patterns = loadPattern();
				navigatorEngine.getAnalyzers().add(new PatternInstanceAnalyzer(patterns));

				// By default JSON output of pattern instances
				navigatorEngine.getOutputProcessors().add(new JSonPatternInstanceOutput());

			} else if ("features".equals(mode)) {
				navigatorEngine.getAnalyzers().clear();
				navigatorEngine.getAnalyzers().add(new FineGrainDifftAnalyzer());
				navigatorEngine.getAnalyzers().add(new FeatureAnalyzer());

				navigatorEngine.getOutputProcessors().add(new FeaturesOutput());

			} else {
				// LOAD Analyzers from command
				loadAnalyzersFromCommand(mode);
			}

		}

		if (ComingProperties.getPropertyBoolean("hunkanalysis")) {
			navigatorEngine.getAnalyzers().add(0, new HunkDifftAnalyzer());
		}
	}

	private void loadAnalyzersFromCommand(String mode) {
		try {
			Object analyzerLoaded = PlugInLoader.loadPlugin(mode, Analyzer.class);
			if (analyzerLoaded != null) {
				navigatorEngine.getAnalyzers().add((Analyzer) analyzerLoaded);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void loadOutputProcessors(String output) {
		String[] outputs = output.split(":");

		for (String singlefoutput : outputs) {
			try {
				if (singlefoutput.equals("changefrequency")) {
					navigatorEngine.getOutputProcessors().add(new JSonChangeFrequencyOutput());
				} else {
					Object outLoaded = PlugInLoader.loadPlugin(singlefoutput, IOutput.class);
					if (outLoaded != null) {
						navigatorEngine.getOutputProcessors().add((IOutput) outLoaded);
					}
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

		String[] filter = filterProperty.split(":");

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

	private List<ChangePatternSpecification> loadPattern() {
		String patternProperty = ComingProperties.getProperty("pattern");
		List<ChangePatternSpecification> patternsFound = new ArrayList();

		if (patternProperty != null) {

			String[] patterns = patternProperty.split(File.pathSeparator);
			// Load pattern from file
			for (String pattern : patterns) {
				File fl = new File(pattern);
				if (fl.exists()) {
					PatternFileParser patternParser = loadPatternParser();
					ChangePatternSpecification patternParsed = patternParser.parse(fl);
					patternsFound.add(patternParsed);
				} else {
					log.error("The pattern file given as input does not exist " + fl.getAbsolutePath());
				}
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
				patternsFound.add(cpattern);
			} else {
				throw new IllegalAccessError("The pattern is not well specified: missing entitytype or entityvalue");
			}

		}
		if (patternsFound.isEmpty()) {
			throw new IllegalAccessError("Any valid pattern file in " + patternProperty);
		}
		return patternsFound;
	}

	public PatternFileParser loadPatternParser() {
		String parser = ComingProperties.getProperty("patternparser");
		if (parser == null || parser.equals("xmlparser")) {
			return new PatternXMLParser();
		} else {

			try {
				Object parserFile = PlugInLoader.loadPlugin(parser, PatternFileParser.class);
				if (parserFile != null) {
					return (PatternFileParser) parserFile;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

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
		return navigatorEngine;
	}

	public void setExperiment(RevisionNavigationExperiment<?> experiment) {
		this.navigatorEngine = experiment;
	}

	public void registerIntermediateCallback(IntermediateResultProcessorCallback callback) {
		if (this.navigatorEngine == null)
			throw new IllegalStateException("Please, initialize the engine first");

		this.navigatorEngine.setIntermediateCallback(callback);
	}
}
