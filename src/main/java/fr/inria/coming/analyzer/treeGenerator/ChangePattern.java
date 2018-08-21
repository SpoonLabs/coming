package fr.inria.coming.analyzer.treeGenerator;

import java.util.ArrayList;
import java.util.List;

public class ChangePattern {

	private String name;
	private List<PatternAction> changes;
	
	public String getName() {
		return name;
	}
	public List<PatternAction> getChanges() {
		return changes;
	}
	public ChangePattern(){
		changes = new ArrayList<PatternAction>();
	}
	public ChangePattern(String name){
		this.name = name;
		changes = new ArrayList<PatternAction>();
	}
	public ChangePattern(List<PatternAction> changes){
		
		this.changes = new ArrayList<PatternAction>();
	}
	
	public void addChange(PatternAction pa){
		this.changes.add(pa);
	}
	
	@Override
	public String toString() {
		return "ChangePattern ["+((name==null)?"":"name=") + name + ", changes=" + changes + "]";
	}
	
}
