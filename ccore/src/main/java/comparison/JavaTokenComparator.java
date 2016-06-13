package comparison;

public class JavaTokenComparator extends FragmentableComparator  {

  @Override
  public Fragmentable createFragmentable(String data) {
	  return new SJavaTokenComparator(data);
  }

}
