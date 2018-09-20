package fr.inria.coming.main;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;

import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.coming.changeminer.analyzer.RepositoryInspector;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.FineGrainChangeCommitAnalyzer;
import fr.inria.coming.changeminer.analyzer.commitAnalyzer.filters.SimpleChangeFilter;
import fr.inria.coming.changeminer.entity.ActionType;
import fr.inria.coming.changeminer.util.ConsoleOutput;
import fr.inria.coming.changeminer.util.XMLOutput;
import fr.inria.coming.core.filter.DummyFilter;
import fr.inria.coming.core.interfaces.Commit;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class ComingMain {

	static Options options = new Options();

	CommandLineParser parser = new BasicParser();

	static {
		options.addOption("location", true, "location of the project");
		options.addOption("entity", true, "entity to mine");
		options.addOption("action", true, "action");
		options.addOption("message", true, "comming message");
		options.addOption("branch", true, "branch");
		options.addOption("showactions", false, "show all actions");
		options.addOption("showentities", false, "show all entities");
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

	public void run(String[] args) throws Exception {
		ConfigurationProperties.clear();

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (UnrecognizedOptionException e) {
			System.out.println("Error: " + e.getMessage());
			help();
			return;
		}
		if (cmd.hasOption("help")) {
			help();
			return;
		}

		RepositoryInspector c = new RepositoryInspector();
		String location = null;
		String entity = null;
		String action = null;
		String branch = null;
		String message = null;

		if (cmd.hasOption("showactions")) {
			System.out.println("Actions availables: " + Arrays.toString(ActionType.values()));
			return;
		}

		if (cmd.hasOption("showentities")) {
			// System.out.println("Entities availables: " + Arrays.toString(Entities));
			System.out.println("TODO");
			return;
		}

		if (cmd.hasOption("message")) {
			message = cmd.getOptionValue("message");
		}

		if (cmd.hasOption("location")) {
			location = cmd.getOptionValue("location");
		}

		if (cmd.hasOption("entity")) {
			entity = cmd.getOptionValue("entity");
		}
		if (cmd.hasOption("action")) {
			action = cmd.getOptionValue("action");
		}
		if (cmd.hasOption("message")) {
			message = cmd.getOptionValue("message");
		}

		if (cmd.hasOption("branch")) {
			branch = cmd.getOptionValue("branch");
		} else
			branch = "master";

		FineGrainChangeCommitAnalyzer analyzer = new FineGrainChangeCommitAnalyzer(
				new SimpleChangeFilter(entity, ActionType.valueOf(action)));

		// TODO:
		Map<Commit, List<Operation>> instancesFound = c.analize(location, branch, analyzer, new DummyFilter());
		ConsoleOutput.printResultDetails(instancesFound);
		XMLOutput.print(instancesFound);
	}

	private static void help() {

		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.out.println("More options and default values at 'configuration.properties' file");

		System.exit(0);

	}

}
