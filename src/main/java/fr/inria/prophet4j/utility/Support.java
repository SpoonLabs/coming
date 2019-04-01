package fr.inria.prophet4j.utility;

import fr.inria.prophet4j.utility.Option.RankingOption;
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

    public static String getFilePath4Ranking(RankingOption rankingOption) {
        String filePath = PROPHET4J_DIR;
        switch (rankingOption) {
            case D_HUMAN:
                filePath += "cardumen_dissection/";
                break;
            case D_CORRECT:
                filePath += "D_correct/";
                break;
            case D_INCORRECT:
                filePath += "D_incorrect/";
                break;
        }
        return filePath;
    }
}
