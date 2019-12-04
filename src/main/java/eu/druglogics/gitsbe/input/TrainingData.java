package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;
import org.apache.commons.lang3.StringUtils;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.druglogics.gitsbe.util.Util.isNumericString;
import static eu.druglogics.gitsbe.util.Util.readLinesFromFile;

public class TrainingData {

	private ArrayList<TrainingDataObservation> observations;
	private Logger logger;

	public TrainingData(String filename, Logger logger) throws IOException, ConfigurationException {
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
	private void loadTrainingDataFile(String filename) throws IOException, ConfigurationException {
		logger.outputStringMessage(3,
				"Reading training data observations file: " + new File(filename).getAbsolutePath());
		ArrayList<String> lines = readLinesFromFile(filename, true);

		ArrayList<String> condition = new ArrayList<>();
		ArrayList<String> response = new ArrayList<>();
		float weight;

		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).toLowerCase().equals("condition")) {
				condition = new ArrayList<>(Arrays.asList(lines.get(++i).split("\t")));
			}

			if (lines.get(i).toLowerCase().equals("response")) {
				response = new ArrayList<>(Arrays.asList(lines.get(++i).split("\t")));
				if (response.get(0).contains("globaloutput")) {
					if (!response.get(0).contains(":")) {
						throw new ConfigurationException("Response: `" + response.get(0) + "` does not contain `:`");
					}
					String value = response.get(0).split(":")[1];
					if (!isNumericString(value))
						throw new ConfigurationException("Response: `" + response.get(0)
							+ "` has a non-numeric value: " + value);

					float globaloutputValue = Float.parseFloat(value);
					if (globaloutputValue < -1.0 || globaloutputValue > 1.0)
						throw new ConfigurationException("Response has `globaloutput` "
							+ "outside the [-1,1] range: " + globaloutputValue);
				} else {
					for (String res : response) {
						if (!res.contains(":")) {
							throw new ConfigurationException("Response: `" + res + "` does not contain `:`");
						}
						String[] responseStr = res.split(":");
						String nodeName = responseStr[0];
						String value = responseStr[1];
						if (!isNumericString(value))
							throw new ConfigurationException("Node `" + nodeName
								+ "` has a non-numeric value: " + value);

						float nodeValue = Float.parseFloat(value);
						if (nodeValue < 0.0 || nodeValue > 1.0)
							throw new ConfigurationException("Node `" + nodeName
								+ "` has value outside the [0,1] range: " + nodeValue);
					}
				}
			}

