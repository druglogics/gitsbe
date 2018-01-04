package gitsbe;

import static gitsbe.Util.*;

import static java.lang.Math.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ModelOutputs {

	private ArrayList<OutputWeight> modelOutputs;
	private Logger logger;

	public ModelOutputs(String filename, Logger logger) throws IOException {
		this.logger = logger;
		this.modelOutputs = new ArrayList<OutputWeight>();

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

		for (int i = 0; i < stableStates.size(); i++) {
			for (int j = 0; j < modelOutputs.size(); j++) {
				int indexStableState = model.getIndexOfEquation(modelOutputs.get(j).getName());
				if (indexStableState >= 0) {
					int temp = Character.getNumericValue(stableStates.get(i).charAt(indexStableState));
					temp *= modelOutputs.get(j).getWeight();
					globaloutput += temp;
				}
			}
		}

		globaloutput /= stableStates.size();

		return ((globaloutput - getMinOutput()) / (getMaxOutput() - getMinOutput() + 1));
	}

	public float getMaxOutput() {
		float maxOutput = 0;

		for (int i = 0; i < modelOutputs.size(); i++) {
			maxOutput += max(modelOutputs.get(i).getWeight(), 0);
		}

		return maxOutput;
	}

	public float getMinOutput() {
		float minOutput = 0;

		for (int i = 0; i < modelOutputs.size(); i++) {
			minOutput += min(modelOutputs.get(i).getWeight(), 0);
		}

		return minOutput;
	}

	public static void saveModelOutputsFileTemplate(String filename) throws IOException {
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		// Write header with '#'
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

	public void loadModelOutputsFile(String filename) throws IOException {
		logger.outputStringMessage(3,
				"Reading model outputs file: " + new File(filename).getAbsolutePath());
		
		ArrayList<String> lines = readLinesFromFile(filename);

		for (int i = 0; i < lines.size(); i++) {
			String temp[] = lines.get(i).split("\t");
			modelOutputs.add(new OutputWeight(temp[0].trim(), Integer.parseInt(temp[1].trim())));
		}
	}

	public String[] getModelOutputs() {
		ArrayList<String> lines = new ArrayList<String>();

		for (int index = 0; index < modelOutputs.size(); index++) {
			lines.add(modelOutputs.get(index).getName() + " with weight: " + modelOutputs.get(index).getWeight());
		}
		
		return (String[]) lines.toArray(new String[0]);
	}
}
