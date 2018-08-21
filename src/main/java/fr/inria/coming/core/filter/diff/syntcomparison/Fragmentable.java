package fr.inria.coming.core.filter.diff.syntcomparison;

import org.eclipse.compare.rangedifferencer.IRangeComparator;

public interface Fragmentable extends IRangeComparator {
  String getFragment(int i);
}
