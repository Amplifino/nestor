package com.amplifino.nestor.bundles.rest;

import java.util.ArrayList;
import java.util.List;

class DependencyWheel {

	public List<String> packageNames = new ArrayList<>();
	public int[][] matrix;

	private DependencyWheel(List<String> names) {
		this.packageNames = names;
		initMatrix();
	}

	static DependencyWheel of(List<String> names) {
		return new DependencyWheel(names);
	}
	private void initMatrix() {
		matrix = new int[packageNames.size()][];
		for (int i = 0; i < packageNames.size(); i++) {
			matrix[i] = new int[packageNames.size()];
		}
	}
		
	void setDependency(String from, String to) {
		matrix[packageNames.indexOf(from)][packageNames.indexOf(to)] = 1;
	} 
	
	void addDependencies(DependencyWheel other) {
		assert(matrix.length == other.matrix.length);
		for(int i = 0 ; i < packageNames.size() ; i++) {
			for (int j = 0 ; j < packageNames.size() ; j++) {
				if (other.matrix[i][j] == 1) {
					this.matrix[i][j] = 1;
				}
			}
		}
	}
}
