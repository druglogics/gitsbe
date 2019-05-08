package eu.druglogics.gitsbe.input;

import javax.naming.ConfigurationException;
import java.lang.reflect.Field;
import java.util.Arrays;

public class ConfigParametersGitsbe extends ConfigParametersGlobal{

    // Model trimming
    boolean remove_output_nodes;
    boolean remove_input_nodes;

    // Exporting initial network file
    boolean export_to_gitsbe;
    boolean export_to_sif;
    boolean export_to_ginml;
    boolean export_to_boolnet;
    boolean export_to_sbml_qual;

    // Parameters for the simulations
    int population;
    int generations;
    int selection;
    int crossovers;
    int balance_mutations;
    int random_mutations;
    int shuffle_mutations;
    int topology_mutations;
    float target_fitness;
    int bootstrap_mutations_factor;
    int mutations_factor;
    int bootstrap_shuffle_factor;
    int shuffle_factor;
    int bootstrap_topology_mutations_factor;
    int topology_mutations_factor;
    int simulations;
    int models_saved;
    boolean best_models_export_to_boolnet;
    boolean best_models_export_to_ginml;
    boolean best_models_export_to_sbml_qual;
    float fitness_threshold;

    public boolean exportToGitsbe() {
        return export_to_gitsbe;
    }

    public boolean exportToSif() {
        return export_to_sif;
    }

    public boolean exportToGINML() {
        return export_to_ginml;
    }

    public boolean exportToBoolNet() {
        return export_to_boolnet;
    }

    public boolean exportToSBMLQual() {
        return export_to_sbml_qual;
    }

    public boolean removeOutputNodes() {
        return remove_output_nodes;
    }

    public boolean removeInputNodes() {
        return remove_input_nodes;
    }

    // Get number of models in current generation
    public int getPopulation() {
        return population;
    }

    public int getGenerations() {
        return generations;
    }

    // Number of (best) models selected for next generation
    public int getSelection() {
        return selection;
    }

    public int getCrossovers() {
        return crossovers;
    }

    public int getBalanceMutations() {
        return balance_mutations;
    }

    public int getRandomMutations() {
        return random_mutations;
    }

    public int getShuffleMutations() {
        return shuffle_mutations;
    }

    public int getTopologyMutations() {
        return topology_mutations;
    }

    public float getTargetFitness() {
        return target_fitness;
    }

    void checkTargetFitness() throws ConfigurationException {
        if (target_fitness < 0.0 || target_fitness > 1.0)
            throw new ConfigurationException("Parameter `target_fitness` "
                    + "should be between 0 and 1");
    }

    public int getBootstrapMutationsFactor() {
        return bootstrap_mutations_factor;
    }

    public int getMutationsFactor() {
        return mutations_factor;
    }

    public int getBootstrapShuffleFactor() {
        return bootstrap_shuffle_factor;
    }

    public int getShuffleFactor() {
        return shuffle_factor;
    }

    public int getBootstrapTopologyMutationsFactor() {
        return bootstrap_topology_mutations_factor;
    }

    public int getTopologyMutationsFactor() {
        return topology_mutations_factor;
    }

    // Number of times (simulations) to run the evolutionary algorithm
    public int getSimulations() {
        return simulations;
    }

    public int getNumOfModelsToSave() {
        return models_saved;
    }

    public boolean exportBestModelsToBoolNet() {
        return best_models_export_to_boolnet;
    }

    public boolean exportBestModelsToGINML() {
        return best_models_export_to_ginml;
    }

    public boolean exportBestModelsToSBMLQual() {
        return best_models_export_to_sbml_qual;
    }

    public float getFitnessThreshold() {
        return fitness_threshold;
    }

    void checkFitnessThreshold() throws ConfigurationException {
        if (fitness_threshold < 0.0 || fitness_threshold > 1.0)
            throw new ConfigurationException("Parameter `fitness_threshold` "
                    + "should be between 0 and 1");
    }

    public String[] getParameters() {
        Field[] fields = ConfigParametersGitsbe.class.getDeclaredFields();

        return Arrays.stream(fields).map(Field::getName).toArray(String[]::new);
    }
}
