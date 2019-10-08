package fr.inria.coming.spoon.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by khesoem on 10/8/2019.
 */
public class TestUtils {
    private static TestUtils _instance;

    public static TestUtils getInstance(){
        if(_instance == null)
            _instance = new TestUtils();
        return _instance;
    }

    public File getFile(String name) throws UnsupportedEncodingException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(URLDecoder.decode(classLoader.getResource(name).getFile(), "UTF-8"));
        return file;
    }
}
