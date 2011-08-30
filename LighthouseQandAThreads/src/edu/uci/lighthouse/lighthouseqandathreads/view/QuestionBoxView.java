package edu.uci.lighthouse.lighthouseqandathreads.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import edu.uci.lighthouse.model.QAforums.ForumThread;
import edu.uci.lighthouse.model.QAforums.LHforum;
import edu.uci.lighthouse.model.QAforums.Post;
import edu.uci.lighthouse.model.QAforums.TeamMember;

public class QuestionBoxView extends ForumElement{
	final StyledText postNewThreadBox;
	private String message;
	private TeamMember tm;
	private LHforum forum;

	public QuestionBoxView(Composite parent, int style, LHforum forum, TeamMember tm) {
		super(parent, style);
		
		this.forum = forum;
		this.tm = tm;
		
		
		GridData postBoxCompoiteData = new GridData(LayoutMetrics.QUESTION_BOX_VIEW_WIDTH, LayoutMetrics.QUESTION_BOX_VIEW_HEIGHT);
		setLayout(new GridLayout(2, false));
		setLayoutData(postBoxCompoiteData);
		
		GridData postNewThreadBoxData = new GridData(450, 30);
		postNewThreadBox = new StyledText(this, SWT.BORDER);
		postNewThreadBox.setLayoutData(postNewThreadBoxData);
		
		postNewThreadBox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setMessage(postNewThreadBox.getText());

			}
		});
		
		Button postButton = new Button(this, SWT.BORDER);
		postButton.setText("post");
		postButton.addSelectionListener(new PostListener());
		
	}
	
	private class PostListener extends SelectionAdapter{
		public void widgetSelected(SelectionEvent e) {
			Post newPost = new Post(true, "", message, tm);
			forum.addThread(newPost);
			postNewThreadBox.setText("");
			
		}
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}


}