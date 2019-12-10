package eu.druglogics.gitsbe;

import eu.druglogics.gitsbe.drug.DrugPanel;
import eu.druglogics.gitsbe.input.Config;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.input.TrainingData;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.Evolution;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.output.Summary;
import eu.druglogics.gitsbe.util.FileDeleter;
import eu.druglogics.gitsbe.util.Logger;
import eu.druglogics.gitsbe.util.RandomManager;
import eu.druglogics.gitsbe.util.Timer;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.stream.IntStream;

import static eu.druglogics.gitsbe.util.FileDeleter.cleanDirectory;
import static eu.druglogics.gitsbe.util.Util.*;

/**
 * Gitsbe - Generic Interactions To Specific Boolean Equations
 * 
 * Copyright Asmund Flobak 2014-2015-2016-2017
 * 
 * email: asmund.flobak@ntnu.no
 * 
 * Based on Sif2BoolMod (asmund)
 * 
 * Uses bnet_reduction (BNReduction.sh) by Veliz-Cuba
 * 
 */

public class Gitsbe implements Runnable {

	private static String appName;
	private static String version;

	private String projectName;
	private String filenameNetwork;
	private String filenameTrainingData;
	private String filenameModelOutputs;
	private String filenameConfig;
	private String filenameDrugs;
	private String directoryOutput;
	private String directoryTmp;

	// Declare one general model that is defined by input files
	private GeneralModel generalModel;

	private Logger logger;
	private ArrayList<String> simulationFileList;

	public Gitsbe(String projectName, String filenameNetwork, String filenameTrainingData,
				  String filenameModelOutputs, String filenameConfig, String filenameDrugs,
				  String directoryOutput, String directoryTmp) {
		this.projectName = projectName;
		this.filenameNetwork = filenameNetwork;
		this.filenameTrainingData = filenameTrainingData;
		this.filenameModelOutputs = filenameModelOutputs;
		this.filenameConfig = filenameConfig;
		this.filenameDrugs = filenameDrugs;
		this.directoryOutput = directoryOutput;
		this.directoryTmp = directoryTmp;
	}

	@Override
	public void run() {

		loadGitsbeProperties();

		System.out.print("Welcome to " + appName + " " + version + "\n\n");

        createOutputDirectory();

		// Create models directory (subfolder to directoryOutput)
		String directoryModels = new File(directoryOutput, "models").getAbsolutePath();
		createModelDirectory(directoryModels);

        String directoryLog = new File(directoryOutput, "log").getAbsolutePath();
        createLogDirectory(directoryLog);

		// Start logger
		initializeGitsbeLogger(directoryLog);

		// Start timer
		Timer timer = new Timer();

		// Load config file
		loadConfigFile();

		// Create general Boolean model from general model or load from file
		generalModel = new GeneralModel(logger);
		BooleanModel generalBooleanModel = loadGeneralBooleanModel();

		// Exports
		exportModelToDiffFormats(generalBooleanModel);

		// Load drugs
		loadDrugPanel(generalBooleanModel);

		// Load training data
		loadTrainingData(generalBooleanModel);

		// Load output weights
		loadModelOutputs(generalBooleanModel);

        createTmpDirectory();
		activateFileDeleter();

		// Summary report for Gitsbe
		Summary summary = initializeSummary();

		// Run simulations
		ArrayList<Random> randomSeedsList = new ArrayList<>();
		int numberOfSimulations = Config.getInstance().getSimulations();
		for (int run = 0; run < numberOfSimulations; run++) {
			// run is the seed for Random
			randomSeedsList.add(new Random(run));
		}

		if (Config.getInstance().useParallelSimulations()) {
			// Run evolution simulations in parallel
			setNumberOfAllowedParallelSimulations();
			simulationFileList = new ArrayList<>();
			IntStream.range(0, numberOfSimulations).parallel().forEach(run ->
				RandomManager.withRandom(randomSeedsList.get(run), () ->
					runSimulation(run, summary, generalBooleanModel, directoryModels, directoryLog)));
			mergeLogFiles(directoryLog);
		} else {
			logger.outputStringMessage(1, "\nRunning simulations serially");
			IntStream.range(0, numberOfSimulations).forEach(run ->
				RandomManager.withRandom(randomSeedsList.get(run), () ->
					runSimulation(run, summary, generalBooleanModel, directoryModels, directoryLog)));
		}

		summary.generateFitnessesReport();

		// Save Models in appropriate file
		saveBestModelsToFile(summary);

		// Clean tmp directory
		cleanDirectory(logger);

		// Compress log and tmp dirs to preserve storage space
		if (Config.getInstance().compressLogAndTmpFiles()) {
			archive(directoryLog, directoryTmp);
		}

		// Stop timer
		timer.stopTimer();
		logger.outputHeader(1, "\nThe end");

		logger.writeLastLoggingMessage(timer);
	}

