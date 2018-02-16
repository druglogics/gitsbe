package gitsbe;

import static gitsbe.Util.*;
import static gitsbe.FileDeleter.*;

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

	protected ArrayList<BooleanEquation> booleanEquations;
	protected ArrayList<String[]> mapAlternativeNames;
	protected ArrayList<String> stableStates;
	protected String modelName;
	protected int verbosity = 2;
	protected String filename;
	protected Logger logger;
	public static String directoryBNET = System.getenv("BNET_HOME");

	public BooleanModel(Logger logger) {
		this.logger = logger;
	}

	// Constructor for defining Boolean model from a "general model" with
	// interactions
	public BooleanModel(GeneralModel generalModel, Logger logger) {

		this.logger = logger;
		this.verbosity = logger.getVerbosity();
		this.modelName = generalModel.getModelName();
		booleanEquations = new ArrayList<BooleanEquation>();
		mapAlternativeNames = new ArrayList<String[]>();
		stableStates = new ArrayList<String>();

		for (int i = 0; i < generalModel.size(); i++) {
			// Define Boolean equation from multiple interaction
			BooleanEquation booleanEquation = new BooleanEquation(generalModel.getMultipleInteraction(i));

			// Build list of alternative names used for Veliz-Cuba bnet stable states
			// compution (x1, x2, x3, ..., xn)

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

		// load gitsbe file format
		if (filename.substring(filename.length() - ".gitsbe".length()).toLowerCase().equals(".gitsbe")) {
			// Boolean equations
			this.booleanEquations = new ArrayList<BooleanEquation>();

			// Alternative names
			this.mapAlternativeNames = new ArrayList<String[]>();

			// Load model
			for (int i = 0; i < lines.size(); i++) {
				String prefix = lines.get(i).substring(0, lines.get(i).indexOf(' '));
				String line = lines.get(i).substring(lines.get(i).indexOf(' '));
				switch (prefix) {
				case "modelname:":
					this.modelName = line.trim();
					break;
				case "equation:":
					booleanEquations.add(new BooleanEquation(line));
					break;
				case "mapping:":

					String[] temp = new String[2];
					temp = line.split(" = ");
					mapAlternativeNames.add(new String[] { temp[0].trim(), temp[1].trim() });

					break;
				}
			}

		} else if (filename.substring(filename.length() - ".booleannet".length()).equals(".booleannet")) {
			// Boolean equations
			this.booleanEquations = new ArrayList<BooleanEquation>();

			// Alternative names
			this.mapAlternativeNames = new ArrayList<String[]>();

			this.modelName = filename.substring(0, filename.indexOf(".booleannet"));

			// Load model
			for (int i = 0; i < lines.size(); i++) {
				booleanEquations.add(new BooleanEquation(lines.get(i)));
				String target = lines.get(i).substring(0, lines.get(i).indexOf(" *=")).trim();
				mapAlternativeNames.add(new String[] { target, new String("x" + (i + 1)) });
			}
		}
	}

	// Copy constructor for defining Boolean model from another Boolean model
	protected BooleanModel(final BooleanModel booleanModel, Logger logger) {
		this.logger = logger;

		// Copy Boolean equations
		this.booleanEquations = new ArrayList<BooleanEquation>();

		for (int i = 0; i < booleanModel.booleanEquations.size(); i++) {
			booleanEquations.add(booleanModel.booleanEquations.get(i));
		}

		// Copy mapAlternativeNames
		this.mapAlternativeNames = new ArrayList<String[]>();

		for (int i = 0; i < booleanModel.mapAlternativeNames.size(); i++) {
			this.mapAlternativeNames.add(booleanModel.mapAlternativeNames.get(i));
		}

		// Stable states (empty)
		stableStates = new ArrayList<String>();

		// Copy modelName
		this.modelName = new String(booleanModel.modelName);

		this.verbosity = logger.getVerbosity();

	}

	public int getNumberOfStableStates() {
		return stableStates.size();
	}

	public void exportSifFile(String directoryOutput, String filename)
			throws FileNotFoundException, UnsupportedEncodingException {

		PrintWriter writer = new PrintWriter(new File(directoryOutput, filename).getAbsolutePath(), "UTF-8");

		for (int i = 0; i < booleanEquations.size(); i++)
			for (int j = 0; j < booleanEquations.get(i).convertToSifLines().length; j++)
				writer.println(booleanEquations.get(i).convertToSifLines()[j]);

		writer.close();

	}

	public void setVerbosity(int verbosity) {
		this.verbosity = verbosity;
	}

	public void exportBoolNetFile(String outputDirectory, String filename)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(new File(outputDirectory, filename), "UTF-8");

		String[] expressions = this.printBooleanModelGinmlExpressions();

		// Write header
		writer.println("targets, factors \n # comment line: " + filename);
		// Write expressions with correct symbols
		for (int i = 0; i < expressions.length; i++) {
			expressions[i] = expressions[i].replaceAll("  ", " ");
			expressions[i] = expressions[i].replaceAll("and", "&");
			expressions[i] = expressions[i].replaceAll("or", "|");
			expressions[i] = expressions[i].replaceAll("not ", "!");
			expressions[i] = expressions[i].replaceAll(" \\*=", ",");

			writer.println(expressions[i]);
		}

		writer.close();
	}

	public void saveFileInGitsbeFormat(String directoryName) throws IOException {

		String filename = this.modelName.substring(this.modelName.lastIndexOf('/') + 1) + ".gitsbe";
		PrintWriter writer = new PrintWriter(new File(directoryName, filename).getPath(), "UTF-8");

		// Write header with '#'
		writer.println("#Boolean model file in gitsbe format");

		// Write model name
		writer.println("modelname: " + this.modelName);

		// Write stable state(s)
		for (int i = 0; i < this.stableStates.size(); i++) {
			writer.println("stablestate: " + this.stableStates.get(i));
		}

		// Write Boolean equations
		for (int i = 0; i < booleanEquations.size(); i++) {
			writer.println("equation: " + booleanEquations.get(i).getBooleanEquation());
		}

		// Write alternative names for Veliz-Cuba
		for (int i = 0; i < mapAlternativeNames.size(); i++) {
			writer.println("mapping: " + mapAlternativeNames.get(i)[0] + " = " + mapAlternativeNames.get(i)[1]);
		}

		writer.close();

	}

	public ArrayList<String> getNodeNames() {
		ArrayList<String> nodeNames = new ArrayList<String>();

		for (int i = 0; i < mapAlternativeNames.size(); i++) {
			nodeNames.add(mapAlternativeNames.get(i)[0]);
		}

		return nodeNames;
	}

	public void writeGinmlFile(String directoryOutput, String filename, ArrayList<SingleInteraction> singleInteractions)
			throws IOException {

		PrintWriter writer = new PrintWriter(new File(directoryOutput, filename).getAbsolutePath(), "UTF-8");
		String[] expressions = this.printBooleanModelGinmlExpressions();

		// write heading

		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<!DOCTYPE gxl SYSTEM \"http://ginsim.org/GINML_2_2.dtd\">");
		writer.println("<gxl xmlns:xlink=\"http://www.w3.org/1999/xlink\">");

		// write nodeorder

		writer.print("<graph class=\"regulatory\" id=\"" + modelName.replace(".", "_") + "\" nodeorder=\"");
		for (int i = 0; i < mapAlternativeNames.size(); i++) {
			writer.print(mapAlternativeNames.get(i)[0] + " ");
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
		for (int i = 0; i < singleInteractions.size(); i++) {
			String source = singleInteractions.get(i).getSource();
			String target = singleInteractions.get(i).getTarget();
			String arc;

			if (singleInteractions.get(i).getArc() == 1) {
				arc = "positive";
			} else {
				arc = "negative";
			}

			writer.print("<edge id=\"" + source + ":" + target + "\" from=\"" + source + "\" to=\"" + target
					+ "\" minvalue=\"1\" sign=\"" + arc + "\">\n");
			writer.println("<edgevisualsetting style=\"\"/>");
			writer.println("</edge>");

		}

		// finalize
		writer.println("</graph>");
		writer.println("</gxl>");
		writer.close();

	}

	public String[] printBooleanModelGinmlExpressions() {

		String temp = "";
		String line = "";

		for (int i = 0; i < booleanEquations.size(); i++) {
			line = booleanEquations.get(i).getBooleanEquation() + "\n";
			temp += line;
		}

		temp = temp.replace("&", "&amp;");
		String lines[] = temp.split("\n");

		return lines;
	}

	public String[] getModelBooleannet() {

		String[] temp = new String[booleanEquations.size()];

		for (int i = 0; i < booleanEquations.size(); i++) {
			temp[i] = booleanEquations.get(i).getBooleanEquation();
		}

		return temp;

	}

	public String[] getModelVelizCuba() {

		String temp = "";

		for (int i = 0; i < booleanEquations.size(); i++) {
			temp = temp + booleanEquations.get(i).getBooleanEquationVC() + "\n";

		}

		// Use alternate names (x1, x2, ..., xn)
		for (int i = 0; i < booleanEquations.size(); i++) {
			temp = temp.replace((" " + mapAlternativeNames.get(i)[0] + " "), " " + mapAlternativeNames.get(i)[1] + " ");
		}

		String lines[] = temp.split("\n");

		String result[] = new String[booleanEquations.size()];

		// Remove target node (line number indicates which variable is defined, i.e.
		// 'x1' on line 1, 'x2' on line 2 etc.
		for (int i = 0; i < lines.length; i++) {
			result[i] = (lines[i].substring(lines[i].indexOf('=') + 1).trim());
		}

		return result;
	}

	public void calculateStableStatesVC(String directoryOutput)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		this.calculateStableStatesVC(directoryBNET, directoryOutput);
	}

	public void calculateStableStatesVC(String directoryBNET, String directoryOutput)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {

		// Defined model in Veliz-Cuba terminology
		String[] modelVC = this.getModelVelizCuba();

		// Write model to file for 'BNreduction.sh'
		String modelDataFileVC = new File(directoryOutput, modelName + ".dat").getAbsolutePath();
		PrintWriter writer = new PrintWriter(modelDataFileVC, "UTF-8");

		for (int i = 0; i < modelVC.length; i++) {
			writer.println(modelVC[i]);
		}
		writer.close();

		// Run the BNReduction script
		runBNReduction(modelDataFileVC);

		// Read stable states from BNReduction.sh output file
		String fixedPointsFile = new File(directoryOutput, modelName + ".dat.fp").getAbsolutePath();

		logger.outputStringMessage(2, "Reading steady states: " + fixedPointsFile);
		ArrayList<String> lines = readLinesFromFile(fixedPointsFile, true);

		for (int index = 0; index < lines.size(); ++index)
			stableStates.add(lines.get(index));

		if (logger.getVerbosity() >= 2) {
			if (stableStates.size() > 0) {
				if (stableStates.get(0).toString().length() > 0) {

					logger.outputStringMessage(2, "BNReduction found " + stableStates.size() + " stable states:");
					for (int i = 0; i < stableStates.size(); i++) {
						logger.outputStringMessage(2, "Stable state " + (i + 1) + ": " + stableStates.get(i));
					}

					// Debug info
					for (int i = 0; i < stableStates.get(0).length(); i++) {
						String states = "";
						for (int j = 0; j < stableStates.size(); j++) {
							states += "\t" + stableStates.get(j).charAt(i);
						}
						logger.debug(mapAlternativeNames.get(i)[0] + states);
					}
				}
			} else {
				logger.outputStringMessage(2, "BNReduction found no stable states.");
			}
		}

		deleteFilesMatchingPattern(logger, modelName);
	}

	private void runBNReduction(String modelDataFileVC) {
		String BNReductionScriptFile = new File(directoryBNET, "BNReduction.sh").getAbsolutePath();

		try {
			ProcessBuilder pb = new ProcessBuilder("timeout", "30", BNReductionScriptFile, modelDataFileVC);

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

	public String[] printBooleanModelBooleannet() {

		String temp = "";
		String line = "";

		for (int i = 0; i < booleanEquations.size(); i++) {
			line = booleanEquations.get(i) + "\n";
			temp += line;
		}

		for (int i = mapAlternativeNames.size() - 1; i >= 0; i--) {
			temp = temp.replace((mapAlternativeNames.get(i)[1]), mapAlternativeNames.get(i)[0] + " ");
		}

		temp = temp.replace("&", "and");
		temp = temp.replace("|", " or");
		temp = temp.replace("!", "not");

		String lines[] = temp.split("\n");

		return lines;

	}

	public String getModelName() {
		return this.modelName;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
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

	/**
	 * Checks if there is one or several stable states for the model
	 * 
	 * @return
	 */
	public boolean hasStableStates() {
		return (stableStates.size() > 0);
	}

	/**
	 * Returns an 2-dimensional Array where the first row has the node names and
	 * every other row contains the truth values (0 or 1) of the corresponding node
	 * (column) in the stable state
	 */
	public String[][] getStableStates() {
		String[][] result = new String[stableStates.size() + 1][getNodeNames().size()];

		result[0] = getNodeNames().toArray(new String[0]);

		for (int i = 0; i < stableStates.size(); i++) {
			result[i + 1] = stableStates.get(i).split("(?!^)"); // if using "" to split this will return the correct
																// list but empty first element, fixed in jdk8
		}

		return result;
	}

	/**
	 * Modify equation, the function will identify correct equation based on target
	 * name
	 * 
	 * @param equation
	 */
	protected void modifyEquation(String equation) {
		// Get index of equation for specified target
		String target = equation.split(" ")[0].trim();
		int index = getIndexOfEquation(target);

		if (index < 0) {
			logger.outputStringMessage(1, "Target of equation [" + equation
					+ "] not found, this will crash the program. Non-matching name in topology and query?");
		}
		booleanEquations.set(index, new BooleanEquation(equation));
	}
}
