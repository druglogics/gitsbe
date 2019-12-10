package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static eu.druglogics.gitsbe.util.Util.abort;
import static eu.druglogics.gitsbe.util.Util.readLinesFromFile;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class ModelOutputs {

	private static ModelOutputs modeloutputs = null;

	private ArrayList<OutputWeight> modelOutputs;
	private float minOutput;
	private float maxOutput;
	private Logger logger;

	private ModelOutputs(String filename, Logger logger) throws IOException {
		this.logger = logger;
		this.modelOutputs = new ArrayList<>();

		loadModelOutputsFile(filename);

		this.minOutput = getMinOutput();
		this.maxOutput = getMaxOutput();
	}

	public static ModelOutputs getInstance() {
		// To ensure only one instance is created
		if (modeloutputs == null) {
			throw new AssertionError("You have to call init first to initialize the ModelOutputs Class");
		}
		return modeloutputs;
	}

	public synchronized static void init(String filename, Logger logger) throws Exception {
		if (modeloutputs != null) {
			throw new AssertionError("You already initialized me");
		}
		modeloutputs = new ModelOutputs(filename, logger);
	}

	public int size() {
		return modelOutputs.size();
	}

	public OutputWeight get(int index) {
		return modelOutputs.get(index);
	}

	public float calculateGlobalOutput(ArrayList<String> stableStates, BooleanModel model) {
		float globaloutput = 0;

		for (String stableState : stableStates) {
			for (OutputWeight outputWeight : modelOutputs) {
				int indexStableState = model.getIndexOfEquation(outputWeight.getNodeName());
				if (indexStableState >= 0) {
					int temp = Character.getNumericValue(stableState.charAt(indexStableState));
					temp *= outputWeight.getWeight();
					globaloutput += temp;
				}
			}
		}

		globaloutput /= stableStates.size();

		return ((globaloutput - minOutput) / (maxOutput - minOutput));
	}

	/**
	 *
	 * @return the sum of all the positive weights
	 */
	float getMaxOutput() {
		float maxOutput = 0;

		for (OutputWeight outputWeight : modelOutputs) {
			maxOutput += max(outputWeight.getWeight(), 0);
		}

		return maxOutput;
	}

	/**
	 *
	 * @return the sum of all the negative weights
	 */
	float getMinOutput() {
		float minOutput = 0;

		for (OutputWeight outputWeight : modelOutputs) {
			minOutput += min(outputWeight.getWeight(), 0);
		}

		return minOutput;
	}

	private void loadModelOutputsFile(String filename) throws IOException {
		logger.outputStringMessage(3, "Reading model outputs file: "
				+ new File(filename).getAbsolutePath());

		ArrayList<String> lines = readLinesFromFile(filename, true);

		for (String line : lines) {
			String[] temp = line.split("\t");
			modelOutputs.add(new OutputWeight(temp[0].trim(), Integer.parseInt(temp[1].trim())));
		}
	}

	/**
	 * Adds errors to the log if there are node names that are not defined in the
	 * model/network topology
	 * 
	 * @param booleanModel
	 */
	public void checkModelOutputNodeNames(BooleanModel booleanModel) {
		logger.outputHeader(3, "Checking Model Output node names");

		ArrayList<String> nodes = booleanModel.getNodeNames();

		for (String nodeName : this.getNodeNames()) {
			if (!nodes.contains(nodeName)) {
				logger.error("Node " + nodeName + " is not in network file.");
				abort();
			}
		}
	}

	/**
	 * A verbose representation of the model outputs used for logging purposes
	 *
	 */
	public String[] getModelOutputs() {
		ArrayList<String> lines = new ArrayList<>();

		for (OutputWeight outputWeight : modelOutputs) {
			lines.add(outputWeight.getNodeName() + " with weight: " + outputWeight.getWeight());
		}

		return lines.toArray(new String[0]);
	}

	ArrayList<String> getNodeNames() {
		ArrayList<String> nodeNames = new ArrayList<>();

		for (OutputWeight outputWeight : this.modelOutputs) {
			nodeNames.add(outputWeight.getNodeName());
		}

		return nodeNames;
	}
}
