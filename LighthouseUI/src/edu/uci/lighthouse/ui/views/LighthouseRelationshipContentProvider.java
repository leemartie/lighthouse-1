package edu.uci.lighthouse.ui.views;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.Animation;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;

import edu.uci.lighthouse.model.ILighthouseUIModelListener;
import edu.uci.lighthouse.model.LighthouseClass;
import edu.uci.lighthouse.model.LighthouseModel;
import edu.uci.lighthouse.model.LighthouseModelManager;
import edu.uci.lighthouse.model.LighthouseRelationship;
import edu.uci.lighthouse.model.LighthouseEvent.TYPE;
import edu.uci.lighthouse.ui.swt.util.ColorFactory;
import edu.uci.lighthouse.ui.utils.GraphUtils;

public class LighthouseRelationshipContentProvider implements IGraphContentProvider, ILighthouseUIModelListener{

	// HashMap used just for speed-up the process. A list could of strings could be used.
	private HashMap<String,LighthouseClass> cacheConnections = new HashMap<String,LighthouseClass>();
	private GraphViewer viewer;
	private enum relType {FROM, TO};
	
	private static Logger logger = Logger.getLogger(LighthouseRelationshipContentProvider.class);
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		logger.info("inputChanged()");
		
		// Ensure that all the arguments are legal
		Assert.isLegal(viewer instanceof GraphViewer, "Invalid viewer, only GraphViewer is supported.");
		this.viewer = (GraphViewer) viewer;
		
		if (oldInput instanceof LighthouseModel){
			LighthouseModel oldModel = (LighthouseModel) oldInput;
			oldModel.removeModelListener(this);
		}
		
		if (newInput instanceof LighthouseModel){
			LighthouseModel newModel = (LighthouseModel) newInput;
			newModel.addModelListener(this);
		}
		
		//filters don't call setInput method
		//this.viewer.setFilters(filters)
		
		// Clean the connections cache
		cacheConnections.clear();		
	}
	
	@Override
	public void dispose() {
		
	}
	
	@Override
	public Object getDestination(Object rel) {
		//return getClassFromRelationship((LighthouseRelationship)rel, relType.TO);
		LighthouseClass c = getClassFromRelationship((LighthouseRelationship)rel, relType.TO);
		logger.info("getDestination: "+c);
		return c;
	}

	@Override
	public Object[] getElements(Object input) {
		logger.info("getElements()");
		if (input instanceof LighthouseModel) {
			LighthouseModel model = (LighthouseModel) input;
			createNodes(model);
			return model.getRelationships().toArray();
		}
		return null;
	}

	@Override
	public Object getSource(Object rel) {
		//return getClassFromRelationship((LighthouseRelationship)rel, relType.FROM);
		LighthouseClass c = getClassFromRelationship((LighthouseRelationship)rel, relType.FROM);
		logger.info("getSource: "+c);
		return c;
	}

	private LighthouseClass getClassFromRelationship(LighthouseRelationship r,
			relType type) {
		LighthouseModelManager manager = new LighthouseModelManager(
				LighthouseModel.getInstance());
		LighthouseClass fromClass = manager.getMyClass(r.getFromEntity());
		LighthouseClass toClass = manager.getMyClass(r.getToEntity());
		if (fromClass != null && toClass != null && !fromClass.equals(toClass)) {
			String key;
			LighthouseClass result;
			if (type == relType.FROM) {
				key = fromClass.getFullyQualifiedName()
						+ toClass.getFullyQualifiedName();
				result = fromClass;
			} else {
				key = toClass.getFullyQualifiedName()
						+ fromClass.getFullyQualifiedName();
				result = toClass;
			}
			if (cacheConnections.get(key) == null) {
				cacheConnections.put(key, result);
				return result;
			}
		}
		return null;
	}

	@Override
	public void classChanged(LighthouseClass aClass, TYPE type) {
		logger.info("classChanged: " + aClass.getShortName()+" ("+type+")");
		GraphItem item = viewer.findGraphItem(aClass);
		if (item != null) {
			switch (type) {
			case ADD:
				viewer.addNode(aClass);
				break;
			case MODIFY: case REMOVE:
				Animation.markBegin();
				GraphUtils.rebuildFigure((GraphNode) item);
				Animation.run(150);
				break;
/*			case REMOVE:
				viewer.removeNode(aClass);
				break;*/
			}
		} else {
			switch (type) {
			case ADD:
				viewer.addNode(aClass);				
				//new GraphNode(viewer.getGraphControl(), SWT.NONE, aClass);
				//viewer.refresh();
				//getLightweightSystem().getUpdateManager().performUpdate();
				break;
			}
		}
	}

	@Override
	public void modelChanged() {
		logger.info("modelChanged()");
		viewer.refresh();
	}

	@Override
	public void relationshipChanged(LighthouseRelationship relationship,
			TYPE type) {
		logger.info("relationshipChanged: "+relationship+" ("+type+")");
		GraphItem item = viewer.findGraphItem(relationship);
		if (item == null) {
			viewer.addRelationship(relationship);
		} else {
			viewer.removeRelationship(relationship);
			removeFromCache(relationship);
		}
	}	
	
	private void removeFromCache(LighthouseRelationship r){
		LighthouseModelManager manager = new LighthouseModelManager(
				LighthouseModel.getInstance());
		LighthouseClass fromClass = manager.getMyClass(r
				.getFromEntity());
		LighthouseClass toClass = manager.getMyClass( r
				.getToEntity());
		cacheConnections.remove(fromClass.getFullyQualifiedName()
						+ toClass.getFullyQualifiedName());
		cacheConnections.remove(toClass.getFullyQualifiedName()
				+ fromClass.getFullyQualifiedName());
	}
	
	private void createNodes(LighthouseModel model){
		logger.info("createNodes()");
		//LighthouseModel model = (LighthouseModel) viewer.getInput();
		for (LighthouseClass aClass : model.getAllClasses()) {
			viewer.addNode(aClass);
		}
		for (Iterator itNodes = viewer.getGraphControl().getNodes().iterator(); itNodes.hasNext();) {
			GraphNode node = (GraphNode) itNodes.next();
			node.setBackgroundColor(ColorFactory.classBackground);
			node.setHighlightColor(ColorFactory.classHighlight);
		}
	}
}