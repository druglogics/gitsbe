package gitsbe;

import drabme.ModelOutputs;

public class Launcher {

	public Launcher() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {


		// Check if location of BNReduction.sh is defined in environment variable BNET_HOME 
		if (System.getenv("BNET_HOME") == null)
		{
			System.out.println("Set environment variable BNET_HOME to point to location of BNReduction.sh");
			System.out.println("BNReduction can be obtained from https://github.com/alanavc/BNReduction") ;
			return ;
		}
		
		// Configure Gitsbe based on command line arguments, set up example run if no parameters are specified
		/*
		 * The command line could also specify:
		 * -r (for generating a 'random' cell line parameterization) - but can be configured also by launcher..
		 * -p n	(for repeating analysis n times)
		 * -v n (for setting verbosity level) - but already in config file
		 * 
		 * Command-line arguments should have higher priority than arguments supplied by config file
		 */
		if (args.length == 0)
		{
			System.out.println("No user argumetns supplied") ;
			System.out.println("Usage: gitsbe <name project> <filename network> <filename config file> <filename steady states file> [output directory]") ;
			System.out.println("\nTestrun: setting up run with example files:");

			args = new String[] {"toy_ags_example", 
								 "toy_ags_network.sif", 
								 "toy_ags_trainingdata_original_names_short.tab", 
								 "toy_ags_config.tab",
								 "toy_ags_modeloutputs.tab"} ;
//			args = new String[] {"toy_model_example", "toy_network.sif", "toy_model_trainingdata.tab", "toy_model_config.tab"} ;
			System.out.println("gitsbe " + args[0] + " " + args[1] + " " + args[2] + " " + args[3] + args[4] + "\n\n")  ;
			
		}
		
		
		String nameProject = args[0] ;
		String filenameNetwork = args[1];
		String filenameConfig = args[3];
		String[] filenameTrainingData = {args[2]} ;
		String filenameModelOutputs = args[4];
//		String directoryOutput = args[4];
		
		// make sure path to tmp directory is absolute, since BNreduction will be run from another working directory
//		if ((directoryOutput.length() > 0) && !(new File (directoryOutput).isAbsolute()))
//			directoryOutput = System.getProperty("user.dir") + File.separator + directoryOutput ;
				
		Thread t ;
		
		
		// Repeat process 100 times to obtain confidence intervals for each evolutionary run - to be used for publication
  //		for (int j = 0; j < 100; j++)
  //		{
			for (int i = 0; i < filenameTrainingData.length; i++)
			{
//				String filenameSteadyState = filenameSteadyStates[i] ;

				t = new Thread (new Gitsbe (nameProject, filenameNetwork, filenameTrainingData[i], filenameConfig, filenameModelOutputs)) ;
				
				t.start();
				try {
					t.join ();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//		}
		
		
	}

}
