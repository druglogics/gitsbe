package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static eu.druglogics.gitsbe.util.Util.abort;
import static eu.druglogics.gitsbe.util.Util.readLinesFromFile;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class ModelOutputs {

	private ArrayList<OutputWeight> modelOutputs;
	private Logger logger;

	public ModelOutputs(String filename, Logger logger) throws IOException {
		this.logger = logger;
		this.modelOutputs = new ArrayList<>();

		loadModelOutputsFile(filename);
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
				int indexStableState = model.getIndexOfEquation(outputWeight.getName());
				if (indexStableState >= 0) {
					int temp = Character.getNumericValue(stableState.charAt(indexStableState));
					temp *= outputWeight.getWeight();
					globaloutput += temp;
				}
			}
		}

		globaloutput /= stableStates.size();

		return ((globaloutput - getMinOutput()) / (getMaxOutput() - getMinOutput()));
	}

	private float getMaxOutput() {
		float maxOutput = 0;

		for (OutputWeight outputWeight : modelOutputs) {
			maxOutput += max(outputWeight.getWeight(), 0);
		}

		return maxOutput;
	}

	private float getMinOutput() {
		float minOutput = 0;

		for (OutputWeight outputWeight : modelOutputs) {
			minOutput += min(outputWeight.getWeight(), 0);
		}

		return minOutput;
	}

	public static void saveModelOutputsFileTemplate(String filename) throws IOException {
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		writer.println("# File for defining model outputs");
		writer.println("# Model outputs specified must match names used in model definition");
		writer.println("# ");
		writer.println("# Use tab-separated columns");
		writer.println("# Name: Node name");
		writer.println("# Weight: Signed integer used for calculating model simulation output.");
		writer.println("# ");
		writer.println("# Name\tWeight");
		writer.println("RPS6KA1\t1");
		writer.println("MYC\t1");
		writer.println("TCF7\t1");
		writer.println("CASP8\t-1");
		writer.println("CASP9\t-1");
		writer.println("FOXO3\t-1");

		writer.flush();
		writer.close();
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
			lines.add(outputWeight.getName() + " with weight: " + outputWeight.getWeight());
		}

		return lines.toArray(new String[0]);
	}

	private ArrayList<String> getNodeNames() {
		ArrayList<String> nodeNames = new ArrayList<>();

		for (OutputWeight outputWeight : this.modelOutputs) {
			nodeNames.add(outputWeight.getName());
		}

		return nodeNames;
	}
}
