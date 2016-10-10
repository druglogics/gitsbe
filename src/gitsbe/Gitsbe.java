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


/* Gitsbe - Generic Interactions To Specific Boolean Equations
 * 
 * Copyright Åsmund Flobak 2014-2015
 * 
 * email: asmund.flobak@ntnu.no
 * 
 * Based on Sif2BoolMod
 * 
 * Uses bnet_reduction (BNReduction.sh) by Veliz-Cuba
 * 
 * 
 */


public class Gitsbe implements Runnable {

	private String appName = "Gitsbe" ;
	private String version = "v0.2" ;
	
	private String filenameNetwork ;
	private String filenameSteadyState ;
	private String filenameConfig ;
	
	// Declare one general model that is defined by input files
	private GeneralModel generalModel = new GeneralModel () ;
	
	
	//private static BooleanModel originalModel = new BooleanModel () ;
//	private ArrayList <BooleanModel> models = new ArrayList <BooleanModel> ();
	
	// Global variable determining verbosity levels
	public static int verbosity ;
	
	private static Random rand ;
	
	public Gitsbe(String filenameNetwork, String filenameSteadyState, String filenameConfig) {
		this.filenameNetwork = filenameNetwork ;
		this.filenameSteadyState = filenameSteadyState ;
		this.filenameConfig = filenameConfig ;
	}

