package eu.druglogics.gitsbe;

import static eu.druglogics.gitsbe.Util.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Config {

	private int verbosity;
	private boolean preserve_outputs;
	private boolean preserve_inputs;
	private boolean preserve_tmp_files;
	// hack: set the 3 below values to true if you want to see the exports
	private boolean export_boolean_model = true;
	private boolean export_trimmed_sif;
	private boolean export_ginml;

	private boolean parallel_simulations;
	private int forkJoinPoolSize;
	private int population;
	private int generations;
	private int selection;
	private int crossovers;
	private int mutations;
	private int balancemutations;
	private int randommutations;
	private int complexmutations;
	private int familymutations;
	private int inhibitorymutations;
	private int activatorymutations;
	private int ormutations;
	private int andmutations;
	private int shufflemutations;
	private int topology_mutations;
	private float target_fitness;
	private int bootstrap_mutations_factor;
	private int mutations_factor;
	private int bootstrap_shuffle_factor;
	private int shuffle_factor;
	private int bootstrap_topology_mutations_factor;
	private int topology_mutations_factor;
	private int simulations;
	private int models_saved;
	private float fitness_threshold;

	private String filenameConfig;

	private Logger logger;

	public Config(String filename, Logger logger) throws IOException {
		this.filenameConfig = filename;
		this.logger = logger;

		loadConfigFile(filename);
	}

	private void loadConfigFile(String filename) throws IOException {

		logger.outputStringMessage(3, "Reading config file: " + new File(filename).getAbsolutePath());
		ArrayList<String> lines = readLinesFromFile(filename, true);

		// Process lines
		for (int i = 0; i < lines.size(); i++) {
			System.out.println(lines.get(i));
			String parameterName = lines.get(i).split("\t")[0];
			String value = lines.get(i).split("\t")[1];

			if (lines.get(i).split("\t").length != 2) {
				logger.outputStringMessage(1, "Incorrect line found in config file");
				continue;
			}

			switch (parameterName) {
			case "verbosity:":
				verbosity = Integer.parseInt(value);
				break;

			case "preserve_tmp_files:":
				preserve_tmp_files = Boolean.parseBoolean(value);
				break;

			case "preserve_outputs:":
				preserve_outputs = Boolean.parseBoolean(value);
				break;

			case "preserve_inputs:":
				preserve_inputs = Boolean.parseBoolean(value);
				break;

			case "export_trimmed_sif:":
				export_trimmed_sif = Boolean.parseBoolean(value);
				break;

			case "export_ginml:":
				export_ginml = Boolean.parseBoolean(value);
				break;

			case "export_boolean_model":
				export_boolean_model = Boolean.parseBoolean(value);
				break;

			case "simulations:":
				simulations = Integer.parseInt(value);
				break;

			case "parallel_simulations:":
				parallel_simulations = Boolean.parseBoolean(value);
				break;

			case "forkJoinPoolSize:":
				forkJoinPoolSize = Integer.parseInt(value);

			case "population:":
				population = Integer.parseInt(value);
				break;

			case "generations:":
				generations = Integer.parseInt(value);
				break;

			case "selection:":
				selection = Integer.parseInt(value);
				break;

			case "crossovers:":
				crossovers = Integer.parseInt(value);
				break;

			case "balancemutations:":
				balancemutations = Integer.parseInt(value);
				break;

			case "randommutations:":
				randommutations = Integer.parseInt(value);
				break;

			case "complexmutations:":
				complexmutations = Integer.parseInt(value);
				break;

			case "familymutations:":
				familymutations = Integer.parseInt(value);
				break;

			case "activatorymutations:":
				activatorymutations = Integer.parseInt(value);
				break;

			case "ormutations:":
				ormutations = Integer.parseInt(value);
				break;

			case "andmutations:":
				andmutations = Integer.parseInt(value);
				break;

			case "shufflemutations:":
				shufflemutations = Integer.parseInt(value);
				break;

			case "topology_mutations:":
				topology_mutations = Integer.parseInt(value);
				break;

			case "target_fitness:":
				target_fitness = Float.parseFloat(value);
				break;

			case "bootstrap_mutations_factor:":
				bootstrap_mutations_factor = Integer.parseInt(value);
				break;

			case "mutations_factor:":
				mutations_factor = Integer.parseInt(value);
				break;

			case "bootstrap_shuffle_factor:":
				bootstrap_shuffle_factor = Integer.parseInt(value);
				break;

			case "shuffle_factor:":
				shuffle_factor = Integer.parseInt(value);
				break;

			case "bootstrap_topology_mutations_factor:":
				bootstrap_topology_mutations_factor = Integer.parseInt(value);
				break;

			case "topology_mutations_factor:":
				topology_mutations_factor = Integer.parseInt(value);
				break;

			case "models_saved:":
				models_saved = Integer.parseInt(value);
				break;

			case "fitness_threshold:":
				fitness_threshold = Float.parseFloat(value);
				break;
			}
		}
	}

	public String[] getConfig() {

		String parameters[] = { "preserve_outputs", "preserve_inputs", "preserve_tmp_files", "export_boolean_model",
				"export_trimmed_sif", "export_ginml", "parallel_simulations", "forkJoinPoolSize", "population",
				"generations", "selection", "crossovers", "mutations", "balancemutations", "randommutations",
				"complexmutations", "familymutations", "inhibitorymutations", "activatorymutations", "ormutations",
				"andmutations", "shufflemutations", "topology_mutations", "target_fitness",
				"bootstrap_mutations_factor", "mutations_factor", "bootstrap_shuffle_factor", "shuffle_factor",
				"bootstrap_topology_mutations_factor", "topology_mutations_factor", "simulations", "models_saved",
				"fitness_threshold" };

		String values[] = { Boolean.toString(preserve_outputs), Boolean.toString(preserve_inputs),
				Boolean.toString(preserve_tmp_files), Boolean.toString(export_boolean_model),
				Boolean.toString(export_trimmed_sif), Boolean.toString(export_ginml),
				Boolean.toString(parallel_simulations), Integer.toString(forkJoinPoolSize),
				Integer.toString(population), Integer.toString(generations), Integer.toString(selection),
				Integer.toString(crossovers), Integer.toString(mutations), Integer.toString(balancemutations),
				Integer.toString(randommutations), Integer.toString(complexmutations),
				Integer.toString(familymutations), Integer.toString(inhibitorymutations),
				Integer.toString(activatorymutations), Integer.toString(ormutations), Integer.toString(andmutations),
				Integer.toString(shufflemutations), Integer.toString(topology_mutations),
				Float.toString(target_fitness), Integer.toString(bootstrap_mutations_factor),
				Integer.toString(mutations_factor), Integer.toString(bootstrap_shuffle_factor),
				Integer.toString(shuffle_factor), Integer.toString(bootstrap_topology_mutations_factor),
				Integer.toString(topology_mutations_factor), Integer.toString(simulations),
				Integer.toString(models_saved), Float.toString(fitness_threshold) };

		ArrayList<String> lines = new ArrayList<String>();

		for (int i = 0; i < parameters.length; i++) {
			lines.add(parameters[i] + ": " + values[i]);
		}

		// adding an empty line for visibility purposes :)
		lines.add(parameters.length, "");
		return lines.toArray(new String[0]);

	}

	public static void writeConfigFileTemplate(String filename) throws IOException {
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		// Write header with '#'
		writer.println("# Gitsbe config file");
		writer.println("# Each line is a parameter name and value");
		writer.println("# Default parameters are given below");
		writer.println("#");
		writer.println("# Parameter\tValue");
		writer.println("#");

		// Write parameters
		writer.println("# Output verbosity level");
		writer.println("verbosity:\t3");
		writer.println();
		writer.println(
				"# Preserve all temporary files in project tmp-folder (all model files generation by evolutionary algorithms");
		writer.println("preserve_tmp_files:\tfalse");
		writer.println();
		writer.println("# Model trimming");
		writer.println("# Preserve outputs");
		writer.println("preserve_outputs:\ttrue");
		writer.println();
		writer.println("# Preserve inputs");
		writer.println("preserve_inputs:\tfalse");
		writer.println();
		writer.println("# Parameters for evolutionary algorithms");
		writer.println("# Number of simulations (evolutions) to run");
		writer.println("simulations:\t3");
		writer.println();
		writer.println("# Run simulations in parallel");
		writer.println("parallel_simulations:\ttrue");
		writer.println();
		writer.println("# Maximum number (>1) of allowed parallel simulations");
		writer.print("# The standard value would be to have as many parallel simulations as the machine's Cores ");
		writer.println("(reduce it if too many parallel simulations are causing issues)");
		writer.println("forkJoinPoolSize:\t4");
		writer.println();
		writer.println("# Number of generations per simulation (or less if target_fitness is reached, see below)");
		writer.println("generations:\t10");
		writer.println();
		writer.println("# Number of models per generation");
		writer.println("population:\t10");
		writer.println();
		writer.println("# Number of crossovers");
		writer.println("crossovers:\t1");
		writer.println();
		writer.println("# Number of models selected for next generation");
		writer.println("selection:\t3");
		writer.println();
		writer.println("# Type of mutations to introduce");
		writer.println("balancemutations:\t5");
		writer.println("randommutations:\t0");
		writer.println("complexmutations:\t0");
		writer.println("familymutations:\t0");
		writer.println("inhibitorymutations:\t0");
		writer.println("activatorymutations:\t0");
		writer.println("ormutations:\t0");
		writer.println("andmutations:\t0");
		writer.println("shufflemutations:\t0");
		writer.println("topology_mutations:\t10");
		writer.println();
		writer.println("# Target fitness threshold to stop evolution (1 is the absolute maximum value)");
		writer.println("target_fitness:\t0.9");
		writer.println();
		writer.println(
				"# Factor to multiply number of mutations until initial phase is over (>0 stable states obtained)");
		writer.println("bootstrap_mutations_factor:\t1000");
		writer.println();
		writer.println(
				"# Factor to multiply number of mutations after initial phase is over (>0 stable states obtained)");
		writer.println("mutations_factor:\t1");
		writer.println();
		writer.println("# Factor to multiply number of regulator priority shuffles until initial phase is over");
		writer.println("bootstrap_shuffle_factor:\t0");
		writer.println();
		writer.println("# Factor to multiply number of regulator priority shuffles after initial phase is over");
		writer.println("shuffle_factor:\t0");
		writer.println();
		writer.println("# Factor to multiply number of topology mutations until initial phase is over");
		writer.println("bootstrap_topology_mutations_factor:\t5");
		writer.println();
		writer.println("# Factor to multiply number of topology mutations after initial phase is over");
		writer.println("topology_mutations_factor:\t1");
		writer.println();
		writer.println("# Number of models to save");
		writer.println("models_saved:\t3");
		writer.println();
		writer.println("# Threshold for saving models");
		writer.println("fitness_threshold:\t0.1");

		writer.flush();
		writer.close();
	}

	public int getVerbosity() {
		return verbosity;
	}

	public boolean isPreserve_outputs() {
		return preserve_outputs;
	}

	public boolean isPreserve_inputs() {
		return preserve_inputs;
	}

	public boolean isPreserve_tmp_files() {
		return preserve_tmp_files;
	}

	public boolean isExportBoolean_model() {
		return export_boolean_model;
	}

	public boolean isExportTrimmedSif() {
		return export_trimmed_sif;
	}

	public boolean isExportGinml() {
		return export_ginml;
	}

	/**
	 * Get number of models in current generation
	 * 
	 * @return
	 */
	public int getPopulation() {
		return population;
	}

	public int getGenerations() {
		return generations;
	}

	/**
	 * Number of (best) models selected for next generation
	 * 
	 * @return
	 */
	public int getSelection() {
		return selection;
	}

	public int getCrossovers() {
		return crossovers;
	}

	public int getMutations() {
		return mutations;
	}

	public int getBalancemutations() {
		return balancemutations;
	}

	public int getRandommutations() {
		return randommutations;
	}

	public int getComplexmutations() {
		return complexmutations;
	}

	public int getFamilymutations() {
		return familymutations;
	}

	public int getInhibitorymutations() {
		return inhibitorymutations;
	}

	public int getActivatorymutations() {
		return activatorymutations;
	}

	public int getOrmutations() {
		return ormutations;
	}

	public int getAndmutations() {
		return andmutations;
	}

	public float getTarget_fitness() {
		return target_fitness;
	}

	public int getBootstrap_mutations_factor() {
		return bootstrap_mutations_factor;
	}

	/**
	 * Number of times (simulations) to run the evolutionary algorithm
	 * 
	 * @return
	 */
	public int getSimulations() {
		return simulations;
	}

	/**
	 * Returns true or false whether the simulations will run in parallel or not
	 * 
	 * @return
	 */
	public boolean runParallelSimulations() {
		return parallel_simulations;
	}

	public int getForkJoinPoolSize() {
		return forkJoinPoolSize;
	}

	public int getModels_saved() {
		return models_saved;
	}

	public float getFitness_threshold() {
		return fitness_threshold;
	}

	public String getFilenameConfig() {
		return filenameConfig;
	}

	/**
	 * @return the mutations_factor
	 */
	public int getMutations_factor() {
		return mutations_factor;
	}

	/**
	 * @return the shuffle_factor
	 */
	public int getShuffle_factor() {
		return shuffle_factor;
	}

	/**
	 * @return the shuffle mutations
	 */
	public int getShufflemutations() {
		return shufflemutations;
	}

	/**
	 * @return the bootstrap_shuffle_factor
	 */
	public int getBootstrap_shuffle_factor() {
		return bootstrap_shuffle_factor;
	}

	/**
	 * @return number of topology mutations
	 */
	public int getTopologyMutations() {
		return topology_mutations;
	}

	public int getBootstrap_topology_mutations_factor() {
		return bootstrap_topology_mutations_factor;

	}

	public int getTopology_mutations_factor() {
		return topology_mutations_factor;
	}

}
