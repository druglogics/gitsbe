package gitsbe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

//import drabme.Drabme;


/**
 *  Gitsbe - Generic Interactions To Specific Boolean Equations
 * 
 * Copyright Asmund Flobak 2014-2015-2016-2017
 * 
 * email: asmund.flobak@ntnu.no
 * 
 * Based on Sif2BoolMod (asmund)
 * 
 * Uses bnet_reduction (BNReduction.sh) by Veliz-Cuba
 * 
 * 
 */


public class Gitsbe implements Runnable {

	private String appName = "Gitsbe";
	private String version = "v0.3";
	
	private String filenameNetwork;
	private String filenameSteadyState;
	private String filenameConfig;
	private String outputDirectory;
	private String nameProject ;
	
	// Declare one general model that is defined by input files
	private GeneralModel generalModel ;
	
	// Global variable determining verbosity levels
	public int verbosity ;
	
	private static Random rand ;
	
	public Gitsbe(String nameProject, String filenameNetwork, String filenameSteadyState, String filenameConfig, String outputDirectory) {
		this.nameProject = nameProject ;
		this.filenameNetwork = filenameNetwork ;
		this.filenameSteadyState = filenameSteadyState ;
		this.filenameConfig = filenameConfig ;
		this.outputDirectory = outputDirectory ;
	}
	public Gitsbe(String nameProject, String filenameNetwork, String filenameSteadyState, String filenameConfig) {
		this.nameProject = nameProject ;
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss") ;
		Calendar cal = Calendar.getInstance();
		
//		String projectName = "BNET";
		String outputDirectory = dateFormat.format(cal.getTime()) + "_" + nameProject + File.separator ;

		this.filenameNetwork = filenameNetwork ;
		this.filenameSteadyState = filenameSteadyState ;
		this.filenameConfig = filenameConfig ;
		this.outputDirectory = outputDirectory ;
  }

	@Override
	public void run() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss") ;
		Calendar cal = Calendar.getInstance();
		
		
		
		System.out.print("Welcome to " + appName + " " + version + "\n\n") ;
		
		
		// Initialization
		rand = new Random(0) ;
		
		
		
		// Create directory structure with timestamp
//		String projectName = "BNET";
//		String directoryName = this.outputDirectory;
//		String projectName = Gitsbe.removeExtension(filenameNetwork) + "__" + Gitsbe.removeExtension(filenameSteadyState) + "__" + Gitsbe.removeExtension(filenameConfig);
		
		// Create output directory name if not specified on launch
		// When gitsbe is launched from rbbt then rbbt will itself specify output dir
		// Default for creating folder is to use project name, which is a concatenation of 
		// network, steadystate and config file names
		if (outputDirectory.length() == 0)
		{
			outputDirectory = System.getProperty("user.dir") + File.separator + nameProject + "_" + dateFormat.format(cal.getTime()) + File.separator ;
		}
		else
		{		
			// Create main project folder, append number if folder already exists 
			// (probably obsolete code because this shouldn't happen with rbbt)
//			int counter = 0 ;
//			String directoryNameTmp = outputDirectory ;
//			
//			while (new File(directoryNameTmp).exists())
//				directoryNameTmp = outputDirectory + "_" + counter++ ;
//			
//			outputDirectory = directoryNameTmp ;
			
//			Logger.output (1, "Setting output directory: " + outputDirectory) ;
		}
		
		if (!new File(outputDirectory).mkdir())
		{
			if (!new File(outputDirectory).exists())
			{
				System.out.println("Error creating project folder (" + outputDirectory + "), exiting.") ;
				return ;
			}
		}

		// Create models folder (subfolder to outputDirectory)
		String modelDirectory = new File(outputDirectory, "models").getPath();
		
		if (!new File(modelDirectory).mkdir())
		{
			System.out.println("Error creating models folder (" + outputDirectory + File.separator + modelDirectory + "), exiting.") ;
			return ;
		}
		
		// Initialize logger
		Logger logger  ;
		
		try {
			logger = new Logger (nameProject + "_output.txt", 
								nameProject + "_summary.txt" , "gitsbe_debug.txt", 
								outputDirectory, 3, false, true);
		} catch (IOException e3) {
			
			// TODO Auto-generated catch block
			e3.printStackTrace();
			return ;
		}

		// Start logger
		dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						
		
		
		logger.outputHeader(1, appName + " " + version);
		logger.output(1, "Start: " + dateFormat.format(cal.getTime()));
		
		long starttime = System.nanoTime() ;
		
		Summary summary = new Summary(new File(this.outputDirectory, nameProject).getPath() + "_summary.txt", logger) ;

		// Load config file
		Config config = new Config(filenameConfig, logger) ;
		this.verbosity = config.getVerbosity() ;

		logger.outputHeader(1, "Loading config file: " + filenameConfig);
		logger.output(1, config.getConfig());
		
		
		
