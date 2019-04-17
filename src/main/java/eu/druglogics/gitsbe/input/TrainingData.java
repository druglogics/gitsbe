package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import static eu.druglogics.gitsbe.util.Util.abort;
import static eu.druglogics.gitsbe.util.Util.readLinesFromFile;

public class TrainingData {

	private ArrayList<TrainingDataObservation> observations;
	private String filenameTrainingData;
	private Logger logger;

	public TrainingData(String filenameTrainingData, Logger logger) throws IOException {
		this.setFilenameTrainingData(filenameTrainingData);
		this.logger = logger;
		this.observations = new ArrayList<>();

		readData(filenameTrainingData);
	}

	/**
	 * Read training data from file
	 *
	 * @param filename
	 * @throws IOException
	 */
	private void readData(String filename) throws IOException {
		logger.outputStringMessage(3,
				"Reading training data observations file: " + new File(filename).getAbsolutePath());
		ArrayList<String> lines = readLinesFromFile(filename, true);

		// Define variables to be read from training data file

		ArrayList<String> condition = new ArrayList<>();
		ArrayList<String> observation = new ArrayList<>();
		float weight;

		// Process lines
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

	private void checkResponses(ArrayList<String> nodes, ArrayList<String> responses) {
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

	private void checkConditions(ArrayList<String> nodes, ArrayList<String> conditions) {
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

	public static void writeTrainingDataTemplateFile(String filename) throws IOException {
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		writer.println("# Gitsbe training data file");
		writer.println("# Each condition specified contains a condition given by drug");
		writer.println("# Each condition specified contains a set node states and/or a global output");
		writer.println();
		writer.println("Condition");
		writer.println("-");
		writer.println("Response");
		writer.println("Antisurvival:0	CASP3:0	Prosurvival:1	CCND1:1	MYC:1	CF7_f:1	NFKB_f:1");
		writer.println("Weight:1");
		writer.println("# Steady state observation across many publications");
		writer.println();
		writer.println("Condition");
		writer.println("CTNNB1:0");
		writer.println("Response");
		writer.println("globaloutput:0");
		writer.println("Weight:0.1");

		writer.flush();
		writer.close();
	}

	/**
	 * Returns maximum fitness determined by training data
	 * 
	 * @return maxFitness
	 */
	public float getMaxFitness() {
		float maxFitness;

		maxFitness = getWeightSum();
		// a fitness of +1 is given for a model with a stable state
		// per condition, thus max fitness must be increased
		maxFitness += 1;
		return maxFitness;
	}

	public float getWeightSum() {
		float weightsum = 0;

		for (TrainingDataObservation observation : observations) {
			weightsum += observation.getWeight();
		}
		return weightsum;
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

	public String getFilenameTrainingData() {
		return filenameTrainingData;
	}

	private void setFilenameTrainingData(String filenameTrainingData) {
		this.filenameTrainingData = filenameTrainingData;
	}
}
