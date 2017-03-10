package gitsbe;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * 
 * @author Asmund Flobak, email: asmund.flobak@ntnu.no
 *
 */


public class Summary {

	// All best models saved, 
	private ArrayList<ArrayList<MutatedBooleanModel>> bestModels ;
	
//	private ArrayList<ArrayList<float[]> fitness ;
	
	// All fitnesses observed in evolution
	private ArrayList<ArrayList<float[]>> fitness ;
	
	//
	
	private String name ;
	
	private int currentSimulation = -1 ;
	
	private Logger logger ;
	
	public Summary(String name, Logger logger) {
		 this.name = name ;
		 
		this.fitness = new ArrayList<ArrayList <float[]>> ();

		 bestModels = new ArrayList<ArrayList<MutatedBooleanModel>> () ;
		 
		 this.logger = logger ;
		 
		}

	/**
	 * generate report of entire session
	 * 
	 * @return summary
	 */
	
	public void getSummary()
	{
		// Write header with '#'
		logger.summary("Summary") ;
		logger.summary("-------") ;
		
		// Write columns with model-defined node names and node names for Veliz-Cuba's algorithm
		for (int i = 0; i < bestModels.size() ; i++)
		{
			for (int j = 0; j < bestModels.get(i).size(); j++)
			{
				logger.summary(bestModels.get(i).get(j).getFilename()) ;
			}
		}
		
	}

	public void generateFitnessesReport()
	{
		
		logger.summary("\nFitness evolution");
		
		for (int i = 0; i < fitness.size(); i++)
		{
		
			logger.summary("\nSimulation " + (i + 1));
			ArrayList <float[]> simulationfit = fitness.get(i) ;
			
			
			for (int row=0; row < simulationfit.size(); row++) {
			    StringBuilder builder = new StringBuilder();
			    String prefix = "";
			    for (int col=0; col<simulationfit.get(row).length; col++) {
			        builder.append(prefix).append(simulationfit.get(row)[col]);
			        prefix = "\t";
			    }
			    logger.summary(builder.toString());
			}
			
		}
	}
	public void addSimulationFitnesses(ArrayList<float []> fitness)
	{
		this.fitness.add(fitness) ;
	}
	
	public void addModel (int simulation, MutatedBooleanModel model)
	{
		if (simulation >= currentSimulation)
		{
			currentSimulation++ ;
			bestModels.add(new ArrayList<MutatedBooleanModel> ()) ;
		}
		
		bestModels.get(currentSimulation).add(model) ;
	}
	
	public void saveModelsIndexFile (String filename, String relativePath) throws IOException
	{
		
		String file = "" ;
		
		PrintWriter writer = new PrintWriter(filename, "UTF-8");
		
		// Write header with '#'
		writer.println("# Each line contains a filename (with relative path to executable) pointing to model to include in analysis") ;
		
		// Write columns with model-defined node names and node names for Veliz-Cuba's algorithm
		for (int i = 0; i < bestModels.size() ; i++)
		{
			for (int j = 0; j < bestModels.get(i).size(); j++)
			{
				writer.println(bestModels.get(i).get(j).getFilename()) ;
			}
		}
		
		writer.close() ;
		
		
		
	}
	
	
	public void addModel (MutatedBooleanModel model)
	{
		
	}
}
