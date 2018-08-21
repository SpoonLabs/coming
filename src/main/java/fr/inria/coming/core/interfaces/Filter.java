package fr.inria.coming.core.interfaces;

import java.util.List;

public interface Filter {
	boolean acceptCommit(Commit c);
	boolean acceptCommitFragments(List<String> fragments);
	boolean acceptFragment(String fragments);
}
