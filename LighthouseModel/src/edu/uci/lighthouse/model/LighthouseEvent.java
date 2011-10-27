/*******************************************************************************
* Copyright (c) {2009,2011} {Software Design and Collaboration Laboratory (SDCL)
*				, University of California, Irvine}.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    {Software Design and Collaboration Laboratory (SDCL)
*	, University of California, Irvine}
*			- initial API and implementation and/or initial documentation
*******************************************************************************/
package edu.uci.lighthouse.model;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;

import org.apache.log4j.Logger;

import edu.uci.lighthouse.model.util.LHStringUtil;

/**
 *  Select new events by using the timestamp and NOT Author
 * */
@NamedQueries ({
	//Lee
	//@NamedQuery(name = "LighthouseEvent.findByTimestampAndAuthor",
			@NamedQuery(name = "LighthouseEvent.findByTimestamp",
				query = "SELECT event " + 
						"FROM LighthouseEvent event " + 
						"WHERE ( event.timestamp > :timestamp " +
						"OR event.committedTime > :timestamp ) " +
						"AND event.author <> :author"),

						//Lee: Kyle was using this but it was actually creating 
						//an exception.  PullModel line 66 tries to pass an argument
						//for author but there is no author parameter in this query!
/*
	/@NamedQuery(name = "LighthouseEvent.findByTimestamp",
				query = "SELECT event " + 
						"FROM LighthouseEvent event " + 
						"WHERE ( event.timestamp > :timestamp " +
						"OR event.committedTime > :timestamp )")*/
})


/**
 * Represents a event (ADD, REMOVE, MODIFY) generated by a developer.
 */
@Entity
public class LighthouseEvent implements Serializable{
	
	private static final long serialVersionUID = 7247791395122774431L;

	private static Logger logger = Logger.getLogger(LighthouseEvent.class);

	@Id /** hash, combination of author+type+artifact*/
	private String id = "";

	/** User that generates the event. */
	@OneToOne(cascade = CascadeType.ALL)
	private LighthouseAuthor author;

	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date timestamp = new Date(0);

	@OneToOne(cascade = CascadeType.ALL)
	private LighthouseEntity entity = null;

	@OneToOne(cascade = CascadeType.ALL)
	private LighthouseRelationship relationship = null;
	
	private boolean isCommitted = false; 
	
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date committedTime = new Date(0);
	
	public static enum TYPE {
		ADD, REMOVE, MODIFY, CUSTOM
	}

	private TYPE type;	

	protected LighthouseEvent() {
	}
	
	/**
	 * Create an event
	 * 
	 * @param type
	 * 		Event Type
	 * 
	 * @param author
	 * 		User that generates the Event
	 * 
	 * @param artifact
	 * 		{@link LighthouseEntity}
	 * 		OR
	 * 		{@link LighthouseRelationship}
	 * */
	protected LighthouseEvent(TYPE type, LighthouseAuthor author, Object artifact) {
		this.author = author;
		this.type = type;
		this.setArtifact(artifact);
		try {
			String hashStringId = author.toString()+type+artifact;
			this.id = LHStringUtil.getMD5Hash(hashStringId); 
		} catch (NoSuchAlgorithmException e) {
			logger.error(e,e);
		} 
	}
		
	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	public LighthouseAuthor getAuthor() {
		return author;
	}

	protected void setAuthor(LighthouseAuthor author) {
		this.author = author;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public LighthouseEntity getEntity() {
		return entity;
	}

	protected void setEntity(LighthouseEntity entity) {
		this.entity = entity;
	}

	public LighthouseRelationship getRelationship() {
		return relationship;
	}

	protected void setRelationship(LighthouseRelationship relationship) {
		this.relationship = relationship;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}

	public Date getCommittedTime() {
		return committedTime;
	}

	public void setCommittedTime(Date committedTime) {
		this.committedTime = committedTime;
	}

	public TYPE getType() {
		return type;
	}

	protected void setType(TYPE type) {
		this.type = type;
	}

	/**
	 * Get the Artifact (Entity or Relationship) related with this event
	 * 
	 * @return
	 * 		{@link LighthouseEntity}
	 * 		OR
	 * 		{@link LighthouseRelationship}
	 * */
	public Object getArtifact() {
		if (entity == null && relationship == null) {
			logger.error(this.toString() + " has a NULL artifact");
			return null;
		} else if (entity != null) {
			return entity;
		} else {
			return relationship;
		}
	}

	/**
	 * Set the Artifact (Entity or Relationship) related with this event
	 * 
	 * @param obj
	 * 		{@link LighthouseEntity}
	 * 		OR
	 * 		{@link LighthouseRelationship}
	 * */
	protected void setArtifact(Object obj) {
		if (obj instanceof LighthouseEntity) {
			if (relationship == null) {
				this.entity = (LighthouseEntity) obj;
			}
		} else if (obj instanceof LighthouseRelationship) {
			if (entity == null) {
				this.relationship = (LighthouseRelationship) obj;
			}
		}
	}

	@Override
	public String toString() {
		return 
		"LighthouseEvent [" 
		+ "getArtifact()=" + getArtifact()
		+ ", type=" + type
		+ ", timestamp=" + timestamp 
		+ ", committedTime=" + committedTime
		+ ", isCommitted=" + isCommitted
		+ ", author=" + author 
		+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LighthouseEvent other = (LighthouseEvent) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
