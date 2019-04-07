package eu.druglogics.gitsbe.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigParametersTest {

    @Test
    void test_get_parameter_names() {
        ConfigParameters parameters = new ConfigParameters();
        int expectedParNum = 30;

        String[] pars = parameters.getParameters();

        assertEquals(expectedParNum, pars.length);

        assertEquals(pars[0], "verbosity");
        assertEquals(pars[1], "delete_tmp_files");
        assertEquals(pars[2], "compress_log_and_tmp_files");
        assertEquals(pars[3], "use_parallel_sim");
        assertEquals(pars[4], "parallel_sim_num");
        assertEquals(pars[5], "attractor_tool");
        assertEquals(pars[6], "remove_output_nodes");
        assertEquals(pars[7], "remove_input_nodes");
        assertEquals(pars[8], "export_to_gitsbe");
        assertEquals(pars[9], "export_to_sif");
        assertEquals(pars[10], "export_to_ginml");
        assertEquals(pars[11], "population");
        assertEquals(pars[12], "generations");
        assertEquals(pars[13], "selection");
        assertEquals(pars[14], "crossovers");
        assertEquals(pars[15], "balance_mutations");
        assertEquals(pars[16], "random_mutations");
        assertEquals(pars[17], "shuffle_mutations");
        assertEquals(pars[18], "topology_mutations");
        assertEquals(pars[19], "target_fitness");
        assertEquals(pars[20], "bootstrap_mutations_factor");
        assertEquals(pars[21], "mutations_factor");
        assertEquals(pars[22], "bootstrap_shuffle_factor");
        assertEquals(pars[23], "shuffle_factor");
        assertEquals(pars[24], "bootstrap_topology_mutations_factor");
        assertEquals(pars[25], "topology_mutations_factor");
        assertEquals(pars[26], "simulations");
        assertEquals(pars[27], "models_saved");
        assertEquals(pars[28], "fitness_threshold");
        assertEquals(pars[29], "max_drug_comb_size");
    }
}
