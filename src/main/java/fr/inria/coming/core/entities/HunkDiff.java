package fr.inria.coming.core.entities;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.RangeDifference;

public class HunkDiff extends ArrayList<RangeDifference> {

	List<HunkPair> hunkpairs = new ArrayList<>();

	public List<HunkPair> getHunkpairs() {
		return hunkpairs;
	}

	public void setHunkpairs(List<HunkPair> hunkpairs) {
		this.hunkpairs = hunkpairs;
	}

	@Override
	public String toString() {
		return "HunkDiff [hunkpairs=" + hunkpairs + "]";
	}

}
