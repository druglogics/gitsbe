package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.input.Config;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.input.TrainingData;
import eu.druglogics.gitsbe.output.Summary;
import eu.druglogics.gitsbe.util.Logger;

import java.io.IOException;
import java.util.ArrayList;

import static eu.druglogics.gitsbe.util.RandomManager.randInt;
import static java.lang.Math.min;

public class Evolution {

	public ArrayList<MutatedBooleanModel> bestModels;

	// Input parameters for genetic algorithm
	private BooleanModel generalBooleanModel;
	private String baseModelName;
	private TrainingData data;
	private String modelDirectory;
	private Summary summary;
	private String directoryOutput;
	private Logger logger;
	private ModelOutputs modelOutputs;
	private boolean initialPhase;

	public Evolution(Summary summary, BooleanModel generalBooleanModel, String baseModelName,
					 TrainingData data, ModelOutputs modelOutputs, String modelDirectory,
					 String directoryOutput, Logger logger) {

		// Initialize
		this.summary = summary;
		this.generalBooleanModel = generalBooleanModel;
		this.baseModelName = baseModelName; // name base of model, used for generations/individuals
		this.data = data;
		this.modelDirectory = modelDirectory;
		this.directoryOutput = directoryOutput;
		this.modelOutputs = modelOutputs;
		this.logger = logger;
	}

