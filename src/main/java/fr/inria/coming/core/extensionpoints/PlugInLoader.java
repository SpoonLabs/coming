package fr.inria.coming.core.extensionpoints;

import org.apache.log4j.Logger;

import fr.inria.coming.main.ComingProperties;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PlugInLoader {

	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	public static Object loadPlugin(String className, Class type) throws Exception {
		Object object = null;
		try {
			Class classDefinition = Class.forName(className);
			object = classDefinition.newInstance();
		} catch (Exception e) {
			log.error("Loading " + className + " --" + e);
			throw new Exception("Error Loading Engine: " + e);
		}
		if (type.isInstance(object))
			return object;
		else
			throw new Exception("The strategy " + className + " does not extend from " + type.getCanonicalName());

	}

	public static Object loadPlugin(ExtensionPoints ep, Class[] typesConst, Object[] args) throws Exception {
		String property = ComingProperties.getProperty(ep.identifier);
		if (property == null || property.trim().isEmpty())
			return null;

		return loadPlugin(property, ep._class, typesConst, args);
	}

	public static Object loadPlugin(String className, Class type, Class[] typesConst, Object[] args) throws Exception {
		Object object = null;
		try {
			Class classDefinition = Class.forName(className);
			object = classDefinition.getConstructor(typesConst).newInstance(args);
		} catch (Exception e) {
			log.error("Loading " + className + " --" + e);
			throw new Exception("Error Loading Engine: " + e);
		}
		if (type.isInstance(object))
			return object;
		else
			throw new Exception("The strategy " + className + " does not extend from " + type.getClass().getName());

	}

}
