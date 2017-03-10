package gitsbe;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class GeneralModel {
	
	   
    private ArrayList <SingleInteraction> singleInteractions = new ArrayList <SingleInteraction> () ;
    private ArrayList <MultipleInteraction> multipleInteractions = new ArrayList <MultipleInteraction> () ;
    private Logger logger ;
    //    ArrayList <Complex> complexes = new ArrayList <Copmplex> () ;
 
    private String modelName ;
    
	public GeneralModel(Logger logger) {
		this.logger = logger ;
		// TODO Auto-generated constructor stub
	}
	
	public void loadStableStateFile (String filename) throws IOException {
		
	}
	
	public void loadComplexesFile (String filename) throws IOException {
		
	}
	
	public int size () {
		return multipleInteractions.size() ;
	}
	
	public void loadInteractionFile (String filenameInteractions) throws IOException {
		
		// Load supported filetype, based on suffix
		switch (filenameInteractions.substring(filenameInteractions.length()-3).toLowerCase())
		{
		case "sif":
			this.loadSifFile(filenameInteractions) ;
			break;
		case "pid":
			this.loadParadigmFile(filenameInteractions);
			break;
		}	
		
		
	}
	
	public String getModelName() {
		return this.modelName ;
	}

	public void setModelName(String path) {
		this.modelName = path;
	}
	
	
	public String toString () 
	{
		String tostring = new String () ;
		
		for (int i = 0; i < multipleInteractions.size(); i++)
		{
			tostring += multipleInteractions.get(i).toString() + "\n" ;
		}
		
		return tostring ;
		
	}
	
	private void loadParadigmFile (String filename) throws IOException {

		ArrayList <String> lines = new ArrayList <String>  ();
		
		// Read given file
		BufferedReader reader = new BufferedReader(new FileReader (filename)) ;
        System.out.println ("\nReading PARADIGM file: " + filename + "\n");
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
        
        ArrayList <SingleInteraction> singleInteractions = new ArrayList <SingleInteraction> () ;
        
        for (int i = 0; i < lines.size(); i++)
        {
        
        }
	
	}

	
	private void loadSifFile (String filename) throws IOException {
		
		ArrayList <String> interactions = new ArrayList <String>  ();
		
		this.modelName =  filename.substring(0, filename.toLowerCase().indexOf(".sif", filename.length() - 5)) ;
		
		// Read given file
		BufferedReader reader = new BufferedReader(new FileReader (filename)) ;
        logger.output(1, "Reading SIF file: " + filename + "\n");
        
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
	            	interactions.add(line);
	            	
	            }
	        
	        }
        }
        finally {
	       	reader.close ();
	    }
     
        for (int i = 0; i < interactions.size(); i++){
        	
        	//System.out.print("i = " + i + " lines: " + lines.size() + " String: " + lines.get(i).toString());
            	
        	if (interactions.get(i).toString().length() > 0) {
        	
        		//System.out.print(lines.get(i).toString()) ;
        		String line = (String) interactions.get(i);
        		
        		if (line.contains("<->"))
        		{
        			String line1 = line.replace ("<->", "<-") ;
        			String line2 = line.replace ("<->", "->") ;
        			SingleInteraction singleInteraction1 = new SingleInteraction (line1) ;
        			SingleInteraction singleInteraction2 = new SingleInteraction (line2) ;
        			singleInteractions.add(singleInteraction1);
        			singleInteractions.add(singleInteraction2);
        		
        		}
        		else if (line.contains("|-|"))
        		{
        			String line1 = line.replace ("|-|", "|-") ;
        			String line2 = line.replace ("|-|", "-|") ;
        			SingleInteraction singleInteraction1 = new SingleInteraction (line1) ;
        			SingleInteraction singleInteraction2 = new SingleInteraction (line2) ;
        			singleInteractions.add(singleInteraction1);
        			singleInteractions.add(singleInteraction2);
        		
        		}
        		else if (line.contains("|->"))
        		{
        			String line1 = line.replace ("|->", "->") ;
        			String line2 = line.replace ("|->", "|-") ;
        			SingleInteraction singleInteraction1 = new SingleInteraction (line1) ;
        			SingleInteraction singleInteraction2 = new SingleInteraction (line2) ;
        			singleInteractions.add(singleInteraction1);
        			singleInteractions.add(singleInteraction2);
        		}
        		else if (line.contains("<-|"))
        		{
        			String line1 = line.replace ("<-|", "<-") ;
        			String line2 = line.replace ("<-|", "-|") ;
        			SingleInteraction singleInteraction1 = new SingleInteraction (line1) ;
        			SingleInteraction singleInteraction2 = new SingleInteraction (line2) ;
        			singleInteractions.add(singleInteraction1);
        			singleInteractions.add(singleInteraction2);
        			
        		}
        		else
        		{
        			String[] temp = line.split(" ", 3) ;

            		
            		SingleInteraction singleInteraction = new SingleInteraction (line);
            		
            		singleInteractions.add(singleInteraction);
        		}
        		
        	}        
        }   
   }
	
	public void loadFromSingleInteractions (SingleInteraction[] interactions)
	{
		for (int i = 0; i < interactions.length; i++)
		{
			singleInteractions.add(new SingleInteraction(interactions[i])) ;
		}
	}
	
