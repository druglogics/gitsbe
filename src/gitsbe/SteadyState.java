package gitsbe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SteadyState {

	private String nameExperimentalSystem ;
	
	private int maxFitness ;
	
	private String[] steadyStates ;
	
	private BooleanModel booleanModel ; 
	
	private Logger logger ;
	
	/**
	 * SteadyState can currently only hold one steady state
	 * 
	 * @param filename pointing to tsv with steady state
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public SteadyState(String filename, BooleanModel booleanModel, Logger logger) throws IOException, FileNotFoundException {
		
		this.booleanModel = booleanModel ;
		this.logger = logger ;
		
		ArrayList <String> lines = new ArrayList <String> () ;
//		steadyState = new String () ;
		
		logger.output(1, "Reading file " + new File(filename).getAbsolutePath());
		
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
        
        
        // Get all steady states in an array of String
        // Read states for those nodes that are specified in steady-state file, leave others with 'unknown' mark ('-')
        steadyStates = new String[numColumns(lines.get(0))] ;
        
        for (int i = 0; i < (numColumns(lines.get(0))); i++)
        {
        	// Fill all states with 'unknown' mark ('-') 
        	
        	StringBuilder steadyState = new StringBuilder(dashes(booleanModel.getNodeNames().size())) ;
        	for (int j = 0; j < lines.size(); j++)
        	{

        		// Get node name from first column
	        	String nodeName = lines.get(j).split("\t")[0] ;
	        	
	        	// Get steady state observation from 2nd++ column
	        	int indexNode = booleanModel.getIndexOfEquation(nodeName) ;
	        	
	        	if (booleanModel.getIndexOfEquation(nodeName) >= 0)
	        	{
	        		steadyState.setCharAt(indexNode, lines.get(j).split("\t")[1+i].toCharArray()[0]);
	        	}
        	}
        	
        	steadyStates[i] = steadyState.toString() ;
        }
        
        // Get all steady states in an array of Strings
        // First two columns of steady state file are nodenames and alternative nodenames, these are skipped
        
        /* Old code - updated 2015.03.04 - but this is working - update to allow steady state file to have nodes ordered randomly,
         * and not following order in boolean model definition (.gitsbe) file
         *
        steadyStates = new String[numColumns(lines.get(0))-1] ;
        
        for (int i = 0; i < (numColumns (lines.get(0)) - 1); i++)
        {
        	steadyStates[i] = "" ;
        	
        	for (int j = 0; j < lines.size(); j++)
        	{
        		steadyStates[i] += lines.get(j).split("\t")[2+i];
        	}
        }
        */
        
        // very old code
//        for (int i = 0; i < lines.size(); i++)
//        {
//        	steadyState += lines.get(i).split("\t")[2] ;
//        }
//		
	}
	
	public String[] getSteadyStates ()
	{
		return steadyStates ;
	}
	
	public String[] getSteadyStatesVerbose ()
	{
		
		ArrayList<String> lines = new ArrayList<String> () ;
		
		for (int i = 0; i < booleanModel.getNodeNames().size(); i++)
		{
			String line = booleanModel.getNodeNames().get(i) ;
			for (int j = 0; j < steadyStates.length; j++)
			{
				line += "\t" + steadyStates[j].charAt(i) ;
			}

			lines.add(line) ;
			
		}
		
		return lines.toArray(new String[0]) ;
	}
//	public String getSteadyState ()
//	{
//		return steadyState ;
//	}
//	
	/**
	 * Returns maximum fitness determined by steady state
	 * 
	 * @return maxFitness
	 */
	public int getMaxFitness ()
	{
		maxFitness = 0;
		
		for (int k = 0; k < steadyStates.length; k++) 
		{
			for (int i = 0; i < steadyStates[k].length(); i++)
			{
				if ((steadyStates[k].charAt(i) == '1') || (steadyStates[k].charAt(i) == '0'))
				{
						maxFitness += 1;
				}
			}
		}
		
		return maxFitness ;
	}
	
	
	
	
	private int numColumns (String line) 
	{
		int counter = 0;
		for( int i=0; i<line.length(); i++ ) {
		    if( line.charAt(i) == '\t' ) {
		        counter++;
		    } 
		}
		return counter ;
	}
	
	/**
	 * Get string with specified number of dashes
	 * 
	 * @param length
	 * @return
	 */
	private static String dashes(int length)
	{
		char[] dashes = new char[length];
		Arrays.fill(dashes, '-');
		return new String (dashes);
	}
}
