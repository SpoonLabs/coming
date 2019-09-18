package fr.inria.coming.main;

import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class ComingProperties {

	public static Properties properties;

	static {
		reset();
	}

	public static void reset() {
		InputStream propFile;
		try {
			properties = new Properties();
			propFile = ComingProperties.class.getClassLoader().getResourceAsStream("config-coming.properties");

			properties.load(propFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static Integer getPropertyInteger(String key) {
		return Integer.valueOf(properties.getProperty(key));
	}

	public static Boolean getPropertyBoolean(String key) {
		return Boolean.valueOf(properties.getProperty(key));
	}

	public static Double getPropertyDouble(String key) {
		return Double.valueOf(properties.getProperty(key));
	}

	public static void main(String[] s) {
		String ss = ComingProperties.properties.getProperty("test");
		System.out.println("-->" + ss);
	}
}
