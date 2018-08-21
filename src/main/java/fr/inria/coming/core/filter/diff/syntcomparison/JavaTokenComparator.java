package fr.inria.coming.core.filter.diff.syntcomparison;

public class JavaTokenComparator extends FragmentableComparator  {

  @Override
  public Fragmentable createFragmentable(String data) {
	  return new SJavaTokenComparator(data);
  }

}
