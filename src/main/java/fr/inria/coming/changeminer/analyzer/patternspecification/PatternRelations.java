package fr.inria.coming.changeminer.analyzer.patternspecification;

import java.util.List;

import fr.inria.coming.utils.MapList;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternRelations {

	List<EntityRelation> relations;
	MapList<PatternAction, EntityRelation> paEntity;

	public PatternRelations(List<EntityRelation> relations, MapList<PatternAction, EntityRelation> paEntity) {
		super();
		this.relations = relations;
		this.paEntity = paEntity;
	}

	public List<EntityRelation> getRelations() {
		return relations;
	}

	public void setRelations(List<EntityRelation> relations) {
		this.relations = relations;
	}

	public MapList<PatternAction, EntityRelation> getPaEntity() {
		return paEntity;
	}

	public void setPaEntity(MapList<PatternAction, EntityRelation> paEntity) {
		this.paEntity = paEntity;
	}

}
