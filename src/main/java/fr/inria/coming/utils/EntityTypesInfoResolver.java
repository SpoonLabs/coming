package fr.inria.coming.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.reflections.Reflections;

import spoon.reflect.declaration.CtElement;

/**
 * Created by khesoem on 10/4/2019.
 */
// FIXME: getPathToRootNode & getOperatoinStats & getLabel... functions should be moved to a separate class
public class EntityTypesInfoResolver {
	private static EntityTypesInfoResolver _instance = null;
	private static final String CLASSES_HIERARCHY_FILE_NAME = "/gumtree-inheritance-relations.txt";

	private Map<String, Set<String>> childrenToParentsRelationsBetweenEntityTypes;

	public static EntityTypesInfoResolver getInstance() {
		if (_instance == null)
			_instance = new EntityTypesInfoResolver();
		return _instance;
	}

	public EntityTypesInfoResolver() {
		loadChildrenToParentsRelationsBetweenEntityTypes();
	}

	private void loadChildrenToParentsRelationsBetweenEntityTypes() {
		childrenToParentsRelationsBetweenEntityTypes = new HashMap<>();
		try {

			Scanner sc = new Scanner(getClass().getResourceAsStream(CLASSES_HIERARCHY_FILE_NAME));

			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] parts = line.split(":");

				String child = parts[0];
				String[] parents = parts[1].split(" ");
				childrenToParentsRelationsBetweenEntityTypes.put(child, new HashSet<>(Arrays.asList(parents)));
			}

			sc.close();
		} catch (Exception e) {
			throw new RuntimeException("Gumtree inheritance information file not found");
		}
	}

	// checks whether parent is an ancestor of or equal to child.
	public boolean isAChildOf(String childEntityTypeName, String parentEntityTypeName) {
		if (!childrenToParentsRelationsBetweenEntityTypes.containsKey(childEntityTypeName))
			return false;
		return childrenToParentsRelationsBetweenEntityTypes.get(childEntityTypeName).contains(parentEntityTypeName);
	}

	private static void extractAndSaveCtElementsHierarchyModel(String outputPath) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File(outputPath));

		Map<String, Set> childrenToParents = new HashMap<>();

		Reflections reflections = new Reflections("spoon");
		Set<Class<? extends CtElement>> allClasses = reflections.getSubTypesOf(CtElement.class);
		allClasses.add(CtElement.class);

		// initializing childrenToParents
		Set<Class> toBeIgnored = new HashSet<>();
		for (Class clazz : allClasses) {
			if (!clazz.getSimpleName().startsWith("Ct")) {
				toBeIgnored.add(clazz);
				continue;
			}
			childrenToParents.put(clazz.getSimpleName().substring(2), new HashSet<>());
		}
		allClasses.removeAll(toBeIgnored);

		for (Class clazz : allClasses) {
			String currentClassName = clazz.getSimpleName().substring(2);
			Set<Class<? extends CtElement>> childrenOfCurrentClass = reflections.getSubTypesOf(clazz);
			for (Class childOfCurrentClass : childrenOfCurrentClass) {
				if (!childOfCurrentClass.getSimpleName().startsWith("Ct"))
					continue;
				String currentChildName = childOfCurrentClass.getSimpleName().substring(2);
				childrenToParents.get(currentChildName).add(currentClassName);
			}
			childrenToParents.get(currentClassName).add(currentClassName); // each class is considered as an ancestor of
																			// itself
		}

		for (Map.Entry<String, Set> childToParents : childrenToParents.entrySet()) {
			String className = childToParents.getKey();
			pw.print(className + ":");
			Set<String> parents = childToParents.getValue();
			for (String parent : parents) {
				pw.print(" " + parent);
			}
			pw.print("\n");
			pw.flush();
		}

		pw.close();
	}

	/**
	 * The label of a CtElement is the simple name of the class without the CT
	 * prefix.
	 *
	 * @param element
	 * @return
	 */
	public static String getNodeLabelFromCtElement(CtElement element) {
		String typeFromCt = element.getClass().getSimpleName();
		if (typeFromCt.trim().isEmpty())
			return typeFromCt;
		return typeFromCt.substring(2, typeFromCt.endsWith("Impl") ? typeFromCt.length() - 4 : typeFromCt.length());
	}
}
