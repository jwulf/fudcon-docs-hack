package com.redhat.contentspec.client.entities;

import java.util.ArrayList;
import java.util.List;

public class RevisionList {
	private List<Revision> revisions;
	private int count = 0;
	private Integer id;
	private String modeName;
	
	public RevisionList() {
		revisions = new ArrayList<Revision>();
	}
	
	public RevisionList(int id, String modeName) {
		this.setId(id);
		this.setModeName(modeName);
		revisions = new ArrayList<Revision>();
	}
	
	public RevisionList(int id, String modeName, List<Revision> revisions) {
		this.setId(id);
		this.setModeName(modeName);
		this.revisions = revisions;
		count = revisions.size();
	}
	
	public RevisionList(int id, String modeName, int count) {
		this.setId(id);
		this.setModeName(modeName);
		revisions = new ArrayList<Revision>();
		this.count = count;
	}
	
	public RevisionList(int id, String modeName, List<Revision> revisions, int count) {
		this.setId(id);
		this.setModeName(modeName);
		this.revisions = revisions;
		this.count = count;
	}

	public List<Revision> getRevisions() {
		return revisions;
	}
	
	public void addRevision(Revision rev) {
		revisions.add(rev);
		count++;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public String toString() {
		String output = "INFO: Revisions for " + modeName + " ID: " + id + "\n";
		for (Revision rev: revisions) {
			output += rev.toString() + "\n";
		}
		return output;
	}

	public String getModeName() {
		return modeName;
	}

	public void setModeName(String modeName) {
		this.modeName = modeName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
