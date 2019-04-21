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
                switch (option.dataOption) {
                    case CARDUMEN:
                    case SANER:
                        stringJoiner.add(option.dataOption.name().toLowerCase());
                        break;
                    case PGA:
                        // PGA seems not well-prepared todo check
                        System.exit(9);
                        break;
                    case BEARS:
                        stringJoiner.add("Bears");
                        break;
                    case BUG_DOT_JAR:
                        stringJoiner.add("Bug-dot-jar");
                        break;
                    case DEFECTS4J:
                        stringJoiner.add("Defects4J");
                        break;
                    case QUIX_BUGS:
                        stringJoiner.add("QuixBugs");
                        break;
                }
                break;
            case FEATURE_DIR:
                stringJoiner = new StringJoiner("][", PROPHET4J_DIR + "[", "]/");
                stringJoiner.add(option.dataOption.name().toLowerCase());
                stringJoiner.add(option.patchOption.name().toLowerCase());
                stringJoiner.add(option.featureOption.name().toLowerCase());
                break;
            case PARAMETER_DIR:
                stringJoiner = new StringJoiner("][", PROPHET4J_DIR + "[", "]/");
                stringJoiner.add(option.dataOption.name().toLowerCase());
                stringJoiner.add(option.patchOption.name().toLowerCase());
                stringJoiner.add(option.featureOption.name().toLowerCase());
                stringJoiner.add("cross_entropy");
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
