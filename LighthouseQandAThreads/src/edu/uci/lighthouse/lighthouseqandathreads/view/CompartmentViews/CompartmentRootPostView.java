package edu.uci.lighthouse.lighthouseqandathreads.view.CompartmentViews;

import java.util.ArrayList;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.uci.lighthouse.lighthouseqandathreads.PersistAndUpdate;
import edu.uci.lighthouse.model.QAforums.ForumThread;
import edu.uci.lighthouse.model.QAforums.TeamMember;
import edu.uci.lighthouse.ui.utils.GraphUtils;

public class CompartmentRootPostView extends Panel {

	private int displayLength = 22;
	private int NUM_COLUMNS = 1;
	private Label messageLabel;
	private String prefix = "? ";
	private Shell treadShell;
	private ForumThread thread;
	private TeamMember tm;
	private PersistAndUpdate pu;
	private CompartmentThreadView view;
	
	public CompartmentRootPostView(String message,  ForumThread thread, TeamMember tm, PersistAndUpdate pu) {

		this.tm = tm;
		this.thread = thread;
		this.pu = pu;
		
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.numColumns = NUM_COLUMNS;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		setLayoutManager(layout);
		
		String author = thread.getRootQuestion().getTeamMemberAuthor().getAuthor().getName();
		
		GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);

		messageLabel = new Label(message.length() >= displayLength ? ""+author + ": " + message.substring(0,displayLength) +"...": ""+author + ": " + message);
		this.add(messageLabel);

		PostMouseMotionListener pl = new PostMouseMotionListener();

		this.addMouseMotionListener(pl);
		messageLabel.addMouseMotionListener(pl);
		
		
	}
	
	public void updateView(){
		view.updateView();
	}

	private class PostMouseMotionListener extends MouseMotionListener.Stub {
		public void mouseHover(MouseEvent me) {
			treadShell = new Shell(GraphUtils.getGraphViewer()
					.getGraphControl().getDisplay().getActiveShell(),
					SWT.NO_TRIM | SWT.DRAG | SWT.RESIZE);

			Point location = me.getLocation();
			org.eclipse.swt.graphics.Point point = GraphUtils.getGraphViewer()
					.getGraphControl().toDisplay(location.x, location.y);


			treadShell.setLocation(point);
			treadShell.addMouseTrackListener(new ThreadTrackListener());
			
			treadShell.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
			
			
			
			
			view = new CompartmentThreadView(treadShell,SWT.None, thread,tm,pu);
			
			
			treadShell.setSize(treadShell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			
			treadShell.open();
			

		
		}

		public void mouseExited(MouseEvent e) {

			
		}

		public void mouseEntered(MouseEvent me) {
			//CompartmentPostView.this.setBackgroundColor(ColorConstants.yellow);


		}
	}
	

	
	private class ThreadTrackListener implements MouseTrackListener {
		
		public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
		}
		public void mouseExit(org.eclipse.swt.events.MouseEvent e) {
			org.eclipse.swt.graphics.Point point = new org.eclipse.swt.graphics.Point(e.x,e.y);
	
			
			
			
			for(Control control : treadShell.getChildren()){
				if(containsPoint(control,point))
					return;
			}
			
			if(!containsPoint(treadShell,point)){
				treadShell.close();
			}
			
		}
		
		
		/**
		 * Counts bounder width as well
		 * 
		 * @param control
		 * @param point
		 * @return
		 */
		public boolean containsPoint(Control control, org.eclipse.swt.graphics.Point point){
			Rectangle bounds = control.getBounds();
			int borderWidth = control.getBorderWidth();
			
			bounds.height = bounds.height+borderWidth;
			bounds.width = bounds.width+borderWidth;
			
			return bounds.contains(point);
		}
		
		public void mouseHover(org.eclipse.swt.events.MouseEvent e) {
		}
	
	}
}