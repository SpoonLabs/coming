package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.inria.coming.utils.EntityTypesInfoResolver;
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
import fr.inria.coming.main.ComingProperties;
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

        log.debug("Diff size " + diffToAnalyze.getAllOperations().size() + ": "
                + diffToAnalyze.getAllOperations().stream()
                .map(e -> e.getClass().getSimpleName() + " " + e.getSrcNode().getClass().getSimpleName())
                .collect(Collectors.joining(" - ")));

        log.debug("#Mapped  " + mapping.mappings.size() + " #NotMapped " + mapping.notMapped.size());

        if (!mapping.getNotMapped().isEmpty()) {
            log.debug("There are pattern actions not mapped: " + mapping.getNotMapped());
            return Collections.EMPTY_LIST;
        }
        List<ChangePatternInstance> instances = calculateValidInstancesFromMapping(changePatternSpecification,
                mapping.getMappings());

        return instances;

    }

    public List<ChangePatternInstance> calculateValidInstancesFromMapping(
            ChangePatternSpecification changePatternSpecification, MapList<PatternAction, MatchingAction> matching) {

        List<ChangePatternInstance> instancesFinalSet = new ArrayList<>();

        // All Combinations
        List<ChangePatternInstance> instancesAllCombinations = (ComingProperties.getPropertyBoolean("singleinstance"))
                ? singleInstance(changePatternSpecification, matching)
                : allCombinations(changePatternSpecification, matching);

        instancesAllCombinations = instancesAllCombinations.stream().filter(e -> validate(e))
                .collect(Collectors.toList());

        // Discarding illegal relations
        // First, get relations between elements of the pattern
        PatternRelations relations = changePatternSpecification.calculateRelations();

        // For each instance
        for (ChangePatternInstance instance : instancesAllCombinations) {

            if (!checkUnchangedActionAlsoUsedByOther(instance)) {
                if (checkInvalidInstanceExistance(instance, relations)) {
                    instancesFinalSet.clear();
                    return instancesFinalSet;
                }

                continue;
            }
            
            log.debug("Analyzing  instance: \n" + instance);
            if (checkPatternRelationsOnInstance(instance, relations)) {
                instancesFinalSet.add(instance);
            }

        }

        return instancesFinalSet;

    }

    /**
     * We return a single instance, with the mapping to the first element. It can we
     * use when it's necessary to assert the presence of a pattern instance, but we
     * dont want to analyze them.
     *
     * @param changePatternSpecification
     * @param matching
     * @return
     */
    private List<ChangePatternInstance> singleInstance(ChangePatternSpecification changePatternSpecification,
                                                       MapList<PatternAction, MatchingAction> matching) {
        List<ChangePatternInstance> instancesAllCombinations = new ArrayList<>();

        ChangePatternInstance ins = new ChangePatternInstance(changePatternSpecification);
        for (PatternAction pa : matching.keySet()) {

            List<ChangePatternInstance> temp = new ArrayList<>();

            List<MatchingAction> actions = matching.get(pa);

            MatchingAction matchingAction = actions.get(0);

            ins.addInstance(matchingAction.getPatternAction(), matchingAction.getOperation());
            ins.getMapping().put(pa, matchingAction);

            temp.add(ins);

            instancesAllCombinations.clear();
            instancesAllCombinations.addAll(temp);
        }
        return instancesAllCombinations;
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
     *
     *
     * @param instance
     * @return
     */
    public boolean validate(ChangePatternInstance instance) {
        return checkAbsenceNotMappedAction(instance) && checkEntitiesUsedOne(instance);
    }

    /**
     * This method checks if the entities are used correctly: eg,
     *
     * @param instance
     * @return
     */
    public boolean checkAbsenceNotMappedAction(ChangePatternInstance instance) {
        for (PatternAction paction : instance.actionOperation.keySet()) {

            if (!instance.getMapping().containsKey(paction)) {
                log.debug("The action has not mapping: " + paction);
                return false;
            }

            MatchingAction matchingAction = instance.getMapping().get(paction);

            if (matchingAction.getMatching().isEmpty()) {
                log.debug("The mappings for action is empty: " + paction);
                return false;
            }
        }
        return true;
    }

    /**
     * This method ckecks if the entities are used correctly: eg,
     *
     * @param instance
     * @return
     */
    private boolean checkEntitiesUsedOne(ChangePatternInstance instance) {
        List<Integer> ids = new ArrayList<>();
        Map<CtElement, Integer> entitiesById = new java.util.HashMap<>();
        for (PatternAction paction : instance.actionOperation.keySet()) {
            if (paction.getAction().isUnchanged()) {
                continue;
            }

            MatchingAction matchingAction = instance.getMapping().get(paction);

            // The first one is always pointed by an action.
            MatchingEntity firstMatchingEntity = matchingAction.getMatching().get(0);
            int id = firstMatchingEntity.patternEntity.getId();
            if (id > 0 && ids.contains(id)) {
                log.debug("Another action affect the same Entity: " + id);
                return false;
            } else {
                ids.add(id);

                CtElement affected = firstMatchingEntity.getAffectedNode();

                if (entitiesById.containsKey(affected) && entitiesById.get(affected) != id) {
                    // The same entity mapped to another entity
                    log.debug("Same entity used twise: " + id);
                    return false;
                }
                entitiesById.put(affected, id);
            }
        }
        return true;
    }

    /***
     * For a ChangedPatternInstance, if it is matched by an entity with UNCHANGED
     * action, which is with lower prior, it's valid only if the entity is also
     * matched by the other entities. Because entity ids cannot be the same, and we
     * want the UNCHANGED entities to match nothing.
     *
     * @param instance
     * @return
     */
    private boolean checkUnchangedActionAlsoUsedByOther(ChangePatternInstance instance) {
        Set<CtElement> mustMatchedEntities = new HashSet<>();
        for (PatternAction paction : instance.actionOperation.keySet()) {
            if (!paction.getAction().isUnchanged()) {
                continue;
            }

            MatchingAction matchingAction = instance.getMapping().get(paction);
            // The first one is always pointed by an action.
            MatchingEntity firstMatchingEntity = matchingAction.getMatching().get(0);
            CtElement affected = firstMatchingEntity.getAffectedNode();
            mustMatchedEntities.add(affected);
        }

        for (PatternAction paction : instance.actionOperation.keySet()) {
            if (paction.getAction().isUnchanged()) {
                continue;
            }

            MatchingAction matchingAction = instance.getMapping().get(paction);
            // The first one is always pointed by an action.
            MatchingEntity firstMatchingEntity = matchingAction.getMatching().get(0);
            CtElement affected = firstMatchingEntity.getAffectedNode();
            if (mustMatchedEntities.contains(affected)) {
                mustMatchedEntities.remove(affected);
            }
        }

        return mustMatchedEntities.isEmpty();
    }

    /**
     * Returns true if the instance of a pattern respect the relation between the
     * elements of the pattern.
     *
     * @param instance
     * @param relations
     * @return
     */
    /**
     * Returns true if the instance of a pattern respect the relation between the
     * elements of the pattern.
     *
     * @param instance
     * @param relations
     * @return
     */
    public boolean checkPatternRelationsOnInstance(ChangePatternInstance instance, PatternRelations relations) {
        return checkPatternRelationsOnInstance(instance, relations, true);
    }

    public boolean checkInvalidInstanceExistance(ChangePatternInstance invalid, PatternRelations relations) {
        return checkPatternRelationsOnInstance(invalid, relations, false);
    }

    private boolean checkPatternRelationsOnInstance(ChangePatternInstance instance, PatternRelations relations,
                                                    boolean checkUnchanged) {

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
                    boolean aInMapping = instance.getMapping().containsKey(actionA);
                    boolean bInMapping = instance.getMapping().containsKey(actionB);
                    if (checkUnchanged) {
                        boolean isAUnchanged = actionA.getAction().isUnchanged();
                        boolean isBUnchanged = actionB.getAction().isUnchanged();
                        if ((!aInMapping && !isAUnchanged) || (!bInMapping && !isBUnchanged)) {
                            return false;
                        }

                        if (!aInMapping || !bInMapping) {
                            continue;
                        }
                    } else {
                        if (!aInMapping || !bInMapping) {
                            return false;
                        }
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
            ActionType patternOperationType = getOperationType(patternAction);
            // For each operation in the diff
            for (Operation operation : operationsFromDiff) {

                Action action = operation.getAction();
                // First, match the type of the action
                if (matchActionTypes(action, patternOperationType)) {
                    // when, match the elements affected by the action.
                    List<MatchingEntity> matching = matchElements(operation, patternAction.getAffectedEntity());

                    if (matching != null && !matching.isEmpty()) {
                        mapped = true;
                        MatchingAction maction = new MatchingAction(operation, patternAction, matching);
                        if (patternOperationType.equals(ActionType.UNCHANGED_HIGH_PRIORITY)) {
                            notMapped.add(patternAction);
                        } else {
                            mapping.add(patternAction, maction);
                        }
                    }
                }
            }
            if (!mapped) {
                if (patternOperationType.isUnchanged()) {
                    continue;
                }

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
            String typeOfNode = EntityTypesInfoResolver.getNodeLabelFromCtElement(currentNodeFromAction);
            String valueOfNode = currentNodeFromAction.toString();
            String roleInParent = (currentNodeFromAction.getRoleInParent() != null)
                    ? currentNodeFromAction.getRoleInParent().toString().toLowerCase()
                    : "";

            String patternEntityValue = (matchnewvalue) ? parentEntity.getNewValue() : parentEntity.getOldValue();
            if ( // type of element
                    ("*".equals(parentEntity.getEntityType())
//                            || (typeOfNode != null && typeOfNode.equals(parentEntity.getEntityType())))
                            || (typeOfNode != null &&
                                    EntityTypesInfoResolver.getInstance().isAChildOf(typeOfNode, parentEntity.getEntityType())))
                            ///
                            &&
                            // value of element
                            ("*".equals(patternEntityValue) || (valueOfNode != null && valueOfNode.equals(patternEntityValue)))
                            //
                            &&
                            // role
                            ("*".equals(parentEntity.getRoleInParent()) || (roleInParent != null
                                    && roleInParent.equals(parentEntity.getRoleInParent().toLowerCase())))) {
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

        return ActionType.ANY.equals(type) || ActionType.UNCHANGED.equals(type)
                || ActionType.UNCHANGED_HIGH_PRIORITY.equals(type)
                || (type.equals(ActionType.INS) && (action instanceof Insert))
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

}