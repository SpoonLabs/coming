package fr.inria.coming.core.entities;

/**
 * 
 * @author Matias Martinez
 *
 */
public class HunkPair {
	String left = "";
	String right = "";

	public HunkPair(String left, String right) {
		super();
		this.left = left;
		this.right = right;
	}

	public String getRight() {
		return right;
	}

	public void setRight(String right) {
		this.right = right;
	}

	@Override
	public String toString() {
		return "left: " + "" + left + "\nright: " + right;
	}

	public String getLeft() {
		return left;
	}

	public void setLeft(String left) {
		this.left = left;
	}

}
