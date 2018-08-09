package eu.druglogics.gitsbe.input;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author asmund
 * 
 */
public class TrainingDataObservation {

	// Array of strings describing perturbed nodes in observation
	private ArrayList<String> condition;
	private ArrayList<String> response;
	private float weight;

	public TrainingDataObservation(ArrayList<String> condition, ArrayList<String> response, float weight) {
		this.condition = condition;
		this.response = response;
		this.weight = weight;
	}

	public String getData() {
		String result = "";

		result += "Condition: " + Arrays.toString(this.condition.toArray(new String[0]));
		result += "Response: " + Arrays.toString(this.response.toArray(new String[0]));
		result += "Weight: " + weight;

		return result;
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

	public int getResponseSize() {
		int size = 0;

		for (int i = 0; i < response.size(); i++) {
			if (!response.get(i).split(":")[1].equals("-")) // skip specified unknown observations
				size++;
		}
		return size;
	}
}
