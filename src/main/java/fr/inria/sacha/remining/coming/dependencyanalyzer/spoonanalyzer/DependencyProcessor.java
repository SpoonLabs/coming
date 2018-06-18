package fr.inria.sacha.remining.coming.dependencyanalyzer.spoonanalyzer;

import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Class;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Class.ClassType;
import fr.inria.sacha.remining.coming.dependencyanalyzer.entity.Dependency;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

/**
 * Analyzes a JAVA class to list all (non-static) dependencies
 * @param <T>
 * @author Romain Philippon
 */
public class DependencyProcessor<T> extends AbstractProcessor<CtType<T>> {

	/**
	 * Is the logger instance which log all dependencies found by this processor
	 */
	private static final Logger logger = Logger.getLogger(DependencyProcessor.class);
	/**
	 * Contains the analysis result for a given class
	 */
	private Class analyzedClass;

	public DependencyProcessor() {
		super();
	}
	
	/**
	 * Launches a dependency class analysis on super class, implemented interfaces, method's parameters and statements and method return statement type
	 */
	@Override
	public void process(CtType<T> loadedClass) {
		CtTypeReference<?> currentType;
		final HashSet<Dependency> dependencies = new HashSet<Dependency>();
		this.analyzedClass = new Class(loadedClass.getQualifiedName(), ClassType.REGULAR, dependencies);

		for(CtTypeReference<?> type : loadedClass.getReferencedTypes()) {
			if (!this.isVoidType(type) && !this.isPrimitiveType(type)) {
				dependencies.add(new Dependency(type));
			}
		}
		
//		/* INHERITANCE AND IMPLEMENTATION ANALYSIS */
//		// super class
//		currentType = loadedClass.getSuperclass();
//		if(currentType != null) {
//			dependencies.add(new Dependency(currentType));
//		}
//
//		// interfaces
//		for(CtTypeReference<?> interfaceImplemented : loadedClass.getSuperInterfaces()){
//			dependencies.add(new Dependency(interfaceImplemented));
//		}
//
//		/* ATTRIBUTE ANALYSIS */
//		for(CtField<?> attribute : loadedClass.getFields()) {
//			for(CtTypeReference<?> type : attribute.getReferencedTypes()) {
//				if (!this.isVoidType(type) && !this.isPrimitiveType(type)) {
//					dependencies.add(new Dependency(type));
//				}
//			}
//		}
//
//		/* CONSTRUCTOR ANALYSIS */
//		for(CtConstructor<?> constructor : loadedClass.getConstructors()) {
//			for(CtStatement statement : constructor.getBody().getStatements()) {
//				for(CtTypeReference<?> type : statement.getReferencedTypes()) {
//					if (!this.isVoidType(type) && !this.isPrimitiveType(type)) {
//						dependencies.add(new Dependency(type));
//					}
//				}
//			}
//		}
//
//		/* METHOD ANALYSIS */
//		for(CtMethod<?> method: loadedClass.getAllMethods()) {
//			if (method.getBody()==null) continue;
//			// parameter type
//			for(CtParameter<?> param : method.getParameters()) {
//				currentType = param.getType();
//				
//				if(!this.isVoidType(currentType) && !this.isPrimitiveType(currentType)) {
//					dependencies.add(new Dependency(currentType));
//				}
//			}
//
//			// all statement type include in method body
//			for(CtStatement statement : method.getBody().getStatements()) {
//				for(CtTypeReference<?> type : statement.getReferencedTypes()) {
//					if(!this.isVoidType(type) && !this.isPrimitiveType(type)) {
//						dependencies.add(new Dependency(type));
//					}
//				}
//			}
//		}
//
//		/* LOG ANALYSIS */
//		logger.info(":::::::::: " + loadedClass.getQualifiedName());
//		for(Dependency dependency : dependencies) {
//			logger.info("\t"+ dependency.getQualifiedDependencyName());
//		}

		/* DELETE USELESS DEPENDENCY */
		dependencies.remove(new Dependency(getFactory().Class().OBJECT));
		dependencies.remove(new Dependency(loadedClass.getReference()));

	}

	/**
	 * Gets all dependencies found during the analysis
	 * 
	 * @return a class instance containing it's type and its dependencies
	 */
	public Class getAnalyzedClass() {
		return this.analyzedClass;
	}
	
	/**
	 * Tells if it is a primitive type : int, long, float, double, boolean, byte and char
	 * @param javaType is the tested type
	 * @return if the type is primitive or not
	 */
	public boolean isPrimitiveType(CtTypeReference<?> javaType) {
		boolean isInt = javaType.equals(getFactory().Class().INTEGER) || javaType.equals(getFactory().Class().INTEGER_PRIMITIVE) || javaType.equals(getFactory().Type().INTEGER) || javaType.equals(getFactory().Type().INTEGER_PRIMITIVE);
		boolean isLong = javaType.equals(getFactory().Class().LONG) || javaType.equals(getFactory().Class().LONG_PRIMITIVE) || javaType.equals(getFactory().Type().LONG) || javaType.equals(getFactory().Type().LONG_PRIMITIVE);
		boolean isFloat = javaType.equals(getFactory().Class().FLOAT) || javaType.equals(getFactory().Class().FLOAT_PRIMITIVE) || javaType.equals(getFactory().Type().FLOAT) || javaType.equals(getFactory().Type().FLOAT_PRIMITIVE);
		boolean isDouble = javaType.equals(getFactory().Class().DOUBLE) || javaType.equals(getFactory().Class().DOUBLE_PRIMITIVE) || javaType.equals(getFactory().Type().DOUBLE) || javaType.equals(getFactory().Type().DOUBLE_PRIMITIVE);
		boolean isBool = javaType.equals(getFactory().Class().BOOLEAN) || javaType.equals(getFactory().Class().BOOLEAN_PRIMITIVE) || javaType.equals(getFactory().Type().BOOLEAN) || javaType.equals(getFactory().Type().BOOLEAN_PRIMITIVE);
		boolean isByte = javaType.equals(getFactory().Class().BYTE) || javaType.equals(getFactory().Class().BYTE_PRIMITIVE) || javaType.equals(getFactory().Type().BYTE) || javaType.equals(getFactory().Type().BYTE_PRIMITIVE);
		boolean isCharacter = javaType.equals(getFactory().Class().CHARACTER) || javaType.equals(getFactory().Class().CHARACTER_PRIMITIVE) || javaType.equals(getFactory().Type().CHARACTER) || javaType.equals(getFactory().Type().CHARACTER_PRIMITIVE);
		
		return isInt || isLong || isFloat || isDouble || isBool || isByte || isCharacter;
	}
	
	/**
	 * Tells if it is a void type
	 * @param javaType is the tested type
	 * @return if the type is void or not
	 */
	public boolean isVoidType(CtTypeReference<?> javaType) {
		return javaType.equals(getFactory().Class().VOID_PRIMITIVE) || javaType.equals(getFactory().Class().nullType());
	}
}
