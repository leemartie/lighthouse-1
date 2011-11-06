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
package edu.uci.lighthouse.model.QAforums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;

import edu.uci.lighthouse.LHmodelExtensions.LHclassPluginExtension;
import edu.uci.lighthouse.model.jpa.AbstractDAO;

@Entity
public class Post extends Observable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8635902581163644158L;

	@Id
	@GeneratedValue
	int id;

	private String subject;
	private boolean question;
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<Post> responses = new HashSet<Post>();
	private String message = "";
	@ManyToOne(cascade = CascadeType.ALL)
	private TeamMember author;

	private boolean root;
	private boolean answer;
	@ManyToOne(cascade = CascadeType.ALL)
	private ForumThread thread;

	private String postTime;

	public Post() {
	}

	public Post(boolean isQuestion, String subject, String message,
			TeamMember author) {
		this.subject = subject;
		this.question = isQuestion;
		this.message = message;
		this.author = author;
		postTime = AbstractDAO.getTimeOnServer();
	}

	private void observeResponses() {
		for (Post post : responses) {
		}
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setAuthor(TeamMember author) {
		this.author = author;
	}

	public TeamMember getTeamMemberAuthor() {
		return author;
	}

	public void setQuestion(boolean question) {
		this.question = question;
	}

	public boolean isQuestion() {
		return question;
	}

	public void setResponses(Set<Post> responses) {
		this.responses = responses;
		observeResponses();
	}

	public Set<Post> getResponses() {
		return responses;
	}

	public void addResponse(Post post) {
		responses.add(post);
		post.setThread(this.getThread());
		PostChanged(new AddEvent<Post, Post>(post, this));
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	private void PostChanged(Object object) {
		setChanged();
		notifyObservers(object);
		clearChanged();
	}

	public void setRoot(boolean root) {
		this.root = root;
		try {
			PostChanged(new UpdateEvent<Post>(this, this.getClass()
					.getDeclaredField("root")));
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isRoot() {
		return root;
	}
	
	/**
	 * The policy is if that only the TeamMember that asked the original post
	 * can set a reply as the answer to it.  The check is done by testing if the TeamMember's
	 * name is equal to the TeamMember's name of the original post. For this to work
	 * there needs to be unique TeamMember name enforcement. 
	 * @param tm
	 * @return
	 */
	public boolean canSetAsAnswer(TeamMember tm){
		return thread.getRootQuestion().getTeamMemberAuthor().getAuthor().getName().equals(tm.getAuthor().getName());
	}
		

	public void setAnswer(boolean answer, TeamMember tm) {

		if (canSetAsAnswer(tm)) {
			this.answer = answer;
			try {
				PostChanged(new UpdateEvent<Post>(this, this.getClass()
						.getDeclaredField("answer")));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (answer) {
				Solution solution = new Solution();
				solution.setAnswer(this);
				solution.setQuestion(thread.getRootQuestion());
				thread.setSolution(solution);
			}
		}
	}

	public boolean isAnswer() {
		return answer;
	}

	public void setThread(ForumThread thread) {
		this.thread = thread;
	}

	public ForumThread getThread() {
		return thread;
	}

	public void setPostTime(String postTime) {
		this.postTime = postTime;
	}

	public String getPostTime() {
		return postTime;
	}

	/**
	 * In Ascending order most recent to oldest post
	 * 
	 * @return
	 */
	public List<Post> orderResponses() {

		int i;
		int min;

		ArrayList<Post> listOfResponses = new ArrayList<Post>();
		listOfResponses.addAll(this.responses);

		for (i = 0; i < this.responses.size(); i++) {
			min = i;
			for (int j = i + 1; j < this.responses.size(); j++) {
				if (listOfResponses.get(j).getPostTime()
						.compareTo(listOfResponses.get(min).getPostTime()) < 0) {
					min = j;
				}
			}

			Post temp = listOfResponses.get(i);
			listOfResponses.set(i, listOfResponses.get(min));
			listOfResponses.set(min, temp);
		}

		return listOfResponses;
	}

}