//	public void saveSifFile ()
//	{
//		if (singleInteractions.size() > 0)
//		{		
//			PrintWriter writer = new PrintWriter(filename, "UTF-8");
//
//			writer.println("# SIF file from model: ");
//			
//			for (int i = 0; i < singleInteractions.size(); i++)
//				writer.println(singleInteractions.get(i).
//				
//			
//			writer.close() ;
//		}
//	}
	
	private int getIndexOfTargetInListOfMultipleInteraction (String target)
	{
		for (int i = 0; i < multipleInteractions.size(); i++)
		{
			if (multipleInteractions.get(i).getTarget().equals(target))
			{
				return i ;
			}
		}
		
		return -1 ;
			
	}
	
	
	public void buildMultipleInteractions ()
	{
		// Convert list of single interactions into interactions with multiple regulators of a single target
//		ArrayList <SingleInteraction> singleInteractions = SIF.getInteractions() ;
		
		for (int i = 0; i < singleInteractions.size(); i++)
		{
			MultipleInteraction multipleInteraction = new MultipleInteraction (singleInteractions.get(i).getTarget()) ;
			
			if (getIndexOfTargetInListOfMultipleInteraction(singleInteractions.get(i).getTarget()) < 0)
			{
					
				for (int k = 0; k < singleInteractions.size(); k++)
				{
					SingleInteraction singleInteraction = singleInteractions.get(k);
					
					if (singleInteraction.getTarget().equals(multipleInteraction.getTarget()))
					{
						if (singleInteraction.getArc() == 1)
						{
							multipleInteraction.addActivatingRegulator(singleInteraction.getSource()) ;
						}
						else if (singleInteraction.getArc() == -1)
						{
							multipleInteraction.addInhibitoryRegulator(singleInteraction.getSource()) ;
						}
						else
						{
							logger.debug("\nError - interaction without regulator found...?\n");
						}
						
						
					}
					
					
					
				}
				multipleInteractions.add(multipleInteraction);
			}
		} 
				
		
	}
	
	public ArrayList <MultipleInteraction> getMultipleInteractions () {
		return multipleInteractions ;
	}
	
	public void removeInputs ()
	{
		int interactionsBeforeTrim = singleInteractions.size () ;
		
		logger.output(1, "\nRemoving inputs. Interactions before trim: " + interactionsBeforeTrim + ". ") ;
		
		int trimIteration = 0 ;
		
		do {
			trimIteration ++ ;
			
			interactionsBeforeTrim = singleInteractions.size () ;
			
//			System.out.println ("DEBUG: Iteration " + trimIteration) ;

			for (int i = singleInteractions.size () - 1; i >= 0; i--)
			{
				if (i == 851)
					continue ;
//				System.out.println (singleInteractions.get(i).getSource() + " " + singleInteractions.get(i).getArc() + " " + singleInteractions.get(i).getTarget()) ;
				if (!this.isAlsoTarget (i))
				{
					logger.output(3, "Removing interaction (i = " + i + ")  (not target): " + singleInteractions.get(i).getInteraction());
					singleInteractions.remove(i) ;
					
				}
			}
		} while (interactionsBeforeTrim > singleInteractions.size()) ;
		
		logger.output(1, "Interactions after trim (" + trimIteration + " iterations): " + singleInteractions.size () + "\n") ;
		
	}
	
	public void removeOutputs ()
	{
		int interactionsBeforeTrim = singleInteractions.size () ;
		
		logger.output(1, "\nRemoving outputs. Interactions before trim: " + interactionsBeforeTrim + ". ") ;
		
		int trimIteration = 0 ;
		
		do {
			trimIteration ++ ;
			
			interactionsBeforeTrim = singleInteractions.size () ;
			
			for (int i = singleInteractions.size () - 1; i >= 0; i--)
			{
				if (!this.isAlsoSource (i))
				{
				 	logger.output(3, "Removing interaction (not source): " + singleInteractions.get(i).getInteraction());
					singleInteractions.remove(i) ;
				}
			}
		} while (interactionsBeforeTrim > singleInteractions.size()) ;
		
		logger.output(1, "Interactions after trim (" + trimIteration + " iterations): " + singleInteractions.size () + "\n") ;
		
	}
	

	
	public void removeInputsOutputs ()
	{
		
		
		int interactionsBeforeTrim = singleInteractions.size();
		
		logger.output(1, "\nInteractions before trim: " + interactionsBeforeTrim + "\n") ;
		
		int trimIteration = 0 ;
		
		do {
			trimIteration++ ;
			
			interactionsBeforeTrim = singleInteractions.size();
			
			
			for (int i = singleInteractions.size() - 1; i >= 0 ; i--)
			{
				if (!this.isAlsoTarget(i))
				{
					logger.output(3, "Removing interaction (not target): " + singleInteractions.get(i).toString());
					singleInteractions.remove(i);
					
				}
				
			}
			
			for (int i = singleInteractions.size() - 1; i >= 0 ; i--)
			{
				if (!this.isAlsoSource(i))
				{
					logger.output(3, "Removing interaction (not source): " + singleInteractions.get(i).toString());
					singleInteractions.remove(i);
				}
				
			}
			
		logger.output(3, "Trimming (iteration " + trimIteration + "): Interactions before iteration: " + interactionsBeforeTrim + " Interactions after iteration: " + singleInteractions.size () + "\n") ;
		
		} while (interactionsBeforeTrim > singleInteractions.size()) ;
		
		logger.output(1, "Interactions after trim (" + trimIteration + " iterations): " + singleInteractions.size () + "\n");
	}
	
	/**
	 * Required if no inputs or targets are removed, to annotate some nodes as inputs to system.
	 * Inputs must be given a value in steadystate file.
	 */
	public void removeNone ()
	{
		for (int i = 0; i < singleInteractions.size(); i++)
		{
			if (!isAlsoTarget(i))
			{
				singleInteractions.add(new SingleInteraction("true", "->", singleInteractions.get(i).getSource())) ;
				logger.output(2, "Annotating " + singleInteractions.get(i).getSource() + " as input to model.");
			}
		}
		
	}
	
	public void removeSelfRegulation ()
	{
		for (int i = singleInteractions.size()-1; i >= 0; i--)
		{
			if (singleInteractions.get(i).getTarget().trim().equals(singleInteractions.get(i).getSource().trim()))
			{
				logger.output(2, "Removing self regulation: " + singleInteractions.get(i).getInteraction());
				singleInteractions.remove(i) ;
			}
			
		}
	}
	
	public void removeSmallFeedbackLoops ()
	{
		logger.output (3, "\nRemoving small feedback loops (positive and negative)") ;
		for (int i = singleInteractions.size() - 1; i >= 0; i--)
		{
			String target = singleInteractions.get(i).getTarget() ;
			int index = getSingleInteractionFromSource(singleInteractions.get(i).getTarget()) ;
			
//			if (singleInteractions.get(i).getTarget().equals(singleInteractions.get(getSingleInteractionFromSource(singleInteractions.get(i).getTarget())).getSource()))
//			if (singleInteractions.get(getSingleInteractionFromTarget(singleInteractions.get(i).getTarget())).getTarget().equals(singleInteractions.get(i).getSource()))
			if (index >= 0)
			{
				if (singleInteractions.get(index).getTarget().equals(singleInteractions.get(i).getSource()))
				{
					logger.output(3, "Small feedback detected: " + singleInteractions.get(i).getInteraction() + " AND " + singleInteractions.get(index).getInteraction()) ;
					if (index > i)
					{
						singleInteractions.remove(index) ;
						singleInteractions.remove(i) ;
					}
					else
					{
						singleInteractions.remove(i) ;
						singleInteractions.remove(index) ;
					}
					
				}
			}
		}
	}
	
	public void removeSmallNegativeFeedbackLoops ()
	{
		logger.output (3, "\nRemoving small negative feedback loops") ;
		for (int i = singleInteractions.size() - 1; i >= 0; i--)
		{
			String target = singleInteractions.get(i).getTarget() ;
			int index = getSingleInteractionFromSource(singleInteractions.get(i).getTarget()) ;
			
			if (index >= 0)
			{
				if (singleInteractions.get(index).getTarget().equals(singleInteractions.get(i).getSource()))
				{
					// Check if arcs have opposite signs
					if (singleInteractions.get(index).getArc() != singleInteractions.get(i).getArc())
					{
						logger.output(3, "Small negative feedback detected: " + singleInteractions.get(i).getInteraction() + " AND " + singleInteractions.get(index).getInteraction()) ;
						if (index > i)
						{
							singleInteractions.remove(index) ;
							singleInteractions.remove(i) ;
						}
						else
						{
							singleInteractions.remove(i) ;
							singleInteractions.remove(index) ;
						}
					}
				}
			}
		}
	}
	
	public void removeSmallPositiveFeedbackLoops ()
	{
		
	}
	
	public MultipleInteraction getMultipleInteraction (int index) {
		return multipleInteractions.get(index) ;
	}
	
	private int getSingleInteractionFromTarget (String target)
	{
		int index = -1 ;
		
		for (int i = 0; i < singleInteractions.size(); i++)
		{
			if (singleInteractions.get(i).getTarget().equals(target))
				index = i ;
		}
		
		return index ;
	}
	
	private int getSingleInteractionFromSource (String source)
	{
		int index = -1 ;
		
		for (int i = 0; i < singleInteractions.size(); i++)
		{
			if (singleInteractions.get(i).getSource().equals(source))
				index = i ;
		}
		
		return index ;
	}
	
	public ArrayList <SingleInteraction> getSingleInteractions ()
	{
		return singleInteractions ;
	}
	
	
	
	private boolean isAlsoSource (int indexInteractions)
	{
		boolean result = false ;
		String targetname = singleInteractions.get(indexInteractions).getTarget() ;

		for (int i = 0; i < singleInteractions.size(); i++)
		{
			if (targetname.equals(singleInteractions.get(i).getSource()))
			{
				result = true ;
			}
		}
		
		return result ;
	}
	
	private boolean isAlsoTarget (int indexInteractions)
	{
		boolean result = false ;
		String sourcename = singleInteractions.get(indexInteractions).getSource() ;

		for (int i = 0; i < singleInteractions.size(); i++)
		{
//			System.out.println("DEBUG: " + singleInteractions.get(i).getTarget());
			if (sourcename.equals(singleInteractions.get(i).getTarget()))
			{
				result = true ;
			}
		}
		
		return result ;
	}
}
