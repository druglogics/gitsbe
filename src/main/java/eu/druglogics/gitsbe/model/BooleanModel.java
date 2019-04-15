package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.util.Logger;

import static eu.druglogics.gitsbe.util.Util.*;
import static eu.druglogics.gitsbe.util.FileDeleter.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * 
 * @author Asmund Flobak, email: asmund.flobak@ntnu.no
 *
 */
public class BooleanModel {

	protected ArrayList<BooleanEquation> booleanEquations;
	ArrayList<String[]> mapAlternativeNames;
	protected ArrayList<String> stableStates;
	protected String modelName;
	private String filename;
	Logger logger;
	private static String directoryBNET = System.getenv("BNET_HOME");

	public BooleanModel(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Constructor for defining Boolean model from a "general model"
	 * made up of singleInteraction objects
	 *
	 * Note that generalModel.buildMultipleInteractions() must be
	 * executed beforehand
 	 */
	public BooleanModel(GeneralModel generalModel, Logger logger) {

		this.logger = logger;
		this.modelName = generalModel.getModelName();
		this.booleanEquations = new ArrayList<>();
		this.mapAlternativeNames = new ArrayList<>();
		this.stableStates = new ArrayList<>();

		for (int i = 0; i < generalModel.size(); i++) {
			// Define Boolean equation from multiple interaction
			BooleanEquation booleanEquation = new BooleanEquation(generalModel.getMultipleInteraction(i));

			// Build list of alternative names used for Veliz-Cuba bnet stable states
			// computation (x1, x2, x3, ..., xn)

			String[] temp = new String[2];
			temp[0] = generalModel.getMultipleInteraction(i).getTarget();
			temp[1] = "x" + (i + 1);

			mapAlternativeNames.add(temp);

			// Add Boolean equation to ArrayList, with index corresponding to
			// mapAlternativeNames
			booleanEquations.add(booleanEquation);
		}
	}

	// Copy constructor for defining Boolean model from another Boolean model

	/**
	 * Constructor for defining Boolean model from a file with a set of Boolean
	 * equations
	 *
	 * Currently two supported filetypes: .gitsbe and .booleannet files
	 *
	 * @param filename
	 */
	public BooleanModel(String filename, Logger logger) {
		this.logger = logger;
		ArrayList<String> lines;

		logger.outputStringMessage(1, "Loading Boolean model from file: " + filename);

		try {
			lines = readLinesFromFile(filename, true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		String fileExtension = getFileExtension(filename);

		switch (fileExtension) {
			case ".gitsbe":
				this.booleanEquations = new ArrayList<>();
				this.mapAlternativeNames = new ArrayList<>();

				for (String line : lines) {
					String prefix = line.substring(0, line.indexOf(' '));
					String value = line.substring(line.indexOf(' '));
					switch (prefix) {
						case "modelname:":
							this.modelName = value.trim();
							break;
						case "equation:":
							booleanEquations.add(new BooleanEquation(value));
							break;
						case "mapping:":
							String[] temp = value.split(" = ");
							mapAlternativeNames.add(new String[]{temp[0].trim(), temp[1].trim()});
							break;
					}
				}
				break;
			case ".booleannet":
				this.booleanEquations = new ArrayList<>();
				this.mapAlternativeNames = new ArrayList<>();
				this.modelName = removeExtension(filename);

				for (int i = 0; i < lines.size(); i++) {
					booleanEquations.add(new BooleanEquation(lines.get(i)));
					String target = lines.get(i).substring(0, lines.get(i).indexOf(" *=")).trim();
					mapAlternativeNames.add(new String[] { target, "x" + (i + 1) });
				}
				break;
			case ".bnet":
				this.booleanEquations = new ArrayList<>();
				this.mapAlternativeNames = new ArrayList<>();
				this.modelName = removeExtension(filename);

				// ignore first line: targets, factors
				for (int i = 1; i < lines.size(); i++) {
					String equationBoolNet = lines.get(i);
					String equationBooleanNet = equationBoolNet
							.replace(",", " *=")
							.replace(" & ", " and ")
							.replace(" | ", " or ")
							.replace(" ! ", " not ")
							.replace(" 1 ", " true ")
							.replace(" 0 ", " false ");
					booleanEquations.add(new BooleanEquation(equationBooleanNet));
					String target = equationBoolNet.split(",")[0].trim();
					mapAlternativeNames.add(new String[] { target, "x" + i });
				}
				break;
			default:
				logger.error("File extension: " + fileExtension + " for loading Boolean " +
						"model from file is not supported");
		}
	}

	protected BooleanModel(final BooleanModel booleanModel, Logger logger) {
		this.logger = logger;

		// Copy Boolean equations
		this.booleanEquations = new ArrayList<>();

		booleanEquations.addAll(booleanModel.booleanEquations);

		// Copy mapAlternativeNames
		this.mapAlternativeNames = new ArrayList<>();

		this.mapAlternativeNames.addAll(booleanModel.mapAlternativeNames);

		// Stable states (empty)
		stableStates = new ArrayList<>();

		// Copy modelName
		this.modelName = booleanModel.modelName;
	}

	public void exportModelToGitsbeFile(String directoryOutput) throws IOException {

		String filename = removeExtension(this.modelName) + ".gitsbe";
		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, filename).getAbsolutePath(), "UTF-8"
		);

		// Write header with '#'
		writer.println("#Boolean model file in gitsbe format");

		// Write model name
		writer.println("modelname: " + this.modelName);

		// Write stable state(s)
		for (String stableState : this.stableStates) {
			writer.println("stablestate: " + stableState);
		}

		// Write Boolean equations
		for (BooleanEquation booleanEquation : booleanEquations) {
			writer.println("equation: " + booleanEquation.getBooleanEquation());
		}

		// Write alternative names for Veliz-Cuba
		for (String[] names : mapAlternativeNames) {
			writer.println("mapping: " + names[0] + " = " + names[1]);
		}

		writer.close();
	}

	public void exportModelToSifFile(String directoryOutput, String filename) throws IOException {

		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, filename).getAbsolutePath(), "UTF-8"
		);

