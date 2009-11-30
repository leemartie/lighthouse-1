package edu.uci.lighthouse.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.uci.lighthouse.model.LighthouseRelationship.TYPE;
import edu.uci.lighthouse.model.jpa.JPAUtilityException;
import edu.uci.lighthouse.model.jpa.LHEntityDAO;
import edu.uci.lighthouse.model.jpa.LHEventDAO;
import edu.uci.lighthouse.model.jpa.LHRelationshipDAO;
import edu.uci.lighthouse.model.util.UtilModifiers;

/**
 * This is a utility class for building model objects. Its purpose is to
 * assist in constructing models for test cases, but it is useful any time a
 * model needs to be constructed from code.
 * 
 * All the methods are static.
 * 
 * @author tproenca
 * 
 */
public class LighthouseModelManager {
	
	private static Logger logger = Logger.getLogger(LighthouseModelManager.class);
	
	protected LighthouseModel model;

	public LighthouseModelManager(LighthouseModel model) {
		super();
		this.model = model;
	}

	private LighthouseEntity addEntity(LighthouseEntity newEntity){
		LighthouseEntity entity = model.getEntity(newEntity.getFullyQualifiedName());
		if (entity!=null) {
			return entity;
		} else {
			model.addEntity(newEntity);
			return newEntity;	
		}
	}
	
	private LighthouseRelationship addRelationship(LighthouseRelationship newRelationship){
		LighthouseRelationship relationship = model.getRelationship(newRelationship);
		if (relationship == null) {
			relationship = newRelationship;
		}
		handleEntitiesNotInsideClass(relationship);		
		model.addRelationship(relationship);
		return newRelationship;
	}
	
	/** Need this method for add ExternalClass and Modifiers in the LHBaseFile
	 * because they have not TYPE.INSIDE relationship */
	private void handleEntitiesNotInsideClass(
			LighthouseRelationship relationship) {
		LighthouseEntity fromEntity = relationship.getFromEntity();
		LighthouseEntity toEntity = relationship.getToEntity();
		if (fromEntity instanceof LighthouseExternalClass) {
			addEntity(fromEntity);
		}
		if (toEntity instanceof LighthouseExternalClass) {
			addEntity(toEntity);
		}
		if (relationship.getType()==TYPE.MODIFIED_BY) {
			addEntity(toEntity);
		}
	}
	
	/** Add <code>event</code> in the LighthouseModel, however do not add the event in the database*/
	public void addEvent(LighthouseEvent event) {
		Object artifact = addArtifact(event.getArtifact());
		event.setArtifact(artifact);
		model.addEvent(event);
	}
	
	public Object addArtifact(Object artifact) {
		if (artifact instanceof LighthouseEntity) {
			LighthouseEntity entity = addEntity((LighthouseEntity) artifact);
			return entity;
		} else if (artifact instanceof LighthouseRelationship) {
			LighthouseRelationship relationship = addRelationship((LighthouseRelationship) artifact);
			return relationship;
		}
		return null;
	}
	
	public LighthouseEntity getEntity(String fqn) {
		return model.getEntity(fqn);
	}
	
	public void saveEventsIntoDatabase(LinkedHashSet<LighthouseEvent> listEvents) throws JPAUtilityException {
		LHEventDAO dao = new LHEventDAO();
		// The entity events need to come before the relationship events
		// we could use to loops to handle that
		for (LighthouseEvent event : listEvents) {
			dao.save(event);
		}
	}
	
	public LighthouseEntity getEntityFromDatabase(String fqn) {
		LighthouseEntity entity = getEntity(fqn);
		if (entity==null) {
			entity = new LHEntityDAO().get(fqn);
		}
		return entity;
	}
	
	public LinkedHashSet<LighthouseEntity> selectEntitiesInsideClass(String fqnClazz) {
		return selectEntitiesInsideClass(new LinkedHashSet<LighthouseEntity>(),fqnClazz);
	}
	
	/**
	 * Going to the database to return the entities inside a class
	 * Recursive method
	 * @param listEntitiesInside should be a new LinkedHashSet()
	 * */ 
	private LinkedHashSet<LighthouseEntity> selectEntitiesInsideClass(LinkedHashSet<LighthouseEntity> listEntitiesInside, String fqnClazz) {
		LighthouseClass clazz = new LighthouseClass(fqnClazz);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("relType", LighthouseRelationship.TYPE.INSIDE);
		parameters.put("toEntity", clazz);
		List<LighthouseEntity> subListEntitiesInside = new LHRelationshipDAO().executeNamedQueryGetFromEntityFqn("LighthouseRelationship.findFromEntityByTypeAndToEntity", parameters);
		for (LighthouseEntity entity : subListEntitiesInside) {
			if (entity instanceof LighthouseClass || entity instanceof LighthouseInterface) { // That is a Inner class
				listEntitiesInside.addAll(selectEntitiesInsideClass(listEntitiesInside, entity.getFullyQualifiedName()));
			}
		}
		listEntitiesInside.addAll(subListEntitiesInside);
		LighthouseEntity entityClazz = getEntityFromDatabase(fqnClazz);
		if (entityClazz instanceof LighthouseClass) {
			listEntitiesInside.add(entityClazz);
		}
		return listEntitiesInside;
	}
	
