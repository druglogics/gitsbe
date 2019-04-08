package eu.druglogics.gitsbe.input;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ConfigParametersGitsbe extends ConfigParametersGlobal{

    // Model trimming
    public boolean remove_output_nodes;
    public boolean remove_input_nodes;

    // Exporting initial network file
    public boolean export_to_gitsbe;
    public boolean export_to_sif;
    public boolean export_to_ginml;

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

    public boolean exportToGitsbeFormat() {
        return export_to_gitsbe;
    }

    public boolean exportToSif() {
        return export_to_sif;
    }

    public boolean exportToGinML() {
        return export_to_ginml;
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

    public float getFitnessThreshold() {
        return fitness_threshold;
    }

    public String[] getParameters() {
        Field[] fields = ConfigParametersGitsbe.class.getDeclaredFields();

        return Arrays.stream(fields).map(Field::getName).toArray(String[]::new);
    }
}
