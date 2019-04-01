package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.util.Logger;

import static eu.druglogics.gitsbe.util.Util.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Config extends ConfigParameters {

	private Logger logger;
	private HashMap<String, String> parameterMap;

	public Config(String filename, Logger logger) throws IOException {
		this.logger = logger;
		loadConfigFile(filename);
	}

	private void loadConfigFile(String filename) throws IOException {

		logger.outputStringMessage(3, "Reading config file: " + new File(filename).getAbsolutePath());
		ArrayList<String> lines = readLinesFromFile(filename, true);
		parameterMap = new LinkedHashMap<>();

		// Process lines
		for (String line: lines) {
			String parameterName = removeLastChar(line.split("\t")[0]);
			String value = line.split("\t")[1];

			if (line.split("\t").length != 2) {
				logger.outputStringMessage(1, "Incorrect line found in config file");
				continue;
			}

			parameterMap.put(parameterName,value);

			switch (parameterName) {
				case "verbosity":
					verbosity = Integer.parseInt(value);
					break;

				case "preserve_tmp_files":
					preserve_tmp_files = Boolean.parseBoolean(value);
					break;

				case "compress_log_and_temporary_files":
					compress_log_and_temporary_files = Boolean.parseBoolean(value);
					break;

				case "parallel_simulations":
					parallel_simulations = Boolean.parseBoolean(value);
					break;

				case "fork_join_pool_size":
					fork_join_pool_size = Integer.parseInt(value);
					break;

				case "attractor_tool":
					attractor_tool = value;
					checkAttractorTool();
					break;

				case "preserve_outputs":
					preserve_outputs = Boolean.parseBoolean(value);
					break;

				case "preserve_inputs":
					preserve_inputs = Boolean.parseBoolean(value);
					break;

				case "export_gitsbe_model":
					export_gitsbe_model = Boolean.parseBoolean(value);
					break;

				case "export_trimmed_sif":
					export_trimmed_sif = Boolean.parseBoolean(value);
					break;

				case "export_ginml":
					export_ginml = Boolean.parseBoolean(value);
					break;

				case "population":
					population = Integer.parseInt(value);
					break;

				case "generations":
					generations = Integer.parseInt(value);
					break;

				case "selection":
					selection = Integer.parseInt(value);
					break;

				case "crossovers":
					crossovers = Integer.parseInt(value);
					break;

				case "balance_mutations":
					balance_mutations = Integer.parseInt(value);
					break;

				case "random_mutations":
					random_mutations = Integer.parseInt(value);
					break;

				case "shuffle_mutations":
					shuffle_mutations = Integer.parseInt(value);
					break;

				case "topology_mutations":
					topology_mutations = Integer.parseInt(value);
					break;

				case "target_fitness":
					target_fitness = Float.parseFloat(value);
					break;

				case "bootstrap_mutations_factor":
					bootstrap_mutations_factor = Integer.parseInt(value);
					break;

				case "mutations_factor":
					mutations_factor = Integer.parseInt(value);
					break;

				case "bootstrap_shuffle_factor":
					bootstrap_shuffle_factor = Integer.parseInt(value);
					break;

				case "shuffle_factor":
					shuffle_factor = Integer.parseInt(value);
					break;

				case "bootstrap_topology_mutations_factor":
					bootstrap_topology_mutations_factor = Integer.parseInt(value);
					break;

				case "topology_mutations_factor":
					topology_mutations_factor = Integer.parseInt(value);
					break;

				case "simulations":
					simulations = Integer.parseInt(value);
					break;

				case "models_saved":
					models_saved = Integer.parseInt(value);
					break;

				case "fitness_threshold":
					fitness_threshold = Float.parseFloat(value);
					break;

				case "combination_size":
					combination_size = Integer.parseInt(value);
					break;

				default:
					logger.outputStringMessage(0,"No parameter exists with the name: " + parameterName);
					System.exit(1);
			}
		}
	}

	private void checkAttractorTool() {
		if (!AttractorTools.contains(attractor_tool)) {
			logger.outputStringMessage(0,"The attractor_tool value: " + attractor_tool + " is not in the list of supported tools");
			logger.outputStringMessage(0,"The bnet_reduction_reduced value will be used for the analysis.");

			attractor_tool = AttractorTools.BNREDUCTION_REDUCED.getTool();
		}
	}

	public String[] getConfig() {

		ArrayList<String> lines = new ArrayList<>();

		for (HashMap.Entry<String, String> entry : parameterMap.entrySet()) {
			lines.add(entry.getKey() + ": " + entry.getValue());
		}

		// adding an empty line for visibility purposes :)
		lines.add(parameterMap.size(), "");
		return lines.toArray(new String[0]);
	}

	public static void writeConfigFileTemplate(String filename) throws IOException {
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		// Write header with '#'
		writer.println("# Gitsbe config file");
		writer.println("# Each line is a parameter name and value");
		writer.println("# Default parameters are given below");
		writer.println("#");
		writer.println("# Parameter:\tValue");
		writer.println("#");

		// Write parameters
		writer.println("# Output verbosity level");
		writer.println("verbosity:\t3");
		writer.println();
		writer.println("Check the run_example_ags/toy_ags_config.tab for a full configuration file");

		writer.flush();
		writer.close();
	}
}
