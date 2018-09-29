package fr.inria.coming.main;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainDifftAnalyzer;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstanceAnalyzer;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.entity.ActionType;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.core.engine.RevisionNavigationExperiment;
import fr.inria.coming.core.engine.git.GITRepositoryInspector;

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
		options.addOption("output", true, "location of the output");
		options.addOption("mode", true, "execution Mode. ");
		options.addOption("input", true, "Input. ");
		// Pattern mining
		options.addOption("pattern", true, "Location XML of pattern ");
		options.addOption("entitytype", true, "entity type to mine");
		options.addOption("entityvalue", true, "entity value to mine");
		options.addOption("action", true, "action");
		options.addOption("parenttype", true, "parent type");
		options.addOption("parentlevel", true,
				"parent level: numbers of AST node where the parent is located. 1 means inmediate parent");

		options.addOption("showactions", false, "show all actions");
		options.addOption("showentities", false, "show all entities");

		// In case of git
		options.addOption("branch", true, "branch");
		options.addOption("message", true, "comming message");

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

		options.addOption("parameters", true, "Parameters, divided by " + File.pathSeparator);

		if (cmd.hasOption("showactions")) {
			System.out.println("Actions availables: " + Arrays.toString(ActionType.values()));
			return null;
		}

		for (Option option : cmd.getOptions()) {

			if (cmd.hasOption(option.getOpt())) {
				String value = cmd.getOptionValue(option.getOpt());
				ComingProperties.properties.setProperty(option.getOpt(), value);
			}

		}
		;

		if (cmd.hasOption("showentities")) {
			// System.out.println("Entities availables: " + Arrays.toString(Entities));
			System.out.println("TODO");
			return null;
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

		} else {
			// extension point
			throw new IllegalArgumentException("The value of argument input does not exist: " + input);
		}

		if ("diff".equals(mode)) {
			experiment.getAnalyzers().clear();
			experiment.getAnalyzers().add(new FineGrainDifftAnalyzer());
		} else if ("mineinstance".equals(mode)) {
			experiment.getAnalyzers().clear();
			experiment.getAnalyzers().add(new FineGrainDifftAnalyzer());

			ChangePatternSpecification pattern = loadPattern();

			//
			experiment.getAnalyzers().add(new PatternInstanceAnalyzer(pattern));

		} else {
			// TODO: LOAD Analyzers from command

		}

		FinalResult result = experiment.analyze();

		System.out.println(result.toString());

		return result;
	}

	private ChangePatternSpecification loadPattern() {
		String patternProperty = ComingProperties.getProperty("pattern");

		if (patternProperty != null) {
			// Load from XML
			File fl = new File(patternProperty);
			if (fl.exists()) {

				ChangePatternSpecification patternParsed = PatternXMLParser.parseFile(fl.getAbsolutePath());
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
