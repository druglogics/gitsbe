package gitsbe;

import static gitsbe.Util.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.io.File;

public class MutatedBooleanModel extends BooleanModel {
	
	private float fitness;

	public MutatedBooleanModel(BooleanModel booleanModel, Logger logger) {
		super(booleanModel, logger);
	}

	// Constructor for creating a mutated model offspring from two parents, using
	// crossover
	public MutatedBooleanModel(MutatedBooleanModel parent1, MutatedBooleanModel parent2, String modelName,
			Logger logger) {

		super(logger);

		// Copy Boolean equations
		booleanEquations = new ArrayList<BooleanEquation>();

		int indexCrossover = randInt(0, parent1.booleanEquations.size());

		// Add equations from parent1
		for (int i = 0; i < indexCrossover; i++) {
			this.booleanEquations.add(new BooleanEquation(parent1.booleanEquations.get(i)));
		}

		// Add equations from parent2
		for (int i = indexCrossover; i < parent2.booleanEquations.size(); i++) {
			this.booleanEquations.add(new BooleanEquation(parent2.booleanEquations.get(i)));
		}

		// Copy mapAlternativeNames
		this.mapAlternativeNames = new ArrayList<String[]>();

		for (int i = 0; i < parent1.mapAlternativeNames.size(); i++) {
			this.mapAlternativeNames.add(parent1.mapAlternativeNames.get(i));
		}

		// Define stable states
		stableStates = new ArrayList<String>();

		// Assign modelName
		this.modelName = modelName;

	}

	public void introduceRandomMutation(int numberOfMutations) {
		for (int i = 0; i < numberOfMutations; i++) {
			// Find random equation to mutate.
			int randomEquation = randInt(0, booleanEquations.size() - 1);

			booleanEquations.get(randomEquation).mutateRandomOperator();
		}
	}

	public void shuffleRandomRegulatorPriorities(int numberOfShuffles) {
		for (int i = 0; i < numberOfShuffles; i++) {
			this.shuffleRandomRegulatorPriority();
		}
	}

	public void shuffleRandomRegulatorPriority() {
		// Find random equation to mutate.
		int randomEquation = randInt(0, booleanEquations.size() - 1);

		booleanEquations.get(randomEquation).shuffleRandomRegulatorPriority();

	}

	/**
	 * Introduce mutations to topology, removing regulators of nodes (but not all
	 * regulators for any node)
	 * 
	 * @param i
	 */
	public void topologyMutations(int numberOfMutations) {
		for (int i = 0; i < numberOfMutations; i++) {
			// Find random equation to mutate.
			int randomEquationIndex = randInt(0, booleanEquations.size() - 1);
			String orig = booleanEquations.get(randomEquationIndex).getBooleanEquation();

			booleanEquations.get(randomEquationIndex).mutateRegulator();

			if (!booleanEquations.get(randomEquationIndex).getBooleanEquation().equals(orig))
				logger.outputStringMessage(1, "Exchanging equation " + randomEquationIndex + "\n\t" + orig + "\n\t" + ""
						+ booleanEquations.get(randomEquationIndex).getBooleanEquation() + "\n");

		}
	}

	public void introduceOrMutation(int numberOfMutations) {

	}

	public void introduceBalanceMutation(int numberOfMutations) {
		for (int i = 0; i < numberOfMutations; i++) {

			// Find random equation to mutate.
			int randomEquationIndex = randInt(0, booleanEquations.size() - 1);

			booleanEquations.get(randomEquationIndex).mutateLinkOperator();
		}
	}

