package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.util.Logger;
import org.apache.commons.lang3.StringUtils;
import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.io.bnet.BNetFormat;
import org.colomoto.biolqm.service.LQMServiceManager;
import org.colomoto.biolqm.tool.fixpoints.FixpointList;
import org.colomoto.biolqm.tool.fixpoints.FixpointService;
import org.colomoto.biolqm.tool.trapspaces.TrapSpace;
import org.colomoto.biolqm.tool.trapspaces.TrapSpaceList;
import org.colomoto.biolqm.tool.trapspaces.TrapSpaceService;
import org.colomoto.biolqm.tool.trapspaces.TrapSpaceTask;

import javax.naming.ConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static eu.druglogics.gitsbe.util.FileDeleter.deleteFilesMatchingPattern;
import static eu.druglogics.gitsbe.util.Util.*;
import static org.colomoto.biolqm.service.LQMServiceManager.load;

public class Attractors {

	private BooleanModel booleanModel;
	private Logger logger;
	private String attractorTool;
	private static String directoryBNET = System.getenv("BNET_HOME");
	private static String directoryMPBN = System.getenv("MPBN_HOME");
	private ArrayList<String> attractors; // stable states or trapspaces (ss with '-' dashes)

	public Attractors(BooleanModel booleanModel, String attractorTool, Logger logger) {
		this.booleanModel = booleanModel;
		this.logger = logger;
		this.attractorTool = attractorTool;
		this.attractors = new ArrayList<>();
	}

	/**
	 * Use this function to calculate the attractors of the {@link Attractors#booleanModel} given in the
	 * constructor of the {@link Attractors} Class. The choice of the library/tool that will
	 * be used is based on the value of {@link Attractors#attractorTool}. </br>
	 * Please use one of these values for the {@link Attractors#attractorTool}:
	 * <i>bnet_reduction</i>, <i>bnet_reduction_reduced</i>, <i>biolqm_stable_states</i>,
	 * <i>biolqm_trapspaces</i>, <i>mpbn_trapspaces</i>.
	 *
	 * @param directoryOutput the name of the directory which will be used to write result
	 *                        files, the model in .bnet or Veliz-Cuba format, etc.
	 * @throws Exception
	 */
	public void calculateAttractors(String directoryOutput) throws Exception {
		if (attractorTool.startsWith("bnet_"))
			calculateStableStatesVC(directoryOutput);
		else if (attractorTool.startsWith("biolqm_"))
			calculateAttractorsBioLQM(directoryOutput);
		else
			calculateAttractorsMPBN(directoryOutput);
	}

	/**
	 * Use this function to find the attractors of the boolean model that was
	 * defined in the constructor of the {@link Attractors} Class using the {@link org.colomoto.biolqm BioLQM} library.
	 *
	 * @param directoryOutput the name of the directory where the .bnet file that describes the
	 *                        boolean model will be generated
	 * @throws Exception
	 */
	private void calculateAttractorsBioLQM(String directoryOutput) throws Exception {
		booleanModel.exportModelToBoolNetFile(directoryOutput);

		// Load model from .bnet file
		File boolNetFile = new File(directoryOutput, booleanModel.getModelName() + ".bnet");
		LogicalModel boolNetModel = load(boolNetFile.getAbsolutePath(), BNetFormat.ID);

		getAttractorsFromLogicalModel(boolNetModel);

		deleteFilesMatchingPattern(logger, booleanModel.getModelName());
	}

