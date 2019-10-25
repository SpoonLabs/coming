package fr.inria.prophet4j.utility;

import fr.inria.prophet4j.utility.Option.RankingOption;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Support {
    public static final String PROPHET4J_DIR = Support.class.getClassLoader().getResource("").getPath() + "prophet4j/";

    public enum DirType {
        // buggy files & patched files by human
        DATA_DIR,
        // feature vectors
        FEATURE_DIR,
        // parameter vectors
        PARAMETER_DIR,
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
                    case BEARS:
                        stringJoiner.add("Bears");
                        break;
                    case BUG_DOT_JAR_MINUS_MATH:
                        stringJoiner.add("Bug-dot-jar-minus-MATH");
                        break;
                    case QUIX_BUGS:
                        stringJoiner.add("QuixBugs");
                        break;
                    case CLOSURE:
                        stringJoiner.add("Closure/human-closure");
                        break;
                }
                break;
            case FEATURE_DIR:
                stringJoiner = new StringJoiner("][", PROPHET4J_DIR + "_BIN/[", "]/");
                stringJoiner.add(option.dataOption.name().toLowerCase());
                stringJoiner.add(option.patchOption.name().toLowerCase());
                stringJoiner.add(option.featureOption.name().toLowerCase());
                break;
            case PARAMETER_DIR:
                stringJoiner = new StringJoiner("][", PROPHET4J_DIR + "_BIN/[", "]/");
                stringJoiner.add(option.dataOption.name().toLowerCase());
                stringJoiner.add(option.patchOption.name().toLowerCase());
                stringJoiner.add(option.featureOption.name().toLowerCase());
                stringJoiner.add("cross_entropy");
                break;
        }
        return stringJoiner.toString();
    }

    public static String getFilePath4Ranking(Option option, RankingOption rankingOption, boolean bin) {
        String filePath = PROPHET4J_DIR;
        if (bin) {
            filePath += "_BIN/" + option.featureOption.name().toLowerCase() + "/";
        }
        switch (rankingOption) {
            case D_HUMAN:
                filePath += "human-patch/";
                break;
            case D_CORRECT:
                filePath += "D_correct/";
                break;
            case D_INCORRECT:
                filePath += "D_incorrect/";
                break;
            case P_CORRECT:
                filePath += "P_correct/";
                break;
            case P_INCORRECT:
                filePath += "P_incorrect/";
                break;
        }
        return filePath;
    }

    public static List<String> deserialize(String filePath) {
        List<String> strings = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            strings = (List<String>) ois.readObject();
            ois.close();
            fis.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return strings;
    }

    public static void serialize(String filePath, List<String> strings) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(strings);
            oos.flush();
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
