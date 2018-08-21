package fr.inria.coming.core.filter.diff.syntcomparison;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.internal.LCSSettings;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;

/** compares two files with different strategies */
public class FragmentableComparator {

  public RangeDifference[] compare(String file1, String file2) {
    Fragmentable sbefore = createFragmentable(file1);
    Fragmentable safter = createFragmentable(file2);
    return compare(sbefore, safter);
  }

  public RangeDifference[] compare(Fragmentable sbefore, Fragmentable safter) {
    RangeDifference[] results = RangeDifferencer.findRanges(new LCSSettings(), sbefore, safter, safter);
    return results;
  }
  
  public List<String> getAfterDifferences(Fragmentable sbefore, Fragmentable safter)  {
	    RangeDifference[] results = RangeDifferencer.findRanges(new LCSSettings(), sbefore, safter); 
	    List<String> l = new ArrayList<String>();
//	    System.err.println("Diffs: " + results.length);
	    

	    for (RangeDifference diff : results) {
	      if (diff.kind() != RangeDifference.NOCHANGE) {
//	        System.err.println("####change " + diff.toString());

//	        for (int i = diff.leftStart(); i < diff.leftEnd(); i++) {
//	          System.err.println("before:" + sbefore.getFragment(i));
//	        }
	        for (int i = diff.rightStart(); i < diff.rightEnd(); i++) {
	          l.add(safter.getFragment(i));
	        }

	      }
	    }
	    return l;

	  }

  // factory method
  public Fragmentable createFragmentable(String data){
    throw new RuntimeException();    
  }

}
