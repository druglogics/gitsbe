package eu.druglogics.gitsbe.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigParametersGlobalTest {

    @Test
    void test_get_parameter_names() {
        ConfigParametersGlobal parameters = new ConfigParametersGlobal();
        int expectedParNum = 6;

        String[] pars = parameters.getParameters();

        assertEquals(expectedParNum, pars.length);

        assertEquals(pars[0], "verbosity");
        assertEquals(pars[1], "delete_tmp_files");
        assertEquals(pars[2], "compress_log_and_tmp_files");
        assertEquals(pars[3], "use_parallel_sim");
        assertEquals(pars[4], "parallel_sim_num");
        assertEquals(pars[5], "attractor_tool");
    }
}