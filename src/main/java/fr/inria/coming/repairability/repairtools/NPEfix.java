package fr.inria.coming.repairability.repairtools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.matchers.MappingStore;

import com.github.gumtreediff.tree.Tree;
import fr.inria.coming.changeminer.analyzer.instancedetector.ChangePatternInstance;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.changeminer.util.PatternXMLParser;
import fr.inria.coming.repairability.models.ASTData;
import fr.inria.coming.utils.ASTInfoResolver;
import fr.inria.coming.utils.EntityTypesInfoResolver;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * NPEfix fixes the program by :
 * <p>
 * 1.local injection of an existing compatible object 2.global injection of an
 * existing compatible object 3.local injection of a new object 4.global
 * injection of a new object
 * <p>
 * 5.skip statement 6.return a null to caller 7.return a new object to caller
 * 8.return an existing compatible object to caller 9.return to caller (void
 * method)
 * <p>
 * I added two pattern files which checks for a variable being inserted or
 * updated (1-4) and another one to check for insertion of return. (5-9)
 */
public class NPEfix extends AbstractRepairTool {
	private static final String NPEFIX_SKIP_PATTERN = "npefix_skip";
	private static final String NPEFIX_S4 = "npefix_s4";
	private static final String NPEFIX_GLOBAL_REPLACE = "npefix_global_replace";
	private static final String NPEFIX_LOCAL_REPLACE = "npefix_local_replace";

	private static final String[] patternFileNames = { "npefix_skip.xml", "npefix_s4.xml", "npefix_global_replace.xml",
			"npefix_local_replace.xml" };

	/**
	 * Encodes the search space of JMutRepair
	 *
	 * @return a List of ChangePatternSpecifications that are supposed to be mined
	 *         by PatternInstanceAnalyzer
	 */
	@Override
	protected List<ChangePatternSpecification> readPatterns() {
		List<ChangePatternSpecification> patterns = new ArrayList<>();
		for (String fileName : patternFileNames) {
			patterns.add(PatternXMLParser.parseFile(getPathFromResources(fileName)));
		}
		return patterns;
	}

