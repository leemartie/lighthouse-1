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
package edu.uci.lighthouse.lighthouseqandathreads;

import java.util.Observer;

import org.eclipse.swt.widgets.Composite;

public interface IController<T> extends Observer{

	public PersistAndUpdate getPersisterAndUpdater();
	public void setPersisterAndUpdater(PersistAndUpdate persisterAndUpdater);
	public Composite getView();
	public T getModel();
	public void init();
}