	public void evolve(int run) {

		Config config = Config.getInstance();

		logger.outputStringMessage(1, "Model optimization over " + config.getGenerations()
				+ " generations with " + config.getPopulation() + " models per generation.");

		// Evolution is in an initial phase until a stable state exists
		initialPhase = true;
		logger.outputStringMessage(3, "Setting initial phase to: " + initialPhase);

		bestModels = new ArrayList<>();

		// Initialize bestModels with original model (in evolution these are replaced by
		// bestfit models)
		for (int i = 0; i < config.getSelection(); i++) {
			bestModels.add(new MutatedBooleanModel(generalBooleanModel, logger));
		}

		ArrayList<float[]> fitnesses = new ArrayList<>();

		// Evolve models through mutations, crossover and selection
		for (int generation = 0; generation < config.getGenerations(); generation++) {

			logger.outputHeader(1, "Generation " + generation);

			// Define generation
			ArrayList<MutatedBooleanModel> generationModels = new ArrayList<>();

			// Crossover (generate the models of the generation)
			crossover(generationModels, generation);

			// Mutations
			mutateModels(generationModels);

			// Calculate stable states and fitness
			for (int i = 0; i < config.getPopulation(); i++) {
				logger.outputStringMessage(2, "\nModel " + generationModels.get(i).getModelName());

				try {
					generationModels.get(i).calculateFitness(data, modelOutputs, directoryOutput);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// Append summary (fitness values for all models in current generation)
			addSummaryFitnessValues(generationModels, fitnesses);

			// Selection (update bestModels)
			float fitnessScore = selection(generationModels, generation);

			// Break if highestFitness is over
			logger.outputStringMessage(2, "Comparing: " + (fitnessScore)
					+ " with: " + (config.getTargetFitness()));

			if (fitnessScore > config.getTargetFitness()) {
				logger.outputStringMessage(2, "Breaking evolution after " + generation
						+ " generations, since the target fitness value is reached: "
						+ config.getTargetFitness());
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
		for (int i = 0; i < Config.getInstance().getSelection(); i++) {
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
		for (MutatedBooleanModel bestModel : bestModels) {
			logger.outputStringMessage(2,
					"\t" + bestModel.getModelName() + "\tFitness: " + bestModel.getFitness());
		}
		logger.outputStringMessage(2, "\n");

		return currentMaxFitness;

	}

	private void addSummaryFitnessValues(ArrayList<MutatedBooleanModel> generationModels,
			ArrayList<float[]> fitnesses) {
		int modelsPerGeneration = Config.getInstance().getPopulation();
		float[] generationFitness = new float[modelsPerGeneration];

		for (int i = 0; i < modelsPerGeneration; i++) {
			generationFitness[i] = generationModels.get(i).getFitness();
		}

		fitnesses.add(generationFitness);
	}

	private void crossover(ArrayList<MutatedBooleanModel> generationModels, int generation) {
		Config config = Config.getInstance();

		// Get indexes for parents randomly pointing to bestModels
		for (int i = 0; i < config.getPopulation(); i++) {
			int parent1 = randInt(0, config.getSelection() - 1);
			int parent2 = randInt(0, config.getSelection() - 1);

			String modelName = baseModelName + "_G" + generation + "_M" + i;
			generationModels.add(new MutatedBooleanModel(
					bestModels.get(parent1), bestModels.get(parent2), modelName, logger)
			);

			logger.outputStringMessage(3, "Define new model " + baseModelName + "_G"
					+ generation + "_M" + i + " from " + bestModels.get(parent1).getModelName()
					+ " and " + bestModels.get(parent2).getModelName());
		}
	}

	private void mutateModels(ArrayList<MutatedBooleanModel> generationModels) {
		Config config = Config.getInstance();

		for (int i = 0; i < config.getPopulation(); i++) {
			int mutationsFactor;
			int shuffleFactor;
			int topologyMutationsFactor;

			if (initialPhase) {
				mutationsFactor = config.getBootstrapMutationsFactor();
				shuffleFactor = config.getBootstrapShuffleFactor();
				topologyMutationsFactor = config.getBootstrapTopologyMutationsFactor();
			} else {
				mutationsFactor = config.getMutationsFactor();
				shuffleFactor = config.getShuffleFactor();
				topologyMutationsFactor = config.getTopologyMutationsFactor();
			}

			if ((config.getBalanceMutations() * mutationsFactor) > 0) {
				logger.outputStringMessage(3, "Introducing "
						+ (config.getBalanceMutations() * mutationsFactor)
						+ " balance mutations to model " + generationModels.get(i).getModelName());
				generationModels.get(i).introduceBalanceMutation(
						mutationsFactor * config.getBalanceMutations()
				);
			}

			if ((config.getRandomMutations() * mutationsFactor) > 0) {
				logger.outputStringMessage(3, "Introducing "
						+ (config.getRandomMutations() * mutationsFactor)
						+ " random mutations to model " + generationModels.get(i).getModelName());
				generationModels.get(i).introduceRandomMutation(
						mutationsFactor * config.getRandomMutations()
				);
			}

			if ((config.getShuffleMutations() * shuffleFactor) > 0) {
				logger.outputStringMessage(3, "Introducing "
						+ (config.getShuffleMutations() * shuffleFactor)
						+ " regulator priority shuffle mutations to model "
						+ generationModels.get(i).getModelName());
				generationModels.get(i).shuffleRandomRegulatorPriorities(
						shuffleFactor * config.getShuffleMutations()
				);
			}

			if ((config.getTopologyMutations() * topologyMutationsFactor) > 0) {
				logger.outputStringMessage(3, "Introducing "
						+ (config.getTopologyMutations() * topologyMutationsFactor)
						+ " topology mutations to model " + generationModels.get(i).getModelName());
				generationModels.get(i).topologyMutations(
						topologyMutationsFactor * config.getTopologyMutations()
				);
			}
		}
	}

	/**
	 * Write to log file the best models for the current simulation
	 */
	public void outputBestModels() {
		if (bestModels.get(0).getFitness() > 0) {
			logger.outputStringMessage(2, "\n" + Config.getInstance().getSelection() + " best models:\n");

			for (MutatedBooleanModel bestModel : bestModels) {
				if (bestModel.getFitness() > 0) {
					logger.outputStringMessage(2,
							"\t" + bestModel.getModelName() + "\tFitness: " + bestModel.getFitness());
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
	public void saveBestModels(int numberToKeep, float fitnessThreshold) throws Exception {

		numberToKeep = min(numberToKeep, Config.getInstance().getSelection());
		boolean exportToBoolNet = Config.getInstance().exportBestModelsToBoolNet();

		logger.outputHeader(1, "Saving up to " + numberToKeep
				+ " best models to files (fitness threshold " + fitnessThreshold + "):");

		int numModelsSaved = 0;

		for (int modelIndex = 0; modelIndex < numberToKeep; modelIndex++) {
			MutatedBooleanModel bestModel = bestModels.get(modelIndex);
			if (bestModel.getFitness() > fitnessThreshold) {

				// Set filename of model
				bestModel.setFilename(bestModel.getModelName() + ".gitsbe");
				logger.outputStringMessage(1, "\tFile: " + modelDirectory + bestModel.getFilename());

				// calculate stable states for saving as part of .gitsbe file
				bestModel.calculateStableStatesVC(directoryOutput, Config.getInstance().getAttractorTool());
				bestModel.exportModelToGitsbeFile(modelDirectory);

				if (exportToBoolNet) {
					bestModel.exportModelToBoolNetFile(modelDirectory);
				}

				numModelsSaved++;
			}
		}

		logger.outputStringMessage(1, "Saved " + numModelsSaved
				+ " models with sufficient fitness in directory:\n" + modelDirectory + "\n");
	}

}
