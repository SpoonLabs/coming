package fr.inria.sacha.coming.analyzer;

import java.util.ArrayList;
import java.util.List;

import fr.labri.gumtree.actions.model.Action;
import fr.labri.gumtree.actions.model.Delete;
import fr.labri.gumtree.actions.model.Insert;
import fr.labri.gumtree.tree.Tree;




/**
 * 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class DiffResult {
	/**
	 * Actions over all tree nodes (CtElements) 
	 */
	List<Action> allActions = null;
	/**
	 * Actions over the changes roots.
	 */
	List<Action> rootActions = null;
	
	
	
	public DiffResult(List<Action> allActions, List<Action> rootActions) {
		super();
		this.allActions = allActions;
		this.rootActions = rootActions;
	}
	public List<Action> getAllActions() {
		return allActions;
	}
	public void setAllActions(List<Action> allActions) {
		this.allActions = allActions;
	}
	public List<Action> getRootActions() {
		return rootActions;
	}
	public void setRootActions(List<Action> rootActions) {
		this.rootActions = rootActions;
	}
	
	public static  List<Action> getAllFilterDuplicate(List<Action> rootActions){
		
		List<Action> result = new ArrayList<Action>(rootActions);
		//For each Action Insert, we search for same delete
		for (Action actionIns : rootActions) {
			if(actionIns instanceof Insert){
				
				//Lets see the Delete
				for (Action actionDel : rootActions) {
					if(actionDel instanceof Delete){
						if(actionDel.getNode().getLabel().equals(actionIns.getNode().getLabel())){
							Tree parentDel = actionDel.getNode().getParent();
							Tree parentIns = actionIns.getNode().getParent();
							while(parentIns != null)
							if( parentIns.isSimilar(parentDel) ){
								result.remove(actionDel);
								result.remove(actionIns);
								break;
							}else{
								parentIns = parentIns.getParent();
							}
							
						}
						
					}
				}
				
			}
		}
		return result;
		
	}
}
