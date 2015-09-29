/*
 * Copyright 2009 University of Zurich, Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.inria.sacha.coming.jdt.visitors;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;

import fr.inria.sacha.coming.entity.EntityType;



//import fr.labri.gumtree.gen.jdt.cd.EntityType;


/**
 * Helper class to assist distiller with Java AST information to create source
 * code entities and structure entities out of AST nodes in a given
 * {@link IFile}.
 * 
 * @author fluri
 */
public final class JavaASTHelper  {

	private static Map<Integer, EntityType> sConversionMap = new HashMap<Integer, EntityType>();


	public static EntityType convertNode(Object node) throws Exception {
		if (!(node instanceof ASTNode)) {
			throw new Exception("Node must be of type ASTNode.");
		}
		ASTNode astNode = (ASTNode) node;
		if (sConversionMap.isEmpty()) {
			for (Field field : EntityType.class.getFields()) {
				try {
					for (Field astField : ASTNode.class.getFields()) {
						if (field.getName().equals(astField.getName())) {
							int type = astField.getInt(ASTNode.class);
							sConversionMap.put(type, EntityType.valueOf(field.getName()));
						}
					}
				} catch (IllegalArgumentException e) {
					throw new Exception("Node type '" + astNode.getClass().getCanonicalName()
							+ "' not defined in EntityType.");
				} catch (IllegalAccessException e) {
					throw new Exception(e.getMessage());
				}
			}
		}
		return sConversionMap.get(astNode.getNodeType());
	}


	
}
