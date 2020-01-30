package eu.druglogics.gitsbe.input;

import java.util.ArrayList;

/**
 * @author asmund
 * 
 */
public class TrainingDataObservation {

	// Array of strings describing perturbed nodes in observation
	private ArrayList<String> condition;
	private ArrayList<String> response;
	private float weight;

	TrainingDataObservation(ArrayList<String> condition, ArrayList<String> response, float weight) {
		this.condition = condition;
		this.response = response;
		this.weight = weight;
	}

	public ArrayList<String> getCondition() {
		return condition;
	}

	public ArrayList<String> getResponse() {
		return response;
	}

	public float getWeight() {
		return weight;
	}

}
