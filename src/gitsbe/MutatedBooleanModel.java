package gitsbe;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.File;

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

//	public void introduceRandomMutation(int numberOfMutations)
//	{
//		
//		
//		for (int i = 0; i < numberOfMutations; i++)
//		{
//			// Find random equation to mutate.
//			int randomEquation = Gitsbe.randInt(0,booleanEquations.size()-1) ;
//			
//			String oldEquation = booleanEquations.get(randomEquation).getBooleanEquation() ;
//			
////						int randomLoc = randInt(0,oldEquation.length()) ;
//			
//			// Search for replacement from beginning of equation - Must be changed if the operator regulating all inhibitory regulators is not the only one to be mutated (NOW: and not to or not)
//			int startLoc = 0;
//			int endLoc = oldEquation.length() ;
//			int midLoc = Gitsbe.randInt(startLoc, endLoc) ;
//			
//
//
//			String newEquation = oldEquation ;
//			
//			
//			// 50 % probability of introducing and to or, or or to and
//			if (Gitsbe.randInt(0,1) > 0.5)
//			{
//				newEquation = oldEquation.substring(startLoc, midLoc) + oldEquation.substring(midLoc, endLoc).replaceFirst(" and ", " or ") ;
//			}
//			else
//			{
//				newEquation = oldEquation.substring(startLoc, midLoc) + oldEquation.substring(midLoc, endLoc).replace(" or ", " and ") ;
//			}
//			
//			if (!newEquation.equals(oldEquation))
//				numTotalMutations += numberOfMutations ;
//			
//			booleanEquations.set(randomEquation, new BooleanEquation(newEquation)) ;
//		}
//	}
	
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
	
	public void introduceOrMutation (int numberOfMutations)
	{
		
	}
	
//	public void introduceActivatorMutation(int numberOfMutations)
//	{
//		for (int i = 0; i < numberOfMutations; i++)
//		{
//			// Find random equation to mutate.
//			int randomEquationIndex = randInt(0,booleanEquations.size()-1) ;
//			
//			String oldEquation = booleanEquations.get(randomEquationIndex) ;
//			
//			// Search for replacement from 
//			int startLoc = 0;
//			int midLoc = oldEquation.indexOf(')') ;
//			int endLoc = oldEquation.indexOf("not") ;
//			
//			// Check if there are any activatory components
//			if (oldEquation.indexOf(')', 0) > endLoc)
//				return ;
//			
//			String newEquation = oldEquation ;
//			
//			
//			// 50 % probability of introducing and to or, or or to and
//			if (randInt(0,1) > 0.5)
//			{
//				newEquation = oldEquation.substring(startLoc, midLoc).replaceFirst(" and ", " or ") + oldEquation.substring(midLoc, endLoc) ;
//			}
//			else
//			{
//				newEquation = oldEquation.substring(startLoc, midLoc).replaceFirst(" or ", " and ") + oldEquation.substring(midLoc, endLoc) ;
//			}
//			
//			booleanEquations.set(randomEquationIndex, newEquation) ;
//		}
//	}
	
//	public void introduceInhibitorMutation(int numberOfMutations)
//	{
//		for (int i = 0; i < numberOfMutations; i++)
//		{
//			// Find random equation to mutate.
//			int randomEquationIndex = randInt(0,booleanEquations.size()-1) ;
//			
//			String oldEquation = booleanEquations.get(randomEquationIndex) ;
//			
//			// Search for replacement from 
//			int startLoc = 0;
//			int midLoc = oldEquation.indexOf(" not ") ;
//			int endLoc = oldEquation.length() ;
//
//			String newEquation = oldEquation ;
//			
//			
//			// 50 % probability of introducing and to or, or or to and
//			if (randInt(0,1) > 0.5)
//			{
//				newEquation = oldEquation.substring(startLoc, midLoc) + oldEquation.substring(midLoc, endLoc).replaceFirst(" and ", " or ") ;
//			}
//			else
//			{
//				newEquation = oldEquation.substring(startLoc, midLoc) + oldEquation.substring(midLoc, endLoc).replaceFirst(" or ", " and ") ;
//			}
//			
//			booleanEquations.set(randomEquationIndex, newEquation) ;
//		}
//	}
	
	public void introduceBalanceMutation(int numberOfMutations)
	{
		for (int i = 0; i < numberOfMutations; i++)
		{
			
			// Find random equation to mutate.
			int randomEquationIndex = Gitsbe.randInt(0,booleanEquations.size()-1) ;
			
			booleanEquations.get(randomEquationIndex).mutateLinkOperator();
		}
	}
	
