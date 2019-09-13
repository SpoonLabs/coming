package fr.inria.coming.spoon.repairability;

import com.github.difflib.text.DiffRow;
import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromDiff;
import fr.inria.coming.changeminer.analyzer.instancedetector.PatternInstancesFromRevision;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.repairability.RepairabilityAnalyzer;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class RepairabilityTest {

    @Test
    public void testRepairabilityInterface() throws Exception {
        TestUtills.runRepairability("JMutRepair", "/repairability_test_files/one/");
    }


    @Test
    public void testDiffResults() throws Exception {
        int count=0;
        FinalResult result = TestUtills.runRepairabilityGit("ALL", "repogit4testv0");
        Map<IRevision, RevisionResult> revisionsMap = result.getAllResults();
        assertEquals(13, revisionsMap.keySet().size());

        for (Map.Entry<IRevision, RevisionResult> entry : revisionsMap.entrySet()) {
            RevisionResult rr = entry.getValue();
            PatternInstancesFromRevision instances = (PatternInstancesFromRevision) rr.getResultFromClass(RepairabilityAnalyzer.class);

            List<DiffRow> row_list = instances.getRow_list();
//            System.out.println("..........");
//            System.out.println(row_list);
            switch (count){
                    case 1:
                        assertEquals(row_list,null);

                    case 9:
                        assertEquals(row_list,null);

                    case 2:
                    assertEquals(row_list,"[[INSERT,,/*], [INSERT,, * Licensed to the Apache Software Foundation (ASF) under one or more], [INSERT,, * contributor license agreements.  See the NOTICE file distributed with], [INSERT,, * this work for additional information regarding copyright ownership.], [INSERT,, * The ASF licenses this file to You under the Apache License, Version 2.0], [INSERT,, * (the \"License\"); you may not use this file except in compliance with], [INSERT,, * the License.  You may obtain a copy of the License at], [INSERT,, *], [INSERT,, *      http://www.apache.org/licenses/LICENSE-2.0], [INSERT,, *], [INSERT,, * Unless required by applicable law or agreed to in writing, software], [INSERT,, * distributed under the License is distributed on an \"AS IS\" BASIS,], [INSERT,, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.], [INSERT,, * See the License for the specific language governing permissions and], [INSERT,, * limitations under the License.], [INSERT,, */], [INSERT,,package org.apache.commons.lang3;], [EQUAL,,], [INSERT,,/**], [INSERT,, * &lt;p&gt;Operations on {@link CharSequence} that are], [INSERT,, * {@code null} safe.&lt;/p&gt;], [INSERT,, *], [INSERT,, * @see CharSequence], [INSERT,, * @since 3.0], [INSERT,, * @version $Id$], [INSERT,, */], [INSERT,,public class CharSequenceUtils {], [INSERT,,], [INSERT,,    private static final int NOT_FOUND = -1;], [INSERT,,], [INSERT,,    /**], [INSERT,,     * &lt;p&gt;{@code CharSequenceUtils} instances should NOT be constructed in], [INSERT,,     * standard programming. &lt;/p&gt;], [INSERT,,     *], [INSERT,,     * &lt;p&gt;This constructor is public to permit tools that require a JavaBean], [INSERT,,     * instance to operate.&lt;/p&gt;], [INSERT,,     */], [INSERT,,    public CharSequenceUtils() {], [INSERT,,        super();], [INSERT,,    }], [INSERT,,], [INSERT,,    //-----------------------------------------------------------------------], [INSERT,,    /**], [INSERT,,     * &lt;p&gt;Returns a new {@code CharSequence} that is a subsequence of this], [INSERT,,     * sequence starting with the {@code char} value at the specified index.&lt;/p&gt;], [INSERT,,     *], [INSERT,,     * &lt;p&gt;This provides the {@code CharSequence} equivalent to {@link String#substring(int)}.], [INSERT,,     * The length (in {@code char}) of the returned sequence is {@code length() - start},], [INSERT,,     * so if {@code start == end} then an empty sequence is returned.&lt;/p&gt;], [INSERT,,     *], [INSERT,,     * @param cs  the specified subsequence, null returns null], [INSERT,,     * @param start  the start index, inclusive, valid], [INSERT,,     * @return a new subsequence, may be null], [INSERT,,     * @throws IndexOutOfBoundsException if {@code start} is negative or if ], [INSERT,,     *  {@code start} is greater than {@code length()}], [INSERT,,     */], [INSERT,,    public static CharSequence subSequence(final CharSequence cs, final int start) {], [INSERT,,        return cs == null ? null : cs.subSequence(start, cs.length());], [INSERT,,    }], [INSERT,,], [INSERT,,    //-----------------------------------------------------------------------], [INSERT,,    /**], [INSERT,,     * &lt;p&gt;Finds the first index in the {@code CharSequence} that matches the], [INSERT,,     * specified character.&lt;/p&gt;], [INSERT,,     *], [INSERT,,     * @param cs  the {@code CharSequence} to be processed, not null], [INSERT,,     * @param searchChar  the char to be searched for], [INSERT,,     * @param start  the start index, negative starts at the string start], [INSERT,,     * @return the index where the search char was found, -1 if not found], [INSERT,,     */], [INSERT,,    static int indexOf(final CharSequence cs, final int searchChar, int start) {], [INSERT,,        if (cs instanceof String) {], [INSERT,,            return ((String) cs).indexOf(searchChar, start);], [INSERT,,        }], [INSERT,,        final int sz = cs.length();], [INSERT,,        if (start &lt; 0) {], [INSERT,,            start = 0;], [INSERT,,        }], [INSERT,,        for (int i = start; i &lt; sz; i++) {], [INSERT,,            if (cs.charAt(i) == searchChar) {], [INSERT,,                return i;], [INSERT,,            }], [INSERT,,        }], [INSERT,,        return NOT_FOUND;], [INSERT,,    }], [INSERT,,], [INSERT,,    /**], [INSERT,,     * Used by the indexOf(CharSequence methods) as a green implementation of indexOf.], [INSERT,,     *], [INSERT,,     * @param cs the {@code CharSequence} to be processed], [INSERT,,     * @param searchChar the {@code CharSequence} to be searched for], [INSERT,,     * @param start the start index], [INSERT,,     * @return the index where the search sequence was found], [INSERT,,     */], [INSERT,,    static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {], [INSERT,,        return cs.toString().indexOf(searchChar.toString(), start);], [INSERT,,//        if (cs instanceof String && searchChar instanceof String) {], [INSERT,,//            // TODO: Do we assume searchChar is usually relatively small;], [INSERT,,//            //       If so then calling toString() on it is better than reverting to], [INSERT,,//            //       the green implementation in the else block], [INSERT,,//            return ((String) cs).indexOf((String) searchChar, start);], [INSERT,,//        } else {], [INSERT,,//            // TODO: Implement rather than convert to String], [INSERT,,//            return cs.toString().indexOf(searchChar.toString(), start);], [INSERT,,//        }], [INSERT,,    }], [INSERT,,], [INSERT,,    /**], [INSERT,,     * &lt;p&gt;Finds the last index in the {@code CharSequence} that matches the], [INSERT,,     * specified character.&lt;/p&gt;], [INSERT,,     *], [INSERT,,     * @param cs  the {@code CharSequence} to be processed], [INSERT,,     * @param searchChar  the char to be searched for], [INSERT,,     * @param start  the start index, negative returns -1, beyond length starts at end], [INSERT,,     * @return the index where the search char was found, -1 if not found], [INSERT,,     */], [INSERT,,    static int lastIndexOf(final CharSequence cs, final int searchChar, int start) {], [INSERT,,        if (cs instanceof String) {], [INSERT,,            return ((String) cs).lastIndexOf(searchChar, start);], [INSERT,,        }], [INSERT,,        final int sz = cs.length();], [INSERT,,        if (start &lt; 0) {], [INSERT,,            return NOT_FOUND;], [INSERT,,        }], [INSERT,,        if (start &gt;= sz) {], [INSERT,,            start = sz - 1;], [INSERT,,        }], [INSERT,,        for (int i = start; i &gt;= 0; --i) {], [INSERT,,            if (cs.charAt(i) == searchChar) {], [INSERT,,                return i;], [INSERT,,            }], [INSERT,,        }], [INSERT,,        return NOT_FOUND;], [INSERT,,    }], [INSERT,,], [INSERT,,    /**], [INSERT,,     * Used by the lastIndexOf(CharSequence methods) as a green implementation of lastIndexOf], [INSERT,,     *], [INSERT,,     * @param cs the {@code CharSequence} to be processed], [INSERT,,     * @param searchChar the {@code CharSequence} to be searched for], [INSERT,,     * @param start the start index], [INSERT,,     * @return the index where the search sequence was found], [INSERT,,     */], [INSERT,,    static int lastIndexOf(final CharSequence cs, final CharSequence searchChar, final int start) {], [INSERT,,        return cs.toString().lastIndexOf(searchChar.toString(), start);], [INSERT,,//        if (cs instanceof String && searchChar instanceof String) {], [INSERT,,//            // TODO: Do we assume searchChar is usually relatively small;], [INSERT,,//            //       If so then calling toString() on it is better than reverting to], [INSERT,,//            //       the green implementation in the else block], [INSERT,,//            return ((String) cs).lastIndexOf((String) searchChar, start);], [INSERT,,//        } else {], [INSERT,,//            // TODO: Implement rather than convert to String], [INSERT,,//            return cs.toString().lastIndexOf(searchChar.toString(), start);], [INSERT,,//        }], [INSERT,,    }], [INSERT,,], [INSERT,,    /**], [INSERT,,     * Green implementation of toCharArray.], [INSERT,,     *], [INSERT,,     * @param cs the {@code CharSequence} to be processed], [INSERT,,     * @return the resulting char array], [INSERT,,     */], [INSERT,,    static char[] toCharArray(final CharSequence cs) {], [INSERT,,        if (cs instanceof String) {], [INSERT,,            return ((String) cs).toCharArray();], [INSERT,,        }], [INSERT,,        final int sz = cs.length();], [INSERT,,        final char[] array = new char[cs.length()];], [INSERT,,        for (int i = 0; i &lt; sz; i++) {], [INSERT,,            array[i] = cs.charAt(i);], [INSERT,,        }], [INSERT,,        return array;], [INSERT,,    }], [INSERT,,], [INSERT,,    /**], [INSERT,,     * Green implementation of regionMatches.], [INSERT,,     *], [INSERT,,     * @param cs the {@code CharSequence} to be processed], [INSERT,,     * @param ignoreCase whether or not to be case insensitive], [INSERT,,     * @param thisStart the index to start on the {@code cs} CharSequence], [INSERT,,     * @param substring the {@code CharSequence} to be looked for], [INSERT,,     * @param start the index to start on the {@code substring} CharSequence], [INSERT,,     * @param length character length of the region], [INSERT,,     * @return whether the region matched], [INSERT,,     */], [INSERT,,    static boolean regionMatches(final CharSequence cs, final boolean ignoreCase, final int thisStart,], [INSERT,,            final CharSequence substring, final int start, final int length)    {], [INSERT,,        if (cs instanceof String && substring instanceof String) {], [INSERT,,            return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start, length);], [INSERT,,        }], [INSERT,,        int index1 = thisStart;], [INSERT,,        int index2 = start;], [INSERT,,        int tmpLen = length;], [INSERT,,], [INSERT,,        while (tmpLen-- &gt; 0) {], [INSERT,,            final char c1 = cs.charAt(index1++);], [INSERT,,            final char c2 = substring.charAt(index2++);], [INSERT,,], [INSERT,,            if (c1 == c2) {], [INSERT,,                continue;], [INSERT,,            }], [INSERT,,], [INSERT,,            if (!ignoreCase) {], [INSERT,,                return false;], [INSERT,,            }], [INSERT,,], [INSERT,,            // The same check as in String.regionMatches():], [INSERT,,            if (Character.toUpperCase(c1) != Character.toUpperCase(c2)], [INSERT,,                    && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {], [INSERT,,                return false;], [INSERT,,            }], [INSERT,,        }], [INSERT,,], [INSERT,,        return true;], [INSERT,,    }], [INSERT,,}]]\n");


            }


        }
    }

    @Test
    public void testOneInstancePerRevision() throws Exception {

        FinalResult result = TestUtills.runRepairability("ALL", "/repairability_test_files/mixed/");

        Map<IRevision, RevisionResult> revisionsMap = result.getAllResults();
        assertEquals(2, revisionsMap.keySet().size());

        for (Map.Entry<IRevision, RevisionResult> entry : revisionsMap.entrySet()) {
            RevisionResult rr = entry.getValue();
            PatternInstancesFromRevision instances = (PatternInstancesFromRevision) rr.getResultFromClass(RepairabilityAnalyzer.class);

            // for each revision
            Set<String> toolsSeen = new HashSet<>();
            for (PatternInstancesFromDiff v : instances.getInfoPerDiff()) {
                for (ChangePatternInstance patternInstance : v.getInstances()) {
                    String toolName = patternInstance.getPattern().getName().split(File.pathSeparator)[0];
                    assertFalse(toolsSeen.contains(toolName)); // to check if the same tool hasn't been seen in this particular revison
                    toolsSeen.add(toolName);
                }
            }
        }
    }
}
