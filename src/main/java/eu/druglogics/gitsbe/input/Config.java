package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.util.Logger;

import static eu.druglogics.gitsbe.util.Util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config extends ConfigParametersGitsbe {

	private static Config config = null;

	private Logger logger;
	private LinkedHashMap<String, String> parameterMap;

	private Config(String filename, Logger logger) throws Exception {
		this.logger = logger;
		loadConfigFile(filename);
	}

	public static Config getInstance() {
		// To ensure only one instance is created
		if (config == null) {
			throw new AssertionError("You have to call init first");
		}
		return config;
	}

	public synchronized static void init(String filename, Logger logger) throws Exception {
		if (config != null) {
			throw new AssertionError("You already initialized me");
		}
		config = new Config(filename, logger);
	}

	private void loadConfigFile(String filename) throws Exception {

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

			parameterMap.put(parameterName, value);

			switch (parameterName) {
				case "verbosity":
					verbosity = Integer.parseInt(value);
					break;

				case "delete_tmp_files":
					delete_tmp_files = Boolean.parseBoolean(value);
					break;

				case "compress_log_and_tmp_files":
					compress_log_and_tmp_files = Boolean.parseBoolean(value);
					break;

				case "use_parallel_sim":
					use_parallel_sim = Boolean.parseBoolean(value);
					break;

				case "parallel_sim_num":
					parallel_sim_num = Integer.parseInt(value);
					break;

				case "attractor_tool":
					attractor_tool = value;
					checkAttractorTool();
					break;

				case "remove_output_nodes":
					remove_output_nodes = Boolean.parseBoolean(value);
					break;

				case "remove_input_nodes":
					remove_input_nodes = Boolean.parseBoolean(value);
					break;

				case "export_to_gitsbe":
					export_to_gitsbe = Boolean.parseBoolean(value);
					break;

				case "export_to_sif":
					export_to_sif = Boolean.parseBoolean(value);
					break;

				case "export_to_ginml":
					export_to_ginml = Boolean.parseBoolean(value);
					break;

				case "export_to_boolnet":
					export_to_boolnet = Boolean.parseBoolean(value);
					break;

				case "export_to_sbml_qual":
					export_to_sbml_qual = Boolean.parseBoolean(value);
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

				case "best_models_export_to_boolnet":
					best_models_export_to_boolnet = Boolean.parseBoolean(value);
					break;

				case "best_models_export_to_ginml":
					best_models_export_to_ginml = Boolean.parseBoolean(value);
					break;

				case "best_models_export_to_sbml_qual":
					best_models_export_to_sbml_qual = Boolean.parseBoolean(value);
					break;

				case "fitness_threshold":
					fitness_threshold = Float.parseFloat(value);
					break;
			}
		}
	}

	public String[] getConfig() {

		ArrayList<String> lines = new ArrayList<>();

		for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
			lines.add(entry.getKey() + ": " + entry.getValue());
		}

		// adding an empty line for visibility purposes :)
		lines.add(parameterMap.size(), "");
		return lines.toArray(new String[0]);
	}
}
