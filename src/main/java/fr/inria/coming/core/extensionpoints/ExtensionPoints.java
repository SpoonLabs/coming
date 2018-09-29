package fr.inria.coming.core.extensionpoints;

import fr.inria.astor.core.solutionsearch.AstorCoreEngine;

/**
 * Enum with all extension points
 * 
 * @author Matias Martinez
 *
 */
public enum ExtensionPoints {

	NAVIGATION_ENGINE("customengine", AstorCoreEngine.class), //
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
