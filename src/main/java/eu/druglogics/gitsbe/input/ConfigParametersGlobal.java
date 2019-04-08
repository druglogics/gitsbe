package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.util.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * This class defines the global parameters, i.e.
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

    protected void checkAttractorTool(Logger logger) {
        if (!AttractorTools.contains(attractor_tool)) {
            logger.outputStringMessage(0, "The attractor_tool value: " + attractor_tool
                    + " is not in the list of supported tools");
            logger.outputStringMessage(0, "The bnet_reduction_reduced value will be "
                    + "used for the analysis.");

            attractor_tool = AttractorTools.BNREDUCTION_REDUCED.getTool();
        }
    }

    public String[] getParameters() {
        Field[] fields = ConfigParametersGlobal.class.getDeclaredFields();

        return Arrays.stream(fields).map(Field::getName).toArray(String[]::new);
    }
}
