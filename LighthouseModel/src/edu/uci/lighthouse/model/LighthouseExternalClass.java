package edu.uci.lighthouse.model;

import javax.persistence.Entity;

@Entity
public class LighthouseExternalClass extends LighthouseEntity {

	protected LighthouseExternalClass() {
	}
	
	public LighthouseExternalClass(String fqn) {
		super(fqn);
	}

}
