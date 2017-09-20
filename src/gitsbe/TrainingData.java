package gitsbe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class TrainingData {

	private ArrayList<TrainingDataObservation> observations;
	
	private String filenameTrainingData;
	private	Logger logger;
	
	private String sep = "\t";
	
	
	public TrainingData (String filenameTrainingData, Logger logger) throws IOException
	{
		this.filenameTrainingData = filenameTrainingData;
		this.logger = logger;
		observations = new ArrayList<TrainingDataObservation> ();
		
		readData(filenameTrainingData);
	}
	
	
	/**
	 * Read training data from file
	 * 
	 * File should be tab separated with observation node in first column followed by observation, separated by a colon ':'
	 * The observation can either be given as an absolute value, or a relative value to unperturbed system 
	 * (indicated by a preceeding + or - sign)
	 * 
	 * If observation is global output (e.g. growth) then name should be 'globaloutput'
	 * 
 	 * After this first observation column each column corresponds to each node and state in condition
	 * Hypothetical example 

Condition
ERK_f:1	PIK3CA:0	GSK_f:0	
Observation
globaloutput:0.1

Condition
ERK_f:-1
Observation
RSK_f:0	PIK3CA:0

Condition
PD
Observation
output:0.4

Condition
5Z
Observation
MAP3K7:-1

Condition
5Z	PD	GSK3_f:1
Observation
globaloutput:0.1

	 * 
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void readData (String filename) throws IOException
	{
		logger.output(3, "Reading training data observations file " + new File(filename).getAbsolutePath());
		
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

				// read lines, skip empty lines and comments (pound char)
				if ((!line.startsWith("#")) & line.length() > 0) {
					lines.add(line);
				}

			}
		} finally {
			reader.close();
		}
		
		
		// Define variables to be read from training data file

		ArrayList<String> condition = new ArrayList<String>() ;
		ArrayList<String> observation = new ArrayList<String>() ;

		// Process lines
		for (int i = 0; i < lines.size(); i++)
		{
			if (lines.get(i).toLowerCase().equals("condition"))
			{
				condition = new ArrayList<String>(Arrays.asList(lines.get(i+1).split("\t")));
				i++;
			}
			
			if (lines.get(i).toLowerCase().equals("observation"))
			{
				observation = new ArrayList<String>(Arrays.asList(lines.get(i+1).split("\t")));
				i++;
				
				
				observations.add(new TrainingDataObservation(condition, observation)) ;
			}
				
		}
		
	}
	
	public void writeTrainingDataTemplateFile (String filename) throws IOException
	{
		PrintWriter writer = new PrintWriter(filename, "UTF-8");
		
		
		// Write header with '#'
		writer.println("# Gitsbe training data file") ;
		writer.println("# Each condition specified contains a condition given by drug") ;
		writer.println("# Each condition specified contains a set node states and/or a global output");
		writer.println("# ");
		
	}

	/**
	 * Returns maximum fitness determined by training data
	 * 
	 * @return maxFitness
	 */
//	public int getMaxFitness ()
//	{
//		int maxFitness = 0;
//		
//		for (int i = 0; i < observations.size(); i++) 
//		{
//			for (int j = 0; j < observations.get(i).length(); i++)
//			{
//				if ((steadyStates[k].charAt(i) == '1') || (steadyStates[k].charAt(i) == '0'))
//				{
//						maxFitness += 1;
//				}
//			}
//		}
		
		return maxFitness ;
	}
	
	public int size ()
	{
		return observations.size();
	}
	
	public ArrayList<TrainingDataObservation> getObservations()
	{
		return observations;
	}
	
	public String[] getTrainingDataVerbose()
	{
		String result = "";
	
		for (int i = 0; i < observations.size(); i++)
		{
			result += "\nCondition " + i+1 + ":\n");
			result += observations.get(i).
		
	}
	
}
