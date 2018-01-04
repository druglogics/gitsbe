package gitsbe;

import static gitsbe.Util.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class TrainingData {

	private ArrayList<TrainingDataObservation> observations;
	private String filenameTrainingData;
	private Logger logger;

	public TrainingData(String filenameTrainingData, Logger logger) throws IOException {
		this.setFilenameTrainingData(filenameTrainingData);
		this.logger = logger;
		this.observations = new ArrayList<TrainingDataObservation>();

		readData(filenameTrainingData);
	}

	/**
	 * Read training data from file
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void readData(String filename) throws IOException {
		logger.outputStringMessage(3,
				"Reading training data observations file: " + new File(filename).getAbsolutePath());
		ArrayList<String> lines = readLinesFromFile(filename);

		// Define variables to be read from training data file

		ArrayList<String> condition = new ArrayList<String>();
		ArrayList<String> observation = new ArrayList<String>();
		float weight = 0;

		// Process lines
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).toLowerCase().equals("condition")) {
				condition = new ArrayList<String>(Arrays.asList(lines.get(i + 1).split("\t")));
				i++;
			}

			if (lines.get(i).toLowerCase().equals("response")) {
				observation = new ArrayList<String>(Arrays.asList(lines.get(i + 1).split("\t")));
				i++;
			}
			if (lines.get(i).toLowerCase().startsWith("weight")) {
				weight = Float.parseFloat(lines.get(i).split(":")[1]);
				observations.add(new TrainingDataObservation(condition, observation, weight));
			}
		}

	}

	public static void writeTrainingDataTemplateFile(String filename) throws IOException {
		PrintWriter writer = new PrintWriter(filename, "UTF-8");
		
		// Write header with '#'
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
		float maxFitness = 0;

		maxFitness = getWeightSum();
		maxFitness += 1; // a fitness of +1 is given for a model with a stable state per condition, thus
							// max fitness must be increased

		return maxFitness;
	}

	public float getWeightSum() {
		float weightsum = 0;

		for (int i = 0; i < observations.size(); i++) {
			weightsum += observations.get(i).getWeight();
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
			result[i * 5 + 3] = "Weight: " + Float.toString(observations.get(i).getWeight());
			result[i * 5 + 4] = "";
		}

		return result;

	}

	public String getFilenameTrainingData() {
		return filenameTrainingData;
	}

	public void setFilenameTrainingData(String filenameTrainingData) {
		this.filenameTrainingData = filenameTrainingData;
	}
}