	/**
	 * Certain patterns/characteristics of search-space of a repair tool can't be
	 * represented by ChangePatternSpecification This filter is supposed to
	 * delete/remove such instances from the results given by
	 * PatternInstanceAnalyser.
	 *
	 * @param instance
	 * @param diff
	 * @return
	 */
	@Override
	public boolean filter(ChangePatternInstance instance, IRevision revision, Diff diff) {
		String patternType = instance.getPattern().getName().split(File.pathSeparator)[1];

		if (patternType.startsWith(NPEFIX_SKIP_PATTERN)) {
			Operation insOp = instance.getActions().get(0);

			if (!(insOp instanceof InsertOperation)) {
				insOp = instance.getActions().get(1);
			}

			CtIf ifNode = (CtIf) insOp.getSrcNode();
			
			if(!(ifNode.getCondition() instanceof CtBinaryOperator))
				return false;
			
			CtBinaryOperator condition = (CtBinaryOperator) ifNode.getCondition();

			if (!condition.getKind().equals(BinaryOperatorKind.NE)) {
				return false;
			}

			if (ifNode.getElseStatement() != null) {
				return false;
			}
			
			if(ifNode.getThenStatement() instanceof CtBlock 
					&& ((CtBlock)ifNode.getThenStatement()).getStatements().size() > 1) {
				return false;
			}
		} else if (patternType.startsWith(NPEFIX_S4)) {
			Operation retOp = instance.getActions().get(0), ifOp = instance.getActions().get(1);

			if (!(retOp.getSrcNode() instanceof CtReturn)) {
				ifOp = instance.getActions().get(0);
				retOp = instance.getActions().get(1);
			}

			CtReturn retNode = (CtReturn) retOp.getSrcNode();
			CtIf ifNode = (CtIf) ifOp.getSrcNode();
			
			if(!(ifNode.getCondition() instanceof CtBinaryOperator))
				return false;
			
			CtBinaryOperator condition = (CtBinaryOperator) ifNode.getCondition();

			if (!condition.getKind().equals(BinaryOperatorKind.EQ)) {
				return false;
			}

			if (ifNode.getElseStatement() != null) {
				return false;
			}
			
			if(ifNode.getThenStatement() instanceof CtBlock 
					&& ((CtBlock)ifNode.getThenStatement()).getStatements().size() > 1) {
				return false;
			}
			
			CtElement srcNode = getWrapperSrcNode(diff, ifNode);
			if(srcNode == null)
				return false;

			String retStr = retNode.toString();
			if (retStr.contains("return null") || retStr.contains("return new") || retStr.length() <= "return ".length() // return
																															// void
					|| new ASTData(ASTInfoResolver.getRootNode(srcNode))
							.canNPEfixGenerateExpression(retNode.getReturnedExpression())) {
				return true;
			}

			return false;
		} else if (patternType.startsWith(NPEFIX_GLOBAL_REPLACE)) {
			Operation ifOp = instance.getActions().get(0), assignOp = instance.getActions().get(1);

			if (!(assignOp instanceof CtAssignment)) {
				assignOp = instance.getActions().get(1);
				ifOp = instance.getActions().get(0);
			}

			CtAssignment assignNode = (CtAssignment) assignOp.getSrcNode();
			CtExpression assigned = assignNode.getAssigned();
			CtIf ifNode = (CtIf) ifOp.getSrcNode();
			
			if(!(ifNode.getCondition() instanceof CtBinaryOperator))
				return false;
			
			CtBinaryOperator condition = (CtBinaryOperator) ifNode.getCondition();

			if (!condition.getKind().equals(BinaryOperatorKind.EQ)) {
				return false;
			}

			if (!ASTInfoResolver.getCleanedName(condition.getLeftHandOperand()).contentEquals(ASTInfoResolver.getCleanedName(assigned))) {
				return false;
			}

			if (ifNode.getElseStatement() != null) {
				return false;
			}
			
			if(ifNode.getThenStatement() instanceof CtBlock 
					&& ((CtBlock)ifNode.getThenStatement()).getStatements().size() > 1) {
				return false;
			}

			String assignStr = assignNode.toString();

			if (assignStr.contains(" = null ") || assignStr.contains(" = new ")) {
				return true;
			}
			
			CtElement srcNode = getWrapperSrcNode(diff, ifNode);
			if(srcNode == null)
				return false;

			return new ASTData(ASTInfoResolver.getRootNode(srcNode))
					.canNPEfixGenerateExpression(assignNode.getAssignment());
		} else if (patternType.startsWith(NPEFIX_LOCAL_REPLACE)) {
			Operation ifOp = null, movOp = null, thenOp = null;

			for (Operation op : instance.getActions()) {
				if (op instanceof MoveOperation) {
					movOp = op;
				} else if (op instanceof InsertOperation) {
					if (op.getSrcNode() instanceof CtIf) {
						ifOp = op;
					} else {
						thenOp = op;
					}
				}
			}

			CtStatement movedNode = (CtStatement) movOp.getDstNode();

			CtIf ifNode = (CtIf) ifOp.getSrcNode();
			
			if(!(ifNode.getCondition() instanceof CtBinaryOperator))
				return false;
			
			CtBinaryOperator condition = (CtBinaryOperator) ifNode.getCondition();

			if (!condition.getKind().equals(BinaryOperatorKind.EQ)) {
				return false;
			}

			if (ifNode.getElseStatement() == null
					|| (ifNode.getElseStatement() != movedNode && ifNode.getElseStatement() != movedNode.getParent())) {
				return false;
			}
			
			if(ifNode.getThenStatement() instanceof CtBlock 
					&& ((CtBlock)ifNode.getThenStatement()).getStatements().size() > 1) {
				return false;
			}
			
			if(ifNode.getElseStatement() instanceof CtBlock 
					&& ((CtBlock)ifNode.getElseStatement()).getStatements().size() > 1) {
				return false;
			}

			List<CtElement> thenElems = thenOp.getSrcNode().getElements(null);

			CtStatement thenStatement = null;
			for (CtElement elem : thenElems) {
				if (elem instanceof CtStatement && !(elem instanceof CtBlock)) {
					thenStatement = (CtStatement) elem;
					break;
				}
			}

			if (thenStatement == null)
				return false;

			CtStatement movedStatement = null;
			if (!(movedNode instanceof CtBlock)) {
				movedStatement = movedNode;
			} else {
				List<CtElement> elseElems = movedNode.getElements(null);

				for (CtElement elem : elseElems) {
					if (elem instanceof CtStatement && !(elem instanceof CtBlock)) {
						movedStatement = (CtStatement) elem;
						break;
					}
				}

				if (movedStatement == null)
					return false;
			}

			String moveStatementStr = movedStatement.toString();
			String thenStatementStr = thenStatement.toString();

			int changedStartInd = -1, changedEndInd = -1, thenLen = thenStatementStr.length(),
					moveLen = moveStatementStr.length();
			for (int i = 0; i < thenLen && i < moveLen; i++) {
				if (thenStatementStr.charAt(i) != moveStatementStr.charAt(i)) {
					changedStartInd = i;
					break;
				}
			}
			for (int i = 0; i < thenLen && i < moveLen; i++) {
				if (thenStatementStr.charAt(thenLen - 1 - i) != moveStatementStr.charAt(moveLen - 1 - i)) {
					changedEndInd = i;
					break;
				}
			}

			for (; changedStartInd > 0
					&& isVariableNameChar(thenStatementStr.charAt(changedStartInd - 1)); changedStartInd--)
				;
			for (; changedEndInd > 0
					&& isVariableNameChar(thenStatementStr.charAt(thenLen - changedEndInd)); changedEndInd--)
				;

			if (changedStartInd < 0 || changedEndInd < 0)
				return false;

			String removedStr = moveStatementStr.substring(changedStartInd, moveLen - changedEndInd),
					addedStr = thenStatementStr.substring(changedStartInd, thenLen - changedEndInd);

			removedStr = ASTInfoResolver.getCleanedName(removedStr);
			addedStr = ASTInfoResolver.getCleanedName(addedStr);
			
			String cleanedLeftOperand = ASTInfoResolver.getCleanedName(condition.getLeftHandOperand());

			if (!cleanedLeftOperand.contains(removedStr))
				return false;

			if (addedStr.startsWith("null") || addedStr.startsWith("new "))
				return true;
			
			
			CtElement srcNode = getWrapperSrcNode(diff, ifNode);
			if(srcNode == null)
				return false;
			CtElement rootNode = ASTInfoResolver.getRootNode(srcNode);

			return (new ASTData(rootNode).canNPEfixGenerateExpression(addedStr)) 
					|| (containsNonvariableChar(addedStr) && rootNode.toString().contains(addedStr));
		}

		return true;
	}

