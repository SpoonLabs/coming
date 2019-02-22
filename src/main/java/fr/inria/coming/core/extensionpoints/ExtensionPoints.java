package fr.inria.coming.core.extensionpoints;

import fr.inria.coming.core.engine.RevisionNavigationExperiment;

/**
 * Enum with all extension points
 * 
 * @author Matias Martinez
 *
 */
public enum ExtensionPoints {

	NAVIGATION_ENGINE("customengine", RevisionNavigationExperiment.class), //
	;
	public String identifier;
	public Class<?> _class;

	ExtensionPoints(String id, Class<?> _class) {
		this.identifier = id;
		this._class = _class;
	}

	public String argument() {
		return "-" + this.identifier;
	}
}
