package fr.inria.sacha.coming.analyzer.commitAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.inria.sacha.coming.analyzer.DiffResult;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternAction;
import fr.inria.sacha.coming.analyzer.treeGenerator.PatternEntity;
import fr.inria.sacha.coming.entity.ActionType;
import fr.labri.gumtree.actions.model.Action;
import fr.labri.gumtree.actions.model.Delete;
import fr.labri.gumtree.actions.model.Insert;
import fr.labri.gumtree.actions.model.Move;
import fr.labri.gumtree.actions.model.Update;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SimpleChangeFilter implements IChangesProcessor {

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
	 * @param typeLabel
	 *            node label to mine
	 * @param operationType
	 *            operation type to mine
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
	public List<Action> process(DiffResult diff) {
		List<Action> actions = diff.getAllActions();
		actions.removeAll(Collections.singleton(null));
		List<Action> filter = new ArrayList<Action>();
		
		
		for (PatternAction patternAction : this.patternActions) {
			boolean added = false;
			for (Action action : actions) {
				try {
					if (action.getNode().getTypeLabel().equals("CompilationUnit"))
						continue;

					if (matchTypeLabel(action, getTypeLabel(patternAction)) && matchTypes(action, getOperationType(patternAction))){
						filter.add(action);
						added = true;
					}	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(!added) 
				return new ArrayList<>();

		}
		return filter;
	}

	protected boolean matchTypes(Action action, ActionType type) {

		return ActionType.ANY.equals(type) || (type.equals(ActionType.INS) && (action instanceof Insert))
				|| (type.equals(ActionType.DEL) && (action instanceof Delete))
				|| (type.equals(ActionType.MOV) && (action instanceof Move))
				|| (type.equals(ActionType.UPD) && (action instanceof Update));
	}

	protected boolean matchTypeLabel(Action action, String typeLabel) {
		return "*".equals(typeLabel) || typeLabel.equals(action.getNode().getTypeLabel());
	}

	public ActionType getOperationType(PatternAction patternAction) {
		return patternAction.getAction();
	}

	public String getTypeLabel(PatternAction patternAction) {
		return patternAction.getAffectedEntity().getEntityName();
	}

	@Override
	public void init() {

	}

	@Override
	public void end() {

	}

}
