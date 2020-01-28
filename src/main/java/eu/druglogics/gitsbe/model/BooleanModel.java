package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.input.OutputWeight;
import eu.druglogics.gitsbe.util.Logger;
import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.io.bnet.BNetFormat;
import org.colomoto.biolqm.io.ginml.GINMLFormat;
import org.colomoto.biolqm.io.sbml.SBMLFormat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

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
	protected Attractors attractors;
	protected String modelName;
	private String filename;
	Logger logger;

	public BooleanModel(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Constructor for defining a {@link BooleanModel} from a {@link GeneralModel},
	 * which is made up of {@link SingleInteraction} objects.
	 * Note that {@link GeneralModel#buildMultipleInteractions()} must be
	 * called first, before this constructor is used and that an appropriate
	 * attractor tool must be given.
	 *
 	 */
	public BooleanModel(GeneralModel generalModel, String attractorTool, Logger logger) {

		this.logger = logger;
		this.modelName = generalModel.getModelName();
		this.booleanEquations = new ArrayList<>();
		this.nodeNameToVariableMap = new LinkedHashMap<>();

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

		this.attractors = new Attractors(this, attractorTool, logger);
	}

	/**
	 * Constructor for defining a Boolean model from a file and setting up the
	 * <i>attractorTool</i> to be used. <br/><br/>
	 *
	 * Currently we support 3 filetypes: <i>.gitsbe</i>, <i>.bnet</i> and <i>.booleannet</i> files.
	 * Note that the boolean equation format must be:
	 * <i>Target *= (Activator OR/AND Activator OR/AND ...)
	 * AND/OR NOT (Inhibitor OR/AND Inhibitor OR/AND ...)</i>
	 *
	 * @param filename
	 * @param attractorTool
	 * @param logger
	 */
	public BooleanModel(String filename, String attractorTool, Logger logger) {
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

		this.attractors = new Attractors(this, attractorTool, logger);
	}

	/**
	 * Copy constructor for defining a {@link BooleanModel} from another {@link BooleanModel}!
	 *
	 * @param booleanModel
	 * @param logger
	 */
	public BooleanModel(final BooleanModel booleanModel, Logger logger) {
		this.logger = logger;

		// (Deep) copy of the Boolean equations objects
		this.booleanEquations = new ArrayList<>();
		for (BooleanEquation booleanEquation: booleanModel.getBooleanEquations()) {
			BooleanEquation booleanEquationCopy = new BooleanEquation(booleanEquation);
			this.booleanEquations.add(booleanEquationCopy);
		}

		// Copy nodeNameToVariableMap
		this.nodeNameToVariableMap = new LinkedHashMap<>();
		this.nodeNameToVariableMap.putAll(booleanModel.nodeNameToVariableMap);

		this.attractors = new Attractors(this, booleanModel.getAttractorTool(), logger);

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

		// Write attractor(s)
		for (String attractor : this.getAttractors()) {
			if (attractor.contains("-"))
				writer.println("trapspace: " + attractor);
			else
				writer.println("stablestate: " + attractor);
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

	public void exportModelToVelizCubaDataFile(String directoryOutput) throws IOException {
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
	public ArrayList<String> getModelBooleanNet() {
		ArrayList<String> equations = new ArrayList<>();

		for (BooleanEquation booleanEquation : booleanEquations) {
			equations.add(booleanEquation.getBooleanEquation().replaceAll(" {2}", " ").trim());
		}

		return equations;
	}

	/**
	 * @return an ArrayList of Strings (the model equations in Veliz-Cuba's format)
	 */
	public ArrayList<String> getModelVelizCuba() {

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
	public ArrayList<String> getModelBoolNet() {
		ArrayList<String> equations = this.getModelBooleanNet();
		ArrayList<String> modifiedEquations = new ArrayList<>();

		for (String equation : equations) {
			modifiedEquations.add(replaceOperators(equation).replaceAll(" \\*=", ","));
		}

		return modifiedEquations;
	}

	/**
	 * Get index of equation ascribed to specified target
	 * 
	 * @param target
	 */
	public int getIndexOfEquation(String target) {
		return new ArrayList<>(nodeNameToVariableMap.keySet()).indexOf(target.trim());
	}

	public ArrayList<String> getNodeNames() {
		return new ArrayList<>(nodeNameToVariableMap.keySet());
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

	/**
	 * Use this function to change a specific boolean equation's link operator.
	 * The equation has to have a <b>and</b> or <b>or</b> link operator
	 *
	 * @param index Integer indicating which equation to change
	 * @throws Exception either when the index given is outside the acceptable range or
	 * the link operator is neither <i>and</i> or <i>or</i>.
	 */
	public void changeLinkOperator(int index) throws Exception {
		BooleanEquation booleanEquation = getBooleanEquations().get(index);

		logger.outputStringMessage(3, "Changing link operator of equation: "
			+ booleanEquation.getBooleanEquation());

		String link = booleanEquation.getLink();
		if (link.equals("and") || link.equals("or")) {
			booleanEquation.mutateLinkOperator();
			booleanEquations.set(index, booleanEquation);
		} else {
			throw new Exception("Link operator of equation: " + booleanEquation.getBooleanEquation()
				+ "is neither `and` or `or`");
		}
	}

	/**
	 * Wrapper-function for the calculation of the attractors. See:
	 * {@link Attractors#calculateAttractors(String)}
	 *
	 * @param directoryOutput
	 * @throws Exception
	 */
	public void calculateAttractors(String directoryOutput) throws Exception {
		attractors.calculateAttractors(directoryOutput);
	}

	/**
	 * Wrapper-function. See: {@link Attractors#hasAttractors()}
	 */
	public boolean hasAttractors() {
		return attractors.hasAttractors();
	}

	/**
	 * Wrapper-function. See: {@link Attractors#hasStableStates()}
	 */
	public boolean hasStableStates() {
		return attractors.hasStableStates();
	}

	/**
	 * Wrapper-function. See: {@link Attractors#getAttractorTool()}
	 */
	public String getAttractorTool() {
		return attractors.getAttractorTool();
	}

	/**
	 * Wrapper-function. See: {@link Attractors#getAttractors()}
	 */
	public ArrayList<String> getAttractors() {
		return attractors.getAttractors();
	}

	/**
	 * Wrapper-function. See: {@link Attractors#getAttractorsWithNodes()}
	 */
	public String[][] getAttractorsWithNodes() {
		return attractors.getAttractorsWithNodes();
	}

	/**
	 * Use this function after you have calculated the
	 * {@link #calculateAttractors(String) attractors} in order to find the <b>normalized</b>
	 * globaloutput of the model, based on the weights of the nodes defined in the
	 * {@link ModelOutputs} Class.
	 *
	 */
	public float calculateGlobalOutput() {
		ModelOutputs modelOutputs = ModelOutputs.getInstance();
		float globaloutput = 0;

		for (String attractor : this.getAttractors()) {
			for (OutputWeight outputWeight : modelOutputs.getModelOutputs()) {
				int nodeIndexInAttractor = this.getIndexOfEquation(outputWeight.getNodeName());
				if (nodeIndexInAttractor >= 0) {
					// can only be '1','0' or '-'
					char nodeState = attractor.charAt(nodeIndexInAttractor);
					float stateValue = (nodeState == '-')
						? (float) 0.5
						: Character.getNumericValue(attractor.charAt(nodeIndexInAttractor));

					globaloutput += stateValue * outputWeight.getWeight();
				}
			}
		}

		globaloutput /= getAttractors().size();

		return ((globaloutput - modelOutputs.getMinOutput()) / (modelOutputs.getMaxOutput() - modelOutputs.getMinOutput()));
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getModelName() {
		return modelName;
	}

	public ArrayList<BooleanEquation> getBooleanEquations() {
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