	public LinkedHashSet<LighthouseEntity> selectEntitiesInsideClass(List<String> listClazzFqn) {
		LinkedHashSet<LighthouseEntity> listFromEntities = new LinkedHashSet<LighthouseEntity>();
		for (String clazzFqn : listClazzFqn) {
			listFromEntities.addAll(selectEntitiesInsideClass(clazzFqn));			
		}
		return listFromEntities;
	}
	
	public void removeArtifactsAndEventsInside(Collection<String> listClazzFqn) {
		Collection<LighthouseEntity> listEntity = LighthouseModelUtil.getEntitiesInsideClasses(model, listClazzFqn);
		Collection<LighthouseRelationship> listRel = LighthouseModelUtil.getRelationships(model, listEntity);
		LinkedHashSet<LighthouseEvent> listEvents = LighthouseModelUtil.getEventsInside(model, listEntity, listRel);
		for (LighthouseEvent event : listEvents) {
			model.removeEvent(event);
		}
		for (LighthouseRelationship rel : listRel) {
			model.removeRelationship(rel);
		}
		for (LighthouseEntity entity : listEntity) {
			model.removeEntity(entity);
		}
	}

	/*  PUT THOSE METHODS BELLOW IN THE LighthouseModelUtil.java */
	
	/**
	 * Returns the associate class or <code>null</code> otherwise.
	 * 
	 * @param model
	 * @param e
	 * @return
	 */
	public LighthouseClass getMyClass(LighthouseEntity e){
		String classFqn = null;
		if (e instanceof LighthouseClass){
			classFqn = e.getFullyQualifiedName();
		} else if (e instanceof LighthouseField){
			classFqn = e.getFullyQualifiedName().replaceAll("(\\.\\w+)\\z", "");
		} else if (e instanceof LighthouseMethod){
			classFqn = e.getFullyQualifiedName().replaceAll("\\.[\\<\\w]\\w+[\\>\\w]\\([,\\w\\.]*\\)","");
		}
		LighthouseEntity c = model.getEntity(classFqn);
		if (c instanceof LighthouseClass){
			return (LighthouseClass)c;
		}
		return null;
	}
	
	public boolean isStatic(LighthouseEntity e){
		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
		for (LighthouseRelationship r : list) {
			if (r.getToEntity() instanceof LighthouseModifier){
				if (UtilModifiers.isStatic(r.getToEntity().getFullyQualifiedName())){
					return true;
				}
			}
		}
		return false;
	}
	public boolean isFinal(LighthouseEntity e){
		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
		for (LighthouseRelationship r : list) {
			if (r.getToEntity() instanceof LighthouseModifier){
				if (UtilModifiers.isFinal(r.getToEntity().getFullyQualifiedName())){
					return true;
				}
			}
		}
		return false;
	}
	public boolean isSynchronized(LighthouseEntity e){
		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
		for (LighthouseRelationship r : list) {
			if (r.getToEntity() instanceof LighthouseModifier){
				if (UtilModifiers.isSynchronized(r.getToEntity().getFullyQualifiedName())){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isPublic(LighthouseEntity e){
		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
		for (LighthouseRelationship r : list) {
			if (r.getToEntity() instanceof LighthouseModifier){
				if (UtilModifiers.isPublic(r.getToEntity().getFullyQualifiedName())){
					return true;
				}
			}
		}
		return false;
	}
	public boolean isProtected(LighthouseEntity e){
		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
		for (LighthouseRelationship r : list) {
			if (r.getToEntity() instanceof LighthouseModifier){
				if (UtilModifiers.isProtected(r.getToEntity().getFullyQualifiedName())){
					return true;
				}
			}
		}
		return false;
	}
	public boolean isPrivate(LighthouseEntity e){
		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
		for (LighthouseRelationship r : list) {
			if (r.getToEntity() instanceof LighthouseModifier){
				if (UtilModifiers.isPrivate(r.getToEntity().getFullyQualifiedName())){
					return true;
				}
			}
		}
		return false;
	}

}