	/**
	 * Calculate fitness of model by computing matches with training data
	 * 
	 * @param data
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	public void calculateFitness(TrainingData data, ModelOutputs modelOutputs, String directoryOutput)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {

		// reset fitness
		fitness = 0;

		// iterate through each data observation
		for (int i = 0; i < data.size(); i++) {
			float conditionfitness = 0;
			float weight = data.getObservations().get(i).getWeight();

			logger.outputHeader(3, "Defining model for training data: " + this.modelName);
			ArrayList<String> condition = data.getObservations().get(i).getCondition();
			ArrayList<String> response = data.getObservations().get(i).getResponse();

			MutatedBooleanModel temp = new MutatedBooleanModel(this, logger);

			temp.modelName += "_condition_" + "" + i;

			// Set up model compliant with condition
			// go through each element (input) of a condition
			for (int j = 0; j < condition.size(); j++) {

				if (condition.get(j).equals("-")) // steady state condition
				{
					logger.outputStringMessage(3, "Defining condition: Unperturbed (weight: " + weight + ")");

					// do nothing, empty condition
				} else // specified state
				{
					String node = condition.get(j).split(":")[0];
					String state = condition.get(j).split(":")[1];

					String equation = node + " *= ";

					if (state.equals("1"))
						equation += "true";
					else if (state.equals("0"))
						equation += "false";
					else {
						logger.outputStringMessage(1,
								"ERROR: Training data with incorrectly formatted response: " + condition.get(j));
						continue;
					}

					logger.outputStringMessage(3, "Defining condition: " + equation);
					temp.modifyEquation(equation);
				}
			}
			
			// compute stable state(s) for condition
			temp.calculateStableStatesVC(directoryOutput);

			// A model with an existing stable state will get higher fitness than models
			// without stable states
			if (temp.stableStates.size() > 0)
				conditionfitness += 1;
			
			// check computed stable state(s) with training data observation
			String[][] stableStates = temp.getStableStates();
			
			// go through each element (output) of a response
			if (temp.hasStableStates()) {
				// check if globaloutput observation
				if (response.get(0).split(":")[0].equals("globaloutput")) {
					// compute a global output of the model by using specified model outputs
					// scaled output to value <0..1] (exclude 0 since ratios then are difficult)
					float observedGlobalOutput = Float.parseFloat(response.get(0).split(":")[1]);
					float predictedGlobalOutput = modelOutputs.calculateGlobalOutput(temp.stableStates, this);
					conditionfitness = 1 - Math.abs(predictedGlobalOutput - observedGlobalOutput);
					logger.outputStringMessage(3, "globaloutput: " + predictedGlobalOutput);

				} else { 
					// if not globaloutput then go through all specified states in observation and contrast with stable state(s)
					
					int foundObservations = 0;

					for (int k = 0; k < response.size(); k++) {
						String node = response.get(k).split(":")[0].trim();
						String obs = response.get(k).split(":")[1].trim();

						int indexNode = getIndexOfEquation(node);

						if (indexNode >= 0) {
							float match = 1
									- Math.abs(Integer.parseInt(stableStates[1][indexNode]) - Float.parseFloat(obs));
							logger.outputStringMessage(1, "Match for observation on node " + node + ": " + match
									+ " (1 - |" + stableStates[1][indexNode] + "-" + obs + "|)");
							foundObservations++;
							conditionfitness += match;
						}

					}
					if (foundObservations > 0)
						conditionfitness /= (foundObservations + 1); // +1 to account for the fact there is also a
																		// stable state, which gives a fitness of 1
																		// itself

				}

				// compute fitness and scale to ratio of weight to weights of all conditions
				fitness += conditionfitness * weight / data.getWeightSum();
				//fitness /= Math.max(temp.stableStates.size(), 1); // use only first stable state above: stableStates[1]

				if (data.size() > 1) {
					logger.outputStringMessage(3, "Scaled fitness [0..1] for model [" + temp.modelName + "] condition "
							+ i + " " + "(weight: " + weight + "): " + conditionfitness);
				}

				// Increase fitness based on number of edges in model (few edges -> high
				// fitness)
				// float fitnessTopologyReduction = 0;
				// int edges = 0;
				// int removedEdges = 0;
				// int nodes = booleanEquations.size();
				//
				// for (int j = 0; j < booleanEquations.size(); j++)
				// {
				// edges += booleanEquations.get(j).getNumRegulators() ;
				// removedEdges += booleanEquations.get(j).getNumBlacklistedRegulators();
				// }
				//
				//
				// fitnessTopologyReduction += (float) removedEdges/(edges-nodes);
				//
				// logger.output(3, "Fitness for topologyreduction: " +
				// fitnessTopologyReduction);
				//
				// fitness += fitnessTopologyReduction;
			}

		}

		logger.outputStringMessage(3, "Scaled fitness [0..1] for model [" + modelName + "] across all (" + data.size()
				+ ") conditions: " + fitness);

	}

	public void saveFileInGitsbeFormat(String directoryName) throws IOException {

		String filename = this.modelName.substring(this.modelName.lastIndexOf('/') + 1) + ".gitsbe";
		PrintWriter writer = new PrintWriter(new File(directoryName, filename).getPath(), "UTF-8");

		// Write header with '#'
		writer.println("#Boolean model file in gitsbe format");

		// Write model name
		writer.println("modelname: " + this.modelName);

		// Write fitness
		writer.println("fitness: " + this.fitness);

		// Write stable state(s)
		for (int i = 0; i < this.stableStates.size(); i++) {
			writer.println("stablestate: " + this.stableStates.get(i));
		}

		// Write Boolean equations
		for (int i = 0; i < booleanEquations.size(); i++) {
			writer.println("equation: " + booleanEquations.get(i).getBooleanEquation());
		}

		// Write alternative names for Veliz-Cuba
		for (int i = 0; i < mapAlternativeNames.size(); i++) {
			writer.println("mapping: " + mapAlternativeNames.get(i)[0] + " = " + mapAlternativeNames.get(i)[1]);
		}

		writer.close();

	}

	/**
	 * returns fitness, calculateFitness() must be called first
	 * 
	 * @return calculated fitness
	 */
	public float getFitness() {
		return this.fitness;
	}

}
