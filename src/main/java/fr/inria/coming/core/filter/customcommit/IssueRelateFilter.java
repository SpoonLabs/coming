package fr.inria.coming.core.filter.customcommit;


import com.alibaba.fastjson.JSONObject;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.core.filter.AbstractChainedFilter;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class IssueRelateFilter extends AbstractChainedFilter<Commit> {

    public IssueRelateFilter() throws IOException {
        super();

    }

    public IssueRelateFilter(IFilter parentFilter) throws IOException {
        super(parentFilter);
    }

    private static Map<String, Object> Json2map(String jsonObj) {

        JSONObject jsonObject = JSONObject.parseObject(jsonObj);
        Map<String, Object> map =new ConcurrentHashMap<>();
        Set<String> keySet = jsonObject.keySet();
        for (String s : keySet) {
            map.put(s, 0);
        }
        return map;
    }

    private ClassPathResource resource = new ClassPathResource("adConfig.json");//本地data
    private File file = resource.getFile();
    private String jsonString = FileUtils.readFileToString(file,"UTF-8");

    @Override
    public boolean accept(Commit c) {

        Map<String,Object> result= Json2map(jsonString);

        if (super.accept(c)) {
            String commitId = c.getName();

            if (result.containsKey(commitId)){
                return true;
            }
        }
        return false;
    }
}
