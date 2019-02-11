package prophet4j.unused;

import java.util.HashMap;
import java.util.Map;

// based on ConfigFile.cpp
public class ConfigFile {
    private Map<String, String> conf_map = new HashMap<>();

    public ConfigFile(String filename) {
        // todo: ~
//        std::ifstream fin(filename.c_str(), std::ifstream::in);
//        if (fin.is_open()) {
//            conf_map.clear();
//            while (!fin.eof()) {
//                std::string line;
//                std::getline(fin, line);
//                line = stripLine(line);
//                size_t idx = line.find('=');
//                if (idx == std::string::npos)
//                break;
//                std::string s1 = line.substr(0, idx);
//                std::string s2 = line.substr(idx + 1);
//                conf_map[s1] = s2;
//            }
//            fin.close();
//        }
//        else
//            fprintf(stderr, "Unable to open configure file %s\n", filename.c_str());
    }

    public String getStr(String key) {
        return conf_map.get(key);
    }

//    boolean hasValue(String key) {
//        return conf_map.count(key);
//    }
}
