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
package edu.uci.lighthouse.views.filters;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.zest.core.viewers.EntityConnectionData;

import edu.uci.lighthouse.model.LighthouseClass;
import edu.uci.lighthouse.model.LighthouseEntity;
import edu.uci.lighthouse.model.LighthouseModel;
import edu.uci.lighthouse.model.LighthouseRelationship;

public class OpenEditorFilter extends ViewerFilter implements IFilter{
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof LighthouseClass) {
			Collection<LighthouseClass> aClasses = getLighthouseClassesFromEditor();
			for (LighthouseClass aClass : aClasses) {
				if (aClass.equals(element) 
						|| belongsToDistance(aClass, (LighthouseClass) element)) {
					return true;
				}
			}
		} else if (element instanceof EntityConnectionData) {
			return true;
		} else if (element instanceof LighthouseRelationship) {
			// FIXME: try to do something similar the if block above
			return true;
		}
		return false;
	}

	public static Collection<LighthouseClass> getLighthouseClassesFromEditor() {
		Collection<LighthouseClass> result = new LinkedList<LighthouseClass>();
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage != null) {
			IEditorReference[] editorReferences = activePage
					.getEditorReferences();
			for (IEditorReference editorReference : editorReferences) {
				try {
					IJavaElement jFile = JavaUI
							.getEditorInputJavaElement(editorReference
									.getEditorInput());
					if (jFile instanceof ICompilationUnit) {
						IType type = ((ICompilationUnit) jFile)
								.findPrimaryType();
						String fqn = jFile.getJavaProject().getElementName()
								+ "." + type.getFullyQualifiedName();
						LighthouseEntity entity = LighthouseModel.getInstance()
								.getEntity(fqn);
						if (entity instanceof LighthouseClass) {
							result.add((LighthouseClass) entity);
						}
					}
				} catch (Exception e) {
					// TODO: logger
				}
			}
		}
		return result;
	}

	private boolean belongsToDistance(LighthouseClass from, LighthouseClass to) {
		LighthouseModel model = LighthouseModel.getInstance();
		Collection<LighthouseEntity> related = model.getConnectTo(from);
		// test this because is faster
		for (LighthouseEntity toClass : related) {
			if (toClass.equals(to)) {
				return true;
			}
		}
		// otherwise test everything
		// for (LighthouseClass fromClass : model.getAllClasses()) {
		related = model.getConnectTo(to);
		for (LighthouseEntity fromClass : related) {
			if (fromClass.equals(from)) {
				return true;
			}
		}
		// }
		return false;
	}

}
