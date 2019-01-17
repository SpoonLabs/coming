package fr.inria.coming.core.entities;

import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * @author Matias Martinez
 *
 */
public class RevisionDataset<Data> {

	Collection<Data> data = null;

	public RevisionDataset() {
		super();
	}

	public RevisionDataset(Collection<Data> data) {
		super();
		this.data = data;
	}

	public Collection<Data> getAllData() {
		return data;
	}

	public void setData(Collection<Data> data) {
		this.data = data;
	}

	public Iterator<Data> getIterator() {
		if (this.data == null)
			return null;
		return this.data.iterator();

	}

	public int size() {

		return data != null ? data.size() : 0;
	}
}
