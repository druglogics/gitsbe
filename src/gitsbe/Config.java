package gitsbe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Config {

	
	private int verbosity ;
	private boolean preserve_outputs ;
	private boolean preserve_inputs ;
	private boolean preserve_tmp_files ;
	private int population ;
	private int generations ;
	private int selection ;
	private int crossovers ;
	private int mutations ;
	private int balancemutations ;
	private int randommutations ;
	private int complexmutations ;
	private int familymutations ;
	private int inhibitorymutations ;
	private int activatorymutations ;
	private int ormutations ;
	private int andmutations ;
	private int shufflemutations ;
	private int target_fitness ;
	private int target_fitness_percent ;
	private int bootstrap_mutations_factor ;
	private int mutations_factor ;
	private int bootstrap_shuffle_factor ;
	private int shuffle_factor ;
	private int simulations ;
	private int models_saved ;
	private float fitness_threshold ;
	
	private String filenameConfig ;
	
	private Logger logger ;

	public Config (String filename, Logger logger)
	{
		filenameConfig = filename ;
		this.logger = logger ;
		
		// Load config file if present
		try {
			loadConfigFile (filename) ;
		} catch (IOException e) {
			
			logger.output(1, "Couldn't load config file " + filename + ". Writing template config file");
			try {
				writeConfigFileTemplate (filename) ;
			} catch (FileNotFoundException | UnsupportedEncodingException e1) {
				logger.output(1, "Couldn't write template config file");
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
	}
	
	private void loadConfigFile (String filename) throws IOException
	{
		logger.output(3, "Reading config file " + new File(filename).getAbsolutePath());
		
		
		// Read lines
		ArrayList <String> lines = new ArrayList <String> () ;
		BufferedReader reader = new BufferedReader(new FileReader (filename)) ;
		
  try {
	  while (true) {
	   String line = reader.readLine();
	   // no more lines to read
	   if (line == null) {
	    reader.close();
	    break;
	   }
	
	   if ((!line.startsWith("#")) & line.length() > 0) 
	   {
	   	lines.add(line);
//	   	System.out.println(line);
	   }
	  
	  }
  }
  finally {
	  	reader.close ();
	 }
  
  // Process lines
  for (int i = 0; i < lines.size(); i++)
  {
  	System.out.println(lines.get(i));
  	String parameterName = lines.get(i).split("\t")[0] ;
  	String value = lines.get(i).split("\t")[1] ;
  	
  	if (lines.get(i).split("\t").length != 2)
  	{
  		logger.output(1, "ERROR: Incorrect line found in config file");
  		continue ;
  	}
  	
  	switch (parameterName)
  	{
  		case "verbosity:":
  			verbosity = Integer.parseInt(value) ;
  			break ;
  			
  		case "preserve_tmp_files:":
  			preserve_tmp_files = Boolean.parseBoolean(value) ;
  			break ;
  			
  		case "preserve_outputs:":
  			preserve_outputs = Boolean.parseBoolean(value) ;
  			break ;
  			
  		case "preserve_intputs:":
  			preserve_inputs = Boolean.parseBoolean(value) ;
  			break ;
  			  			
  		case "simulations:":
  			simulations = Integer.parseInt(value) ;
  			break ;
  			
  		case "population:":
  			population = Integer.parseInt(value) ;
  			break ;
  			
  		case "generations:":
  			generations = Integer.parseInt(value) ;
  			break ;
  			
  		case "selection:":
  			selection = Integer.parseInt(value) ;
  			break ;
  			
  		case "crossovers:":
  			crossovers = Integer.parseInt(value) ;
  			break ;
  			
  		case "balancemutations:":
  			balancemutations = Integer.parseInt(value) ;
  			break ;
  			
  		case "randommutations:":
  			randommutations = Integer.parseInt(value) ;
  			break ;
  			
  		case "complexmutations:":
  			complexmutations = Integer.parseInt(value) ;
  			break ;
  			
  		case "familymutations:":
  			familymutations = Integer.parseInt(value) ;
  			break ;
  			
  		case "activatorymutations:":
  			activatorymutations = Integer.parseInt(value) ;
  			break ;
  			
  		case "ormutations:":
  			ormutations = Integer.parseInt(value) ;
  			break ;
  			
  		case "andmutations:":
  			andmutations = Integer.parseInt(value) ;
  			break ;
  			
  		case "shufflemutations:":
  			shufflemutations = Integer.parseInt(value) ;
  			break ;
  			
  		case "target_fitness:":
  			target_fitness = Integer.parseInt(value) ;
  			break ;
  			
  		case "target_fitness_percent:":
  			target_fitness_percent = Integer.parseInt(value) ;
  			break ;
  			
  		case "bootstrap_mutations_factor:":
  			bootstrap_mutations_factor = Integer.parseInt(value) ;
  			break ;
  			
  		case "mutations_factor:":
  			mutations_factor = Integer.parseInt(value) ;
  			break ;
  			
  		case "bootstrap_shuffle_factor:":
  			bootstrap_shuffle_factor = Integer.parseInt(value) ;
  			break ;
  			
  		case "shuffle_factor:":
  			shuffle_factor = Integer.parseInt(value) ;
  			break ;
  			
  		case "models_saved:":
  			models_saved = Integer.parseInt(value) ;
  			break ;
  			
  		case "fitness_threshold:":
  			fitness_threshold = Float.parseFloat(value) ;
  			break ;
  			}
  		}
	}
	
	public String[] getConfig ()
	{
		
		String parameters[] = {
				"preserve_outputs",
				"preserve_inputs",
				"preserve_tmp_files",
				"population",
				"generations",
				"selection",
				"crossovers",
				"mutations",
				"balancemutations",
				"randommutations",
				"complexmutations",
				"familymutations",
				"inhibitorymutations",
				"activatorymutations",
				"ormutations",
				"andmutations",
				"shufflemutations",
				"target_fitness",
				"target_fitness_percent",
				"bootstrap_mutations_factor",
				"mutations_factor",
				"bootstrap_shuffle_factor",
				"shuffle_factor",
				"simulations",
				"models_saved",
				"fitness_threshold"
		} ;
		
		String values [] = {
				Boolean.toString(preserve_outputs),
				Boolean.toString(preserve_inputs),
				Boolean.toString(preserve_tmp_files),
				Integer.toString(population),
				Integer.toString(generations),
				Integer.toString(selection),
				Integer.toString(crossovers),
				Integer.toString(mutations),
				Integer.toString(balancemutations),
				Integer.toString(randommutations),
				Integer.toString(complexmutations),
				Integer.toString(familymutations),
				Integer.toString(inhibitorymutations),
				Integer.toString(activatorymutations),
				Integer.toString(ormutations),
				Integer.toString(andmutations),
				Integer.toString(shufflemutations),
				Integer.toString(target_fitness),
				Integer.toString(target_fitness_percent),
				Integer.toString(bootstrap_mutations_factor),
				Integer.toString(mutations_factor),
				Integer.toString(bootstrap_shuffle_factor),
				Integer.toString(shuffle_factor),
				Integer.toString(simulations),
				Integer.toString(models_saved),
				Float.toString(fitness_threshold),
		} ;

		ArrayList<String> lines = new ArrayList<String> () ;
		
		for (int i = 0; i < parameters.length; i++)
		{
			lines.add(parameters[i] + ": " + values[i]) ;
		}
		return (String[]) lines.toArray(new String[0]) ;

		
		
		
	}
	
	private void writeConfigFileTemplate (String filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter(filename, "UTF-8");
		
		
		// Write header with '#'
		writer.println("# Gitsbe config file") ;
		writer.println("# Each line is a parameter name and value") ;
		writer.println("# Default parameters are given below") ;
		writer.println("#") ;
		writer.println("# Parameter\tValue") ;
		writer.println("#") ;

		// Write parameters
		writer.println("# Output verbosity level") ;
		writer.println("verbosity:\t3") ;
		writer.println() ;
		writer.println("# Preserve all temporary files in project tmp-folder (all model files generation by evolutionary algorithms") ;
		writer.println("preserve_tmp_files:\ttrue") ;
		writer.println() ;
		writer.println("# Model trimming") ;
		writer.println("# Preserve outputs") ;
		writer.println("preserve_outputs:\ttrue") ;
		writer.println() ;
		writer.println("# Preserve intputs") ;
		writer.println("preserve_intputs:\tfalse") ;
		writer.println() ;
		writer.println("# Invoke drug response module after automated model definitions") ;
		writer.println("invoke_drabme:\tfalse") ;
		writer.println() ;
		writer.println("# Location of directory with drabme") ;
		writer.println("location_drabme:\t../Drabme") ;
		writer.println() ;
		writer.println("# Parameters for evolutionary algorithms") ;
		writer.println("# Number of simulations (evolutions) to run") ;
		writer.println("simulations:\t10");
		writer.println() ;
		writer.println("# Number of models per generation") ;
		writer.println("population:\t20") ;
		writer.println() ;
		writer.println("# Number of generations per simulation (or less if target_fitness is defined, below)") ;
		writer.println("generations:\t20") ;
		writer.println() ;
		writer.println("# Number of crossovers") ;
		writer.println("crossovers:\t1");
		writer.println() ;
		writer.println("# Number of models selected for next generation") ;
		writer.println("selection:\t4") ;
		writer.println() ;
		writer.println("# Type of mutations to introduce") ;
		writer.println("balancemutations:\t5") ;
		writer.println("randommutations:\t0") ;
		writer.println("complexmutations:\t0") ;
		writer.println("familymutations:\t0") ;
		writer.println("inhibitorymutations:\t0") ;
		writer.println("activatorymutations:\t0") ;
		writer.println("ormutations:\t0") ;
		writer.println("andmutations:\t0") ;
		writer.println("shufflemutations:\t5") ;
		writer.println() ;
		writer.println("# Target fitness threshold to stop evolution (0 means disabled)") ;
		writer.println("target_fitness:\t0") ;
		writer.println("") ;
		writer.println("# Target fitness threshold to stop evolution in percent of max possible fitness given by steady state (0 means disabled)") ;
		writer.println("target_fitness_percent:\t0") ;
		writer.println() ;
		writer.println("# Factor to multiply number of mutations until initial phase is over (>0 stable states obtained)") ;
		writer.println("bootstrap_mutations_factor:\t2") ;
		writer.println() ;
		writer.println("# Factor to multiply number of mutations after initial phase is over (>0 stable states obtained)") ;
		writer.println("mutations_factor:\t1") ;
		writer.println() ;
		writer.println("# Factor to multiply number of regulator priority shuffles until initial phase is over") ;
		writer.println("bootstrap_shuffle_factor:\t0") ;
		writer.println() ;
		writer.println("# Factor to multiply number of regulator priority shuffles after initial phase is over") ;
		writer.println("shuffle_factor:\t1") ;
		writer.println() ;
		writer.println("# Number of models to save") ;
		writer.println("models_saved:\t5") ;
		writer.println() ;
		writer.println("# Threshold for savig models") ;
		writer.println("fitness_threshold:\t0.1") ;
		
		writer.close() ;
	}

	public int getVerbosity() {
		return verbosity;
	}

	public boolean isPreserve_outputs() {
		return preserve_outputs;
	}

	public boolean isPreserve_inputs() {
		return preserve_inputs;
	}

	public boolean isPreserve_tmp_files() {
		return preserve_tmp_files;
	}

	public int getPopulation() {
		return population;
	}

	public int getGenerations() {
		return generations;
	}

	public int getSelection() {
		return selection;
	}

	public int getCrossovers() {
		return crossovers;
	}

	public int getMutations() {
		return mutations;
	}

	public int getBalancemutations() {
		return balancemutations;
	}

	public int getRandommutations() {
		return randommutations;
	}

	public int getComplexmutations() {
		return complexmutations;
	}

	public int getFamilymutations() {
		return familymutations;
	}

	public int getInhibitorymutations() {
		return inhibitorymutations;
	}

	public int getActivatorymutations() {
		return activatorymutations;
	}

	public int getOrmutations() {
		return ormutations;
	}

	public int getAndmutations() {
		return andmutations;
	}

	public int getTarget_fitness() {
		return target_fitness;
	}

	public int getTarget_fitness_percent() {
		return target_fitness_percent;
	}

	public int getBootstrap_mutations_factor() {
		return bootstrap_mutations_factor;
	}

	public int getSimulations() {
		return simulations;
	}

	public int getModels_saved() {
		return models_saved;
	}

	public float getFitness_threshold() {
		return fitness_threshold;
	}

	public String getFilenameConfig() {
		return filenameConfig;
	}

	/**
	 * @return the mutations_factor
	 */
	public int getMutations_factor() {
		return mutations_factor;
	}

	/**
	 * @return the shuffle_factor
	 */
	public int getShuffle_factor() {
		return shuffle_factor;
	}

	/**
	 * @return the shufflemutations
	 */
	public int getShufflemutations() {
		return shufflemutations;
	}

	/**
	 * @return the bootstrap_shuffle_factor
	 */
	public int getBootstrap_shuffle_factor() {
		return bootstrap_shuffle_factor;
	}

	
	

	

	
}