//	public void introduceBalanceMutation(int numberOfMutations)
//	{
//		
//		for (int i = 0; i < numberOfMutations; i++)
//		{
//			
//			
//			
//			// Find random equation to mutate.
//			int randomEquationIndex = Gitsbe.randInt(0,booleanEquations.size()-1) ;
//			
//			String oldEquation = booleanEquations.get(randomEquationIndex) ;
//			
////			int randomLoc = randInt(0,oldEquation.length()) ;
//			
//			// Search for replacement from beginning of equation - Must be changed if the operator regulating all inhibitory regulators is not the only one to be mutated (NOW: and not to or not)
//			int randomLoc = 0 ;
//
//			String newEquation = oldEquation ;
//			
//			if (Gitsbe.randInt(0,1) > 0.5)
//			{
//				newEquation = oldEquation.substring(0, randomLoc) + oldEquation.substring(randomLoc, oldEquation.length()).replaceFirst("and  not", "or  not") ;
//			}
//			else
//			{
//				newEquation = oldEquation.substring(0, randomLoc) + oldEquation.substring(randomLoc, oldEquation.length()).replace("or  not", "and  not") ;
//			}
//			
//			if (!newEquation.equals(oldEquation))
//				numTotalMutations += numberOfMutations ;
//			
//			booleanEquations.set(randomEquationIndex, newEquation) ;
//			}
//	}
	
	/**
	 * calculate fitness, uses average of stable states found by bnet
	 * since an average is used only a single stable state can be targeted (not a list of states)
	 * 
	 * @param targetStableStates
	 */
	public void calculateFitnessAgainstStableStatesAveraged (String targetStableState)
	{
		
		fitness = 0 ;
		
		// Find difference between number of stablestates in model and intended number of stablestates
//		int numberOfStatesDifference = Math.abs(targetStableStates.length-stableStates.size()) ;
		

//		logger.output(2, "Size: " + stableStates.size());
		ArrayList <Float> averageStableState = new ArrayList <Float> () ;
		
//		float[] averageStableState = new float[stableStates.size()];
		
		if (stableStates.size() >= 1)
		{
				
			for (int i = 0; i < stableStates.get(0).length(); i++)
			{
				float sum = 0 ;
				
				for (int j = 0; j < stableStates.size(); j++)
				{
					sum += Character.getNumericValue(stableStates.get(j).charAt(i)) ;
//					logger.output(2, "sum: " + sum);
				}
				
				averageStableState.add((float) (sum/stableStates.size()));
			}
			
			for (int j = 0; j < targetStableState.length(); j++)
			{
				char state = targetStableState.charAt(j) ;
				
				switch (state) {
				case '-':
					break;
			
				case '1':
					fitness += (float) (averageStableState.get(j)) ;
					break ;
					
				case '0':
					fitness += (float) (1-averageStableState.get(j)) ;
					break ;
				
				default:
					logger.output(1, "ERROR - unknown state in stable state file: " + state);
						
				}
				
			}
		}
		
//		fitness /= (1+numberOfStatesDifference) ;
		
		logger.output(1, this.modelName + " AVERAGE fitness: " + fitness);

	}
	
	/**
	 * calculate fitness from training data
	 * 
	 */
	public void calcualteFitnessFromTrainingData (TrainingData data)
	{
		for ()
	}
	
	/**
	 * calculates fitness
	 * 
	 * @param 
	 */
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
	 */
	public void calculateFitness (TrainingData data)
	{
		fitness = 0 ;
		
		// A model with an existing stable state will get higher fitness than models without
		// stable states
		if (stableStates.size() > 0)
			fitness = 1 ;
		
		// iterate through each data observation
		for (int i = 0; i < data.size(); i++)
		{
			
		}
		
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