		for (BooleanEquation booleanEquation : booleanEquations)
			for (String sifLine : booleanEquation.convertToSifLines("\t"))
				writer.println(sifLine);

		writer.close();
	}

	public void exportModelToGINMLFile(String directoryOutput, String filename,
									   ArrayList<SingleInteraction> singleInteractions) throws IOException {

		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, filename).getAbsolutePath(), "UTF-8"
		);
		ArrayList<String> equations = this.getModelBooleanNet();

		// write heading

		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<!DOCTYPE gxl SYSTEM \"http://ginsim.org/GINML_2_2.dtd\">");
		writer.println("<gxl xmlns:xlink=\"http://www.w3.org/1999/xlink\">");

		// write nodeorder

		writer.print("<graph class=\"regulatory\" id=\"" + modelName.replace(".", "_") + "\" nodeorder=\"");
		for (String[] names : mapAlternativeNames) {
			writer.print(names[0] + " ");
		}
		writer.print("\">\n");

		// node style edge style

		// write nodes with Boolean expression
		int index = 0;
		for (String equation : equations) {
			writer.println("<node id=\"" + mapAlternativeNames.get(index)[0] + "\" maxvalue=\"1\">");
			writer.println("\t<value val=\"1\">");
			writer.println("\t\t<exp str=\"" + equation + "\"/>");
			writer.println("\t</value>");
			writer.println("\t<nodevisualsetting x=\"10\" y=\"10\" style=\"\"/>");
			writer.println("</node>");
			index++;
		}

		// write edges
		for (SingleInteraction singleInteraction : singleInteractions) {
			String source = singleInteraction.getSource();
			String target = singleInteraction.getTarget();
			String arc;

			if (singleInteraction.getArc() == 1) {
				arc = "positive";
			} else {
				arc = "negative";
			}

			writer.print("<edge id=\"" + source + ":" + target + "\" from=\""
					+ source + "\" to=\"" + target + "\" minvalue=\"1\" sign=\"" + arc + "\">\n");
			writer.println("<edgevisualsetting style=\"\"/>");
			writer.println("</edge>");
		}

		// finalize
		writer.println("</graph>");
		writer.println("</gxl>");
		writer.close();
	}

	public void exportModelToBoolNetFile(String directoryOutput, String filename) throws IOException {

		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, filename).getAbsoluteFile(), "UTF-8"
		);

		ArrayList<String> equations = getModelBoolNet();

		// Write header
		writer.println("targets, factors");

		// Write equations
		for (String equation : equations) {
			writer.println(equation);
		}

		writer.close();
	}

	private void exportModelToVelizCubaFile(String directoryOutput, String filename) throws IOException {
		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, filename).getAbsoluteFile(), "UTF-8"
		);

		// Defined model in Veliz-Cuba terminology
		ArrayList<String> equationsVC = getModelVelizCuba();

		for (String equation : equationsVC) {
			writer.println(equation);
		}

		writer.close();
	}

	/**
	 * @return an ArrayList of Strings (the model equations in Booleannet format)
	 */
	ArrayList<String> getModelBooleanNet() {
		ArrayList<String> equations = new ArrayList<>();

		for (BooleanEquation booleanEquation : booleanEquations) {
			equations.add(booleanEquation.getBooleanEquation().replaceAll(" {2}", " ").trim());
		}

		return equations;
	}

	/**
	 * @return an ArrayList of Strings (the model equations in Veliz-Cuba's format)
	 */
	ArrayList<String> getModelVelizCuba() {

		ArrayList<String> equations = this.getModelBooleanNet();
		ArrayList<String> modifiedEquations = new ArrayList<>();

		for (String equation : equations) {
			// Remove target node (line number indicates which variable
			// is defined, i.e. 'x1' on line 1, 'x2' on line 2 etc.)
			String modifiedEquation =
					replaceOperators(equation)
					.substring(equation.indexOf('=') + 1)
					.trim();
			// Use the alternate names (x1, x2, ..., xn)
			for (String[] map: mapAlternativeNames) {
				modifiedEquation = modifiedEquation.replace(" " + map[0] + " ", " " + map[1] + " ");
			}
			modifiedEquations.add(modifiedEquation);
		}

		return modifiedEquations;
	}

	/**
	 * @return an ArrayList of Strings (the model equations in BoolNet format)
	 */
	ArrayList<String> getModelBoolNet() {
		ArrayList<String> equations = this.getModelBooleanNet();
		ArrayList<String> modifiedEquations = new ArrayList<>();

		for (String equation : equations) {
			modifiedEquations.add(replaceOperators(equation).replaceAll(" \\*=", ","));
		}

		return modifiedEquations;
	}

	public void calculateStableStatesVC(String directoryOutput) throws IOException {
		String filenameVC = this.modelName + ".dat";
		exportModelToVelizCubaFile(directoryOutput, filenameVC);

		// Run the BNReduction script
		String filenameVCFullPath = new File(directoryOutput, filenameVC).getAbsolutePath();
		runBNReduction(filenameVCFullPath);

		// Read stable states from BNReduction.sh output file
		String fixedPointsFile = new File(directoryOutput, modelName + ".dat.fp").getAbsolutePath();

		logger.outputStringMessage(2, "Reading steady states: " + fixedPointsFile);
		ArrayList<String> lines = readLinesFromFile(fixedPointsFile, true);

		stableStates.addAll(lines);

		if (stableStates.size() > 0) {
			if (stableStates.get(0).length() > 0) {

				logger.outputStringMessage(1, "BNReduction found " + stableStates.size() + " stable states:");
				for (int i = 0; i < stableStates.size(); i++) {
					logger.outputStringMessage(2, "Stable state " + (i + 1) + ": " + stableStates.get(i));
				}

				// Debug info
				for (int i = 0; i < stableStates.get(0).length(); i++) {
					StringBuilder states = new StringBuilder();
					for (String stableState : stableStates) {
						states.append("\t").append(stableState.charAt(i));
					}
					logger.debug(mapAlternativeNames.get(i)[0] + states);
				}
			}
		} else {
			logger.outputStringMessage(1, "BNReduction found no stable states.");
		}

		deleteFilesMatchingPattern(logger, modelName);
	}

	private void runBNReduction(String filenameVC) {
		String BNReductionScriptFile = new File(directoryBNET, "BNReduction.sh").getAbsolutePath();

		try {
			ProcessBuilder pb = new ProcessBuilder("timeout", "30",
					BNReductionScriptFile, filenameVC);

			if (logger.getVerbosity() >= 3) {
				pb.redirectErrorStream(true);
				pb.redirectOutput();
			}

			pb.directory(new File(directoryBNET));
			logger.outputStringMessage(3, "Running BNReduction.sh in directory " + pb.directory());

			Process p;
			p = pb.start();

			try {
				p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Redirecting the output of BNReduction.sh
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while (r.ready()) {
				logger.outputStringMessage(3, r.readLine());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get index of equation ascribed to specified target
	 * 
	 * @param target
	 * @return
	 */
	public int getIndexOfEquation(String target) {
		int index = -1;

		for (int i = 0; i < mapAlternativeNames.size(); i++) {
			if (target.trim().equals(mapAlternativeNames.get(i)[0].trim())) {
				index = i;
			}
		}

		return index;
	}

	public ArrayList<String> getNodeNames() {
		ArrayList<String> nodeNames = new ArrayList<>();

		for (String[] names : mapAlternativeNames) {
			nodeNames.add(names[0]);
		}

		return nodeNames;
	}

	/**
	 * Checks if there is one or several stable states for the model
	 * 
	 * @return
	 */
	boolean hasStableStates() {
		return (stableStates.size() > 0);
	}

	/**
	 * Returns an 2-dimensional Array where the first row has the node names and
	 * every other row contains the activity state values (0 or 1) of the corresponding
	 * node (column) in the stable state
	 */
	String[][] getStableStates() {
		String[][] result = new String[stableStates.size() + 1][getNodeNames().size()];

		result[0] = getNodeNames().toArray(new String[0]);

		for (int i = 0; i < stableStates.size(); i++) {
			result[i + 1] = stableStates.get(i).split("(?!^)");
		}

		return result;
	}

	/**
	 * Modify equation based on correct identification of target's name
	 *
	 * @param equation Equation string to substitute
	 */
	void modifyEquation(String equation) throws Exception {
		// Get index of equation for specified target
		String target = equation.split(" ")[0].trim();
		int index = getIndexOfEquation(target);

		if (index < 0) {
			throw new Exception("Target of equation [" + equation + "] not found");
		}
		booleanEquations.set(index, new BooleanEquation(equation));
	}

	public String getModelName() {
		return this.modelName;
	}

	ArrayList<BooleanEquation> getBooleanEquations() {
		return booleanEquations;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	ArrayList<String[]> getMapAlternativeNames() {
		return mapAlternativeNames;
	}
}
