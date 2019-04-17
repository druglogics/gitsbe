package eu.druglogics.gitsbe.input;

import javax.naming.ConfigurationException;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * This class defines the global configuration parameters, i.e.
 * the ones used by both Drabme and Gitsbe modules
 */
public class ConfigParametersGlobal {

    public int verbosity;
    public boolean delete_tmp_files;
    public boolean compress_log_and_tmp_files;
    public boolean use_parallel_sim;
    public int parallel_sim_num;
    public String attractor_tool;

    public int getVerbosity() {
        return verbosity;
    }

    public boolean deleteTmpDir() {
        return delete_tmp_files;
    }

    public boolean compressLogsAndTmpFiles() {
        return compress_log_and_tmp_files;
    }

    // Returns true or false whether the simulations will run in parallel or not
    public boolean useParallelSimulations() {
        return use_parallel_sim;
    }

    public int parallelSimulationsNumber() {
        return parallel_sim_num;
    }

    public String getAttractorTool() {
        return attractor_tool;
    }

    protected void checkAttractorTool() throws Exception {
        if (!AttractorTools.contains(attractor_tool)) {
            throw new ConfigurationException("The attractor_tool value: " + attractor_tool
                    + " is not in the list of supported tools: " + AttractorTools.getTools());
        }
        checkBNETHomeVar();
    }

    /**
     * Checks if the BNET_HOME environment variable is set
     *
     * @throws Exception
     */
    private void checkBNETHomeVar() throws Exception {
        if (useBNReductionScript() && System.getenv("BNET_HOME") == null) {
            throw new Exception("Set environment variable BNET_HOME to point to location of " +
                    "BNReduction.sh (see druglogics_dep installation guidelines)");
        }
    }

    boolean useBNReductionScript() {
        return attractor_tool.equals(AttractorTools.BNREDUCTION_FULL.getTool())
                || attractor_tool.equals(AttractorTools.BNREDUCTION_REDUCED.getTool());
    }

    public String[] getParameters() {
        Field[] fields = ConfigParametersGlobal.class.getDeclaredFields();

        return Arrays.stream(fields).map(Field::getName).toArray(String[]::new);
    }
}
