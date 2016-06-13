package comparison;

import org.eclipse.compare.rangedifferencer.IRangeComparator;

public interface Fragmentable extends IRangeComparator {
  String getFragment(int i);
}
