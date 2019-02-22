package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;

import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.analyzer.patternspecification.EntityRelation;
import fr.inria.coming.changeminer.analyzer.patternspecification.ParentPatternEntity;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternRelations;
import fr.inria.coming.changeminer.entity.ActionType;
import fr.inria.coming.utils.MapList;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DetectorChangePatternInstanceEngine {

	Logger log = Logger.getLogger(this.getClass().getName());

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ChangePatternInstance> findPatternInstances(ChangePatternSpecification changePatternSpecification,
			Diff diffToAnalyze) {

		ResultMapping mapping = mappingActions(changePatternSpecification, diffToAnalyze);
		List<ChangePatternInstance> instances = calculateValidInstancesFromMapping(changePatternSpecification,
				mapping.getMappings());
		return instances;

	}

	public List<ChangePatternInstance> calculateValidInstancesFromMapping(
			ChangePatternSpecification changePatternSpecification, MapList<PatternAction, MatchingAction> matching) {

		List<ChangePatternInstance> instancesFinalSet = new ArrayList<>();

		// All Combinations
		List<ChangePatternInstance> instancesAllCombinations = allCombinations(changePatternSpecification, matching);

		// Discarding illegal relations
		// First, get relations between elements of the pattern
		PatternRelations relations = changePatternSpecification.calculateRelations();

		// For each instance
		for (ChangePatternInstance instance : instancesAllCombinations) {

			log.debug("Analyzing  instance: \n" + instance);
			if (checkPatternRelationsOnInstance(instance, relations)) {
				instancesFinalSet.add(instance);
			}

		}

		return instancesFinalSet;

	}

	public List<ChangePatternInstance> allCombinations(ChangePatternSpecification changePatternSpecification,
			MapList<PatternAction, MatchingAction> matching) {

		List<ChangePatternInstance> instancesAllCombinations = new ArrayList<>();

		for (PatternAction pa : matching.keySet()) {

			List<ChangePatternInstance> temp = new ArrayList<>();

			List<MatchingAction> actions = matching.get(pa);
			for (MatchingAction matchingAction : actions) {
				if (instancesAllCombinations.isEmpty()) {
					ChangePatternInstance ins = new ChangePatternInstance(changePatternSpecification);
					ins.addInstance(matchingAction.getPatternAction(), matchingAction.getOperation());
					ins.getMapping().put(pa, matchingAction);
					// ins.getMatching().addAll(matchingAction.matching);
					temp.add(ins);
				} else {
					for (ChangePatternInstance changePatternInstance : instancesAllCombinations) {
						ChangePatternInstance ins = new ChangePatternInstance(changePatternSpecification);
						ins.getActionOperation().putAll(changePatternInstance.getActionOperation());
						ins.getActions().addAll(changePatternInstance.getActions());
						ins.getMapping().putAll(changePatternInstance.getMapping());
						ins.addInstance(matchingAction.getPatternAction(), matchingAction.getOperation());
						ins.getMapping().put(pa, matchingAction);
						temp.add(ins);
					}
				}
			}
			instancesAllCombinations.clear();
			instancesAllCombinations.addAll(temp);
		}
		return instancesAllCombinations;

	}

	/**
	 * Returns true if the instance of a pattern respect the relation between the
	 * elements of the pattern.
	 * 
	 * @param instance
	 * @param relations
	 * @return
	 */
	public boolean checkPatternRelationsOnInstance(ChangePatternInstance instance, PatternRelations relations) {

		MapList<PatternAction, EntityRelation> entitiesByAction = relations.getPaEntity();

		// for each patter action, let's check if the elements that it points respect
		// the relation defined by the pattern
		for (PatternAction paction : instance.actionOperation.keySet()) {
			// if the pattern action has a relation
			if (entitiesByAction.containsKey(paction)) {
				// It has relation:
				List<EntityRelation> relationsOfPatternAction = entitiesByAction.get(paction);
				for (EntityRelation entityRelation : relationsOfPatternAction) {

					// Get the two actions related by the Relation (one of them is paction)
					PatternAction actionA = entityRelation.getAction1();
					PatternAction actionB = entityRelation.getAction2();

					//
					if (!instance.getMapping().containsKey(actionA) || !instance.getMapping().containsKey(actionB)) {
						return false;
					}

					// get the matching of each action
					MatchingEntity meA = instance.getMapping().get(actionA).getMatching().stream()
							.filter(e -> e.patternEntity == entityRelation.getEntity()).findFirst().get();

					MatchingEntity meB = instance.getMapping().get(actionB).getMatching().stream()
							.filter(e -> e.patternEntity == entityRelation.getEntity()).findFirst().get();

					// == comparing objects.
					// if the Object referenced by an action is the same than that one referenced by
					// the related action B, then continue, otherwise discard instance, the relation
					// is not respected in that instance
					CtElement affectedNodeA = meA.getAffectedNode();
					CtElement affectedNodeB = meB.getAffectedNode();
					if (affectedNodeA != affectedNodeB) {
						// Discard instance
						return false;
					}

				}

			}
		}
		return true;
	}

	/**
	 * Receives a pattern specification and a diff and creates a mapping between
	 * elements from the pattern and those affected by the diff
	 * 
	 * @param changePatternSpecification
	 * @param diffToAnalyze
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ResultMapping mappingActions(ChangePatternSpecification changePatternSpecification, Diff diffToAnalyze) {
		List<Operation> operationsFromDiff = diffToAnalyze.getAllOperations();

		MapList<PatternAction, MatchingAction> mapping = new MapList<>();
		List<PatternAction> notMapped = new ArrayList();

		// For each abstract change in the pattern
		for (PatternAction patternAction : changePatternSpecification.getAbstractChanges()) {
			boolean mapped = false;
			// For each operation in the diff
			for (Operation operation : operationsFromDiff) {

				Action action = operation.getAction();
				// First, match the type of the action
				if (matchActionTypes(action, getOperationType(patternAction))) {
					// when, match the elements affected by the action.
					List<MatchingEntity> matching = matchElements(operation, patternAction.getAffectedEntity());

					if (matching != null && !matching.isEmpty()) {
						mapped = true;
						MatchingAction maction = new MatchingAction(operation, patternAction, matching);
						mapping.add(patternAction, maction);
					}
				}
			}
			if (!mapped) {
				log.debug("Abstract change not mapped: " + patternAction);
				notMapped.add(patternAction);
			}
		}
		return new ResultMapping(mapping, notMapped);
	}

	/**
	 * Match the element affected by an operation from the diff and the elements in
	 * the pattern specification
	 * 
	 * @param affectedOperation
	 * @param affectedEntity
	 * @return
	 */
	private List<MatchingEntity> matchElements(Operation affectedOperation, PatternEntity affectedEntity) {

		List<MatchingEntity> matching = new ArrayList<>();

		int parentLevel = 1;
		PatternEntity parentEntity = affectedEntity;
		// Let's get the parent of the affected
		CtElement currentNodeFromAction = null;
		boolean matchnewvalue = false;

		// Search the node to select according to the type of operation and the pattern
		if (affectedOperation.getDstNode() != null && affectedEntity.getNewValue() != null) {
			currentNodeFromAction = affectedOperation.getDstNode();
			matchnewvalue = true;
		} else if (affectedOperation instanceof UpdateOperation && (affectedEntity.getOldValue() != null)) {
			currentNodeFromAction = affectedOperation.getSrcNode();
			matchnewvalue = false;
		} else {
			matchnewvalue = true;
			currentNodeFromAction = affectedOperation.getNode();
		}

		int i_levels = 1;
		// Scale the parent hierarchy and check types.
		while (currentNodeFromAction != null && i_levels <= parentLevel) {
			String typeOfNode = getNodeLabelFromCtElement(currentNodeFromAction);
			String valueOfNode = currentNodeFromAction.toString();

			String patternEntityValue = (matchnewvalue) ? parentEntity.getNewValue() : parentEntity.getOldValue();
			if ( // type of element
			("*".equals(parentEntity.getEntityType())
					|| (typeOfNode != null && typeOfNode.equals(parentEntity.getEntityType()))) &&
			// value of element
					"*".equals(patternEntityValue) || (valueOfNode != null && valueOfNode.equals(patternEntityValue))

			) {
				MatchingEntity match = new MatchingEntity(currentNodeFromAction, parentEntity);
				matching.add(match);

				ParentPatternEntity parentEntityFromPattern = parentEntity.getParentPatternEntity();
				if (parentEntityFromPattern == null) {
					return matching;
				}
				i_levels = 1;
				parentLevel = parentEntityFromPattern.getParentLevel();
				parentEntity = parentEntityFromPattern.getParent();

			} else {
				// Not match
				i_levels++;
			}
			currentNodeFromAction = currentNodeFromAction.getParent();
		}
		// Not all matched
		return null;

	}

	private boolean matchActionTypes(Action action, ActionType type) {

		return ActionType.ANY.equals(type) || (type.equals(ActionType.INS) && (action instanceof Insert))
				|| (type.equals(ActionType.DEL) && (action instanceof Delete))
				|| (type.equals(ActionType.MOV) && (action instanceof Move))
				|| (type.equals(ActionType.UPD) && (action instanceof Update));
	}

	public ActionType getOperationType(PatternAction patternAction) {
		return patternAction.getAction();
	}

	public String getTypeLabel(PatternAction patternAction) {
		return patternAction.getAffectedEntity().getEntityType();
	}

	/**
	 * The label of a CtElement is the simple name of the class without the CT
	 * prefix.
	 * 
	 * @param element
	 * @return
	 */
	public String getNodeLabelFromCtElement(CtElement element) {
		String typeFromCt = element.getClass().getSimpleName();
		if (typeFromCt.trim().isEmpty())
			return typeFromCt;
		return typeFromCt.substring(2, typeFromCt.length() - 4);
	}

}
