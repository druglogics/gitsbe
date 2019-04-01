package eu.druglogics.gitsbe.input;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ConfigParameters {

    // Global section
    public int verbosity;
    public boolean preserve_tmp_files;
    public boolean compress_log_and_temporary_files;
    public boolean parallel_simulations;
    public int fork_join_pool_size;
    public String attractor_tool;

    // Gitsbe section

    // Model trimming
    public boolean preserve_outputs;
    public boolean preserve_inputs;
    // Exporting network file
    public boolean export_gitsbe_model;
    public boolean export_trimmed_sif;
    public boolean export_ginml;
    // Parameters for the simulations
    public int population;
    public int generations;
    public int selection;
    public int crossovers;
    public int balance_mutations;
    public int random_mutations;
    public int shuffle_mutations;
    public int topology_mutations;
    public float target_fitness;
    public int bootstrap_mutations_factor;
    public int mutations_factor;
    public int bootstrap_shuffle_factor;
    public int shuffle_factor;
    public int bootstrap_topology_mutations_factor;
    public int topology_mutations_factor;
    public int simulations;
    public int models_saved;
    public float fitness_threshold;

    // Drabme section
    public int combination_size;

    public int getVerbosity() {
        return verbosity;
    }

    public boolean isPreserve_tmp_files() {
        return preserve_tmp_files;
    }

    /**
     * Returns true or false whether the simulations will run in parallel or not
     *
     * @return
     */
    public boolean isParallel_simulations() {
        return parallel_simulations;
    }

    public int getFork_join_pool_size() {
        return fork_join_pool_size;
    }

    public String getAttractor_tool() {
        return attractor_tool;
    }

    public boolean isExport_gitsbe_model() {
        return export_gitsbe_model;
    }

    public boolean isExport_trimmed_sif() {
        return export_trimmed_sif;
    }

    public boolean isExport_ginml() {
        return export_ginml;
    }

    public boolean isPreserve_outputs() {
        return preserve_outputs;
    }

    public boolean isPreserve_inputs() {
        return preserve_inputs;
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

    public int getBalance_mutations() {
        return balance_mutations;
    }

    public int getRandom_mutations() {
        return random_mutations;
    }

    public int getShuffle_mutations() {
        return shuffle_mutations;
    }

    public int getTopology_mutations() {
        return topology_mutations;
    }

    public float getTarget_fitness() {
        return target_fitness;
    }

    public int getBootstrap_mutations_factor() {
        return bootstrap_mutations_factor;
    }

    public int getMutations_factor() {
        return mutations_factor;
    }

    public int getBootstrap_shuffle_factor() {
        return bootstrap_shuffle_factor;
    }

    public int getShuffle_factor() {
        return shuffle_factor;
    }

    public int getBootstrap_topology_mutations_factor() {
        return bootstrap_topology_mutations_factor;
    }

    public int getTopology_mutations_factor() {
        return topology_mutations_factor;
    }

    /**
     * Number of times (simulations) to run the evolutionary algorithm
     *
     * @return
     */
    public int getSimulations() {
        return simulations;
    }

    public int getModels_saved() {
        return models_saved;
    }

    public float getFitness_threshold() {
        return fitness_threshold;
    }

    public boolean isCompress_log_and_temporary_files() {
        return compress_log_and_temporary_files;
    }

    public int getCombination_size() {
        return combination_size;
    }

    public String[] getParameters() {
        Field[] fields = ConfigParameters.class.getFields();

        return Arrays.stream(fields).map(Field::getName).toArray(String[]::new);
    }
}
