package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.output.Summary;
import eu.druglogics.gitsbe.input.Config;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.input.TrainingData;
import eu.druglogics.gitsbe.util.Logger;

import static eu.druglogics.gitsbe.util.RandomManager.*;
import static java.lang.Math.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Evolution {

	public ArrayList<MutatedBooleanModel> bestModels;

	// Input parameters for genetic algorithm
	private BooleanModel generalBooleanModel;
	private String baseModelName;
	private TrainingData data;
	private String modelDirectory;
	private Summary summary;
	private String directoryOutput;
	private Config config;
	private Logger logger;
	private ModelOutputs modelOutputs;
	private boolean initialPhase;

	public Evolution(Summary summary, BooleanModel generalBooleanModel, String baseModelName, TrainingData data,
			ModelOutputs modelOutputs, String modelDirectory, String directoryOutput, Config config, Logger logger) {

		// Initialize
		this.summary = summary;
		this.generalBooleanModel = generalBooleanModel;
		this.baseModelName = baseModelName; // name base of model, used for generations/individuals
		this.data = data;
		this.modelDirectory = modelDirectory;
		this.directoryOutput = directoryOutput;
		this.modelOutputs = modelOutputs;
		this.config = config;
		this.logger = logger;
	}

	public void evolve(int run) {

		logger.outputStringMessage(1, "Model optimization over " + config.getGenerations() + " generations with "
				+ config.getPopulation() + " models per generation.");

		// Evolution is in an initial phase until a stable state exists
		initialPhase = true;
		logger.outputStringMessage(3, "Setting initial phase to: " + initialPhase);

		bestModels = new ArrayList<MutatedBooleanModel>();

		// Initialize bestModels with original model (in evolution these are replaced by
		// bestfit models)
		for (int i = 0; i < config.getSelection(); i++) {
			bestModels.add(new MutatedBooleanModel(generalBooleanModel, logger));
		}

		ArrayList<float[]> fitnesses = new ArrayList<float[]>();

		// Evolve models through mutations, crossover and selection
		for (int generation = 0; generation < config.getGenerations(); generation++) {

			logger.outputHeader(1, "Generation " + generation);

			// Define generation
			ArrayList<MutatedBooleanModel> generationModels = new ArrayList<MutatedBooleanModel>();

			// Crossover (generate the models of the generation)
			crossover(generationModels, generation);

			// Mutations
			mutateModels(generationModels);

			// Calculate stable states and fitness
			for (int i = 0; i < config.getPopulation(); i++) {
				logger.outputStringMessage(2, "\nModel " + generationModels.get(i).getModelName());

				try {
					generationModels.get(i).calculateFitness(data, modelOutputs, directoryOutput);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Append summary (fitness values for all models in current generation)
			addSummaryFitnessValues(generationModels, fitnesses);

			// Selection (update bestModels)
			float fitnessScore = selection(generationModels, generation);

			// Break if highestFitness is over
			logger.outputStringMessage(2, "Comparing: " + String.valueOf(fitnessScore) + " with: "
					+ String.valueOf(config.getTargetFitness()));

			if (fitnessScore > config.getTargetFitness()) {
				logger.outputStringMessage(2, "Breaking evolution after " + generation
						+ " generations, since the target fitness value is reached: " + config.getTargetFitness());
				break;
			}

		}

		// append to summary the fitnesses of the models for all generations of the
		// current simulation/run
		summary.addSimulationFitnesses(fitnesses, run);

	}

	/**
	 * Sets the best models of the current generation
	 * 
	 * @param generationModels
	 * @param generation
	 * @return The smallest fitness score among the best models selected
	 * 
	 */
	private float selection(ArrayList<MutatedBooleanModel> generationModels, int generation) {
		float currentMaxFitness = 0;

		// Choose the new bestModels based on the highest fitness score
		for (int i = 0; i < config.getSelection(); i++) {
			int indexBest = 0;
			currentMaxFitness = 0; // reset currentMaxFitness

			for (int k = 0; k < generationModels.size(); k++) {
				if (generationModels.get(k).getFitness() > currentMaxFitness) {
					currentMaxFitness = generationModels.get(k).getFitness();
					indexBest = k;
				}
			}

			// Add the highest fitness model and remove it from the generation's model list
			bestModels.set(i, generationModels.get(indexBest));
			generationModels.remove(indexBest);
		}

		// If all best models have a stableState with fitness > 0 then
		// initial phase is over (currentMaxFitness holds the smallest highest fitness
		// score among
		// the best models)
		if ((initialPhase) && (currentMaxFitness > 0)) {
			initialPhase = false;
			logger.outputStringMessage(3, "Setting initial phase to: " + initialPhase);
		}

		logger.outputStringMessage(2, "\nBest models in generation " + generation + ": ");
		for (int k = 0; k < bestModels.size(); k++) {
			logger.outputStringMessage(2,
					"\t" + bestModels.get(k).getModelName() + "\tFitness: " + bestModels.get(k).getFitness());
		}
		logger.outputStringMessage(2, "\n");

		return currentMaxFitness;

	}

	private void addSummaryFitnessValues(ArrayList<MutatedBooleanModel> generationModels,
			ArrayList<float[]> fitnesses) {
		float generationfitness[] = new float[config.getPopulation()];

		for (int i = 0; i < config.getPopulation(); i++) {
			generationfitness[i] = generationModels.get(i).getFitness();
		}

		fitnesses.add(generationfitness);
	}

	private void crossover(ArrayList<MutatedBooleanModel> generationModels, int generation) {
		// Get indexes for parents randomly pointing to bestModels
		for (int i = 0; i < config.getPopulation(); i++) {
			int parent1 = randInt(0, config.getSelection() - 1);
			int parent2 = randInt(0, config.getSelection() - 1);

			generationModels.add(new MutatedBooleanModel(bestModels.get(parent1), bestModels.get(parent2),
					baseModelName + "_G" + generation + "_M" + i, logger, config));

			logger.outputStringMessage(3, "Define new model " + baseModelName + "_G" + generation + "_M" + i + " from "
					+ bestModels.get(parent1).getModelName() + " and " + bestModels.get(parent2).getModelName());
		}
	}

	private void mutateModels(ArrayList<MutatedBooleanModel> generationModels) {
		for (int i = 0; i < config.getPopulation(); i++) {
			int mutations_factor;
			int shuffle_factor;
			int topology_mutations_factor;

			if (initialPhase) {
				mutations_factor = config.getBootstrapMutationsFactor();
				shuffle_factor = config.getBootstrapShuffleFactor();
				topology_mutations_factor = config.getBootstrapTopologyMutationsFactor();
			} else {
				mutations_factor = config.getMutationsFactor();
				shuffle_factor = config.getShuffleFactor();
				topology_mutations_factor = config.getTopologyMutationsFactor();
			}

			if ((config.getBalanceMutations() * mutations_factor) > 0) {
				logger.outputStringMessage(3, "Introducing " + (config.getBalanceMutations() * mutations_factor)
						+ " balance mutations to model " + generationModels.get(i).getModelName());
				generationModels.get(i).introduceBalanceMutation(mutations_factor * config.getBalanceMutations());
			}

			if ((config.getRandomMutations() * mutations_factor) > 0) {
				logger.outputStringMessage(3, "Introducing " + (config.getRandomMutations() * mutations_factor)
						+ " random mutations to model " + generationModels.get(i).getModelName());
				generationModels.get(i).introduceRandomMutation(mutations_factor * config.getRandomMutations());
			}

			if ((config.getShuffleMutations() * shuffle_factor) > 0) {
				logger.outputStringMessage(3, "Introducing " + (config.getShuffleMutations() * shuffle_factor)
						+ " regulator priority shuffle mutations to model " + generationModels.get(i).getModelName());
				generationModels.get(i).shuffleRandomRegulatorPriorities(shuffle_factor * config.getShuffleMutations());
			}

			if ((config.getTopologyMutations() * topology_mutations_factor) > 0) {
				logger.outputStringMessage(3,
						"Introducing " + (config.getTopologyMutations() * topology_mutations_factor)
								+ " topology mutations to model " + generationModels.get(i).getModelName());

				generationModels.get(i).topologyMutations(topology_mutations_factor * config.getTopologyMutations());
			}
		}
	}

	/**
	 * Write to log file the best models for the current simulation
	 */
	public void outputBestModels() {
		if (bestModels.get(0).getFitness() > 0) {
			logger.outputStringMessage(2, "\n" + config.getSelection() + " best models:\n");

			for (int k = 0; k < bestModels.size(); k++) {
				if (bestModels.get(k).getFitness() > 0) {
					logger.outputStringMessage(2,
							"\t" + bestModels.get(k).getModelName() + "\tFitness: " + bestModels.get(k).getFitness());
				}
			}
		} else {
			logger.outputStringMessage(2, "No models were found with fitness > 0");
		}
	}

	/**
	 * Save best models in the models directory
	 * 
	 * @param numberToKeep
	 *            number of models to save, if bigger than number of models selected
	 *            per generation, number selected will be used
	 * @param fitnessThreshold
	 *            threshold of lowest fitness to keep
	 * @throws IOException
	 */
	public void saveBestModels(int numberToKeep, float fitnessThreshold) throws IOException {

		numberToKeep = min(numberToKeep, config.getSelection());

		logger.outputHeader(1,
				"Saving up to " + numberToKeep + " best models to files (fitness threshold " + fitnessThreshold + "):");

		int numModelsSaved = 0;

		for (int i = 0; i < numberToKeep; i++) {
			if (bestModels.get(i).getFitness() > fitnessThreshold) {

				// Set filename of model
				bestModels.get(i).setFilename(bestModels.get(i).getModelName() + ".gitsbe");
				logger.outputStringMessage(1, "\tFile: " + modelDirectory + bestModels.get(i).getFilename());

				// calculate stable states for saving as part of .gitsbe file
				bestModels.get(i).calculateStableStatesVC(directoryOutput);

				bestModels.get(i).saveFileInGitsbeFormat(modelDirectory);

				numModelsSaved++;

			}
		}

		logger.outputStringMessage(1,
				"Saved " + numModelsSaved + " models with sufficient fitness in directory:\n" + modelDirectory + "\n");
	}

}
