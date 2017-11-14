package gitsbe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.io.File;

import drabme.ModelOutputs;

public class MutatedBooleanModel extends BooleanModel {

	
	private int numTotalMutations ;
	private float fitness ;

	public MutatedBooleanModel(BooleanModel booleanModel, Logger logger) {
		super(booleanModel, logger);
		
		
		// TODO Auto-generated constructor stub
	}
	
	// Constructor for creating a mutated model offspring from two parents, using crossover
	public MutatedBooleanModel(MutatedBooleanModel parent1, MutatedBooleanModel parent2, String modelName, Logger logger)
	{
		 
		super(logger) ;
		
		// Copy Boolean equations
		booleanEquations = new ArrayList <BooleanEquation> () ;
		
//		System.out.println(parent1.booleanEquations.size()) ;
		int indexCrossover = Gitsbe.randInt (0, parent1.booleanEquations.size()) ;
		
		// Add equations from parent1
		for (int i = 0; i < indexCrossover; i++)
		{
			this.booleanEquations.add(new BooleanEquation(parent1.booleanEquations.get(i))) ;
		}
		
		//Add equations from parent2
		for (int i = indexCrossover; i < parent2.booleanEquations.size(); i++)
		{
			this.booleanEquations.add(new BooleanEquation(parent2.booleanEquations.get(i))) ;
		}
						
		// Copy mapAlternativeNames
		
		this.mapAlternativeNames = new ArrayList <String[]> () ;
		
		for(int i = 0; i < parent1.mapAlternativeNames.size(); i++)
		{
			this.mapAlternativeNames.add(parent1.mapAlternativeNames.get(i)) ;
		}
		
		// Define stable states
		stableStates = new ArrayList <String> () ;
		
		// Assign modelName
		this.modelName = modelName ;

		
	}
	
	public void introduceRandomMutation(int numberOfMutations)
	{
		for (int i = 0; i < numberOfMutations; i++)
		{
			// Find random equation to mutate.
			int randomEquation = Gitsbe.randInt(0,booleanEquations.size()-1) ;
			
			booleanEquations.get(randomEquation).mutateRandomOperator();
		}
	}

	
	public void shuffleRandomRegulatorPriority ()
	{
		// Find random equation to mutate.
		int randomEquation = Gitsbe.randInt(0,booleanEquations.size()-1) ;
		
		booleanEquations.get(randomEquation).shuffleRandomRegulatorPriority();
					
	}

	public void shuffleRandomRegulatorPriorities (int numberOfShuffles)
	{
		for (int i = 0; i < numberOfShuffles; i++)
		{
			this.shuffleRandomRegulatorPriority();
		}		
	}
	
	/**
	 * Introduce mutations to topology, removing regulators of nodes (but not all regulators for any node)
	 * @param i
	 */
	public void topologyMutations(int numberOfMutations) {
		for (int i = 0; i < numberOfMutations; i++)
		{
			// Find random equation to mutate.
			int randomEquationIndex = Gitsbe.randInt(0,booleanEquations.size()-1) ;
			String orig = booleanEquations.get(randomEquationIndex).getBooleanEquation() ;
			
			booleanEquations.get(randomEquationIndex).mutateRegulator();
			
			logger.output(3, "Exchanging equation \n\t" + orig + "\n\t"
					+ "" + booleanEquations.get(randomEquationIndex).getBooleanEquation() + "\n");
			
		}
	}
	
	public void introduceOrMutation (int numberOfMutations)
	{
		
	}
	

	public void introduceBalanceMutation(int numberOfMutations)
	{
		for (int i = 0; i < numberOfMutations; i++)
		{
			
			// Find random equation to mutate.
			int randomEquationIndex = Gitsbe.randInt(0,booleanEquations.size()-1) ;
			
			booleanEquations.get(randomEquationIndex).mutateLinkOperator();
		}
	}
	
	
	/**
	 * calculate fitness, uses average of stable states found by bnet
	 * since an average is used only a single stable state can be targeted (not a list of states)
	 * 
	 * @param targetStableStates
	 */
//	public void calculateFitnessAgainstStableStatesAveraged (String targetStableState)
//	{
//		
//		fitness = 0 ;
//		
//		// Find difference between number of stablestates in model and intended number of stablestates
////		int numberOfStatesDifference = Math.abs(targetStableStates.length-stableStates.size()) ;
//		
//
////		logger.output(2, "Size: " + stableStates.size());
//		ArrayList <Float> averageStableState = new ArrayList <Float> () ;
//		
////		float[] averageStableState = new float[stableStates.size()];
//		
//		if (stableStates.size() >= 1)
//		{
//				
//			for (int i = 0; i < stableStates.get(0).length(); i++)
//			{
//				float sum = 0 ;
//				
//				for (int j = 0; j < stableStates.size(); j++)
//				{
//					sum += Character.getNumericValue(stableStates.get(j).charAt(i)) ;
////					logger.output(2, "sum: " + sum);
//				}
//				
//				averageStableState.add((float) (sum/stableStates.size()));
//			}
//			
//			for (int j = 0; j < targetStableState.length(); j++)
//			{
//				char state = targetStableState.charAt(j) ;
//				
//				switch (state) {
//				case '-':
//					break;
//			
//				case '1':
//					fitness += (float) (averageStableState.get(j)) ;
//					break ;
//					
//				case '0':
//					fitness += (float) (1-averageStableState.get(j)) ;
//					break ;
//				
//				default:
//					logger.output(1, "ERROR - unknown state in stable state file: " + state);
//						
//				}
//				
//			}
//		}
//		
////		fitness /= (1+numberOfStatesDifference) ;
//		
//		logger.output(1, this.modelName + " AVERAGE fitness: " + fitness);
//
//	}


	
//	/**
//	 * calculates fitness
//	 * 
//	 * @param 
//	 */
//	// BUG AAF - currently intended stable states and calculated states must match in order - that's a bug
//	public void calculateFitness (String[] targetStableStates, TrainingData data)
//	{
//		fitness = 0 ;
//		
//		// A model with an existing stable state will be selected over models without
//		// stable states
//		if (stableStates.size() > 0)
//			fitness = 1 ;
//		
//		// Find difference between number of stablestates in model and intended number of stablestates
//		int numberOfStatesDifference = Math.abs(targetStableStates.length-stableStates.size()) ;
//		
//		
//		for (int i = 0; i < min(targetStableStates.length, stableStates.size()); i++)
//		{
//			for (int j = 0; j < targetStableStates[i].length(); j++)
//			{
//				char state = targetStableStates[i].charAt(j) ;
//				
//				switch (state) {
//				case '-':
//					break;
//				
//				
//				case '0':
//				case '1':
//					if (stableStates.get(i).charAt(j) == state) 
//					{
//						fitness++ ;
//					}
//				}
//				
//			}
//		}
//		
//		fitness /= (1+numberOfStatesDifference) ;
//		
//		// Find difference between model behavior and training data
//		for (int i = 0; i < data.getObservations().size(); i++)
//		{
//			
//		}
//		
//		logger.output(2, this.modelName + " fitness: " + fitness);
//
//	}

