package gitsbe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * 
 * @author Asmund Flobak, email: asmund.flobak@ntnu.no
 *
 */
public class BooleanModel {

	protected ArrayList <BooleanEquation> booleanEquations ; 
	
	protected ArrayList <String[]> mapAlternativeNames ;
	
	protected ArrayList <String> stableStates ;
	
	protected String modelName ;
	
	protected int verbosity = 2 ;
	
	protected String filename ;
	
	protected Logger logger ;

	public static String directoryBNET = System.getenv("BNET_HOME");
	
	public BooleanModel(Logger logger)
	{
		this.logger = logger ;
	}
	
	// Constructor for defining Boolean model from a "general model" with interactions
	public BooleanModel(GeneralModel generalModel, Logger logger) {
	
		this.logger = logger ;
		
		this.verbosity = logger.getVerbosity() ;
		
		this.modelName = generalModel.getModelName() ;
		
		booleanEquations = new ArrayList <BooleanEquation> () ;
		
		mapAlternativeNames = new ArrayList <String[]> () ;
		
		stableStates = new ArrayList <String> () ;
		
		for (int i = 0; i < generalModel.size() ; i++)
		{
			// Define Boolean equation from multiple interaction
			BooleanEquation booleanEquation = new BooleanEquation (generalModel.getMultipleInteraction(i)) ;
			
			// Build list of alternative names used for Veliz-Cuba bnet stable states compution (x1, x2, x3,  ..., xn)
			
			String[] temp = new String[2] ;
			temp[0] = generalModel.getMultipleInteraction(i).getTarget() ;
			temp[1] = "x" + (i + 1) ;
			
			mapAlternativeNames.add(temp) ;
			
			// Add Boolean equation to ArrayList, with index corresponding to mapAlternativeNames
			booleanEquations.add(booleanEquation) ;
		}
	}
	
	
	/**
	 * Constructor for defining Boolean model from a file with a set of Boolean equations
	 * 
	 * Currently two supported filetypes: .gitsbe and .booleannet files
	 * 
	 * @param filename
	 */
	public BooleanModel (String filename, Logger logger)
	{
		this.logger = logger ;
		ArrayList<String> lines ;
		
		logger.output(1, "Loading Boolean model from file: " + filename);
		
		try {
			lines = this.loadFile(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ;
		}
		
		// load gitsbe file format
		if (filename.substring(filename.length() - ".gitsbe".length()).toLowerCase().equals(".gitsbe")) 
		{
			// Boolean equations
			this.booleanEquations = new ArrayList <BooleanEquation> () ;
			
			// Alternative names
			this.mapAlternativeNames = new ArrayList <String[]> () ;
			
			// Load model
			for (int i = 0 ; i < lines.size(); i++)
			{ 
				String prefix = lines.get(i).substring(0, lines.get(i).indexOf(' ')) ;
				String line = lines.get(i).substring(lines.get(i).indexOf(' ')) ;
				switch(prefix) {
					case "modelname:":
						this.modelName =  line.trim() ;
						break ;
					case "equation:":
						booleanEquations.add(new BooleanEquation(line)) ;
						break ;
					case "mapping:":
						
						String[] temp = new String[2] ;
						temp = line.split(" = ") ; 
						mapAlternativeNames.add(new String[]{temp[0].trim(), temp[1].trim()}) ;
						
						break ;
				}
			}
		
		} 
		else if (filename.substring(filename.length() - ".booleannet".length()).equals(".booleannet"))
		{
			// Boolean equations
			this.booleanEquations = new ArrayList <BooleanEquation> () ;
			
			// Alternative names
			this.mapAlternativeNames = new ArrayList <String[]> () ;
			
			this.modelName = filename.substring(0, filename.indexOf(".booleannet")) ;
			
			// Load model
			for (int i = 0 ; i < lines.size(); i++)
			{ 
				booleanEquations.add(new BooleanEquation(lines.get(i))) ;
				String target = lines.get(i).substring(0, lines.get(i).indexOf(" *=")).trim() ;
				mapAlternativeNames.add( new String[]{target, new String ("x" + (i + 1))}) ;
			}
		}
	}
	
	// Copy constructor for defining Boolean model from another Boolean model
	protected BooleanModel (final BooleanModel booleanModel, Logger logger) 
	{
		this.logger = logger ;
		
		// Copy Boolean equations
		this.booleanEquations = new ArrayList <BooleanEquation> () ;
		
		for (int i = 0; i < booleanModel.booleanEquations.size(); i++)
		{
			booleanEquations.add(booleanModel.booleanEquations.get(i)) ;
		}
				
		// Copy mapAlternativeNames
		this.mapAlternativeNames = new ArrayList <String[]> () ;
		
		for(int i = 0; i < booleanModel.mapAlternativeNames.size(); i++)
		{
			this.mapAlternativeNames.add(booleanModel.mapAlternativeNames.get(i)) ;
		}
		
		// Stable states (empty)
		stableStates = new ArrayList <String> () ;
		
		// Copy modelName
		this.modelName = new String(booleanModel.modelName) ;
		
		this.verbosity = logger.getVerbosity () ;
        
	}
	
	public int getNumberOfStableStates ()
	{
		return stableStates.size() ;
	}
		
		
	public void exportSifFile (String outputDirectory, String filename) throws FileNotFoundException, UnsupportedEncodingException
	{

		PrintWriter writer = new PrintWriter(new File(outputDirectory, filename).getAbsolutePath(), "UTF-8");

		for (int i = 0; i < booleanEquations.size(); i++) 
			for (int j = 0; j < booleanEquations.get(i).convertToSifLines().length; j++)
				writer.println(booleanEquations.get(i).convertToSifLines()[j]);
		
		
		writer.close() ;

		
	}
	public void setVerbosity (int verbosity)
	{
		this.verbosity = verbosity ;
	}
	
	
	
	public void saveFile (String directoryName) throws IOException
	{
	    String filename = this.modelName.substring(this.modelName.lastIndexOf('/') + 1) + ".gitsbe";
	
		PrintWriter writer = new PrintWriter(new File(directoryName, filename).getPath(), "UTF-8");
	
		
		// Write header with '#'
		writer.println("#Boolean model file in gitsbe format") ;
		
		// Write model name
		writer.println("modelname: " + this.modelName) ;
		
		// Write stable states 
		for (int i = 0; i < this.stableStates.size(); i++)
		{
			writer.println("stablestate: " + this.stableStates.get(i)) ;
		}
		
		// Write Boolean equations
		for (int i = 0; i < booleanEquations.size() ; i++)
		{
			writer.println("equation: " + booleanEquations.get(i).getBooleanEquation()) ;
		}
		
		// Write alternative names for Veliz-Cuba
		for (int i = 0; i < mapAlternativeNames.size(); i++)
		{
			writer.println("mapping: " + mapAlternativeNames.get(i)[0] + " = " + mapAlternativeNames.get(i)[1]) ;
		}
	
		writer.close() ;
			
	}
	
	protected ArrayList<String> loadFile (String filename) throws IOException 
	{
		ArrayList <String> lines = new ArrayList <String>() ;
		
		BufferedReader reader = new BufferedReader(new FileReader (filename)) ;
		
        try {
	        while (true) {
	            String line = reader.readLine();
	            // no more lines to read
	            if (line == null) {
	                reader.close();
	                break;
	            }
	
	            if (!line.startsWith("#")) 
	            {
	            	lines.add(line);
	            	
	            }
	        
	        }
        }
        
        finally {
	       	reader.close ();
	    }
        
       return lines ;
	}

	public ArrayList <String> getNodeNames ()
	{
		ArrayList <String> nodeNames = new ArrayList <String> () ;
		
		for (int i = 0; i < mapAlternativeNames.size(); i++)
		{
			nodeNames.add(mapAlternativeNames.get(i)[0]) ;
		}
		
		return nodeNames ;
	}
	
	
	public void writeGinmlFile (ArrayList <SingleInteraction> singleInteractions) throws IOException
	{
		PrintWriter writer = new PrintWriter(modelName + ".ginml", "UTF-8") ;
		
		String[] expressions = this.printBooleanModelGinmlExpressions() ;
		
		//write heading
		
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") ;
		writer.println("<!DOCTYPE gxl SYSTEM \"http://ginsim.org/GINML_2_2.dtd\">") ;
		writer.println("<gxl xmlns:xlink=\"http://www.w3.org/1999/xlink\">") ;
		
		// write nodeorder
		
		writer.print("<graph class=\"regulatory\" id=\"" + modelName.replace(".", "_") + "\" nodeorder=\"") ;
		for (int i = 0; i < mapAlternativeNames.size(); i++)
		{
			writer.print(mapAlternativeNames.get(i)[0] + " ") ;
		}
		writer.print("\">\n") ;
		
		// node style edge style
		
		// write nodes with Boolean expression
		for (int i = 0; i < mapAlternativeNames.size(); i++)
		{
			writer.println("<node id=\"" + mapAlternativeNames.get(i)[0] + "\" maxvalue=\"1\">") ;
			writer.println("\t<value val=\"1\">") ;
			writer.println("\t\t<exp str=\"" + expressions[i] + "\"/>") ;
			writer.println("\t</value>") ;
			writer.println("\t<nodevisualsetting x=\"10\" y=\"10\" style=\"\"/>") ;
			writer.println("</node>") ;
			
		}
		
		// write edges
		for (int i = 0; i < singleInteractions.size(); i++)
		{
			String source = singleInteractions.get(i).getSource();
			String target = singleInteractions.get(i).getTarget() ;
			String arc ;
			
			if (singleInteractions.get(i).getArc() == 1)
			{
				arc = "positive" ;
			}
			else
			{
				arc = "negative" ;
			}
					
			writer.print("<edge id=\"" + source + ":" + target + "\" from=\"" + source + "\" to=\"" 
					+ target + "\" minvalue=\"1\" sign=\"" + arc + "\">\n") ;
			writer.println("<edgevisualsetting style=\"\"/>") ;
			writer.println("</edge>");
			
		}
		
		// finalize
		writer.println("</graph>");
		writer.println("</gxl>");
		writer.close();
		
	}
	
	public String[] printBooleanModelGinmlExpressions () 
	{
		
		String temp = "" ;
		String line = "" ;
		
		for (int i = 0; i < booleanEquations.size(); i++)
		{
			line = booleanEquations.get(i).getBooleanEquation() + "\n" ;
			temp += line ;
		}
		
		for (int i = mapAlternativeNames.size() -1; i >= 0 ; i--)
		{
			temp = temp.replace((mapAlternativeNames.get(i)[1]), mapAlternativeNames.get(i)[0] + " ") ;
		}
		
		temp = temp.replace ("&", "&amp;");
		
		String lines[] = temp.split("\n") ;
		
		return lines ;
		
	}

	public void writeBooleanExpressionGinmlFile () throws IOException
	{
		
		
		String temp = "" ;
		String line = "" ;
		
		for (int i = 0; i < booleanEquations.size(); i++)
		{
			line = mapAlternativeNames.get(i)[0] + ": " + booleanEquations.get(i).getBooleanEquation() + "\n" ;
			temp += line ;
		}
		
		for (int i = mapAlternativeNames.size() -1; i >= 0 ; i--)
		{
			temp = temp.replace((mapAlternativeNames.get(i)[1]), mapAlternativeNames.get(i)[0] + " ") ;
		}
		
		String lines[] = temp.split("\n") ;
		
		PrintWriter writer = new PrintWriter(modelName + "_rawexpr.txt");
		
		for (int i = 0; i < lines.length; i++)
		{
			writer.println(lines[i]) ;
		}
		writer.close();
		
	}
	
	public String[] getModelBooleannet ()
	{
		
		String [] temp = new String[booleanEquations.size()] ;
		
		for (int i = 0; i < booleanEquations.size(); i++)
		{
			temp[i] = booleanEquations.get(i).getBooleanEquation() ;
		}
		
		
		return temp ;
		
	}
	
	public String[] getModelVelizCuba ()
	{
		
		String temp = "" ;
		
		for (int i = 0; i < booleanEquations.size() ; i++)
		{
			temp = temp + booleanEquations.get(i).getBooleanEquationVC() + "\n";	
			
		}
		
		// Use alternate names (x1, x2, ..., xn)
		for (int i = 0; i < booleanEquations.size(); i++)
		{
			temp = temp.replace((" " + mapAlternativeNames.get(i)[0] + " "), " " + mapAlternativeNames.get(i)[1] + " ") ;
		}
				
		String lines[] = temp.split("\n") ;
		
		String result [] = new String[booleanEquations.size()] ;
		
		// Remove target node (line number indicates which variable is defined, i.e. 'x1' on line 1, 'x2' on line 2 etc.
		for (int i = 0; i < lines.length; i++)
		{
			result[i] = (lines[i].substring(lines[i].indexOf('=') + 1).trim()) ;
		}
		
		return result ;
	}
	
	public void calculateStableStatesVC (String outputDirectory) throws FileNotFoundException, UnsupportedEncodingException, IOException
	{
		this.calculateStableStatesVC(directoryBNET, outputDirectory);
	}
	
	public void calculateStableStatesVC (String directoryBNET, String outputDirectory) throws FileNotFoundException, UnsupportedEncodingException, IOException {
	
		// Defined model in Veliz-Cuba terminology
		String [] modelVC = this.getModelVelizCuba();
				
		// Write model to file for 'BNreduction.sh'
				
		PrintWriter writer = new PrintWriter(outputDirectory + File.separator + modelName + ".dat", "UTF-8");
		
		for (int i = 0; i < modelVC.length ; i++)
		{
			writer.println(modelVC[i]) ;
		}
		
		writer.close();
		
		try {
			
			// "BNReduction_timeout.sh" calls BNReduction.sh, but with the 'timeout' commanding, ensuring that the process has to
			// complete within specified amount of time (in case BNReduction should hang).
			
			
//			ProcessBuilder pb = new ProcessBuilder("sh", "BNReduction_timeout.sh", outputDirectory + File.separator + modelName + ".dat");
			ProcessBuilder pb = new ProcessBuilder("timeout", "30", 
													new File(directoryBNET, "BNReduction.sh").getAbsolutePath(), 
													new File(outputDirectory, modelName + ".dat").getAbsolutePath());
			
			if (logger.getVerbosity() >= 3)
			{
				pb.redirectErrorStream(true);
				pb.redirectOutput() ;
			}
			
			// TODO - AAF - get working directory and etc... this hack works for now
			
			pb.directory (new File (directoryBNET)) ;
			
//			logger.output(3, "Running BNReduction_timeout.sh in directory " + pb.directory()) ;
			logger.output(3, "Running BNReduction.sh in directory " + pb.directory()) ;
			
			Process p ;
			p = pb.start ();
			
			try {
				p.waitFor() ;
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while(r.ready()) {
	        	logger.output(3, r.readLine());
            }
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Read stable states from BNReduction.sh output file
		String filename = outputDirectory + File.separator + modelName + ".dat.fp";
		
		BufferedReader reader = new BufferedReader(new FileReader (filename)) ;
		
		logger.output(2, "Reading steady states: " + filename);
		
		
		try {
	        while (true) {
	            String line = reader.readLine();
	            // no more lines to read
	            if (line == null) {
	                reader.close();
	                break;
	            }
	            
	            if (!line.startsWith("#")) 
	            {
	            	
	            	stableStates.add(line);
	            	
	            }
	        
	        }
        }
        finally {
	       	reader.close ();
	    }
		
		if (logger.getVerbosity()>= 2)
			{
	        if (stableStates.size() > 0)
	        {
	        	if (stableStates.get(0).toString().length() > 0)
	        	{
	        		
	        		logger.output(2, "BNReduction found " + stableStates.size() + " stable states:") ;
	        		for (int i = 0; i < stableStates.size(); i++)
	        		{
	        			logger.output(2, "Stable state " + (i + 1) + ": " + stableStates.get(i)) ;
	        		}
	        		
	        			
    				for (int i = 0; i < stableStates.get(0).length(); i++)
    				{
	        			String states = "" ;

    					for (int j = 0; j < stableStates.size(); j++)
    					{
    						states += "\t" + stableStates.get(j).charAt(i) ;
    					}
    					
    					
    					
//    					logger.debug(mapAlternativeNames.get(i)[0] + states) ;
    				}
    				
	        	}
	        }
	        
	        else
	        {
	        	logger.output(2, "BNReduction found no stable states.\n");
	        }
		}
        /*
        for (int i = 0; i < mapAlternativeNames.size(); i++)
        {
        	System.out.print("\n" + mapAlternativeNames.get(i)[0] + " = " + mapAlternativeNames.get(i)[1] + ": ") ;
        			
        	for (int j = 0; j < lines.size() ; j++)
        	{
        		System.out.print(lines.get(j).toString().charAt(i)) ;

        	}
        }
		*/
	}
	

	
	public String[] printBooleanModelBooleannet () 
	{
		ArrayList <String> booleanEquationsBooleannet = new ArrayList <String> () ;
		
		String temp = "" ;
		String line = "" ;
		
		for (int i = 0; i < booleanEquations.size(); i++)
		{
			line = booleanEquations.get(i) + "\n" ;
			temp += line ;
		}
		
		for (int i = mapAlternativeNames.size() -1; i >= 0 ; i--)
		{
			temp = temp.replace((mapAlternativeNames.get(i)[1]), mapAlternativeNames.get(i)[0] + " ") ;
		}
		
		temp = temp.replace ("&", "and");
		temp = temp.replace ("|", " or") ;
		temp = temp.replace ("!", "not") ;
		
		String lines[] = temp.split("\n") ;
		
		return lines ;
		
	}
	
	public void writeSteadyStateTemplateFile () throws IOException
	{
		PrintWriter writer = new PrintWriter(modelName + "_SteadyState", "UTF-8");
		
		
		// Write header with '#'
		writer.println("# Gitsbe steady states file") ;
		writer.println("# Each line is a set of stable state observations per node") ;
		writer.println("# First column is node name (which must match name in model file),") ;
		writer.println("# subsequent columns are observations (0=inactive, 1=active)") ;
		writer.println("#") ;
		writer.println("#NodeName\tValue") ;
		
		// Write columns with model-defined node names and node names for Veliz-Cuba's algorithm
		for (int i = 0; i < booleanEquations.size() ; i++)
		{
			writer.println(mapAlternativeNames.get(i)[0] + "\t-" ) ;
		}
		
		writer.close() ;
	}
	
	public String getModelName () {
		return this.modelName ;
	}
	
	public void setFilename (String filename)
	{
		this.filename = filename ;
	}
	
	public String getFilename ()
	{
		return filename ;
	}
	
	/**
	 * Get index of equation ascribed to specified target
	 * 
	 * @param target
	 * @return
	 */
	public int getIndexOfEquation (String target)
	{

		int index = -1 ;
		
		for (int i = 0; i < mapAlternativeNames.size(); i++)
		{

			if (target.trim().equals(mapAlternativeNames.get(i)[0].trim()))
			{
				
				index = i ;
				
			}
		}
		
		return index ;
	}
	
	/**
	 * Checks if there is one or several stable states for the model
	 * 
	 * @return
	 */
	public boolean hastStableStates()
	{
		return (stableStates.size() > 0) ;
	}
	
	public String[][] getStableStates()
	{
		String[][] result = new String[stableStates.size() + 1][getNodeNames().size()];
		
		result[0] = getNodeNames().toArray(new String[0]);
		
		for (int i = 0; i < stableStates.size(); i++)
		{
			result[i+1] = stableStates.get(i).split("(?!^)"); // if using "" to split this will return the correct list but empty first element, fixed in jdk8
		}
		
		return result;
		
	}
	
	/** Compute stable state for given condition (modification) to model
	 * Each condition consists of two strings: One specifying node name, the second specifying fixed states for corresponding nodes (0 or 1)
	 * Note that condition can optionally contain un-specified nodes, indicated by dashes ('-')
	 * 
	 * @param condition
	 * @return
	 */
	public String[][] getStableStatesPerturbation(String[][] condition, String outputDirectory)
	{

		// modifyEquation
		BooleanModel temp = new BooleanModel(this, logger);

		for (int i = 0; i < condition.length; i++)
		{
			// Define target of equation
			String equation = condition[i][0].trim() + " *= ";

			// Modify equation, for now only fixes to true/false supported, but could also be 
			// generic, i.e. any changes to equations.
			switch (condition[i][1].trim())
			{
				case "1":
				case "true":
					equation += "true";
					break;
				case "0":
				case "false":
					equation += "false";
					break;
				default:
					
			}

			temp.modifyEquation(equation);

		}
		
		
		
		
		try {
			temp.calculateStableStatesVC(outputDirectory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return temp.getStableStates() ;
	}
	
	/**
	 * Modify equation, the function will identify correct equation based on target name
	 * 
	 * @param equation
	 */
	protected void modifyEquation(String equation)
	{
		// Get index of equation for specified target
		int index = getIndexOfEquation (equation.split(" ")[0].trim()) ;
		
		if (index < 0)
		{
			logger.error("Target of equation [" + equation + "] not found, this will crash the program. Non-matching name in topology and query?");
		}
		booleanEquations.set(index, new BooleanEquation(equation));
		
	}
	
}
