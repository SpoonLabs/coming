package fr.inria.sacha.coming.analyzer;

import org.apache.log4j.Logger;

import fr.inria.sacha.coming.util.ConfigurationProperties;

public class Parameters {

	static Logger log = Logger.getLogger(Parameters.class.getName());
	
	// PARAMETERS
	public static int MAX_LINES_PER_HUNK;
	public static int MAX_HUNKS_PER_FILECOMMIT;
	public static int MAX_FILES_PER_COMMIT;
	public static int MAX_AST_CHANGES_PER_FILE;
	public static int MIN_AST_CHANGES_PER_FILE;
	public static boolean ONLY_COMMIT_WITH_TEST_CASE;
	public static boolean ONLY_ROOTS;
	

	public static void setUpProperties() {
		
		MAX_LINES_PER_HUNK = ConfigurationProperties
				.getPropertyInteger("MAX_LINES_PER_HUNK");

		MAX_HUNKS_PER_FILECOMMIT = ConfigurationProperties
				.getPropertyInteger("MAX_HUNKS_PER_FILECOMMIT");

		MAX_FILES_PER_COMMIT = ConfigurationProperties
				.getPropertyInteger("MAX_FILES_PER_COMMIT");

		MAX_AST_CHANGES_PER_FILE = ConfigurationProperties
				.getPropertyInteger("MAX_AST_CHANGES_PER_FILE");

		MIN_AST_CHANGES_PER_FILE = ConfigurationProperties
				.getPropertyInteger("MIN_AST_CHANGES_PER_FILE");

		ONLY_COMMIT_WITH_TEST_CASE = ConfigurationProperties
				.getPropertyBoolean("excludeCommitWithOutTest");
		
		ONLY_COMMIT_WITH_TEST_CASE = ConfigurationProperties
				.getPropertyBoolean("only_roots");
		
	}
	
	
	public static void printParameters(){
		log.info("MAX_LINES_PER_HUNK: " + Parameters.MAX_LINES_PER_HUNK);
		log.info("MAX_HUNKS_PER_FILECOMMIT: " + Parameters.MAX_HUNKS_PER_FILECOMMIT);
		log.info("MAX_FILES_PER_COMMIT: " + Parameters.MAX_FILES_PER_COMMIT);
		log.info("MAX_AST_CHANGES_PER_FILE: " + Parameters.MAX_AST_CHANGES_PER_FILE);
		log.info("MUST_INCLUDE_TEST: " + Parameters.ONLY_COMMIT_WITH_TEST_CASE);
	}
}
