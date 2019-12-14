package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.util.Logger;
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
	private ArrayList<String> attractors; // stable states or trapspaces (ss with '-' dashes)

	public Attractors(BooleanModel booleanModel, Logger logger, String attractorTool) {
		this.booleanModel = booleanModel;
		this.logger = logger;
		this.attractorTool = attractorTool;
		this.attractors = new ArrayList<>();
	}

	/**
	 * Use this function to calculate the attractors of the boolean model given in the
	 * constructor of the <i>Attractors</i> Class. The choice of the library/tool that will
	 * be used is based on the <i>AttractorTool</i> String value of the same class.
	 *
	 * @param directoryOutput the name of the directory which will be used to write result
	 *                        files, the model in .bnet or Veliz-Cuba format, etc.
	 * @throws Exception
	 */
	public void calculateAttractors(String directoryOutput) throws Exception {
		if (attractorTool.startsWith("bnet_"))
			calculateStableStatesVC(directoryOutput);
		else
			calculateAttractorsBioLQM(directoryOutput);
	}

	/**
	 * Use this function to find the attractors of the boolean model that was
	 * defined in the constructor of the <i>Attractors</i> Class using the BioLQM library.
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
	}

	/**
	 * This function uses BioLQM's <i>FixpointService</i> and <i>TrapSpaceService</i> Class
	 * to calculate the attractors (stable states/fixpoints or terminal trapspaces using BDD's -
	 * binary decision diagrams) of the <i>boolNetModel</i>.
	 *
	 * @param boolNetModel a logical model, instance of the BioLQM's <i>LogicalModel</i> Class
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
			throw new ConfigurationException("Attractor tool is neither `biolqm_stable_states` nor" +
				"`biolqm_trapspaces`");
		}
	}

	/**
	 * Use this function to by-pass the booleanModel defined in the <i>Attractors</i> Class
	 * in order to load a generic boolean model from a .bnet file and compute its attractors
	 * using the BioLQM library.
	 *
	 * @param boolNetFile The absolute file path of the .bnet file
	 */
	public void calculateAttractorsFromBoolNetFile(String boolNetFile) throws Exception {
		// Load model from .bnet file
		LogicalModel boolNetModel = load(boolNetFile, BNetFormat.ID);

		getAttractorsFromLogicalModel(boolNetModel);
	}

	/**
	 * Use Veliz-Cuba BNReduction.sh script to calculate stable states (fixpoints)
	 * for the boolean model that was defined in the constructor of the <i>Attractors</i> Class
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
	 * @return
	 */
	boolean hasAttractors() {
		return attractors.size() > 0;
	}

	/**
	 * Does the model have at least one stable state? (no dashes in the state vector)
	 *
	 * @return
	 */
	boolean hasStableStates() {
		return getStableStates().size() > 0;
	}

	ArrayList<String> getAttractors() {
		return attractors;
	}

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
}
