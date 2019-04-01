package eu.druglogics.gitsbe.input;

import org.junit.Assert;
import org.junit.Test;

public class ConfigParametersTest {

    @Test
    public void test_get_parameter_names() {
        ConfigParameters parameters = new ConfigParameters();
        int expectedParNum = 30;

        String[] pars = parameters.getParameters();

        Assert.assertEquals(expectedParNum, pars.length);

        Assert.assertEquals(pars[0], "verbosity");
        Assert.assertEquals(pars[1], "delete_tmp_files");
        Assert.assertEquals(pars[2], "compress_log_and_tmp_files");
        Assert.assertEquals(pars[3], "use_parallel_sim");
        Assert.assertEquals(pars[4], "parallel_sim_num");
        Assert.assertEquals(pars[5], "attractor_tool");
        Assert.assertEquals(pars[6], "remove_output_nodes");
        Assert.assertEquals(pars[7], "remove_input_nodes");
        Assert.assertEquals(pars[8], "export_to_gitsbe");
        Assert.assertEquals(pars[9], "export_to_sif");
        Assert.assertEquals(pars[10], "export_to_ginml");
        Assert.assertEquals(pars[11], "population");
        Assert.assertEquals(pars[12], "generations");
        Assert.assertEquals(pars[13], "selection");
        Assert.assertEquals(pars[14], "crossovers");
        Assert.assertEquals(pars[15], "balance_mutations");
        Assert.assertEquals(pars[16], "random_mutations");
        Assert.assertEquals(pars[17], "shuffle_mutations");
        Assert.assertEquals(pars[18], "topology_mutations");
        Assert.assertEquals(pars[19], "target_fitness");
        Assert.assertEquals(pars[20], "bootstrap_mutations_factor");
        Assert.assertEquals(pars[21], "mutations_factor");
        Assert.assertEquals(pars[22], "bootstrap_shuffle_factor");
        Assert.assertEquals(pars[23], "shuffle_factor");
        Assert.assertEquals(pars[24], "bootstrap_topology_mutations_factor");
        Assert.assertEquals(pars[25], "topology_mutations_factor");
        Assert.assertEquals(pars[26], "simulations");
        Assert.assertEquals(pars[27], "models_saved");
        Assert.assertEquals(pars[28], "fitness_threshold");
        Assert.assertEquals(pars[29], "max_drug_comb_size");
    }
}
