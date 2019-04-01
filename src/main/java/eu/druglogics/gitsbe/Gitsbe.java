package eu.druglogics.gitsbe;

import eu.druglogics.gitsbe.input.Config;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.input.TrainingData;
import eu.druglogics.gitsbe.output.Summary;
import eu.druglogics.gitsbe.model.Evolution;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.util.Logger;
import eu.druglogics.gitsbe.util.RandomManager;
import eu.druglogics.gitsbe.util.Timer;
import eu.druglogics.gitsbe.util.FileDeleter;

import static eu.druglogics.gitsbe.util.Util.*;
import static eu.druglogics.gitsbe.util.FileDeleter.*;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;
import java.util.stream.IntStream;

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

	private String filenameNetwork;
	private String filenameTrainingData;
	private String filenameConfig;
	private String directoryOutput;
	private String directoryTmp;
	private String nameProject;
	private String filenameModelOutputs;

	// Declare one general model that is defined by input files
	private GeneralModel generalModel;

	// Global variable determining verbosity levels
	public int verbosity = 3;

	private Logger logger;
	private ArrayList<String> simulationFileList;

	public Gitsbe(String nameProject, String filenameNetwork, String filenameTrainingData, String filenameModelOutputs,
			String filenameConfig, String directoryOutput, String directoryTmp) {
		this.nameProject = nameProject;
		this.filenameNetwork = filenameNetwork;
		this.filenameTrainingData = filenameTrainingData;
		this.filenameModelOutputs = filenameModelOutputs;
		this.filenameConfig = filenameConfig;
		this.directoryOutput = directoryOutput;
		this.directoryTmp = directoryTmp;
	}

	@Override
	public void run() {

		loadGitsbeProperties();

		System.out.print("Welcome to " + appName + " " + version + "\n\n");

		// Create output directory
		if (!createDirectory(directoryOutput))
			return;

		// Create models directory (subfolder to directoryOutput)
		String modelDirectory = new File(directoryOutput, "models").getAbsolutePath();
		if (!createDirectory(modelDirectory))
			return;

		// Create log directory (subfolder to directoryOutput)
		String logDirectory = new File(directoryOutput, "log").getAbsolutePath();
		if (!createDirectory(logDirectory))
			return;

		// Start logger
		initializeGitsbeLogger(logDirectory);

		// Start timer
		Timer timer = new Timer();

		// Load config file
		Config config = loadConfigFile();

		// Create general Boolean model from general model or load from file
		generalModel = new GeneralModel(logger);
		BooleanModel generalBooleanModel = loadGeneralBooleanModel(config);

		// Exports
		exportDifferentModelFormats(config, generalBooleanModel);
		outputDifferentModelFormats(generalBooleanModel);

		// Load training data
		TrainingData data = loadTrainingData(generalBooleanModel);

		// Load output weights
		ModelOutputs outputs = loadModelOutputs(generalBooleanModel);

		// Create temp directory
		if (!createDirectory(directoryTmp, logger))
			return;
		initializeFileDeleter(config);

		// Summary report for Gitsbe
		Summary summary = initializeSummary(config);

		// Run simulations
		ArrayList<Random> randomSeedsList = new ArrayList<>();
		int numberOfSimulations = config.getSimulations();
		for (int run = 0; run < numberOfSimulations; run++) {
			// run is the seed for Random
			randomSeedsList.add(new Random(run));
		}

		if (config.useParallelSimulations()) {
			// Run evolution simulations in parallel
			setNumberOfAllowedParallelSimulations(config);
			simulationFileList = new ArrayList<String>();
			IntStream.range(0, numberOfSimulations).parallel()
					.forEach(run -> RandomManager.withRandom(randomSeedsList.get(run), () -> runSimulation(run, config,
							summary, generalBooleanModel, data, outputs, modelDirectory, logDirectory)));
			mergeLogFiles(logDirectory);
		} else {
			// Run evolution simulations in serial
			IntStream.range(0, numberOfSimulations)
					.forEach(run -> RandomManager.withRandom(randomSeedsList.get(run), () -> runSimulation(run, config,
							summary, generalBooleanModel, data, outputs, modelDirectory, logDirectory)));
		}

		summary.generateFitnessesReport();

		// Save Models in appropriate file
		saveBestModelsToFile(summary);

		// Clean tmp directory
		cleanDirectory(logger);

		// Stop timer
		timer.stopTimer();
		logger.outputHeader(1, "\nThe end");

		closeLogger(timer);
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

	private void setNumberOfAllowedParallelSimulations(Config config) {
		int parallelSimulationsNumber = config.parallelSimulationsNumber();
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", Integer.toString(parallelSimulationsNumber-1));
		logger.outputStringMessage(1, "\nSetting number of parallel simulations to: " + parallelSimulationsNumber);
	}

	private void initializeFileDeleter(Config config) {
		FileDeleter fileDeleter = new FileDeleter(directoryTmp);
		if (config.deleteTmpDir()) {
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
			mergeFiles(simulationFileList, mergedLogFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void runSimulation(int run, Config config, Summary summary, BooleanModel generalBooleanModel,
			TrainingData data, ModelOutputs outputs, String modelDirectory, String logDirectory) {
		
		int simulation = run + 1;
		Logger simulation_logger = null;

		// create new logger for each parallel simulation
		if (config.useParallelSimulations()) {
			String filenameOutput = appName + "_simulation_" + String.valueOf(simulation) + ".log";
			addFileToSimulationFileList(new File(logDirectory, filenameOutput).getAbsolutePath());
			try {
				simulation_logger = new Logger(filenameOutput, logDirectory, verbosity, true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			simulation_logger = this.logger;
		}

		simulation_logger.outputHeader(1,
				"Evolutionary algorithms, evolution " + (simulation) + " of " + config.getSimulations());

		String baseModelName = removeExtension(generalBooleanModel.getModelName()) + "_run_" + run + "_";
		Evolution ga = new Evolution(summary, generalBooleanModel, baseModelName,
				 data, outputs, modelDirectory, directoryTmp,
				config, simulation_logger);

		ga.evolve(run);
		ga.outputBestModels();

		try {
			ga.saveBestModels(config.getNumOfModelsToSave(), config.getFitnessThreshold());
		} catch (IOException e) {
			e.printStackTrace();
		}

		addModelsToSummaryBestModelList(ga, run, config, summary, simulation_logger);
	}

	synchronized private void addFileToSimulationFileList(String filename) {
		simulationFileList.add(filename);
	}

	private void exportDifferentModelFormats(Config config, BooleanModel generalBooleanModel) {
		// Export network file in gitsbe format
		exportBooleanModelFileInGitsbeFormat(config, generalBooleanModel);
		// Export trimmed network .sif file
		exportTrimmedSifNetworkFile(config, generalBooleanModel);
		// Export network file in ginml format
		exportBooleanModelFileInGinmlFormat(config, generalBooleanModel);
	}

	private void exportBooleanModelFileInGitsbeFormat(Config config, BooleanModel generalBooleanModel) {
		if (config.exportToGitsbeFormat()) {
			try {
				logger.outputStringMessage(2,
						"Exporting .sif file in .gitsbe format: " + removeExtension(generalBooleanModel.getModelName()) + ".gitsbe");
				generalBooleanModel.saveFileInGitsbeFormat(directoryOutput);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void exportTrimmedSifNetworkFile(Config config, BooleanModel generalBooleanModel) {
		if (config.exportToSif()) {
			try {
				String filename = generalBooleanModel.getModelName() + "_trimmed.sif";
				logger.outputStringMessage(2, "Exporting trimmed .sif file: " + filename);
				generalBooleanModel.exportSifFile(directoryOutput, filename);
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	private void exportBooleanModelFileInGinmlFormat(Config config, BooleanModel generalBooleanModel) {
		if (config.exportToGinML()) {
			try {
				String filename = generalBooleanModel.getModelName() + "_export.ginml";
				logger.outputStringMessage(2, "Exporting .sif file in GINML format: " + filename);
				generalBooleanModel.writeGinmlFile(directoryOutput, filename, generalModel.getSingleInteractions());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Write in the Gitsbe logger the network's model in different formats
	 * 
	 * @param generalBooleanModel
	 */
	private void outputDifferentModelFormats(BooleanModel generalBooleanModel) {

		logger.outputHeader(3, "General model in Booleannet format");
		logger.outputLines(3, generalBooleanModel.getModelBooleannet());

		logger.outputHeader(3, "Model in Veliz-Cuba's format");
		logger.outputLines(3, generalBooleanModel.getModelVelizCuba());
	}

	private void saveBestModelsToFile(Summary summary) {
		String filenameBooleanModelsIndex = new File(directoryOutput, nameProject).getAbsolutePath() + "_models.txt";

		try {
			summary.saveModelsIndexFile(filenameBooleanModelsIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	synchronized private void addModelsToSummaryBestModelList(Evolution ga, int run, Config config, Summary summary,
			Logger simulation_logger) {
		for (int i = 0; i < ga.bestModels.size(); i++) {
			if (ga.bestModels.get(i).getFitness() > config.getFitnessThreshold()) {
				simulation_logger.outputStringMessage(2,
						"Adding model " + ga.bestModels.get(i).getModelName() + " to summary output models list.");
				summary.addModel(run, ga.bestModels.get(i));
			}
		}
	}

	private ModelOutputs loadModelOutputs(BooleanModel generalBooleanModel) {
		ModelOutputs outputs = null;
		try {
			outputs = new ModelOutputs(filenameModelOutputs, logger);
		} catch (IOException e) {
			e.printStackTrace();
			File file = new File(directoryOutput);
			filenameModelOutputs = file.getParent() + "/" + "modeloutputs.tab";
			logger.outputStringMessage(1,
					"Cannot find model outputs file, generating template file: " + filenameModelOutputs);
			try {
				ModelOutputs.saveModelOutputsFileTemplate(filenameModelOutputs);
				outputs = new ModelOutputs(filenameModelOutputs, logger);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}

		logger.outputHeader(1, "Model Outputs");
		logger.outputLines(1, outputs.getModelOutputs());

		outputs.checkModelOutputNodeNames(generalBooleanModel);

		return outputs;
	}

	private TrainingData loadTrainingData(BooleanModel generalBooleanModel) {
		TrainingData data = null;
		try {
			data = new TrainingData(filenameTrainingData, logger);
		} catch (IOException e) {
			e.printStackTrace();
			File file = new File(directoryOutput);
			filenameTrainingData = file.getParent() + "/" + "training_data.tab";
			logger.outputStringMessage(1,
					"Cannot find steady state file, generating template file: " + filenameTrainingData);
			try {
				TrainingData.writeTrainingDataTemplateFile(filenameTrainingData);
				data = new TrainingData(filenameTrainingData, logger);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}

		logger.outputHeader(2, "Training Data");
		logger.outputLines(2, data.getTrainingDataVerbose());
		logger.outputStringMessage(2, "Max fitness: " + data.getMaxFitness() + "\n");

		data.checkTrainingDataConsistency(generalBooleanModel);

		return data;
	}

	private BooleanModel loadGeneralBooleanModel(Config config) {
		BooleanModel generalBooleanModel;

		if (getFileExtension(filenameNetwork).equals(".sif")) {

			// Create generalModel from interactions file
			logger.outputStringMessage(3, "Loading model from .sif file: " + filenameNetwork);
			try {
				generalModel.loadInteractionFile(filenameNetwork);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

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

	private void closeLogger(Timer timer) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar calendarData = Calendar.getInstance();
		logger.outputStringMessage(1, "End: " + dateFormat.format(calendarData.getTime()));
		logger.outputStringMessage(1, "Analysis completed in " + timer.getHoursOfDuration() + " hours, "
				+ timer.getMinutesOfDuration() + " minutes, and " + timer.getSecondsOfDuration() + " seconds ");
		logger.outputStringMessage(1, "\nWith that we say thank you and good bye!");
		logger.finish();
	}

	private Summary initializeSummary(Config config) {
		Summary summary = null;
		try {
			summary = new Summary(new File(this.directoryOutput, nameProject).getAbsolutePath() + "_summary.txt", logger,
					config);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return summary;
	}

	private Config loadConfigFile() {
		Config config = null;
		try {
			config = new Config(filenameConfig, logger);
		} catch (IOException e) {
			e.printStackTrace();
			File file = new File(directoryOutput);
			filenameConfig = file.getParent() + "/" + "config.tab";
			logger.outputStringMessage(1, "Cannot find config file, generating template file: " + filenameConfig);
			try {
				Config.writeConfigFileTemplate(filenameConfig);
				config = new Config(filenameConfig, logger);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}

		this.verbosity = config.getVerbosity();
		// Now that we got the verbosity from the config, we can re-set it in the logger
		logger.setVerbosity(verbosity);
		logger.outputHeader(1, "Config options");
		logger.outputLines(1, config.getConfig());
		return config;
	}

	private void initializeGitsbeLogger(String directory) {
		try {
			String filenameOutput = appName + "_" + nameProject + ".log";
			this.logger = new Logger(filenameOutput, directory, verbosity, true);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar calendarData = Calendar.getInstance();
		logger.outputHeader(1, appName + " " + version);
		logger.outputStringMessage(1, "Start: " + dateFormat.format(calendarData.getTime()));
	}
}
