package fr.inria.coming.spoon.patterns;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.Test;

import fr.inria.sacha.coming.analyzer.ComingMain;
import com.github.gumtreediff.matchers.Matcher;
/**
 * 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class MainTest {

	
	@Before
	public void setUp() throws Exception {

		ConsoleAppender console = new ConsoleAppender();
		String PATTERN = "%m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.INFO);
		console.activateOptions();
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		Logger.getRootLogger().addAppender(console);
		
		
		
		java.util.logging.Logger.getLogger("fr.labri.gumtree.matchers").setLevel(java.util.logging.Level.OFF);
		Matcher.LOGGER.setLevel(java.util.logging.Level.OFF);
	}
	
	@Test
	public void testListEntities() {
		
		ComingMain.main(new String[]{"-e"});
	}
	
	@Test
	public void testListActions() {
		
		ComingMain.main(new String[]{"-a"});
	}
	
	@Test
	public void testMineBinaryOperator() {		
		ComingMain.main(new String[]{"repogit4testv0",
				"BinaryOperator","INS"});
	}


}
