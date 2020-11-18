package fr.inria.coming.core.engine.files;

import java.io.File;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FileUtil {

	public static String extractFileName(String completeFileName) {

		int idx = completeFileName.lastIndexOf(File.separator);
		return completeFileName.substring(idx + 1);

	}

}
