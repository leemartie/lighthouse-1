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
package edu.uci.lighthouse.model.jpa;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @param <T>
 * @param <PK>
 */
public interface InterfaceDAO<T, PK extends Serializable> {
	
	/**
	 * List all tuple of a particular entity
	 * @return
	 * @throws JPAException 
	 */
	public List<T> list() throws JPAException;
	
	/**
	 * Return a list of <code>T</code> from a
	 * <code>query</code> pre-defined using namedQuery JPA notation
	 * @param query
	 * @param parameters query parameters
	 * @return
	 * @throws JPAException 
	 */
	public List<T> executeNamedQuery(String query,
			Map<String, Object> parameters) throws JPAException;
	
	/**
	 * Return a list of <code>T</code> from a
	 * <code>query</code> pre-defined using namedQuery JPA notation
	 * @param query
	 * @param parameters query parameters
	 * @return
	 * @throws JPAException 
	 */
	public List<T> executeNamedQuery(String query, Object[] parameters) throws JPAException;
	
	/**
	 * Execute query that are dynamically build using the parameters
	 * @param parameters
	 * @return
	 * @throws JPAException 
	 */
	public List<T> executeDynamicQuery(String strQuery) throws JPAException;
	
	/**
	 * @param query
	 * @throws JPAException 
	 * */
	public void executeUpdateQuery(String strQuery) throws JPAException;
	
	/**
	 * Return an instance of <code>T</code> using the key <code>pk</code>
	 * @param pk
	 * @return
	 * @throws JPAException 
	 */
	public T get(PK pk) throws JPAException;
	
	/**
	 * Save and UPDATE an instance of <code>T</code> in the database.
	 * Working with Detached entities
	 * @param entity
	 * @return T
	 * @throws JPAUpdateException
	 */
	public T save(T entity) throws JPAException;
	
	/**
	 * Remove an instance of <code>T</code>> from the data base.
	 * @param entity
	 * @throws JPADeleteException
	 */
	public void remove(T entity) throws JPAException;
	
}
