package fr.inria.coming.core.filter.diff.syntcomparison;


public class LineComparator extends FragmentableComparator {

  @Override
  public Fragmentable createFragmentable(String data)  {
		String dataParsed = data.replaceAll(" |\\t|\\{|\\}", "");

		return new SLineComparator(dataParsed);
  }

}