	/**
	 * Use the <code>mpbn-attrators.py</code> script to calculate the terminal trapspaces for the
	 * {@link Attractors#booleanModel boolean model} that was defined in the constructor of the
	 * {@link Attractors} Class.
	 *
	 * @param directoryOutput the name of the directory where the .bnet file that describes the
	 *                        boolean model will be generated
	 * @throws Exception
	 */
	private void calculateAttractorsMPBN(String directoryOutput) throws Exception {
		booleanModel.exportModelToBoolNetFile(directoryOutput);
		File boolNetFile = new File(directoryOutput, booleanModel.getModelName() + ".bnet");
		String boolNetFilename = boolNetFile.getAbsolutePath();

		String MPBNScriptFile = new File(directoryMPBN, "mpbn-attractors.py").getAbsolutePath();

		// Run the mpbn script
		try {
			ProcessBuilder pb = new ProcessBuilder("python", MPBNScriptFile, boolNetFilename);

			if (logger.getVerbosity() >= 3) {
				pb.redirectErrorStream(true);
			}

			pb.directory(new File(boolNetFile.getParent()));
			logger.outputStringMessage(3, "Running mpbn-attractors.py in directory " + boolNetFile.getParent());

			Process p;
			p = pb.start();

			// Redirecting the output (attractors) of mpbn-attractors.py
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String line;

				while ((line = reader.readLine()) != null) {
					line = StringUtils.replace(line, "*", "-");
					attractors.add(line);
				}
			}

			if (attractors.size() > 0) {
				logger.outputStringMessage(1, "MPBN found " + attractors.size() + " trapspaces:");
				int count = 0;
				for (String trapSpace: attractors) {
					if (isStringAllDashes(trapSpace)) {
						logger.outputStringMessage(2, "Found trivial trapspace (all dashes) which will be ignored");
					} else {
						logger.outputStringMessage(2, "Trapspace " + (++count) + ": " + attractors.get(count-1));
					}
				}
			} else {
				logger.outputStringMessage(1, "MPBN found no trapspaces.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * This function uses BioLQM's {@link org.colomoto.biolqm.tool.fixpoints.FixpointService FixpointService}
	 * and {@link org.colomoto.biolqm.tool.trapspaces.TrapSpaceService TrapSpaceService} Class
	 * to calculate the attractors (stable states/fixpoints or terminal trapspaces using BDD's -
	 * binary decision diagrams) of the <code>boolNetModel</code>.
	 *
	 * @param boolNetModel a logical model, see BioLQM's {@link org.colomoto.biolqm.LogicalModel} Interface
	 * @throws Exception
	 */
	private void getAttractorsFromLogicalModel(LogicalModel boolNetModel) throws Exception {
		if (attractorTool.equals("biolqm_stable_states")) {
			FixpointService fixpointTool = LQMServiceManager.get(FixpointService.class);
			FixpointList fixpointList = fixpointTool.getTask(boolNetModel).call();

			if (fixpointList.size() > 0) {
				logger.outputStringMessage(1, "BioLQM found " + fixpointList.size() + " stable states:");
				int count = 0;
				for (byte[] fixpoint: fixpointList) {
					attractors.add(byteArrayToString(fixpoint));
					logger.outputStringMessage(2, "Stable state " + (++count) + ": " + attractors.get(count-1));
				}
			} else {
				logger.outputStringMessage(1, "BioLQM found no stable states.");
			}
		} else if (attractorTool.equals("biolqm_trapspaces")) {
			TrapSpaceService trapspaceTool = LQMServiceManager.get(TrapSpaceService.class);
			// BDD-based solver is faster than using ASP clingo solver in BioLQM (tested)
			TrapSpaceTask trapSpaceTask = trapspaceTool.getTask(boolNetModel, "BDD");
			TrapSpaceList trapSpaceList = trapSpaceTask.call();

			if (trapSpaceList.size() > 0) {
				logger.outputStringMessage(1, "BioLQM found " + trapSpaceList.size() + " trapspaces:");
				int count = 0;
				for (TrapSpace trapSpace: trapSpaceList) {
					String trapSpaceStr = trapSpace.shortString();
					if (isStringAllDashes(trapSpaceStr)) {
						logger.outputStringMessage(2, "Found trivial trapspace (all dashes) which will be ignored");
					} else {
						attractors.add(trapSpaceStr);
						logger.outputStringMessage(2, "Trapspace " + (++count) + ": " + attractors.get(count-1));
					}
				}
			} else {
				logger.outputStringMessage(1, "BioLQM found no trapspaces.");
			}
		} else {
			throw new ConfigurationException("Attractor tool is neither `biolqm_stable_states` nor "
				+ "`biolqm_trapspaces`");
		}
	}

	/**
	 * Use this function to by-pass the {@link Attractors#booleanModel}
	 * in order to load a generic boolean model from a <i>.bnet</i> file and compute its attractors
	 * using the <b>BioLQM</b> library.
	 *
	 * @param boolNetFile The absolute file path of the .bnet file
	 */
	public void calculateAttractorsFromBoolNetFile(String boolNetFile) throws Exception {
		// Load model from .bnet file
		LogicalModel boolNetModel = load(boolNetFile, BNetFormat.ID);

		getAttractorsFromLogicalModel(boolNetModel);
	}

	/**
	 * Use Veliz-Cuba <code>BNReduction.sh</code> script to calculate stable states (fixpoints)
	 * for the {@link Attractors#booleanModel boolean model} that was defined in the constructor of the {@link Attractors} Class
	 *
	 * @param directoryOutput name of directory to write the <i>.dat</i> (model in Veliz-Cuba format)
	 *                        and <i>.dat.fp</i> (file with the fixpoints)
	 * @throws IOException
	 */
	public void calculateStableStatesVC(String directoryOutput) throws IOException {
		booleanModel.exportModelToVelizCubaDataFile(directoryOutput);
		String modelName = booleanModel.getModelName();

		// Run the BNReduction script
		String filenameVCFullPath = new File(directoryOutput, modelName + ".dat").getAbsolutePath();
		runBNReduction(filenameVCFullPath);

		// Read stable states from BNReduction.sh output file
		String fixedPointsFile = new File(directoryOutput, modelName + ".dat.fp").getAbsolutePath();

		logger.outputStringMessage(2, "Reading steady states: " + fixedPointsFile);
		ArrayList<String> lines = readLinesFromFile(fixedPointsFile, true);

		attractors.addAll(lines);

		if (attractors.size() > 0 && attractors.get(0).length() > 0) {
			logger.outputStringMessage(1, "BNReduction found " + attractors.size() + " stable states:");
			int count = 0;
			for (String attractor : attractors) {
				logger.outputStringMessage(2, "Stable state " + (++count) + ": " + attractor);
			}
		} else {
			logger.outputStringMessage(1, "BNReduction found no stable states.");
		}

		deleteFilesMatchingPattern(logger, modelName);
	}

	private void runBNReduction(String filenameVC) {
		String BNReductionScriptFile = new File(directoryBNET, "BNReduction.sh").getAbsolutePath();

		try {
			ProcessBuilder pb = null;
			String timeoutSeconds = "30";
			switch(attractorTool) {
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
	 * Does the model have at least one attractor? (stable state or trapspace)
	 *
	 */
	boolean hasAttractors() {
		return attractors.size() > 0;
	}

	/**
	 * Does the model have at least one stable state? (no dashes in the state vector)
	 *
	 */
	boolean hasStableStates() {
		return getStableStates().size() > 0;
	}

	/**
	 * Get all the attractors (stable states or trapspaces)
	 *
	 */
	ArrayList<String> getAttractors() {
		return attractors;
	}

	/**
	 * Get only the attractors that do not have any dashes (<i>-</i>): the stable states
	 *
	 */
	ArrayList<String> getStableStates() {
		return attractors.stream()
			.filter(state -> !state.contains("-"))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Returns an 2-dimensional String array where the first row has the node names and
	 * every other row contains the activity state values (0 or 1 or '-') of the corresponding
	 * node (column) in the attractor (stable state or trapspace)
	 */
	String[][] getAttractorsWithNodes() {
		String[][] result = new String[attractors.size() + 1][booleanModel.getNodeNames().size()];

		result[0] = booleanModel.getNodeNames().toArray(new String[0]);

		for (int i = 0; i < attractors.size(); i++) {
			result[i + 1] = attractors.get(i).split("(?!^)");
		}

		return result;
	}

	/**
	 * This function should be used only for <b>testing purposes</b>. The attractors should always
	 * be calculated with {@link #calculateAttractors(String)} or
	 * {@link #calculateAttractorsFromBoolNetFile(String)}.
	 *
	 * @param attractors
	 */
	void setAttractors(ArrayList<String> attractors) {
		this.attractors = attractors;
	}

	public String getAttractorTool() {
		return attractorTool;
	}
}
