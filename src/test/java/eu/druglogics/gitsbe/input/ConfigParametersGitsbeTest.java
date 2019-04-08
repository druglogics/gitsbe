package eu.druglogics.gitsbe.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigParametersGitsbeTest {

    @Test
    void test_get_parameter_names() {
        ConfigParametersGitsbe parameters = new ConfigParametersGitsbe();
        int expectedParNum = 23;

        String[] pars = parameters.getParameters();

        assertEquals(expectedParNum, pars.length);

        assertEquals(pars[0], "remove_output_nodes");
        assertEquals(pars[1], "remove_input_nodes");
        assertEquals(pars[2], "export_to_gitsbe");
        assertEquals(pars[3], "export_to_sif");
        assertEquals(pars[4], "export_to_ginml");
        assertEquals(pars[5], "population");
        assertEquals(pars[6], "generations");
        assertEquals(pars[7], "selection");
        assertEquals(pars[8], "crossovers");
        assertEquals(pars[9], "balance_mutations");
        assertEquals(pars[10], "random_mutations");
        assertEquals(pars[11], "shuffle_mutations");
        assertEquals(pars[12], "topology_mutations");
        assertEquals(pars[13], "target_fitness");
        assertEquals(pars[14], "bootstrap_mutations_factor");
        assertEquals(pars[15], "mutations_factor");
        assertEquals(pars[16], "bootstrap_shuffle_factor");
        assertEquals(pars[17], "shuffle_factor");
        assertEquals(pars[18], "bootstrap_topology_mutations_factor");
        assertEquals(pars[19], "topology_mutations_factor");
        assertEquals(pars[20], "simulations");
        assertEquals(pars[21], "models_saved");
        assertEquals(pars[22], "fitness_threshold");
    }
}
