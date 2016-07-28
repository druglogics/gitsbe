package gitsbe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

public class Evolution {

	ArrayList<MutatedBooleanModel> bestModels ;
	

	// Input parameters for genetic algorithm

	BooleanModel generalBooleanModel ;
	String baseModelName ;
	String stableStates[] ;
	String directoryName ;
	Summary summary ;
	float[][] fitnessEvolution ;
	String directorytmp ;
//	int targetFitnessThreshold ;
	SteadyState steadyState ;
//	Config config ;
			
	//
	
	public Evolution (Summary summary,
						BooleanModel generalBooleanModel, 

						String baseModelName,
						SteadyState steadyState,
						String directoryName,
						String directorytmp

						) {
		
		// Initialize
		this.summary = summary ;
		this.generalBooleanModel = generalBooleanModel ;

		this.baseModelName = baseModelName ; // name base of model, used for generations/individuals
		this.steadyState = steadyState ;
		this.stableStates = steadyState.getSteadyStates() ;
		this.directoryName = directoryName ;
		this.directorytmp = directorytmp ;

				
	}

	public void evolve () {
		
		
		Logger.output(1, "Model optimization over " + Config.getGenerations() + " generations with " + Config.getPopulation() + " models per generations.");
		
		// Define ArrayList of arrays, each cell referencing a mutated model
//		ArrayList<MutatedBooleanModel>[] mutatedModels = (ArrayList<MutatedBooleanModel>[]) new ArrayList[numGenerations];
		
		// Evolution is in an initial phase until a stable state exists
		boolean initialphase = true ;
		
		bestModels = new ArrayList <MutatedBooleanModel> () ;
		
		// Initialize bestModels with original model (in evolution these are replaced by bestfit models)
		for (int i = 0; i < Config.getSelection(); i++)
		{
			bestModels.add(new MutatedBooleanModel(generalBooleanModel)) ;
		}
		
		ArrayList<float[]> fitnesses = new ArrayList<float[]> () ; //new float[numGenerations][population] ;
		
		// Evolve models through mutations, crossover and selection
		for (int generation = 0; generation < Config.getGenerations(); generation++)
		{
			// Output i
			Logger.outputHeader(2, "Generation " + generation);
			
			// Define generation
			ArrayList<MutatedBooleanModel> generationModels = new ArrayList<MutatedBooleanModel> ();
			
			// ---------
			// Crossover
			// ---------
			
			// Get indexes for parents randomly pointing to bestModels
			for (int i = 0; i < Config.getPopulation(); i++)
			{
				int parent1 = Gitsbe.randInt(0, Config.getSelection()-1) ;
				int parent2 = Gitsbe.randInt(0, Config.getSelection()-1) ;
						
//				mutatedModels[generation].add(new MutatedBooleanModel (bestModels.get(parent1), bestModels.get(parent2), "test" + i)) ;
				generationModels.add(new MutatedBooleanModel (bestModels.get(parent1), bestModels.get(parent2),  
						baseModelName + "_G" + generation + "_M" + i)) ;
				
				Logger.output(3, "Define new model " + baseModelName + "_G" + generation + "_M" + i + " from " + bestModels.get(parent1).getModelName() + " and " + bestModels.get(parent2).getModelName());
			}
			
			// ---------
			// Mutations
			// ---------
			
			for (int i = 0; i < Config.getPopulation(); i++)
			{
				int mutations_factor ;
				int shuffle_factor ;
				
				if (initialphase)
				{
					mutations_factor = Config.getBootstrap_mutations_factor() ;
					shuffle_factor = Config.getBootstrap_shuffle_factor() ;
				}
				else
				{
					mutations_factor = Config.getMutations_factor() ;
					shuffle_factor = Config.getShuffle_factor() ;
				}
				
				if ((Config.getBalancemutations() * mutations_factor) > 0)
				{
					Logger.output(3, "Introducing " + (Config.getBalancemutations() * mutations_factor) + " balance mutations to model " + generationModels.get(i).getModelName());
					generationModels.get(i).introduceBalanceMutation(mutations_factor * Config.getBalancemutations());
				}
				
				if ((Config.getRandommutations() * mutations_factor) > 0)
				{
					Logger.output(3, "Introducing " + (Config.getRandommutations() * mutations_factor) + " random mutations to model " + generationModels.get(i).getModelName());
					generationModels.get(i).introduceRandomMutation(mutations_factor * Config.getRandommutations());
				}
				
				if ((Config.getShufflemutations() * shuffle_factor) > 0)
				{
					Logger.output(3, "Introducing " + (Config.getShuffle_factor() * shuffle_factor) + " regulator priority shuffle mutations to model " + generationModels.get(i).getModelName());
					generationModels.get(i).shuffleRandomRegulatorPriorities(shuffle_factor * Config.getShufflemutations());
				}
				
			}
			
			// ----------
			// Calculate stable states and fitness
			// ----------
			for (int i = 0; i < Config.getPopulation(); i++)
			{
				Logger.output(2, "\nModel " + generationModels.get(i).getModelName()) ;
				
				try {
//					System.out.println("DEBUG: " + directorytmp) ;
					generationModels.get(i).calculateStableStatesVC("/home/asmund/Dokumenter/Cycret/Gitsbe/bnet/", directorytmp);
					generationModels.get(i).calculateFitness(stableStates);
//					generationModels.get(i).calculateFitnessAgainstStableStatesAveraged(stableStates[0]);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			// --------------
			// Append summary (fitness for all models in generation)
			// --------------
			
			float generationfitness[] = new float[Config.getPopulation()] ;
			
			for (int i = 0; i < Config.getPopulation(); i++)
			{
				generationfitness[i] = generationModels.get(i).getFitness() ;
			}
			
			fitnesses.add(generationfitness) ;
			
			
			// float highestFitness has lowest fitness among bestModels
			float highestFitness = 0;

			// ----------
			// Update bestModels
			// ----------
			for (int i = 0; i < Config.getSelection(); i++)
			{
				highestFitness = 0 ; // reset highestfitness
				
				int indexBest = 0 ;
				
				for (int k = 0; k < generationModels.size(); k++)
				{
					if (generationModels.get(k).getFitness() > highestFitness)
					{
						highestFitness = generationModels.get(k).getFitness() ;
						indexBest = k ;
					}
					
				}
				bestModels.set(i, generationModels.get(indexBest)) ;
				generationModels.remove(indexBest) ;
				
				// If all best models have a stableState with fitness > 0 then initial phase is over
				if (initialphase)
					if (highestFitness > 0)
					{
						initialphase = false ;
					}
				
				
			}
		
			Logger.output(2, "\nBest models in generation " + generation + ": ");
			for (int k = 0; k < bestModels.size(); k++)
			{
				Logger.output(2, "\t" + bestModels.get(k).getModelName() + "\tFitness: "+ bestModels.get(k).getFitness());
			}
			Logger.output(2, "\n");
			
			// Break if highestFitness is over 
			if (highestFitness > (steadyState.getMaxFitness() * Config.getTarget_fitness_percent() / 100))
			{
				Logger.output(2, "Breaking evolution after " + generation + " generations, since target fitness reached (" + Config.getTarget_fitness_percent() + " %)");
				break ;
			}
			
				
		}
		// With updated bestModels we are ready for a new generation...
		
		// append to summary
		
		summary.addSimulationFitnesses(fitnesses);
		
	}
	
	public void outputBestModels () {
		if (bestModels.get(0).getFitness() > 0)
		{
			Logger.output(2, "\n" + Config.getSelection() + " best models:\n") ;
			
			for (int k = 0; k < bestModels.size(); k++)
			{
				if (bestModels.get(k).getFitness() > 0)
				{
					Logger.output(2, "\t" + bestModels.get(k).getModelName() + "\tFitness: "+ bestModels.get(k).getFitness());
				}
			}
		}
		else
		{
			Logger.output(2, "No models were found with fitness > 0") ;
		}
	}
	
	/**
	 * Save best models to file
	 * 
	 * @param numberToKeep number of models to save, if bigger than number of models selected per generation, number selected will be used
	 * @param fitnessThreshold threshold of lowest fitness to keep
	 * @throws IOException
	 */
	public void saveBestModels (int numberToKeep, float fitnessThreshold) throws IOException {
		
		numberToKeep = Evolution.min(numberToKeep, Config.getSelection()) ;
		
		Logger.outputHeader(1, "Saving up to " + numberToKeep + " best models to files (fitness threshold " + fitnessThreshold + ":");
		
		int numModelsSaved = 0;
		
		for (int i = 0; i < numberToKeep; i++)
		{
			// next line were commented out for RANDOM simulation 20151105 to preserve files  even
			// when maximum fitness was (by definition of random) 0
			if (bestModels.get(i).getFitness() > fitnessThreshold)
			{
				// Set filename of model
				bestModels.get(i).setFilename(bestModels.get(i).getModelName() + ".gitsbe");
				
				
				Logger.output(1, "\tFile: " + directoryName + bestModels.get(i).getFilename());
				bestModels.get(i).saveFile(directoryName);
				
				numModelsSaved++ ;
				
				
			}
		}
		
		Logger.output(1, "Saved " + numModelsSaved + " models with sufficient fitness");

		//		if (bestModels.get(0).getFitness() > 0)
//			
//		{
//			
//			Logger.outputHeader(1, "Saving " + selection + " best models to files:");
//			
//			for (int k = 0; k < bestModels.size(); k++)
//			{
//				Logger.output(1, "\tFile: " + bestModels.get(k).getModelName() + ".gitsbe") ;
//				
//				bestModels.get(k).saveFile();
//			}
//		}
//		else
//		{
//			Logger.output(1, "No models were found with fitness > 0");
//		}
	}
	
	private static int min(int a, int b)
	{
		if (a > b) return b;
		else return a ;
		
	}

	
}
