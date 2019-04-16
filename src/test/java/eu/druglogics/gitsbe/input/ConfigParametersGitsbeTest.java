package eu.druglogics.gitsbe.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigParametersGitsbeTest {

    @Test
    void test_get_parameter_names() {
        ConfigParametersGitsbe parameters = new ConfigParametersGitsbe();
        int expectedParNum = 25;

        String[] pars = parameters.getParameters();

        assertEquals(expectedParNum, pars.length);

        assertEquals(pars[0], "remove_output_nodes");
        assertEquals(pars[1], "remove_input_nodes");
        assertEquals(pars[2], "export_to_gitsbe");
        assertEquals(pars[3], "export_to_sif");
        assertEquals(pars[4], "export_to_ginml");
        assertEquals(pars[5], "export_to_boolnet");
        assertEquals(pars[6], "population");
        assertEquals(pars[7], "generations");
        assertEquals(pars[8], "selection");
        assertEquals(pars[9], "crossovers");
        assertEquals(pars[10], "balance_mutations");
        assertEquals(pars[11], "random_mutations");
        assertEquals(pars[12], "shuffle_mutations");
        assertEquals(pars[13], "topology_mutations");
        assertEquals(pars[14], "target_fitness");
        assertEquals(pars[15], "bootstrap_mutations_factor");
        assertEquals(pars[16], "mutations_factor");
        assertEquals(pars[17], "bootstrap_shuffle_factor");
        assertEquals(pars[18], "shuffle_factor");
        assertEquals(pars[19], "bootstrap_topology_mutations_factor");
        assertEquals(pars[20], "topology_mutations_factor");
        assertEquals(pars[21], "simulations");
        assertEquals(pars[22], "models_saved");
        assertEquals(pars[23], "best_models_export_to_boolnet");
        assertEquals(pars[24], "fitness_threshold");
    }
}
