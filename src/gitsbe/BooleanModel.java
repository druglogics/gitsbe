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

//import drabme.Drug;



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
	
	public BooleanModel()
	{
		this.verbosity = Gitsbe.verbosity ;
	}
	
	// Constructor for defining Boolean model from a "general model" with interactions
	public BooleanModel(GeneralModel generalModel) {
	
		this.verbosity = Gitsbe.verbosity ;
		
		this.modelName = generalModel.getModelName () ;
		
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
	
	
	public int getNumberOfStableStates ()
	{
		return stableStates.size() ;
	}
	
	// Constructor for defining Boolean model from a file with a set of Boolean equations
	public BooleanModel (String filename)
	{
		ArrayList<String> lines ;
		
		Logger.output(1, "Loading Boolean model from file: " + filename);
		
		try {
			lines = this.loadFile(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ;
		}
		
		// load gitsbe file format
		if (filename.substring(filename.length() - ".gitsbe".length()).equals(".gitsbe")) 
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
			
//			for (int i = 0; i < mapAlternativeNames.size(); i++)
//			{
//				System.out.println (mapAlternativeNames.get(i)[0] + " = " + mapAlternativeNames.get(i)[1]) ;
//			}
//			try {
//				this.saveFile("ags_paradigm_debug.gitsbe") ;
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
	}
	
	public void exportSifFile (String filename) throws FileNotFoundException, UnsupportedEncodingException
	{

		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		for (int i = 0; i < booleanEquations.size(); i++) 
			for (int j = 0; j < booleanEquations.get(i).convertToSifLines().length; j++)
				writer.println(booleanEquations.get(i).convertToSifLines()[j]);
		
		
		writer.close() ;

		
	}
	public void setVerbosity (int verbosity)
	{
		this.verbosity = verbosity ;
	}
	
	// Copy constructor for defining Boolean model from another Boolean model
	protected BooleanModel (final BooleanModel booleanModel) {
		
		
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
		
		this.verbosity = Gitsbe.verbosity ;
        
	}
	
	public void saveFile (String directoryName) throws IOException
	{
		String filename = this.modelName + ".gitsbe" ;
		
		PrintWriter writer = new PrintWriter(directoryName + filename, "UTF-8");
		
		
		// Write header with '#'
		writer.println("#Boolean model file in gitsbe format") ;
		
		// Write model name
		writer.println("modelname: " + this.modelName) ;
		
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
			writer.println("<value val=\"1\">") ;
			writer.println("<exp str=\"" + expressions[i] + "\"/>") ;
			writer.println("</value>") ;
			writer.println("<nodevisualsetting x=\"10\" y=\"10\" style=\"\"/>") ;
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
//		temp = temp.replace ("|", " or") ;
//		temp = temp.replace ("!", "not") ;
		
		String lines[] = temp.split("\n") ;
		
		return lines ;
		
	}

	public void writeBooleanExpressionGinmlFile () throws IOException
	{
		
		
//		ArrayList <String> lines = new ArrayList <String> () ;
		
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
		
		/*
		temp = temp.replace ("&", "and");
		temp = temp.replace ("|", " or") ;
		temp = temp.replace ("!", "not") ;
		*/
		
		
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
		
		// Replace Boolean operators
//		temp = temp.replace (" and ", " & ");
//		temp = temp.replace (" or ", " | ") ;
//		temp = temp.replace (" not ", " ! ") ;
//		temp = temp.replace (" true ", " 1 ") ;
//		temp = temp.replace (" false ", " 0 ") ;
		
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
	
	public void calculateStableStatesVC (String directorybnet) throws FileNotFoundException, UnsupportedEncodingException, IOException
	{
		// Use default temporary folder, under the bnet folder
//		System.out.println("DEBUG: " + directorybnet);
		this.calculateStableStatesVC(directorybnet, directorybnet + "tmp" + File.separator);
	}
	
	public void calculateStableStatesVC (String directorybnet, String directorytmp) throws FileNotFoundException, UnsupportedEncodingException, IOException {
	
		// Defined model in Veliz-Cuba terminology
		String [] modelVC = this.getModelVelizCuba() ;
				
		// Write model to file for 'BNreduction.sh'
		PrintWriter writer = new PrintWriter(directorytmp + modelName + ".dat", "UTF-8");
		
		for (int i = 0; i < modelVC.length ; i++)
		{
			writer.println(modelVC[i]) ;
		}
		
		writer.close();
		
		try {
			
			// "BNReduction_timeout.sh" calls BNReduction.sh, but with the 'timeout' commanding, ensuring that the process has to
			// complete within specified amount of time (in case BNReduction should hang).
			
			
			ProcessBuilder pb = new ProcessBuilder("sh", "BNReduction_timeout.sh", directorytmp + modelName + ".dat");
			
			if (Logger.getVerbosity() >= 3)
			{
				pb.redirectErrorStream(true);
				pb.redirectOutput() ;
			}
			
			// TODO - AAF - get working directory and etc... this hack works for now
			
			pb.directory (new File (directorybnet)) ;
			
			Logger.output(3, "Running BNReduction_timeout.sh in directory " + pb.directory()) ;
			
			
			Process p ;
			p = pb.start ();
			
//			System.out.print("\nOutput from BNReduction.sh:\n") ;
			try {
				p.waitFor() ;
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while(r.ready()) {
                System.out.println(r.readLine());
            }
//			System.out.print(p.getOutputStream().toString());
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Read stable states from BNReduction.sh output file
		String filename = directorytmp + modelName + ".dat.fp" ;
		
		BufferedReader reader = new BufferedReader(new FileReader (filename)) ;
		
		Logger.output(2, "Reading steady states: " + filename);
		
		
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
		
		if (Logger.getVerbosity()>= 2)
			{
	        if (stableStates.size() > 0)
	        {
	        	if (stableStates.get(0).toString().length() > 0)
	        	{
	        		
	        		Logger.output(2, "BNReduction found " + stableStates.size() + " stable states:") ;
	        		for (int i = 0; i < stableStates.size(); i++)
	        		{
	        			Logger.output(2, "Stable state " + (i + 1) + ": " + stableStates.get(i)) ;
	        		}
	        		
	        		if (Logger.getVerbosity() >= 3)
        			{
	        			
        				for (int i = 0; i < stableStates.get(0).length(); i++)
        				{
    	        			String states = "" ;

        					for (int j = 0; j < stableStates.size(); j++)
        					{
        						states += "\t" + stableStates.get(j).charAt(i) ;
        					}
        					
        					
        					
        					Logger.output(3, mapAlternativeNames.get(i)[0] + states) ;
        				}
        			}
	        		
	        	}
	        }
	        
	        else
	        {
	        	Logger.output(2, "BNReduction found no stable states.\n");
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
	
	/*
	public String[] printBooleanModelVelizCuba ()
	{
		String[] model = new String[booleanEquations.size()] ;
		
		for (int i = 0; i < booleanEquations.size(); i++)
		{
			model[i] = booleanEquations.get(i) ;
			
		}
		
		return model ;
	}*/
	
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
	
	protected int getIndexOfEquation (String target)
	{

		int index = -1 ;
		
		for (int i = 0; i < mapAlternativeNames.size(); i++)
		{
//			System.out.println(mapAlternativeNames.get(i)[0]) ;

			if (target.trim().equals(mapAlternativeNames.get(i)[0].trim()))
			{
				
				index = i ;
				
			}
		}
		
		return index ;
	}
	
	
	
}
