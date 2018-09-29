package fr.inria.coming.core.extensionpoints.navigation;

import java.util.Collection;
import java.util.Iterator;

import fr.inria.coming.core.entities.interfaces.RevisionOrder;

/**
 * Same order than the list received as parameter.s
 * 
 * @author Matias Martinez
 *
 * @param <T>
 */
public class InOrderRevisionNavigation<T> implements RevisionOrder<T> {

	@Override
	public Iterator<T> orderOfNavigation(Collection<T> elements) {

		return elements.iterator();
	}

}