		// Model index output file
		String filenameBooleanModelsIndex = new File(outputDirectory, nameProject).getPath() + "_models.txt" ;
		
		// -----------------------------------------------------------------
		// Create general Boolean model from general model or load from file
		// -----------------------------------------------------------------

		generalModel = new GeneralModel (logger) ;
		BooleanModel generalBooleanModel ;
		
		if (filenameNetwork.substring(filenameNetwork.length() - ".sif".length()).toLowerCase().equals(".sif"))
		{
			// ------------------------------------------
			// Create generalModel from interactions file
			// ------------------------------------------
			try {
				generalModel.loadInteractionFile(filenameNetwork) ;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// From rbbt compliance (?)
//		      String generationDirectory=new File(outputDirectory, "generations").getPath();
//		      generalModel.setModelName(generationDirectory + File.separator + "model");
//		      new File(generationDirectory).mkdir();
				
		
			// Trim model according to command line arguments

//			generalModel.removeSelfRegulation();
//			generalModel.removeSmallNegativeFeedbackLoops();
			
//			generalModel.removeNone () ;
			if (!config.isPreserve_inputs()) generalModel.removeInputs();
			if (!config.isPreserve_outputs()) generalModel.removeOutputs();
//			generalModel.removeInputsOutputs();
			
			
			// Assemble single interactions into equations with multiple regulators based on trimming and complexes
			generalModel.buildMultipleInteractions();
			
			generalBooleanModel = new BooleanModel(generalModel, logger) ;
		}
		else
		{
			// -----------------------------------------------------------------
			// Load general boolean model from prepared file in supported format
			// -----------------------------------------------------------------
			
			generalBooleanModel = new BooleanModel(filenameNetwork, logger) ;
			
			
		}

		// ----------
		// Export sif
		// ----------
		try {
			logger.output(2, "Exporting trimmed sif file: " + generalBooleanModel.getModelName() + "_export.sif"); 
			generalBooleanModel.exportSifFile(generalBooleanModel.getModelName() + "_export.sif") ;
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// -------------
		// Export Ginsim
		//--------------
		logger.outputHeader(3, "General model raw expressions for CoLoMoTo");
		logger.output(3, generalBooleanModel.printBooleanModelGinmlExpressions());
//		try {
//			generalBooleanModel.writeBooleanExpressionGinmlFile();
//		} catch (IOException e3) {
//			// TODO Auto-generated catch block
//			e3.printStackTrace();
//		}
					
		logger.outputHeader(3, "General model");
		logger.output(3, generalBooleanModel.getModelBooleannet());
							
		logger.outputHeader(3,  "Model in Veliz-Cuba's format");
		logger.output(3,  generalBooleanModel.getModelVelizCuba());
		
		// ------------------------------------------------------
		// Load SteadyState, if non-existent create template file
		// ------------------------------------------------------
		SteadyState ss ;
		try {
			logger.output(1, "\nReading steady state from file: " + filenameSteadyState);
			
			ss = new SteadyState (filenameSteadyState, generalBooleanModel, logger) ;
			
			logger.output(1, "Max fitness: " + ss.getMaxFitness());
			
		} catch (FileNotFoundException e)
		{
			logger.output(1, "Cannot find steady state file, generating template file: " + filenameSteadyState);
			try {
				generalBooleanModel.writeSteadyStateTemplateFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return ;
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return ;
		}
		
		// --------------------------------------------------------
		// Obtain specific Boolean model from general Boolean model
		// --------------------------------------------------------

		
		logger.outputHeader(2, "Steady states");
		String[] stableStates = ss.getSteadyStates() ;
//		logger.output(2, stableStates);
		logger.output(2, ss.getSteadyStatesVerbose());

		

		// Where to store all temporary files
		String bnetOutputDirectory ;

		if (config.isPreserve_tmp_files())
		{
			
			bnetOutputDirectory = new File(outputDirectory,"tmp").getPath();
//			System.out.println("DEBUG: " + bnetOutputDirectory) ;
			if (!new File (bnetOutputDirectory).mkdir())
			{
				System.out.println("Error creating temporary folder, exiting.") ;
				return ;
			}
		}
		else
		{
			bnetOutputDirectory = System.getProperty("user.dir") + File.separator + "bnet" + File.separator + "tmp" + File.separator ;
//			System.out.println("DEBUG: " + bnetOutputDirectory) ;
			
		}
		

		// Run evolution several times
		for (int run = 0; run < config.getSimulations(); run++)
		{
			logger.outputHeader(1, "Evolutionary algorithms, evolution " + (run) + " of " + config.getSimulations());
			
			Evolution ga = new Evolution (summary, 
					generalBooleanModel, 
//					Config.getPopulation(), 
//					Config.getGenerations(), 
//					Config.getSelection(), 
//					Config.getCrossovers(), 
//					Config.getBalancemutations(), 
//					Config.getBootstrap_mutations_factor(), 
          generalBooleanModel.getModelName() + "_run_" + run + "_", 
          ss, 
          modelDirectory , 
          bnetOutputDirectory,
          config,
          logger
//					Config.getTarget_fitness_percent()
					) ;
		
			ga.evolve();
			
			ga.outputBestModels();
			
			try {
				ga.saveBestModels(config.getModels_saved(), config.getFitness_threshold()) ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (int i = 0; i < ga.bestModels.size(); i++)
			{
				if (ga.bestModels.get(i).getFitness() > config.getFitness_threshold())
				{
					logger.output(2, "Adding model " + ga.bestModels.get(i).getModelName() + " to output models list.");
					summary.addModel(run, ga.bestModels.get(i));
				}
			}
		}
		
		
		
		// Output best specific Boolean model(s), associated fitness(es) and computed stable state(s)
		 
		// Save the best model(s) in desired output format 

		// Write summary
		try {
			summary.saveModelsIndexFile(filenameBooleanModelsIndex, modelDirectory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long endtime = System.nanoTime();
		
		long duration = (endtime - starttime)/1000000000 ;
		
		int seconds = (int) (duration) % 60 ;
		int minutes = (int) ((duration / 60) % 60);
		int hours   = (int) ((duration / (60*60)));
		
		summary.generateFitnessesReport();
		
		// -------------------
		// Clean tmp directory
		// -------------------
		logger.output(2, "Cleaning tmp directory...") ;
		cleanTmpDirectory(new File (outputDirectory, "tmp")) ;
			
		logger.outputHeader(1, "\nThe end");
		logger.output(1, "End: " + dateFormat.format(cal.getTime()));
		logger.output(1, "Analysis completed in " + hours + " hours, " + minutes + " minutes, and " + seconds + " seconds ");
		
		
		logger.output(1, "\nWith that we say thank you and good bye!");
		
		if (config.isInvoke_drabme())
		{
			
			logger.output(1, "\n Now invoking Drabme...");
//			this.invokeDrabme(this.outputDirectory, config.getLocation_drabme() + directoryName, projectName, filenameBooleanModelsIndex);
			
			
		}
	}
	
	/**
	 * get random integer between min and max (inclusive)
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int randInt(int min, int max) 
	{
		return rand.nextInt((max - min) + 1) + min;
		
	}
	
	/**
	 * removes extension of string, author: coobird (http://stackoverflow.com/users/17172/coobird)
	 * 
	 * @param str fillename
	 * @return filename without extension
	 */
	public static String removeExtension(String str) {

	    String separator = System.getProperty("file.separator");
	    String filename;

	    // Remove the path upto the filename.
	    int lastSeparatorIndex = str.lastIndexOf(separator);
	    if (lastSeparatorIndex == -1) {
	        filename = str;
	    } else {
	        filename = str.substring(lastSeparatorIndex + 1);
	    }

	    // Remove the extension.
	    int extensionIndex = filename.lastIndexOf(".");
	    if (extensionIndex == -1)
	        return filename;

	    return filename.substring(0, extensionIndex);
	}


	public void invokeDrabme (String directory, String destination, String projectName, String filenameBooleanModelsIndex)  
	{
		try {
			copy (new File (directory), new File (destination)) ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String filenameOutput = directory + File.separator + projectName + "_drabme_output.txt";
//		String filenameSummary = directory + File.separator + projectName + "_drabme_summary.txt" ;
//		
//		int combosize = 2 ;
//		
//		
//		String filenameDrugs = "ags_drugpanel.txt" ;
//		String filenameModelOutputs = "ags_modeloutputs.txt" ;
//		
//		System.out.println("help help");
//		Drabme d = new Drabme(Config.getVerbosity(), filenameBooleanModelsIndex, filenameDrugs, filenameModelOutputs, filenameOutput, filenameSummary, combosize) ;
//		d.run();

	}
	
	
	public void copy(File sourceLocation, File targetLocation) throws IOException {
	    if (sourceLocation.isDirectory()) {
	        copyDirectory(sourceLocation, targetLocation);
	    } else {
	        copyFile(sourceLocation, targetLocation);
	    }
	}

	private void copyDirectory(File source, File target) throws IOException {
	    if (!target.exists()) {
	        target.mkdir();
	    }

	    for (String f : source.list()) {
	        copy(new File(source, f), new File(target, f));
	    }
	}

	private void copyFile(File source, File target) throws IOException {        
	    try (
	            InputStream in = new FileInputStream(source);
	            OutputStream out = new FileOutputStream(target)
	    ) {
	        byte[] buf = new byte[1024];
	        int length;
	        while ((length = in.read(buf)) > 0) {
	            out.write(buf, 0, length);
	        }
	    }
	}
	
	private void cleanTmpDirectory (File dir)
	{
	    for (File file: dir.listFiles()) {
	        if (file.isDirectory()) cleanTmpDirectory(file);
	        file.delete();
	    }
	}

}
