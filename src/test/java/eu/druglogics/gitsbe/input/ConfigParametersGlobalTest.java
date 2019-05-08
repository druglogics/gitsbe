package eu.druglogics.gitsbe.input;

import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void test_check_not_available_attractor_tool() {
        ConfigParametersGlobal parameters = new ConfigParametersGlobal();

        assertNull(parameters.attractor_tool);
        assertThrows(ConfigurationException.class, parameters::checkAttractorTool);
    }

    @Test
    void test_use_bnr_reduction_script() {
        ConfigParametersGlobal parameters = new ConfigParametersGlobal();

        parameters.attractor_tool = AttractorTools.BNREDUCTION_FULL.getTool();
        assertTrue(parameters.useBNReductionScript());

        parameters.attractor_tool = AttractorTools.BNREDUCTION_REDUCED.getTool();
        assertTrue(parameters.useBNReductionScript());

        parameters.attractor_tool = "otherTool";
        assertFalse(parameters.useBNReductionScript());
    }

    @Test
    void test_check_verbosity() {
        ConfigParametersGlobal parameters = new ConfigParametersGlobal();

        // Default value is 0
        assertThat(parameters.getVerbosity()).isEqualTo(0);

        parameters.verbosity = 1;
        assertThat(parameters.getVerbosity()).isEqualTo(1);
        parameters.verbosity = 2;
        assertThat(parameters.getVerbosity()).isEqualTo(2);
        parameters.verbosity = 3;
        assertThat(parameters.getVerbosity()).isEqualTo(3);

        parameters.verbosity = 4;
        assertThrows(ConfigurationException.class, parameters::checkVerbosity);
    }
}