	@Override
	public void run() {
		
		System.out.print("Welcome to " + appName + " " + version + "\n\n") ;
				
		Config.initialize(filenameConfig);
		this.verbosity = Config.getVerbosity() ;
		
		// Initialization
		rand = new Random() ;
		

		
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss") ;
		
		Calendar cal = Calendar.getInstance();
		
		// Create directory structure with timestamp
		String projectName = Gitsbe.removeExtension(filenameNetwork) + "_" + Gitsbe.removeExtension(filenameSteadyState) ;
		String directoryName = dateFormat.format(cal.getTime()) + "_" + projectName + File.separator ;
				
		if (!new File (directoryName).mkdir())
		{
			System.out.println("Error creating project folder, exiting.") ;
			return ;
		}
		
		if (!new File (directoryName + "models" + File.separator).mkdir())
		{
			System.out.println("Error creating models folder, exiting.") ;
			return ;
		}
		
		// Initialize logger
		
		String directory = System.getProperty("user.dir") + File.separator + directoryName ;
		System.out.println (directory) ;
		
		try {
			Logger.initialize(projectName + "_output.txt", 
								projectName + "_summary.txt" , "gitsbe_debug.txt", 
								directory, Config.getVerbosity(), false, true);
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}

		// Start logger
		dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						
		Logger.outputHeader(1, appName + " " + version);
		Logger.output(1, "Start: " + dateFormat.format(cal.getTime()));
		
		long starttime = System.nanoTime() ;
		
		Summary summary = new Summary(directoryName + projectName + "_summary.txt") ;

		// Load config file
		Logger.outputHeader(1, "Loading config file: " + filenameConfig);
		Logger.output(1, Config.getConfig());
		
		
		// -------------------
		// Clean tmp directory
		// -------------------
		cleanTmpDirectory(new File ("/home/asmund/Dokumenter/Cycret/Gitsbe/bnet/tmp")) ;
		Logger.output(2, "Cleaning tmp directory...") ;
		
		// Model index output file
		String filenameBooleanModelsIndex = directoryName + projectName + "_models.txt" ;
		
		// -----------------------------------------------------------------
		// Create general Boolean model from general model or load from file
		// -----------------------------------------------------------------

		BooleanModel generalBooleanModel ;
		
		if (filenameNetwork.substring(filenameNetwork.length() - ".sif".length()).equals(".sif"))
		{
			// ------------------------------------------
			// Create generalModel from interactions file
			// ------------------------------------------
			try {
				generalModel.loadInteractionFile (filenameNetwork) ;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// Trim model according to command line arguments

//			generalModel.removeSelfRegulation();
//			generalModel.removeSmallNegativeFeedbackLoops();
			
//			generalModel.removeNone () ;
			if (!Config.isPreserve_inputs()) generalModel.removeInputs();
			if (!Config.isPreserve_outputs()) generalModel.removeOutputs();
//			generalModel.removeInputsOutputs();
			
			
			// Assemble single interactions into equations with multiple regulators based on trimming and complexes
			generalModel.buildMultipleInteractions();
			
			generalBooleanModel = new BooleanModel(generalModel) ;
		}
		else
		{
			// -----------------------------------------------------------------
			// Load general boolean model from prepared file in supported format
			// -----------------------------------------------------------------
			
			generalBooleanModel = new BooleanModel(filenameNetwork) ;
			
			
		}
		
		
		// ----------
		// Export sif
		// ----------
		try {
			Logger.output(2, "Exporting trimmed sif file: " + generalBooleanModel.getModelName() + "_export.sif"); 
			generalBooleanModel.exportSifFile(generalBooleanModel.getModelName() + "_export.sif") ;
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// -------------
		// Export Ginsim
		//--------------
		Logger.outputHeader(3, "General model raw expressions for CoLoMoTo");
		Logger.output(3, generalBooleanModel.printBooleanModelGinmlExpressions());
//		try {
//			generalBooleanModel.writeBooleanExpressionGinmlFile();
//		} catch (IOException e3) {
//			// TODO Auto-generated catch block
//			e3.printStackTrace();
//		}
					
		Logger.outputHeader(3, "General model");
		Logger.output(3, generalBooleanModel.getModelBooleannet());
							
		Logger.outputHeader(3,  "Model in Veliz-Cuba's format");
		Logger.output(3,  generalBooleanModel.getModelVelizCuba());
		
		// ------------------------------------------------------
		// Load SteadyState, if non-existent create template file
		// ------------------------------------------------------
		SteadyState ss ;
		try {
			Logger.output(1, "\nReading steady state from file: " + filenameSteadyState);
			
			ss = new SteadyState (filenameSteadyState, generalBooleanModel) ;
			
			Logger.output(1, "Max fitness: " + ss.getMaxFitness());
			
		} catch (FileNotFoundException e)
		{
			Logger.output(1, "Cannot find steady state file, generating template file: " + filenameSteadyState);
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

		
		Logger.outputHeader(2, "Steady states");
		String[] stableStates = ss.getSteadyStates() ;
//		Logger.output(2, stableStates);
		Logger.output(2, ss.getSteadyStatesVerbose());

		

		// Where to store all temporary files
		String directorytmp ;
		if (Config.isPreserve_tmp_files())
		{
			
			
			directorytmp = directory + "tmp" + File.separator ;
//			System.out.println("DEBUG: " + directorytmp) ;
			if (!new File (directorytmp).mkdir())
			{
				System.out.println("Error creating temporary folder, exiting.") ;
				return ;
			}
		}
		else
		{
			directorytmp = System.getProperty("user.dir") + File.separator + "bnet" + File.separator + "tmp" + File.separator ;
//			System.out.println("DEBUG: " + directorytmp) ;
			
		}
		

		// Run evolution several times
		for (int run = 0; run < Config.getSimulations(); run++)
		{
			Logger.outputHeader(1, "Evolutionary algorithms, evolution " + (run) + " of " + Config.getSimulations());
			
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
					directoryName + "models" + File.separator, 
					directorytmp
//					Config.getTarget_fitness_percent()
					) ;
		
			ga.evolve();
			
			ga.outputBestModels();
			
			try {
				ga.saveBestModels(Config.getModels_saved(), Config.getFitness_threshold()) ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (int i = 0; i < ga.bestModels.size(); i++)
			{
				if (ga.bestModels.get(i).getFitness() > Config.getFitness_threshold())
				{
					Logger.output(2, "Adding model " + ga.bestModels.get(i).getModelName() + " to output models list.");
					summary.addModel(run, ga.bestModels.get(i));
				}
			}
		}
		
		
		
		// Output best specific Boolean model(s), associated fitness(es) and computed stable state(s)
		 
		// Save the best model(s) in desired output format 

		// Write summary
		try {
			summary.saveModelsIndexFile(filenameBooleanModelsIndex, directoryName + "models" + File.separator);
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
		
			
		Logger.outputHeader(1, "\nThe end");
		Logger.output(1, "End: " + dateFormat.format(cal.getTime()));
		Logger.output(1, "Analysis completed in " + hours + " hours, " + minutes + " minutes, and " + seconds + " seconds ");
		
		
		Logger.output(1, "\nWith that we say thank you and good bye!");
		
		if (Config.isInvoke_drabme())
		{
			
			Logger.output(1, "\n Now invoking Drabme...");
			this.invokeDrabme(directory, Config.getLocation_drabme() + directoryName, projectName, filenameBooleanModelsIndex);
			
			
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
