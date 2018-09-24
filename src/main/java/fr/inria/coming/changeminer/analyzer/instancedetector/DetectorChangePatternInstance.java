package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;

import fr.inria.astor.util.MapList;
import fr.inria.coming.changeminer.analyzer.patternspecification.ChangePatternSpecification;
import fr.inria.coming.changeminer.analyzer.patternspecification.EntityRelation;
import fr.inria.coming.changeminer.analyzer.patternspecification.ParentPatternEntity;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.analyzer.patternspecification.Relations;
import fr.inria.coming.changeminer.entity.ActionType;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DetectorChangePatternInstance {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ChangePatternInstance> findPatternInstances(ChangePatternSpecification changePatternSpecification,
			Diff diffToAnalyze) {

		ResultMapping mapping = s1mappingActions(changePatternSpecification, diffToAnalyze);
		// Now, Parent analysis:

		return null;

	}

	public List<ChangePatternInstance> s2Linking(ChangePatternSpecification changePatternSpecification,
			MapList<PatternAction, MatchingAction> matching) {
		List<ChangePatternInstance> instancesFinalSet = new ArrayList<>();

		// All Combinations
		List<ChangePatternInstance> instancesAllCombinations = allCombinations(changePatternSpecification, matching);

		// Discarting illegal relations
		Relations relations = changePatternSpecification.calculateRelations();

		List<EntityRelation> entityRelations = relations.getRelations();
		MapList<PatternAction, EntityRelation> paEntity = relations.getPaEntity();

		// For each instace
		for (ChangePatternInstance instance : instancesAllCombinations) {

			System.out.println("Analyzing \n" + instance);
			if (checkRelationsOnInstance(entityRelations, paEntity, instance)) {
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

	public boolean checkRelationsOnInstance(List<EntityRelation> entityRelations,
			MapList<PatternAction, EntityRelation> paEntity, ChangePatternInstance instance
	// MapList<PatternAction, MatchingAction> matching
	) {
		// for each paction, let's check if respect the entity relation
		for (PatternAction paction : instance.actionOperation.keySet()) {
			// if the pattern action has a relation
			if (paEntity.containsKey(paction)) {
				// It has relation:
				// for (EntityRelation entityRelation : entityRelations) {
				List<EntityRelation> relationsOfPatternAction = paEntity.get(paction);
				for (EntityRelation entityRelation : relationsOfPatternAction) {

					PatternAction actionA = entityRelation.getAction1();
					PatternAction actionB = entityRelation.getAction2();

					MatchingEntity meA = instance.getMapping().get(actionA).getMatching().stream()
							.filter(e -> e.patternEntity == entityRelation.getEntity()).findFirst().get();

					MatchingEntity meB = instance.getMapping().get(actionB).getMatching().stream()
							.filter(e -> e.patternEntity == entityRelation.getEntity()).findFirst().get();

					// == comparing objects.
					// if the Object referenced by an action is the same than that one referenced by
					// the related action B, then continue, otherwise discard instance
					CtElement affectedNodeA = meA.getAffectedNode();
					CtElement affectedNodeB = meB.getAffectedNode();
					if (affectedNodeA == affectedNodeB) {
						// if (instance.getActionOperation().get(actionA) ==
						// instance.getActionOperation().get(actionB)) {
						// it's ok

					} else {
						// Discard instance
						return false;
					}

				}

			}
		}
		return true;
	}

	/**
	 * 
	 * @param changePatternSpecification
	 * @param diffToAnalyze
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ResultMapping s1mappingActions(ChangePatternSpecification changePatternSpecification, Diff diffToAnalyze) {
		List<Operation> operations = diffToAnalyze.getAllOperations();

		MapList<PatternAction, MatchingAction> mapping = new MapList<>();
		List<PatternAction> notMapped = new ArrayList();

		// For each abstract change in the pattern
		for (PatternAction patternAction : changePatternSpecification.getAbstractChanges()) {
			boolean mapped = false;
			// For each operation
			for (Operation operation : operations) {

				Action action = operation.getAction();

				if (matchActionTypes(action, getOperationType(patternAction))) {

					List<MatchingEntity> matching = matchElements(operation, patternAction.getAffectedEntity());
					if (matching != null && !matching.isEmpty()) {
						mapped = true;
						MatchingAction maction = new MatchingAction(operation, patternAction, matching);
						mapping.add(patternAction, maction);
					}
				}
			}
			if (!mapped) {
				System.out.println("Abstract change not mapped: " + patternAction);
				notMapped.add(patternAction);
			}
		}
		return new ResultMapping(mapping, notMapped);
	}

	private List<MatchingEntity> matchElements(Operation affectedOperation, PatternEntity affectedEntity) {

		List<MatchingEntity> matching = new ArrayList<>();

		int parentLevel = 1;
		PatternEntity parentEntity = affectedEntity;
		// Let's get the parent of the affected
		CtElement currentNodeFromAction = null;
		boolean matchnewvalue = false;

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
		// Scale the hierarchie and check types.
		while (currentNodeFromAction != null && i_levels <= parentLevel) {
			String typeOfNode = getNodeLabelFromCtElement(currentNodeFromAction);
			String valueOfNode = currentNodeFromAction.toString();

			String patternEntityValue = (matchnewvalue) ? parentEntity.getNewValue() : parentEntity.getOldValue();
			if ( // type
			("*".equals(parentEntity.getEntityType())
					|| (typeOfNode != null && typeOfNode.equals(parentEntity.getEntityType()))) &&
			// value
			// matchValues(operation, parentEntity.getValue()) ;
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
				// Not matching

				i_levels++;
			}
			currentNodeFromAction = currentNodeFromAction.getParent();
		}
		// Not all matched
		return null;

	}

	public static boolean matchActionTypes(Action action, ActionType type) {

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
