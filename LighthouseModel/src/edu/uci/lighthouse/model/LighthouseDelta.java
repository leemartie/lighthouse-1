package edu.uci.lighthouse.model;

import java.util.Collection;
import java.util.LinkedHashSet;

import edu.uci.lighthouse.model.LighthouseEvent.TYPE;

public class LighthouseDelta {
	
	private LinkedHashSet<LighthouseEvent> listEvents = new LinkedHashSet<LighthouseEvent>();
	private LighthouseAuthor author;
	
	public LighthouseDelta() {
	}
	
	public LighthouseDelta (LighthouseAuthor author, LighthouseFile oldModel, LighthouseFile newModel) {
		this.author = author;
		if (oldModel==null && newModel==null) {
			return;
		} else if (oldModel==null) {
			findAddedChanges(newModel);
		} else if (newModel==null) {
			findRemovedChanges(oldModel);
		} else {
			findEntityChanges(oldModel, newModel);
			findRelationshipChanges(oldModel, newModel);
		}		
	}

	private void findEntityChanges(LighthouseFile oldModel, LighthouseFile newModel) {
		Collection<LighthouseEntity> listOldEntities = oldModel.getEntities();
		for(LighthouseEntity entity : listOldEntities){
			if(!newModel.containsEntity(entity.getFullyQualifiedName())) {
				addEvent(new LighthouseEvent(TYPE.REMOVE, this.author, entity));				
			}
		}
		Collection<LighthouseEntity> listNewEntities = newModel.getEntities();
		for(LighthouseEntity entity : listNewEntities){
			if(!oldModel.containsEntity(entity.getFullyQualifiedName())) {
				addEvent(new LighthouseEvent(TYPE.ADD, this.author, entity));
			}
		}
	}
	
	private void findRelationshipChanges(LighthouseFile oldModel, LighthouseFile newModel) {
		LinkedHashSet<LighthouseRelationship> listOldRel = oldModel.getRelationships();
		for (LighthouseRelationship oldRel : listOldRel) {
			if (!newModel.containsRelationship(oldRel)) {
				LighthouseRelationship rel = new LighthouseRelationship(oldRel.getFromEntity(),oldRel.getToEntity(),oldRel.getType());
				addEvent(new LighthouseEvent(TYPE.REMOVE, this.author, rel));
				addEntityModifyEvent(rel);
			}
		}
		LinkedHashSet<LighthouseRelationship> listNewRel = newModel.getRelationships();
		for (LighthouseRelationship newRel : listNewRel) {
			if (!oldModel.containsRelationship(newRel)) {
				LighthouseRelationship rel = new LighthouseRelationship(newRel.getFromEntity(),newRel.getToEntity(),newRel.getType());
				addEvent(new LighthouseEvent(TYPE.ADD, this.author, rel));
				addEntityModifyEvent(rel);
			}
		}
	}
	
	// Handle modification events
	private void addEntityModifyEvent(LighthouseRelationship rel) {
		LighthouseEntity fromEntity = rel.getFromEntity();
		if (fromEntity instanceof LighthouseField || fromEntity instanceof LighthouseMethod) {
			LighthouseEvent tempEventAdd = new LighthouseEvent(TYPE.ADD, this.author, fromEntity);
			LighthouseEvent tempEventRemove = new LighthouseEvent(TYPE.REMOVE, this.author, fromEntity);
			if (!(listEvents.contains(tempEventAdd) || listEvents.contains(tempEventRemove))) {
				addEvent(new LighthouseEvent(TYPE.MODIFY, this.author, fromEntity));
			}
		} 
	}

	private void findAddedChanges(LighthouseFile model) {
		Collection<LighthouseEntity> listEntities = model.getEntities();
		for(LighthouseEntity entity : listEntities) {
			addEvent(new LighthouseEvent(TYPE.ADD, this.author,entity));
		}
		LinkedHashSet<LighthouseRelationship> listRel = model.getRelationships();
		for(LighthouseRelationship rel : listRel) {
			addEvent(new LighthouseEvent(TYPE.ADD, this.author, rel));
		}
	}
	
	private void findRemovedChanges(LighthouseFile model){
		Collection<LighthouseEntity> listEntities = model.getEntities();
		for(LighthouseEntity entity : listEntities) {
			addEvent(new LighthouseEvent(TYPE.REMOVE, this.author, entity));
		}
		LinkedHashSet<LighthouseRelationship> listRel = model.getRelationships();
		for(LighthouseRelationship rel : listRel){
			addEvent(new LighthouseEvent(TYPE.REMOVE, this.author, rel));
		}
	}
	
	public void addEvent(LighthouseEvent event) {
		listEvents.add(event);
	}
	
	public LinkedHashSet<LighthouseEvent> getEvents() {
		return listEvents;
	}
	
}
