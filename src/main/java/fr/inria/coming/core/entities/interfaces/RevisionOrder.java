package fr.inria.coming.core.entities.interfaces;

import java.util.Iterator;

import fr.inria.coming.core.entities.RevisionDataset;
import fr.inria.coming.core.extensionpoints.ComingExtensionPoint;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface RevisionOrder<T> extends ComingExtensionPoint {

	public Iterator<T> orderOfNavigation(RevisionDataset<T> data);

}
