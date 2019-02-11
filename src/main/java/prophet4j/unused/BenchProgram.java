package prophet4j.unused;

// based on BenchProgram.cpp
public class BenchProgram {
    private final String LOCALIZATION_RESULT = "profile_localization.res";
    private final String CONFIG_FILE_PATH = "repair.conf";
    private final String SOURCECODE_BACKUP = "__backup";
    private final String SOURCECODE_BACKUP_LOG = "__backup.log";
    private final String LDBACKUP = "ld-backup";

    private ConfigFile config;

    public BenchProgram(String workDirPath) {
        config = new ConfigFile(workDirPath + "/" + CONFIG_FILE_PATH);
        Init(workDirPath, true);
    }

    public BenchProgram(String configFileName, String workDirPath, boolean no_clean_up) {
        config = new ConfigFile(configFileName);
        Init(workDirPath, no_clean_up);
    }

    private void Init(String workDirPath, boolean no_clean_up) {
        // todo: ~
//        compile_cnt = 0;
//        test_cnt = 0;
//        this->cache = NULL;
//        this->no_clean_up = no_clean_up;
//        this->ori_src_dir = config.getStr("src_dir");
//        this->wrap_ld = false;
//        if (config.hasValue("wrap_ld"))
//            this->wrap_ld = (config.getStr("wrap_ld") == "yes");
//        if ((workDirPath == "") || (!exist_directory(workDirPath))) {
//            int ret;
//            std::string cmd;
//            if (workDirPath == "") {
//                do {
//                    this->work_dir = getRndName();
//                    std::string cmd = "mkdir ";
//                    ret = system((cmd + work_dir).c_str());
//                }
//                while (ret != 0);
//            }
//            else {
//                this->work_dir = workDirPath;
//                std::string cmd = "mkdir ";
//                ret = system((cmd + work_dir).c_str());
//                assert( ret == 0 );
//            }
//            this->work_dir = getFullPath(this->work_dir);
//            // This part is obsolete, it seems it will not add much performance
//        /*if (using_ramfs) {
//            cmd = "mount -t ramfs ramfs ";
//            cmd += work_dir;
//            ret = system(cmd.c_str());
//            if (ret != 0) {
//                fprintf(stderr, "You should run it with root permission!\n");
//                exit(1);
//            }
//            cmd = "chmod a+w ";
//            ret = system((cmd + work_dir).c_str());
//            assert(ret == 0);
//        }*/
//
//            // We create an initial clone of the basic src direcotry
//            src_dirs.clear();
//            createSrcClone("src");
//            this->src_dir = getFullPath(this->work_dir + "/src");
//
//            std::string ori_test_dir = config.getStr("test_dir");
//            if (ori_test_dir != "") {
//                cmd = "cp -rf ";
//                cmd += ori_test_dir + " " + work_dir + "/tests";
//                ret = system(cmd.c_str());
//                assert(ret == 0);
//            }
//        }
//        else {
//            this->work_dir = getFullPath(workDirPath);
//            this->src_dir = getFullPath(work_dir + "/src");
//            src_dirs.clear();
//            src_dirs.insert(std::make_pair("src", true));
//
//            // If we just in middle of repair, we need to restore before we go on
//            std::ifstream fin((work_dir + "/" + SOURCECODE_BACKUP_LOG).c_str(), std::ifstream::in);
//            if (fin.is_open()) {
//                std::string target_file;
//                char tmp[1000];
//                size_t cnt = 0;
//                int ret;
//                std::string cmd;
//                while (fin.getline(tmp, 1000)) {
//                    target_file = tmp;
//                    {
//                        std::ostringstream sout;
//                        sout << "cp -rf "  << work_dir << "/" << SOURCECODE_BACKUP << cnt << " " << src_dir << "/" << target_file;
//                        cmd = sout.str();
//                    }
//                    ret = system(cmd.c_str());
//                    assert( ret == 0);
//                    {
//                        std::ostringstream sout;
//                        sout << "rm -rf " << work_dir << "/" << SOURCECODE_BACKUP << cnt;
//                        cmd = sout.str();
//                    }
//                    ret = system(cmd.c_str());
//                    assert( ret == 0);
//                }
//                fin.close();
//                cmd = "rm -rf " + work_dir + "/" + SOURCECODE_BACKUP_LOG;
//                ret = system(cmd.c_str());
//                assert( ret == 0);
//            }
//        }
//
//        dep_dir = config.getStr("dep_dir");
//        if (dep_dir != "")
//            dep_dir = getFullPath(dep_dir);
//
//        this->test_dir = getFullPath(work_dir+"/tests");
//        this->build_log_file = work_dir + "/build.log";
//        // Clean up builg log for every execution
//        std::string cmd = std::string("rm -rf ") + build_log_file;
//        int ret = system(cmd.c_str());
//        assert( ret == 0);
//        this->build_cmd = getFullPath(config.getStr("build_cmd"));
//        this->test_cmd = getFullPath(config.getStr("test_cmd"));
//        this->localization_filename = work_dir + "/" + LOCALIZATION_RESULT;
//
//        // The files for controling timeout stuff
//        total_repair_build_time = 0;
//        repair_build_cnt = 0;
//        this->case_timeout = 60;
//        if (config.getStr("single_case_timeout") != "") {
//            std::istringstream sin(config.getStr("single_case_timeout"));
//            sin >> case_timeout;
//        }
//
//        std::string revision_file = config.getStr("revision_file");
//        parseRevisionLog(revision_file, negative_cases, positive_cases);
    }

    public boolean verifyTestCases() {
//        buildFull("src");
//        //prepare_test();
//        std::set<unsigned long> tmp = testSet("src", negative_cases, std::map<std::string, std::string>());
//        if (tmp.size() != 0) {
//            outlog_printf(0, "Unexpected pass:\n");
//            for (std::set<unsigned long>::iterator it = tmp.begin(); it != tmp.end(); ++it)
//            outlog_printf(0, "%lu\n", *it);
//            return false;
//        }
//        tmp = testSet("src", positive_cases, std::map<std::string, std::string>());
//        if (tmp.size() != positive_cases.size()) {
//            outlog_printf(0, "Unexpected fail:\n");
//            for (std::set<unsigned long>::iterator it = positive_cases.begin(); it != positive_cases.end(); ++it)
//            if (tmp.count(*it) == 0)
//            outlog_printf(0, "%lu\n", *it);
//            outlog_printf(0, "Only passed tot: %lu\n", tmp.size());
//            return false;
//            //fprintf(stderr, "Eliminate not passed cases!\n");
//            //positive_cases = tmp;
//            //return true;
//        }
//        outlog_printf(0, "All passed!\n");
        return true;
    }

    public ConfigFile getCurrentConfig() {
        return config;
    }
}
