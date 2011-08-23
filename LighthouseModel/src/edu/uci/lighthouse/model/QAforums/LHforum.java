package edu.uci.lighthouse.model.QAforums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import edu.uci.lighthouse.LHmodelExtensions.LHclassPluginExtension;

@Entity
public class LHforum extends LHclassPluginExtension implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -8791302461228282266L;

	@Id
    @GeneratedValue
    int id;
    
    
	ArrayList<ForumThread> threads = new ArrayList<ForumThread>();
	
	public void addThread(Post rootPost){
		ForumThread thread = new ForumThread(rootPost);
		addThread(thread);
	}
	public void addThread(ForumThread thread){
		threads.add(thread);
		forumChanged();
	}
	
	public List<ForumThread> getThreads(){
		return threads;
	}
	
	private void forumChanged(){
        setChanged();
        notifyObservers();
        clearChanged();
	}
}