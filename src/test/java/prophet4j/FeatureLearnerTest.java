package prophet4j;

import org.junit.Test;
import prophet4j.defined.FeatureType.*;
import prophet4j.feature.FeatureResolver;

import static org.junit.Assert.assertEquals;

public class FeatureLearnerTest {
    @Test
    public void testFeatureLearner() {
        // todo: ...
        String str0, str1;
        FeatureResolver featureResolver = new FeatureResolver();
        {
            str0 = "class foo{public void bar(){\nint a=1;\nint b=1;\nint c=1;\n}}";
            str1 = "class foo{public void bar(){\nint a=1;\nint b=1+1;\nint c=1;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.OP_ADD_AF));
        }
        {
            str0 = "package kth.se;\nclass foo{public void bar(){\nint a=1;\na=1;\n}}";
            str1 = "package kth.se;\nclass foo{public void bar(){\nint a=1;\n++a;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.UOP_INC_AF));
            str1 = "package kth.se;\nclass foo{public void bar(){\nint a=1;\na++;\n}}";
            assertEquals(Boolean.TRUE, featureResolver.easyExtractor(str0, str1).containFeatureType(AtomicFeature.UOP_INC_AF));
        }
    }
}