	private void loadDrugPanel(BooleanModel booleanModel) {
		if (filenameDrugs != null) {
			logger.outputHeader(2, "Loading drug panel");

			try {
				DrugPanel.init(filenameDrugs, logger);
			} catch (IOException e) {
				e.printStackTrace();
				logger.outputStringMessage(1, "Problem with input drugpanel file: " + filenameDrugs);
				abort();
			} catch (Exception e) {
				e.printStackTrace();
				abort();
			}

			DrugPanel.getInstance().checkDrugTargets(booleanModel);
		}
	}

	private void loadGitsbeProperties() {
		final Properties properties = new Properties();
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("gitsbe.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		version = properties.getProperty("version");
		appName = properties.getProperty("appName");
	}

    private void createOutputDirectory() {
        try {
            createDirectory(directoryOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createModelDirectory(String modelDirectory) {
        try {
            createDirectory(modelDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    private void createLogDirectory(String logDirectory) {
        try {
            createDirectory(logDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTmpDirectory() {
        try {
            createDirectory(directoryTmp, logger);

			// Hack: if full BNReduction is used, copy FPGB.m2 file in the gitsbe_tmp
            if (Config.getInstance().getAttractorTool().equals("bnet_reduction")) {
				String directoryBNET = System.getenv("BNET_HOME");
				Files.copy(Paths.get(directoryBNET + "/FPGB.m2"),
						Paths.get(directoryTmp + "/FPGB.m2"));
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private void setNumberOfAllowedParallelSimulations() {
		int parallelSimulationsNumber = Config.getInstance().parallelSimulationsNumber();
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
				Integer.toString(parallelSimulationsNumber - 1));
		logger.outputStringMessage(1, "\nSetting number of parallel simulations to: "
				+ parallelSimulationsNumber);
	}

	private void activateFileDeleter() {
		FileDeleter fileDeleter = new FileDeleter(directoryTmp);
		if (Config.getInstance().deleteTmpDir()) {
			fileDeleter.activate();
		}
	}

	/**
	 * Merges all Gitsbe simulation logging files into one
	 * 
	 * @param logDirectory
	 */
	private void mergeLogFiles(String logDirectory) {
		String mergedLogFilename = new File(logDirectory, appName + "_simulations.log").getAbsolutePath();

		// sort the files to have the simulations in ascending order
		sortFiles(simulationFileList);

		try {
			mergeFiles(simulationFileList, mergedLogFilename, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void runSimulation(int run, Summary summary, BooleanModel generalBooleanModel,
							   String modelDirectory, String logDirectory) {
		int simulation = run + 1;
		Logger simulationLogger = null;
		int verbosity = logger.getVerbosity();
		Config config = Config.getInstance();

		// create new logger for each parallel simulation
		if (config.useParallelSimulations()) {
			String filenameOutput = appName + "_simulation_" + simulation + ".log";
			addFileToSimulationFileList(new File(logDirectory, filenameOutput).getAbsolutePath());
			try {
				simulationLogger = new Logger(filenameOutput, logDirectory, verbosity, true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			simulationLogger = this.logger;
		}

		simulationLogger.outputHeader(1, "Evolutionary algorithms, evolution "
				+ (simulation) + " of " + config.getSimulations());

		String baseModelName = removeExtension(generalBooleanModel.getModelName()) + "_run_" + run + "_";
		Evolution ga = new Evolution(summary, generalBooleanModel, baseModelName,
			modelDirectory, directoryTmp, simulationLogger);

		ga.evolve(run);
		ga.outputBestModels();

		try {
			ga.saveBestModels(config.getNumOfModelsToSave(), config.getFitnessThreshold());
		} catch (Exception e) {
			e.printStackTrace();
		}

		addModelsToSummaryBestModelList(ga, run, summary, simulationLogger);
	}

	synchronized private void addFileToSimulationFileList(String filename) {
		simulationFileList.add(filename);
	}

	private void exportModelToDiffFormats(BooleanModel generalBooleanModel) {
		exportModelToGitsbeFormat(generalBooleanModel);
		exportModelToSifFormat(generalBooleanModel);
		exportModelToGINMLFormat(generalBooleanModel);
		exportModelToSBMLFormat(generalBooleanModel);
		exportModelToBoolNetFormat(generalBooleanModel);
	}

	private void exportModelToGitsbeFormat(BooleanModel generalBooleanModel) {
		if (Config.getInstance().exportToGitsbe()) {
			try {
				logger.outputStringMessage(2, "Exporting network file to gitsbe format");
				generalBooleanModel.exportModelToGitsbeFile(directoryOutput);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void exportModelToSifFormat(BooleanModel generalBooleanModel) {
		if (Config.getInstance().exportToSif()) {
			try {
				logger.outputStringMessage(2, "Exporting network file to sif format");
				generalBooleanModel.exportModelToSifFile(directoryOutput);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void exportModelToGINMLFormat(BooleanModel generalBooleanModel) {
		if (Config.getInstance().exportToGINML()) {
			try {
				logger.outputStringMessage(2, "Exporting network file to ginml format");
				generalBooleanModel.exportModelToGINMLFile(directoryOutput);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void exportModelToSBMLFormat(BooleanModel generalBooleanModel) {
		if (Config.getInstance().exportToSBMLQual()) {
			try {
				logger.outputStringMessage(2, "Exporting network file to sbml format");
				generalBooleanModel.exportModelToSBMLFile(directoryOutput);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void exportModelToBoolNetFormat(BooleanModel generalBooleanModel) {
		if (Config.getInstance().exportToBoolNet()) {
			try {
				logger.outputStringMessage(2, "Exporting network file to boolnet format");
				generalBooleanModel.exportModelToBoolNetFile(directoryOutput);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveBestModelsToFile(Summary summary) {
		String filenameBooleanModelsIndex =
				new File(directoryOutput, projectName).getAbsolutePath() + "_models.txt";

		try {
			summary.saveBestModelsToFile(filenameBooleanModelsIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	synchronized private void addModelsToSummaryBestModelList(Evolution ga, int run, Summary summary,
															  Logger simulationLogger) {
		for (int i = 0; i < ga.bestModels.size(); i++) {
			if (ga.bestModels.get(i).getFitness() > Config.getInstance().getFitnessThreshold()) {
				simulationLogger.outputStringMessage(2, "Adding model "
						+ ga.bestModels.get(i).getModelName() + " to summary output models list.");
				summary.addModel(run, ga.bestModels.get(i));
			}
		}
	}

	private void loadModelOutputs(BooleanModel generalBooleanModel) {
		try {
			ModelOutputs.init(filenameModelOutputs, logger);
		} catch (Exception e) {
			e.printStackTrace();
			abort();
		}

		logger.outputHeader(1, "Model Outputs");
		logger.outputLines(1, ModelOutputs.getInstance().getModelOutputs());

		ModelOutputs.getInstance().checkModelOutputNodeNames(generalBooleanModel);
	}

	private void loadTrainingData(BooleanModel generalBooleanModel) {
		try {
			TrainingData.init(filenameTrainingData, logger);
		} catch (Exception e) {
			e.printStackTrace();
			abort();
		}

		logger.outputHeader(2, "Training Data");
		logger.outputLines(2, TrainingData.getInstance().getTrainingDataVerbose());

		try {
			TrainingData.getInstance().checkTrainingDataConsistency(generalBooleanModel);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			abort();
		}
	}

	private BooleanModel loadGeneralBooleanModel() {
		BooleanModel generalBooleanModel;

		if (getFileExtension(filenameNetwork).equals(".sif")) {

			// Create generalModel from interactions file
			logger.outputStringMessage(3, "Loading model from .sif file: " + filenameNetwork);
			try {
				generalModel.loadInteractionsFile(filenameNetwork);
			} catch (IOException e) {
				e.printStackTrace();
			}

			Config config = Config.getInstance();

			// if both config variables are false, no removal of nodes will be done
			if (config.removeInputNodes() & config.removeOutputNodes())
				generalModel.removeInputsOutputs();
			else if (config.removeInputNodes())
				generalModel.removeInputs();
			else if (config.removeOutputNodes())
				generalModel.removeOutputs();

			// Assemble single interactions into equations with multiple regulators based on
			// trimming and complexes
			generalModel.buildMultipleInteractions();

			generalBooleanModel = new BooleanModel(generalModel, logger);
		} else {
			// Load general boolean model from prepared file in supported format
			generalBooleanModel = new BooleanModel(filenameNetwork, logger);
		}
		return generalBooleanModel;
	}

	private Summary initializeSummary() {
		String summaryFilename = new File(this.directoryOutput, projectName).getAbsolutePath()
				+ "_summary.txt";
		return new Summary(summaryFilename, logger);
	}

	private void loadConfigFile() {
		try {
			Config.init(filenameConfig, logger);
		} catch (Exception e) {
			e.printStackTrace();
			abort();
		}

		// Now that we have the verbosity from the config, we can re-set it in the logger
		logger.setVerbosity(Config.getInstance().getVerbosity());
		logger.outputHeader(1, "Config options");
		logger.outputLines(1, Config.getInstance().getConfig());
	}

	private void initializeGitsbeLogger(String directory) {
		try {
			String filenameOutput = appName + "_" + projectName + ".log";
			this.logger = new Logger(filenameOutput, directory, 3, true);
		} catch (IOException e) {
			e.printStackTrace();
			abort();
		}

		logger.writeFirstLoggingMessage(appName, version);
	}
}
