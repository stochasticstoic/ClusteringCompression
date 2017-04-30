package com.tecacet.intellijence.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomCenterSelector<T> implements CenterSelector<T> {

	private final Random random;

	public RandomCenterSelector() {
		this(new Random());
	}

	public RandomCenterSelector(Random generator) {
		this.random = generator;
	}

	public List<T> chooseInitialCenters(List<T> dataPoints, int clusters) {
		if (dataPoints.size() < clusters) {
			throw new IllegalArgumentException("There are fewer points than clusters!");
		}
		Set<Integer> indexes = new HashSet<Integer>(clusters);
		while (indexes.size() < clusters) {
			indexes.add(random.nextInt(dataPoints.size()));
		}
		ArrayList<T> centers = new ArrayList<T>(clusters);
		for (int index : indexes) {
			centers.add(dataPoints.get(index));
		}
		return centers;
	}

}
