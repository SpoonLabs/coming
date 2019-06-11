package fr.inria.coming.changeminer.analyzer.patternspecification;

import java.util.ArrayList;
import java.util.List;

import fr.inria.coming.utils.MapList;

/**
 * Specification of a Pattern
 * 
 * @author Matias Martinez
 *
 */
public class ChangePatternSpecification {

	private String name;
	private List<PatternAction> changes;

	public String getName() {
		return name;
	}

	public List<PatternAction> getAbstractChanges() {
		return changes;
	}

	public ChangePatternSpecification() {
		changes = new ArrayList<PatternAction>();
	}

	public ChangePatternSpecification(String name) {
		this();
		this.name = name;
	}

	public ChangePatternSpecification(List<PatternAction> changes) {

		this.changes = new ArrayList<PatternAction>();
	}

	public void addChange(PatternAction pa) {
		this.changes.add(pa);
	}

	@Override
	public String toString() {
		return "ChangePattern [" + ((name == null) ? "" : "name=") + name + ", changes=" + changes + "]";
	}

	public PatternRelations calculateRelations() {
		MapList<PatternEntity, PatternAction> commonEntitiesByPattern = new MapList<>();
		// Store all relations between Actions i.e., hare an object
		List<EntityRelation> relations = new ArrayList<>();
		MapList<PatternAction, EntityRelation> paEntity = new MapList<>();

		for (PatternAction patternAction : changes) {

			PatternEntity pe = patternAction.getAffectedEntity();
			do {
				// Get Actions of an Entity
				commonEntitiesByPattern.add(pe, patternAction);
				pe = (pe.getParentPatternEntity() != null) ? pe.getParentPatternEntity().getParent() : null;
			} while (pe != null);
		}
		// For each entity
		for (PatternEntity sharedEntity : commonEntitiesByPattern.keySet()) {
			List<PatternAction> pl = commonEntitiesByPattern.get(sharedEntity);
			if (pl.size() > 1) {
				// each combination
				for (int i = 0; i < pl.size(); i++) {

					PatternAction pA = pl.get(i);
					// Create a relation with the other Actions that are pointed by the same Entity
					for (int j = i + 1; j < pl.size(); j++) {
						PatternAction pB = pl.get(j);

						EntityRelation r = new EntityRelation(sharedEntity, pA, pB);
						relations.add(r);
						paEntity.add(pA, r);
						paEntity.add(pB, r);
					}
				}
			}
		}
		return new PatternRelations(relations, paEntity);
	}

    public void setName(String name) {
        this.name = name;
    }
}