	/**
	 * Calculate fitness of model by computing matches with training data
	 * 
	 * @param data
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public void calculateFitness (TrainingData data, ModelOutputs modelOutputs, String outputDirectory) throws FileNotFoundException, UnsupportedEncodingException, IOException
	{


		// reset fitness
		fitness = 0 ;		
		
		// iterate through each data observation
		for (int i = 0; i < data.size(); i++)
		{
			
			// here condition refers to each single specification in input of one collective condition
			// (terminology is a bit confusing, in input file a condition is collection of single states)
			
			float conditionfitness = 0;
			
			logger.outputHeader(3, "Defining model for training data: " + this.modelName);
			ArrayList<String> condition = data.getObservations().get(i).getCondition();
			ArrayList<String> response = data.getObservations().get(i).getResponse();

			MutatedBooleanModel temp = new MutatedBooleanModel(this, logger);
			
			temp.modelName += "_condition_"
					+ "" + i;

			// Set up model compliant with condition
			// go through each element (input) of a condition 
			for (int j = 0; j < condition.size(); j++)
			{

				if (condition.get(j).equals("-")) // steady state condition
				{
					logger.output(3, "Defining condition: Unperturbed");

					// do nothing, empty condition
				}
				else //specified state 
				{
					String node = condition.get(j).split(":")[0] ;
					String state = condition.get(j).split(":")[1] ;
					
					
					String equation = node + " *= " ;
					
					if (state.equals("1"))
						equation += "true";
					else if (state.equals("0"))
						equation += "false";
					else
					{
						logger.error("Training data with incorrectly formatted response: " + condition.get(j));
						continue;
					}
					
					logger.output(3, "Defining condition: " + equation);
					temp.modifyEquation(equation);
				}
			}
			
			// compute stable state(s) for condition
			temp.calculateStableStatesVC(outputDirectory);
			
			// A model with an existing stable state will get higher fitness than models without
			// stable states
			if (temp.stableStates.size() > 0)
				conditionfitness += 1 ;

			// check computed stable state(s) with training data observation
			String[][] stableStates = temp.getStableStates();
			
			
			// go through each element (output) of a response
			if (temp.hastStableStates())
			{
				// check if globaloutput observation
				if (response.get(0).split(":")[0].equals("globaloutput"))
				{
					// compute a global output of the model by using specified model outputs
					// scaled output to value <0..1] (exclude 0 since ratios then are difficult)
          float observedGlobalOutput = Float.parseFloat(response.get(0).split(":")[1]);
					conditionfitness = 1 - Math.abs(modelOutputs.calculateGlobalOutput(temp.stableStates, this) - observedGlobalOutput);
					
					
				}
				else // if not globaloutput then go through all specified states in observation and contrast with stable state(s)
				{
					for (int k = 0; k < response.size(); k++)
					{
						String node = response.get(k).split(":")[0].trim() ;
						String obs = response.get(k).split(":")[1].trim();
						
						int indexNode = getIndexOfEquation (node);
						
						if (indexNode > 0)
						{ 
							float match = 1 - Math.abs(Integer.parseInt(stableStates[1][indexNode]) - Float.parseFloat(obs));
							logger.debug("Match for observation on node " + node + ": " + match + " (1 - |" + stableStates[1][indexNode] + "-" + obs +"|)");
	
							conditionfitness += match/min(temp.stableStates.size(), 1);
						}
						
					}
					
				}
				logger.output(3, "Fitness for model [" + temp.modelName + "] condition " + i + ": " + conditionfitness);
				fitness += conditionfitness;
			}
		

		}
		

		logger.output(3, "Fitness for model [" + modelName + "] across all conditions: " + fitness);

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
		
		// Write fitness
		writer.println("fitness: " + this.fitness) ;
		
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
	
	
	/**
	 * returns fitness, calculateFitness() must be called first
	 * 
	 * @return calculated fitness
	 */
	public float getFitness ()
	{
		return this.fitness ;
	}

	private int min(int a, int b)
	{
		if (a > b) return b;
		else return a ;
		
	}

}
