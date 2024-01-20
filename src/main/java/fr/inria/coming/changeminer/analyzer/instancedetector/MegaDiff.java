package fr.inria.coming.changeminer.analyzer.instancedetector;

import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.TreeContext;

import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import spoon.reflect.declaration.CtElement;

public class MegaDiff implements Diff {

	/**
	 * Actions over all tree nodes (CtElements)
	 */
	private List<Operation> allOperations = new ArrayList<>();
	/**
	 * Actions over the changes roots.
	 */
	private List<Operation> rootOperations = new ArrayList<>();
	/**
	 * the mapping of this diff
	 */
	private MappingStore _mappingsComp;
	/**
	 * Context of the spoon diff.
	 */
	private TreeContext context = new TreeContext();

	public void merge(Diff anotherDiff) {

		this.allOperations.addAll(anotherDiff.getAllOperations());
		this.rootOperations.addAll(anotherDiff.getRootOperations());

		for (Mapping map : anotherDiff.getMappingsComp().asSet()) {
			_mappingsComp = new MappingStore(map.first, map.second);
		}

		// context.
	}

	@Override
	public List<Operation> getRootOperations() {
		return rootOperations;
	}

	@Override
	public List<Operation> getAllOperations() {
		return allOperations;
	}

	@Override
	public List<Operation> getOperationChildren(Operation operationParent, List<Operation> rootOperations) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CtElement changedNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CtElement changedNode(Class<? extends Operation> operationWanted) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CtElement commonAncestor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsOperation(OperationKind kind, String nodeKind) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsOperation(OperationKind kind, String nodeKind, String nodeLabel) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsOperations(OperationKind kind, String nodeKind, String nodeLabel, String newLabel) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsOperations(List<Operation> operations, OperationKind kind, String nodeKind,
			String nodeLabel) {
		return false;
	}

	@Override
	public boolean containsOperations(List<Operation> list, OperationKind operationKind, String s) {
		return containsOperations(list, operationKind, s,"EMPTY");
	}

	@Override
	public void debugInformation() {

	}

	@Override
	public MappingStore getMappingsComp() {
		return _mappingsComp;
	}

}
