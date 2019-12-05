package eu.druglogics.gitsbe.input;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "=")
public class CommandLineArgs {

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = { "--project", "-p" },
            description = "Name of the project", order = 0)
    private String projectName;

    @Parameter(names = { "--network", "-n" }, required = true,
            description = "Network/Topology file", order = 1)
    private String filenameNetwork;

    @Parameter(names = { "--trainingdata", "-t" }, required = true,
            description = "Training data file", order = 2)
    private String filenameTrainingData;

    @Parameter(names = { "--config", "-c" }, required = true,
            description = "Configuration file", order = 3)
    private String filenameConfig;

    @Parameter(names = { "--modeloutputs", "-m" }, required = true,
            description = "Model outputs file", order = 4)
    private String filenameModelOutputs;

    @Parameter(names = { "--drugs", "-dr" }, description = "Drugs/DrugPanel file", order = 5)
    private String filenameDrugs;

    public String getProjectName() {
        return projectName;
    }

    public String getFilenameNetwork() {
        return filenameNetwork;
    }

    public String getFilenameTrainingData() {
        return filenameTrainingData;
    }

    public String getFilenameConfig() {
        return filenameConfig;
    }

    public String getFilenameModelOutputs() {
        return filenameModelOutputs;
    }

    public String getFilenameDrugs() {
        return filenameDrugs;
    }
}
