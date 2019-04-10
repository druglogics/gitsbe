package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.util.Logger;

import static eu.druglogics.gitsbe.util.Util.*;
import static eu.druglogics.gitsbe.util.FileDeleter.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * 
 * @author Asmund Flobak, email: asmund.flobak@ntnu.no
 *
 */
public class BooleanModel {

	ArrayList<BooleanEquation> booleanEquations;
	ArrayList<String[]> mapAlternativeNames;
	ArrayList<String> stableStates;
	String modelName;
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
		if (fileExtension.equals(".gitsbe")) {
			// Boolean equations
			this.booleanEquations = new ArrayList<>();

			// Alternative names
			this.mapAlternativeNames = new ArrayList<>();

			// Load model
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

		} else if (fileExtension.equals(".booleannet")) {
			// Boolean equations
			this.booleanEquations = new ArrayList<>();

			// Alternative names
			this.mapAlternativeNames = new ArrayList<>();

			this.modelName = filename.substring(0, filename.indexOf(".booleannet"));

			// Load model
			for (int i = 0; i < lines.size(); i++) {
				booleanEquations.add(new BooleanEquation(lines.get(i)));
				String target = lines.get(i).substring(0, lines.get(i).indexOf(" *=")).trim();
				mapAlternativeNames.add(new String[] { target, "x" + (i + 1) });
			}
		}
	}

	// Copy constructor for defining Boolean model from another Boolean model
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

	public void exportModelToSifFile(String directoryOutput, String filename)
			throws FileNotFoundException, UnsupportedEncodingException {

		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, filename).getAbsolutePath(), "UTF-8"
		);

		for (BooleanEquation booleanEquation : booleanEquations)
			for (String sifLine : booleanEquation.convertToSifLines("\t"))
				writer.println(sifLine);

		writer.close();
	}

	public void exportModelToBoolNetFile(String outputDirectory, String filename)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(new File(outputDirectory, filename), "UTF-8");

		String[] expressions = this.printBooleanModelGinmlExpressions();

		// Write header
		writer.println("targets, factors \n # comment line: " + filename);
		// Write expressions with correct symbols
		for (int i = 0; i < expressions.length; i++) {
			expressions[i] = expressions[i].replaceAll(" {2}", " ");
			expressions[i] = expressions[i].replaceAll("and", "&");
			expressions[i] = expressions[i].replaceAll("or", "|");
			expressions[i] = expressions[i].replaceAll("not ", "!");
			expressions[i] = expressions[i].replaceAll(" \\*=", ",");

			writer.println(expressions[i]);
		}

		writer.close();
	}

	public void exportModelToGitsbeFile(String directoryName) throws IOException {

		String filename = removeExtension(this.modelName) + ".gitsbe";
		PrintWriter writer = new PrintWriter(
				new File(directoryName, filename).getAbsolutePath(), "UTF-8"
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

	public void exportModelToGinMLFile(String directoryOutput, String filename,
									   ArrayList<SingleInteraction> singleInteractions) throws IOException {

		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, filename).getAbsolutePath(), "UTF-8"
		);
		String[] expressions = this.printBooleanModelGinmlExpressions();

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
		for (int i = 0; i < mapAlternativeNames.size(); i++) {
			writer.println("<node id=\"" + mapAlternativeNames.get(i)[0] + "\" maxvalue=\"1\">");
			writer.println("\t<value val=\"1\">");
			writer.println("\t\t<exp str=\"" + expressions[i] + "\"/>");
			writer.println("\t</value>");
			writer.println("\t<nodevisualsetting x=\"10\" y=\"10\" style=\"\"/>");
			writer.println("</node>");

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

	private String[] printBooleanModelGinmlExpressions() {

		StringBuilder temp = new StringBuilder();
		String line;

		for (BooleanEquation booleanEquation : booleanEquations) {
			line = booleanEquation.getBooleanEquation() + "\n";
			temp.append(line);
		}

		temp = new StringBuilder(temp.toString().replace("&", "&amp;"));

		return temp.toString().split("\n");
	}

	public String[] printBooleanModelBooleannet() {

		StringBuilder temp = new StringBuilder();
		String line;

		for (BooleanEquation booleanEquation : booleanEquations) {
			line = booleanEquation + "\n";
			temp.append(line);
		}

		for (int i = mapAlternativeNames.size() - 1; i >= 0; i--) {
			temp = new StringBuilder(temp.toString().replace(
					(mapAlternativeNames.get(i)[1]),
					mapAlternativeNames.get(i)[0] + " "));
		}

		temp = new StringBuilder(temp.toString().replace("&", "and"));
		temp = new StringBuilder(temp.toString().replace("|", " or"));
		temp = new StringBuilder(temp.toString().replace("!", "not"));

		return temp.toString().split("\n");
	}

	public String[] getModelBooleannet() {

		String[] temp = new String[booleanEquations.size()];

		for (int i = 0; i < booleanEquations.size(); i++) {
			temp[i] = booleanEquations.get(i).getBooleanEquation();
		}

		return temp;

	}

	public String[] getModelVelizCuba() {

		StringBuilder temp = new StringBuilder();

		for (BooleanEquation booleanEquation : booleanEquations) {
			temp.append(booleanEquation.getBooleanEquationVC()).append("\n");
		}

		// Use alternate names (x1, x2, ..., xn)
		for (int i = 0; i < booleanEquations.size(); i++) {
			temp = new StringBuilder(temp.toString().replace(
					(" " + mapAlternativeNames.get(i)[0] + " "),
					 " " + mapAlternativeNames.get(i)[1] + " "));
		}

		String[] lines = temp.toString().split("\n");

		String[] result = new String[booleanEquations.size()];

		// Remove target node (line number indicates which variable is defined, i.e.
		// 'x1' on line 1, 'x2' on line 2 etc.
		for (int i = 0; i < lines.length; i++) {
			result[i] = (lines[i].substring(lines[i].indexOf('=') + 1).trim());
		}

		return result;
	}

	public void calculateStableStatesVC(String directoryOutput)
			throws IOException {

		// Defined model in Veliz-Cuba terminology
		String[] modelVC = this.getModelVelizCuba();

		// Write model to file for 'BNreduction.sh'
		String modelDataFileVC = new File(directoryOutput, modelName + ".dat").getAbsolutePath();
		PrintWriter writer = new PrintWriter(modelDataFileVC, "UTF-8");

		for (String line : modelVC) {
			writer.println(line);
		}
		writer.close();

		// Run the BNReduction script
		runBNReduction(modelDataFileVC);

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

	private void runBNReduction(String modelDataFileVC) {
		String BNReductionScriptFile = new File(directoryBNET, "BNReduction.sh").getAbsolutePath();

		try {
			ProcessBuilder pb = new ProcessBuilder("timeout", "30",
					BNReductionScriptFile, modelDataFileVC);

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
	 * @param equation
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
