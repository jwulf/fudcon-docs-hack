package com.redhat.contentspec.client.entities;

import java.util.ArrayList;
import java.util.List;

public class SpecList {

	private List<Spec> specs;
	private long count = 0;
	
	public SpecList() {
		specs = new ArrayList<Spec>();
	}
	
	public SpecList(List<Spec> specs) {
		this.specs = specs;
		count = specs.size();
	}
	
	public SpecList(long count) {
		specs = new ArrayList<Spec>();
		this.count = count;
	}
	
	public SpecList(List<Spec> specs, long count) {
		this.specs = specs;
		this.count = count;
	}
	
	public List<Spec> getSpecs() {
		return specs;
	}
	
	public void addSpec(Spec spec) {
		specs.add(spec);
		count++;
	}

	public long getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public String toString() {
		String output = "Number of Content Specifications: " + count + "\n";
		for (Spec spec: specs) {
			output += spec.toString() + "\n";
		}
		return output;
	}
}
