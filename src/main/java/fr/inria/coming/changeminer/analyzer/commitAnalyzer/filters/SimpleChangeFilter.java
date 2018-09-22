package fr.inria.coming.changeminer.analyzer.commitAnalyzer.filters;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.IChangesProcessor;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternAction;
import fr.inria.coming.changeminer.analyzer.patternspecification.PatternEntity;
import fr.inria.coming.changeminer.entity.ActionType;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SimpleChangeFilter implements IChangesProcessor {
	Logger log = Logger.getLogger(SimpleChangeFilter.class.getName());
	List<PatternAction> patternActions = null;

	public SimpleChangeFilter(List<PatternAction> patternAction) {
		super();
		this.patternActions = patternAction;
	}

	public SimpleChangeFilter(PatternAction patternAction) {
		this.patternActions = new ArrayList<>();
		this.patternActions.add(patternAction);

	}

	public SimpleChangeFilter(PatternEntity entity, ActionType operationType) {

		this(new PatternAction(entity, operationType));

	}

	/**
	 * 
	 * @param typeLabel     node label to mine
	 * @param operationType operation type to mine
	 */
	public SimpleChangeFilter(String typeLabel, ActionType operationType) {

		this(new PatternAction(new PatternEntity(typeLabel), operationType));

	}

	/**
	 * Return the actions according to a type label.
	 *
	 * @param actions
	 * @param typeLabel
	 * @param operationType
	 * @param granularity2
	 * @return
	 */
	@Override
	public List<Operation> process(Diff diff) {

		List<Operation> operations = diff.getAllOperations();

		// actions.removeAll(Collections.singleton(null));
		List<Operation> filter = new ArrayList<Operation>();

		for (PatternAction patternAction : this.patternActions) {
			boolean added = false;
			for (Operation operation : operations) {

				// for (Action action : actions) {
				Action action = operation.getAction();
				try {

					if (matchTypeLabel(operation, getTypeLabel(patternAction))
							&& matchTypes(action, getOperationType(patternAction))) {
						filter.add(operation);
						added = true;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			if (!added)
				return new ArrayList<>();

		}
		return filter;
	}

	public static boolean matchTypes(Action action, ActionType type) {

		return ActionType.ANY.equals(type) || (type.equals(ActionType.INS) && (action instanceof Insert))
				|| (type.equals(ActionType.DEL) && (action instanceof Delete))
				|| (type.equals(ActionType.MOV) && (action instanceof Move))
				|| (type.equals(ActionType.UPD) && (action instanceof Update));
	}

	protected boolean matchTypeLabel(Operation operation, String typeLabel) {
		log.debug("-->>" + operation.getNode().getClass().getSimpleName() + "<->" + typeLabel);
		return "*".equals(typeLabel)
				// ||
				// typeLabel.equals(diff.ge,.getTypeLabel(action.getNode().getType()));
				// TODO:
				|| operation.getNode() != null && getNodeLabelFromCtElement(operation.getNode()).equals(typeLabel)
				|| (operation.getDstNode() != null
						&& getNodeLabelFromCtElement(operation.getDstNode()).equals(typeLabel))
				|| (operation.getSrcNode() != null
						&& getNodeLabelFromCtElement(operation.getSrcNode()).equals(typeLabel));
	}

	public ActionType getOperationType(PatternAction patternAction) {
		return patternAction.getAction();
	}

	public String getTypeLabel(PatternAction patternAction) {
		return patternAction.getAffectedEntity().getEntityType();
	}

	@Override
	public void init() {

	}

	@Override
	public void end() {

	}

	public String getNodeLabelFromCtElement(CtElement element) {
		String typeFromCt = element.getClass().getSimpleName();
		if (typeFromCt.trim().isEmpty())
			return typeFromCt;
		return typeFromCt.substring(2, typeFromCt.length() - 4);
	}

}
