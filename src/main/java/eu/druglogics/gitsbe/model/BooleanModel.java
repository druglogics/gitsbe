package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.util.Logger;
import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.io.bnet.BNetFormat;
import org.colomoto.biolqm.io.ginml.GINMLFormat;
import org.colomoto.biolqm.io.sbml.SBMLFormat;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static eu.druglogics.gitsbe.util.FileDeleter.deleteFilesMatchingPattern;
import static eu.druglogics.gitsbe.util.Util.*;
import static org.colomoto.biolqm.service.LQMServiceManager.load;

/**
 * 
 * @author Asmund Flobak, email: asmund.flobak@ntnu.no
 *
 */
public class BooleanModel {

	protected ArrayList<BooleanEquation> booleanEquations;
	LinkedHashMap<String, String> nodeNameToVariableMap;
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
		this.nodeNameToVariableMap = new LinkedHashMap<>();
		this.stableStates = new ArrayList<>();

		for (int i = 0; i < generalModel.size(); i++) {
			// Define Boolean equation from multiple interaction
			BooleanEquation booleanEquation = new BooleanEquation(generalModel.getMultipleInteraction(i));

			// Build mapping between the node names and the variables used for Veliz-Cuba
			// bnet stable states computation (x1, x2, x3, ..., xn)
			String target = generalModel.getMultipleInteraction(i).getTarget();
			String var    = "x" + (i + 1);
			nodeNameToVariableMap.put(target, var);

			// Add Boolean equation to ArrayList, with index corresponding to
			// nodeNameToVariableMap
			booleanEquations.add(booleanEquation);
		}
	}

	// Copy constructor for defining Boolean model from another Boolean model

	/**
	 * Constructor for defining Boolean model from a file with a set of Boolean
	 * equations
	 *
	 * Currently 3 supported filetypes: .gitsbe, .bnet and .booleannet files
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
				this.nodeNameToVariableMap = new LinkedHashMap<>();

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
							nodeNameToVariableMap.put(temp[0].trim(), temp[1].trim());
							break;
					}
				}
				break;
			case ".booleannet":
				this.booleanEquations = new ArrayList<>();
				this.nodeNameToVariableMap = new LinkedHashMap<>();
				this.modelName = removeExtension(filename);

				for (int i = 0; i < lines.size(); i++) {
					booleanEquations.add(new BooleanEquation(lines.get(i)));
					String target = lines.get(i).substring(0, lines.get(i).indexOf(" *=")).trim();
					String var    = "x" + (i + 1);
					nodeNameToVariableMap.put(target, var);
				}
				break;
			case ".bnet":
				this.booleanEquations = new ArrayList<>();
				this.nodeNameToVariableMap = new LinkedHashMap<>();
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
					String var    = "x" + i;
					nodeNameToVariableMap.put(target, var);
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

		// Copy nodeNameToVariableMap
		this.nodeNameToVariableMap = new LinkedHashMap<>();
		this.nodeNameToVariableMap.putAll(booleanModel.nodeNameToVariableMap);

		// Stable states (empty)
		stableStates = new ArrayList<>();

		// Copy modelName
		this.modelName = booleanModel.getModelName();
	}

	public void exportModelToGitsbeFile(String directoryOutput) throws IOException {

		String filename = removeExtension(modelName) + ".gitsbe";
		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, filename).getAbsolutePath(), "UTF-8"
		);

		// Write header with '#'
		writer.println("#Boolean model file in gitsbe format");

		// Write model name
		writer.println("modelname: " + modelName);

		// Write stable state(s)
		for (String stableState : stableStates) {
			writer.println("stablestate: " + stableState);
		}

		// Write Boolean equations
		for (BooleanEquation booleanEquation : booleanEquations) {
			writer.println("equation: " + booleanEquation.getBooleanEquation());
		}

		// Write alternative names for Veliz-Cuba
		for (Map.Entry<String, String> entry : nodeNameToVariableMap.entrySet()) {
			writer.println("mapping: " + entry.getKey() + " = " + entry.getValue());
		}

		writer.close();
	}

	public void exportModelToSifFile(String directoryOutput) throws IOException {

		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, modelName + ".sif").getAbsolutePath(), "UTF-8"
		);

		for (BooleanEquation booleanEquation : booleanEquations)
			for (String sifLine : booleanEquation.convertToSifLines("\t"))
				writer.println(sifLine);

		writer.close();
	}

	public void exportModelToBoolNetFile(String directoryOutput) throws IOException {
		File boolNetFile = new File(directoryOutput, modelName + ".bnet").getAbsoluteFile();
		PrintWriter writer = new PrintWriter(boolNetFile, "UTF-8");

		ArrayList<String> equations = getModelBoolNet();

		// Write header
		writer.println("targets, factors");

		// Write equations
		for (String equation : equations) {
			writer.println(equation);
		}

		writer.close();
	}

	/**
	 * Exports Boolean Model to GINML format using BioLQM library. Firstly, the
	 * model is converted to the BoolNet format and a .bnet file is written. After
	 * the GINML file is created, the .bnet file is deleted.
	 *
	 * @param directoryOutput
	 * @throws IOException
	 */
	public void exportModelToGINMLFile(String directoryOutput) throws IOException {
		File boolNetFile = new File(directoryOutput, modelName + ".bnet");

		if (!boolNetFile.exists()) {
			exportModelToBoolNetFile(directoryOutput);
		}

		LogicalModel boolNetModel = load(boolNetFile.getAbsolutePath(), BNetFormat.ID);
		GINMLFormat ginml = new GINMLFormat();

		try {
			ginml.export(boolNetModel, new File(directoryOutput, modelName + ".ginml").getAbsoluteFile());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!boolNetFile.delete()) {
			throw new IOException("Couldn't delete file: " + boolNetFile);
		}
	}

	/**
	 * Exports Boolean Model to SBML-Qual format using BioLQM library. Firstly, the
	 * model is converted to the BoolNet format and a .bnet file is written. After
	 * the SBML-Qual file is created, the .bnet file is deleted.
	 *
	 * @param directoryOutput
	 * @throws IOException
	 */
	public void exportModelToSBMLFile(String directoryOutput) throws IOException {
		File boolNetFile = new File(directoryOutput, modelName + ".bnet");

		if (!boolNetFile.exists()) {
			exportModelToBoolNetFile(directoryOutput);
		}

		LogicalModel boolNetModel = load(boolNetFile.getAbsolutePath(), BNetFormat.ID);
		SBMLFormat sbml = new SBMLFormat();

		try {
			sbml.export(boolNetModel, new File(directoryOutput, modelName + ".xml").getAbsoluteFile());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!boolNetFile.delete()) {
			throw new IOException("Couldn't delete file: " + boolNetFile);
		}
	}

	void exportModelToVelizCubaDataFile(String directoryOutput) throws IOException {
		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, modelName + ".dat").getAbsoluteFile(), "UTF-8"
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
			for (Map.Entry<String, String> entry : nodeNameToVariableMap.entrySet()) {
				modifiedEquation = modifiedEquation
						.replace(" " + entry.getKey() + " ", " " + entry.getValue() + " ");
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

	public void calculateStableStatesVC(String directoryOutput, String tool) throws IOException {
		exportModelToVelizCubaDataFile(directoryOutput);

		// Run the BNReduction script
		String filenameVCFullPath = new File(directoryOutput, modelName + ".dat").getAbsolutePath();
		runBNReduction(filenameVCFullPath, tool);

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
			}
		} else {
			logger.outputStringMessage(1, "BNReduction found no stable states.");
		}

		deleteFilesMatchingPattern(logger, modelName);
	}

	private void runBNReduction(String filenameVC, String tool) {
		String BNReductionScriptFile = new File(directoryBNET, "BNReduction.sh").getAbsolutePath();

		try {
			ProcessBuilder pb = null;
			String timeoutSeconds = "30";
			switch(tool) {
				case "bnet_reduction":
					pb = new ProcessBuilder("timeout", timeoutSeconds, BNReductionScriptFile,
							filenameVC);
					break;
				case "bnet_reduction_reduced":
					pb = new ProcessBuilder("timeout", timeoutSeconds, BNReductionScriptFile,
							filenameVC, "reduced");
					break;
			}

			if (logger.getVerbosity() >= 3) {
				pb.redirectErrorStream(true);
				pb.redirectOutput();
			}

			File parentVCdir = new File(new File(filenameVC).getParent());
			pb.directory(parentVCdir);
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
		return new ArrayList<>(nodeNameToVariableMap.keySet()).indexOf(target.trim());
	}

	public ArrayList<String> getNodeNames() {
		return new ArrayList<>(nodeNameToVariableMap.keySet());
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
		return modelName;
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

	LinkedHashMap<String, String> getNodeNameToVariableMap() {
		return nodeNameToVariableMap;
	}
}
