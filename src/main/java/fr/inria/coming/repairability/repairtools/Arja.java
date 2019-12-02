package fr.inria.coming.repairability.repairtools;

import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.*;
import spoon.Launcher;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.*;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Arja tries to correct the code by inserting statements that are in the source
 * file either in a direct approach or a type matching approach.
 * <p>
 * Direct Approach: the extracted variable/Method with the same name and
 * compatible types exists in the variable/Method scope.
 * <p>
 * statement or the invocation exists in the source code with compatible types.
 * (Read more about this on
 * https://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=8485732)
 */

public class Arja extends AbstractRepairTool {
	private static final String ARJA_DEEP_PATTERN = "arja_deep_change";
	private static final String ARJA_SHALLOW_PATTERN = "arja_shallow_change";

	private static final String[] patternFileNames = { ARJA_DEEP_PATTERN + ".xml", ARJA_SHALLOW_PATTERN + ".xml" };

	@Override
	protected List<ChangePatternSpecification> readPatterns() {
		List<ChangePatternSpecification> patterns = new ArrayList<>();
		for (String fileName : patternFileNames) {
			patterns.add(PatternXMLParser.parseFile(getPathFromResources(fileName)));
		}
		return patterns;
	}

	@Override
	public boolean filter(ChangePatternInstance instance, IRevision revision, Diff diff) {
		String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];
		Operation op = instance.getActions().get(0);
		String previousVersionString = (String) revision.getChildren().get(0).getPreviousVersion();
		CtClass srcClass = Launcher.parseClass(previousVersionString);

		if (patternType.contains(ARJA_SHALLOW_PATTERN)) {

			if (op instanceof MoveOperation) {
				return false;
			} else if (op instanceof DeleteOperation) {
				return true;
			}else if(op instanceof InsertOperation) {
				return canBeReproducedFromSrc(srcClass, (CtStatement) op.getSrcNode());
			}

		} else if (patternType.contains(ARJA_DEEP_PATTERN)) {
		}

		return false;
	}

	private boolean canBeReproducedFromSrc(CtElement src, CtStatement target) {
		List<CtElement> targetElements = target.getElements(null);
		List<CtElement> srcElements = src.getElements(null);
		
		boolean templateFoundInSrc = false;
		
		for(int i = 0; i <= srcElements.size(); i++) {
			
			CtElement curSrcParElem = srcElements.get(i);
			if(!(curSrcParElem instanceof CtStatement)) {
				continue;
			}
			
			String curSrcAsStr = getStrOfElement(curSrcParElem);
			String srcAsStr = curSrcParElem.toString();
			
			boolean isCurSrcElemTemplateOfTarget = true;
			
			
			Set<CtElement> elementsInSubtree = new HashSet<CtElement>();
			
			for(int j = 0; j < targetElements.size(); j++) {
				CtElement curSrcElement = srcElements.get(i + j);
				CtElement curTargetElem = targetElements.get(j);
				
				if(!curTargetElem.getClass().equals(curSrcElement.getClass())) {
					isCurSrcElemTemplateOfTarget = false;
					break;
				}
			}
			
			if(isCurSrcElemTemplateOfTarget) {
				templateFoundInSrc = true;
				break;
			}
		}
		
		if(!templateFoundInSrc)
			return false;
		
		return false;
	}
	
	private String getStrOfElement(CtElement elem) {
		return null;
	}
}
