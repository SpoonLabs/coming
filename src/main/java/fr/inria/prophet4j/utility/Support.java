package fr.inria.prophet4j.utility;

import java.util.StringJoiner;

public class Support {
    public static final String PROPHET4J_DIR = "src/main/resources/prophet4j/";

    public enum DirType {
        // buggy files & patched files by human
        DATA_DIR,
        // feature vectors
        FEATURE_DIR,
        // parameter vectors
        PARAMETER_DIR,
        // csv files
        CSV_DIR,
    }

    public static String getFilePath(DirType dirType, Option option) {
        StringJoiner stringJoiner = new StringJoiner("_", PROPHET4J_DIR, "/");
        switch (dirType) {
            case DATA_DIR:
                stringJoiner.add(option.dataOption.name().toLowerCase());
                break;
            case FEATURE_DIR:
                stringJoiner.add(option.dataOption.name().toLowerCase());
                stringJoiner.add(option.patchOption.name().toLowerCase());
                stringJoiner.add(option.featureOption.name().toLowerCase());
                break;
            case PARAMETER_DIR:
                stringJoiner.add(option.dataOption.name().toLowerCase());
                stringJoiner.add(option.patchOption.name().toLowerCase());
                stringJoiner.add(option.featureOption.name().toLowerCase());
                stringJoiner.add(option.getModelOption().name().toLowerCase());
                break;
            case CSV_DIR:
                stringJoiner.add("_csv");
                stringJoiner.add(option.dataOption.name().toLowerCase());
                stringJoiner.add(option.patchOption.name().toLowerCase());
                stringJoiner.add(option.featureOption.name().toLowerCase());
                break;
        }
        return stringJoiner.toString();
    }
}
