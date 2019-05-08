package eu.druglogics.gitsbe.input;

import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigParametersGitsbeTest {

    @Test
    void test_get_parameter_names() {
        ConfigParametersGitsbe parameters = new ConfigParametersGitsbe();
        int expectedParNum = 28;

        String[] pars = parameters.getParameters();

        assertEquals(expectedParNum, pars.length);

        assertEquals(pars[0], "remove_output_nodes");
        assertEquals(pars[1], "remove_input_nodes");
        assertEquals(pars[2], "export_to_gitsbe");
        assertEquals(pars[3], "export_to_sif");
        assertEquals(pars[4], "export_to_ginml");
        assertEquals(pars[5], "export_to_boolnet");
        assertEquals(pars[6], "export_to_sbml_qual");
        assertEquals(pars[7], "population");
        assertEquals(pars[8], "generations");
        assertEquals(pars[9], "selection");
        assertEquals(pars[10], "crossovers");
        assertEquals(pars[11], "balance_mutations");
        assertEquals(pars[12], "random_mutations");
        assertEquals(pars[13], "shuffle_mutations");
        assertEquals(pars[14], "topology_mutations");
        assertEquals(pars[15], "target_fitness");
        assertEquals(pars[16], "bootstrap_mutations_factor");
        assertEquals(pars[17], "mutations_factor");
        assertEquals(pars[18], "bootstrap_shuffle_factor");
        assertEquals(pars[19], "shuffle_factor");
        assertEquals(pars[20], "bootstrap_topology_mutations_factor");
        assertEquals(pars[21], "topology_mutations_factor");
        assertEquals(pars[22], "simulations");
        assertEquals(pars[23], "models_saved");
        assertEquals(pars[24], "best_models_export_to_boolnet");
        assertEquals(pars[25], "best_models_export_to_ginml");
        assertEquals(pars[26], "best_models_export_to_sbml_qual");
        assertEquals(pars[27], "fitness_threshold");
    }

    @Test
    void test_check_fitness_threshold() {
        ConfigParametersGitsbe parameters = new ConfigParametersGitsbe();

        // Default value is float 0
        assertThat(parameters.getFitnessThreshold()).isEqualTo((float) 0);

        parameters.fitness_threshold = (float) 1.2;
        assertThrows(ConfigurationException.class, parameters::checkFitnessThreshold);
    }

    @Test
    void test_check_target_fitness() {
        ConfigParametersGitsbe parameters = new ConfigParametersGitsbe();

        // Default value is 0
        assertThat(parameters.getTargetFitness()).isEqualTo((float) 0);

        parameters.target_fitness = (float) 1.2;
        assertThrows(ConfigurationException.class, parameters::checkTargetFitness);
    }
}
