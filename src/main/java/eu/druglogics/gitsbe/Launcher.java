package eu.druglogics.gitsbe;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import eu.druglogics.gitsbe.input.CommandLineArgs;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static eu.druglogics.gitsbe.util.Util.*;

public class Launcher {

    public static void main(String[] args) {
        Launcher gitsbeLauncher = new Launcher();
        gitsbeLauncher.start(args);
    }

    public void start(String[] args) {
        if (environmentalVariableBNETisNULL())
            return;
        setupInputAndRun(args);
    }

    private void setupInputAndRun(String[] args) {

        try {
            CommandLineArgs arguments = new CommandLineArgs();
            JCommander.newBuilder().addObject(arguments).build().parse(args);

            String projectName = arguments.getProjectName();
            String filenameNetwork = arguments.getFilenameNetwork();
            String filenameTrainingData = arguments.getFilenameTrainingData();
            String filenameModelOutputs = arguments.getFilenameModelOutputs();
            String filenameConfig = arguments.getFilenameConfig();

            // Inferring the input directory from the network file
            String directoryInput = inferInputDir(filenameNetwork);

            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String directoryOutput = new File(directoryInput,
                    projectName + "_" + dateFormat.format(Calendar.getInstance().getTime()))
                    .getAbsolutePath();
            String directoryTmp = new File(directoryOutput, "gitsbe_tmp").getAbsolutePath();

            Thread thread = new Thread(new Gitsbe(
                    projectName,
                    filenameNetwork,
                    filenameTrainingData,
                    filenameModelOutputs,
                    filenameConfig,
                    directoryOutput,
                    directoryTmp
            ));
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } catch (ParameterException parEx) {
            System.out.println("\nOptions preceded by an asterisk are required.");
            parEx.getJCommander().setProgramName("eu.druglogics.gitsbe.Launcher");
            parEx.usage();
        }
    }
}
