package fr.inria.coming.core.filter.diff.syntcomparison;


//import org.junit.Test;

// should be run as "JUnit Plugin test" in headless more (Run an application: no application headless mode)
public class Main {	
	
	
//	@Test
//	public void main() throws Exception {
//		FragmentableComparator comp = new LineComparator();
//		comp.compare("AlterTableAlterColumn_r4076_src.java",
//				"AlterTableAlterColumn_r4129_dst.java");
//		FragmentableComparator comp2 = new JavaTokenComparator();
//		comp2.compare("AlterTableAlterColumn_r4076_src.java",
//				"AlterTableAlterColumn_r4129_dst.java");
//	}
	
	public static void main() throws Exception {
	FragmentableComparator comp = new LineComparator();
	comp.compare("AlterTableAlterColumn_r4076_src.java",
			"AlterTableAlterColumn_r4129_dst.java");
	FragmentableComparator comp2 = new JavaTokenComparator();
	comp2.compare("AlterTableAlterColumn_r4076_src.java",
			"AlterTableAlterColumn_r4129_dst.java");
}

//	public static void main(String[] argv) {
//		
//		FragmentableComparator comp = new LineComparator();
//		try {
////			comp.compare("AlterTableAlterColumn_r4076_src.java",
////					"AlterTableAlterColumn_r4129_dst.java");
//			
////			Repository repo = new RepositoryGit("/home/budd/INRIA/SDL/Iconoclaste-Underplate/.git", comp);
//			Repository repo = new RepositoryGit("/home/budd/INRIA/SDL/software-dna/RepoTest/.git", comp);
//			
//			int n = 0;
//			for (Commit c : repo.history()) {
//				System.out.println(c.getName());
//				Fragmentable f = c.getNewFragments();
//				
//				if (f.alreadyExists(repo.at(c.date()), repo.getComparator()))
//					n++;
//			}
//			System.out.println(n + " fragments ajoutés existaient déjà");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