			if (lines.get(i).toLowerCase().startsWith("weight")) {
				weight = Float.parseFloat(lines.get(i).split(":")[1]);
				observations.add(new TrainingDataObservation(condition, response, weight));
			}
		}
	}

	/**
	 * Adds warnings to the log if there are node names in the responses
	 * that are not defined in the model/network topology.
	 * Performs several configuration checks and throws exceptions were appropriate.
	 *
	 * @param booleanModel A BooleanModel, whose nodes we are going to use to check for
	 *                     inconsistencies in the training data conditions & responses
	 * @throws ConfigurationException
	 */
	public void checkTrainingDataConsistency(BooleanModel booleanModel) throws ConfigurationException {
		logger.outputHeader(3, "Checking Training Data");

		ArrayList<String> nodes = booleanModel.getNodeNames();

		for (TrainingDataObservation observation : this.observations) {
			ArrayList<String> conditions = observation.getCondition();
			ArrayList<String> responses = observation.getResponse();

			checkConditions(nodes, conditions);
			checkResponses(nodes, responses);
		}
	}

	void checkResponses(ArrayList<String> nodes, ArrayList<String> responses) throws ConfigurationException {
		for (String response : responses) {
			if (!response.contains(":")) {
				throw new ConfigurationException("Response: `" + response + "` does not contain `:`");
			}
			if (!response.split(":")[0].equals("globaloutput")) {
				String nodeName = response.split(":")[0];
				if (!nodes.contains(nodeName)) {
					logger.outputStringMessage(3, "Warning: Node `" + nodeName
							+ "` defined in response `" + response + "` is not in network file.");
				}
			}
		}
	}

	void checkConditions(ArrayList<String> nodes, ArrayList<String> conditions) throws ConfigurationException {
		if (conditions.size() == 1) {
			String condition = conditions.get(0);
			if (!condition.equals("-") && !condition.startsWith("Drug") && !condition.contains(":")) {
				throw new ConfigurationException("Only one condition defined: `" + condition
					+ "` that has neither `-`, `:` or starts with `Drug`");
			}

			if (condition.startsWith("Drug")) {
				int count = StringUtils.countMatches(condition, "Drug");
				if (count == 1) {
					Pattern singleDrugPattern = Pattern.compile("^Drug\\((.*)\\)$");
					Matcher singleDrugMatcher = singleDrugPattern.matcher(condition);
					if (singleDrugMatcher.find() && singleDrugMatcher.groupCount() == 1) {
						String drug = singleDrugMatcher.group(1);
						// check drug is in the drugpanel
					} else {
						throw new ConfigurationException("Wrong format: `" + condition + "`");
					}
				} else if (count == 3) {
					if (condition.contains("< min(Drug")) { // HSA
						Pattern twoDrugsHSAPattern = Pattern.compile("^Drug\\((.*)\\+(.*)\\) < min\\(Drug\\((.*)\\),Drug\\((.*)\\)\\)$");
						Matcher twoDrugsHSAMatcher = twoDrugsHSAPattern.matcher(condition);
						if (twoDrugsHSAMatcher.find() && twoDrugsHSAMatcher.groupCount() == 4) {
							String firstDrugInComb  = twoDrugsHSAMatcher.group(1);
							String secondDrugInComb = twoDrugsHSAMatcher.group(2);
							String firstDrugAlone   = twoDrugsHSAMatcher.group(3);
							String secondDrugAlone  = twoDrugsHSAMatcher.group(4);
							if(!firstDrugAlone.equals(firstDrugInComb) || !secondDrugAlone.equals(secondDrugInComb))
								throw new ConfigurationException("In condition: `" + condition
									+ "` drug names don't match");
							// check that drugs are in the drugpanel
						} else {
							throw new ConfigurationException("Wrong format: `" + condition + "`");
						}
					} else if (condition.contains("< product(Drug")) { // Bliss
						Pattern twoDrugsBlissPattern = Pattern.compile("^Drug\\((.*)\\+(.*)\\) < product\\(Drug\\((.*)\\),Drug\\((.*)\\)\\)$");
						Matcher twoDrugsBlissMatcher = twoDrugsBlissPattern.matcher(condition);
						if (twoDrugsBlissMatcher.find() && twoDrugsBlissMatcher.groupCount() == 4) {
							String firstDrugInComb  = twoDrugsBlissMatcher.group(1);
							String secondDrugInComb = twoDrugsBlissMatcher.group(2);
							String firstDrugAlone   = twoDrugsBlissMatcher.group(3);
							String secondDrugAlone  = twoDrugsBlissMatcher.group(4);
							if(!firstDrugAlone.equals(firstDrugInComb) || !secondDrugAlone.equals(secondDrugInComb))
								throw new ConfigurationException("In condition: `" + condition
									+ "` drug names don't match");
							// check that drugs are in the drugpanel
						} else {
							throw new ConfigurationException("Wrong format: `" + condition + "`");
						}
					} else {
						throw new ConfigurationException("Condition: `" + condition
							+ "` has neither of the strings: `< min(Drug` or `< product(Drug`");
					}
				} else {
					throw new ConfigurationException("Neither 1 nor 3 `Drug` keywords "
						+ "in condition: `" + condition + "`");
				}

				return;
			}
		}

		for (String condition : conditions) {
			if (!condition.equals("-")) {
				String nodeName = condition.split(":")[0];
				if (!nodes.contains(nodeName)) {
					throw new ConfigurationException("Node `" + nodeName + "` defined in condition `"
						+ condition + "` is not in network file.");
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
