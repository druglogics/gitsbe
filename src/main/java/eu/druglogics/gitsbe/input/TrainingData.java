package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static eu.druglogics.gitsbe.util.Util.abort;
import static eu.druglogics.gitsbe.util.Util.readLinesFromFile;

public class TrainingData {

	private ArrayList<TrainingDataObservation> observations;
	private Logger logger;

	public TrainingData(String filename, Logger logger) throws IOException {
		this.logger = logger;
		this.observations = new ArrayList<>();

		loadTrainingDataFile(filename);
	}

	/**
	 * Read training data from file
	 *
	 * @param filename
	 * @throws IOException
	 */
	private void loadTrainingDataFile(String filename) throws IOException {
		logger.outputStringMessage(3,
				"Reading training data observations file: " + new File(filename).getAbsolutePath());
		ArrayList<String> lines = readLinesFromFile(filename, true);

		ArrayList<String> condition = new ArrayList<>();
		ArrayList<String> observation = new ArrayList<>();
		float weight;

		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).toLowerCase().equals("condition")) {
				condition = new ArrayList<>(Arrays.asList(lines.get(i + 1).split("\t")));
				i++;
			}
			if (lines.get(i).toLowerCase().equals("response")) {
				observation = new ArrayList<>(Arrays.asList(lines.get(i + 1).split("\t")));
				i++;
			}
			if (lines.get(i).toLowerCase().startsWith("weight")) {
				weight = Float.parseFloat(lines.get(i).split(":")[1]);
				observations.add(new TrainingDataObservation(condition, observation, weight));
			}
		}
	}

	/**
	 * Adds warnings (errors) to the log if there are node names in the responses
	 * (conditions) that are not defined in the model/network topology
	 *
	 */
	public void checkTrainingDataConsistency(BooleanModel booleanModel) {
		logger.outputHeader(3, "Checking Training Data");

		ArrayList<String> nodes = booleanModel.getNodeNames();

		for (TrainingDataObservation observation : this.observations) {
			ArrayList<String> conditions = observation.getCondition();
			ArrayList<String> responses = observation.getResponse();

			checkConditions(nodes, conditions);
			checkResponses(nodes, responses);
		}
	}

	void checkResponses(ArrayList<String> nodes, ArrayList<String> responses) {
		for (String response : responses) {
			if (!response.split(":")[0].equals("globaloutput")) {
				String nodeName = response.split(":")[0];
				if (!nodes.contains(nodeName)) {
					logger.outputStringMessage(3, "Warning: Node " + nodeName
							+ " defined in response " + response + " is not in network file.");
				}
			}
		}
	}

	void checkConditions(ArrayList<String> nodes, ArrayList<String> conditions) {
		for (String condition : conditions) {
			if (!condition.equals("-")) {
				String nodeName = condition.split(":")[0];
				if (!nodes.contains(nodeName)) {
					logger.error("Node " + nodeName + " defined in condition " + condition
							+ " is not in network file.");
					abort();
				}
			}
		}
	}

	public float getWeightSum() {
		float sum = 0;

		for (TrainingDataObservation observation : observations) {
			sum += observation.getWeight();
		}
		return sum;
	}

	public int size() {
		return observations.size();
	}

	public ArrayList<TrainingDataObservation> getObservations() {
		return observations;
	}

	public String[] getTrainingDataVerbose() {

		int size = observations.size();
		int observationNumber;
		String[] result = new String[size * 5];

		for (int i = 0; i < size; i++) {
			observationNumber = i + 1;
			result[i * 5] = "Observation " + (observationNumber) + ":";
			result[i * 5 + 1] = "Condition: "
					+ Arrays.toString(observations.get(i).getCondition().toArray(new String[0]));
			result[i * 5 + 2] = "Response: "
					+ Arrays.toString(observations.get(i).getResponse().toArray(new String[0]));
			result[i * 5 + 3] = "Weight: " + observations.get(i).getWeight();
			result[i * 5 + 4] = "";
		}

		return result;
	}
}