	private CtElement getWrapperSrcNode(Diff diff, CtElement dstNode) {
		Tree ifTree = (Tree) dstNode.getParent().getParent().getMetadata("gtnode");
		MappingStore mapping = diff.getMappingsComp();
		if (!mapping.isDstMapped(ifTree))
			return null;
		CtElement srcNode = (CtElement) mapping.getSrcForDst(ifTree).getMetadata("spoon_object");
		return srcNode;
	}
	
	private boolean containsNonvariableChar(String str) {
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(!isVariableNameChar(c))
				return true;
		}
		return false;
	}

	private boolean isVariableNameChar(char c) {
		return (c <= 'z' && c >= 'a') || (c <= '9' && c >= '0') || c == '_' || (c <= 'Z' && c >= 'A');
	}

	protected boolean coveredByInstanceNodes(Set<CtElement> instanceCoveredNodes, CtElement node) {
		List<CtElement> pathToDiffRoot = ASTInfoResolver.getPathToRootNode(node);
		for (CtElement element : pathToDiffRoot) {
			for (CtElement instanceNode : instanceCoveredNodes) {
				if (element == instanceNode)
					return true;
				if(element == instanceNode.getParent() && element instanceof CtBlock) // see: patch1-Math-1115
					return true;
			}
		}
		return false;
	}

}
