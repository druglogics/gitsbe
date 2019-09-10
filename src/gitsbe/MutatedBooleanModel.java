package gitsbe;

import static gitsbe.RandomManager.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;

public class MutatedBooleanModel extends BooleanModel {

	private float fitness;

	public MutatedBooleanModel(BooleanModel booleanModel, Logger logger) {
		super(booleanModel, logger);
	}

	// Constructor for creating a mutated model offspring from two parents, using
	// crossover
	public MutatedBooleanModel(MutatedBooleanModel parent1, MutatedBooleanModel parent2, String modelName,
			Logger logger, Config config) {

		super(logger);

		// Copy Boolean equations from parents
		crossoverCopy(logger, config, parent1, parent2);

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

	private void crossoverCopy(Logger logger, Config config, MutatedBooleanModel parent1, MutatedBooleanModel parent2) {
		booleanEquations = new ArrayList<BooleanEquation>();

		int crossovers = config.getCrossovers();
		int numberOfBooleanEquations = parent1.booleanEquations.size();
		logger.debug("Crossovers: " + crossovers + "\nNumber of boolean equations: " + numberOfBooleanEquations);

		if (crossovers >= numberOfBooleanEquations - 1) {
			// the offspring will take equations alternatively from the parents
			for (int i = 0; i < numberOfBooleanEquations; i++) {
				if ((i % 2) == 0) {
					this.booleanEquations.add(new BooleanEquation(parent1.booleanEquations.get(i)));
					logger.debug("i: " + i + " -> Added equation from parent 1");
				} else {
					this.booleanEquations.add(new BooleanEquation(parent2.booleanEquations.get(i)));
					logger.debug("i: " + i + " -> Added equation from parent 2");
				}
			}
		} else {
			ArrayList<Integer> crossoverList = new ArrayList<>();
			for (int i = 0; i < crossovers; i++) {
				crossoverList.add(randInt(0, numberOfBooleanEquations));
			}
			Collections.sort(crossoverList);
			crossoverList.add(numberOfBooleanEquations);
			logger.debug(crossoverList.toString());

			int currentIndex = 0;
			int currentParent = 1;

			for (int crossoverIndex : crossoverList) {
				int i;
				logger.debug("currentindex: " + currentIndex + " CrossoverIndex: " + crossoverIndex
						+ " Current parent: " + currentParent);
				for (i = currentIndex; i < crossoverIndex; i++) {
					if (currentParent == 1) {
						this.booleanEquations.add(new BooleanEquation(parent1.booleanEquations.get(i)));
						logger.debug("i: " + i + " -> Added equation from parent 1");
					} else {
						this.booleanEquations.add(new BooleanEquation(parent2.booleanEquations.get(i)));
						logger.debug("i: " + i + " -> Added equation from parent 2");
					}
				}
				currentIndex = i;

				// change parent
				if (currentParent == 1)
					currentParent = 2;
				else
					currentParent = 1;
			}
		}
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
				logger.outputStringMessage(2, "Exchanging equation " + randomEquationIndex + "\n\t" + orig + "\n\t" + ""
						+ booleanEquations.get(randomEquationIndex).getBooleanEquation() + "\n");
		}
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
		for (int conditionNumber = 0; conditionNumber < data.size(); conditionNumber++) {
			float conditionfitness = 0;
			float weight = data.getObservations().get(conditionNumber).getWeight();

			logger.outputHeader(3, "Defining model for training data: " + this.modelName);
			ArrayList<String> condition = data.getObservations().get(conditionNumber).getCondition();
			ArrayList<String> response = data.getObservations().get(conditionNumber).getResponse();

			MutatedBooleanModel temp = new MutatedBooleanModel(this, logger);

			temp.modelName += "_condition_" + "" + conditionNumber;

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
						logger.error("Training data with incorrectly formatted response: " + condition.get(j));
						continue;
					}

					logger.outputStringMessage(3, "Defining condition: " + equation);
					temp.modifyEquation(equation);
				}
			}

			// compute stable state(s) for condition
			temp.calculateStableStatesVC(directoryOutput);

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

					logger.outputStringMessage(3, "Observed globalOutput: " + observedGlobalOutput);
					logger.outputStringMessage(3, "Predicted globalOutput: " + predictedGlobalOutput);

					conditionfitness = 1 - Math.abs(predictedGlobalOutput - observedGlobalOutput);
				} else {
					// if not globaloutput then go through all specified states in observation and
					// contrast with stable state(s)

					// A model with an existing stable state will get higher fitness than models
					// without stable states
					conditionfitness += 1;

					float averageMatch = 0;
					int foundObservations = 0;
					ArrayList<Float> matches = new ArrayList<Float>();

					for (int indexState = 1; indexState < stableStates.length; indexState++) {
						logger.outputStringMessage(2, "Checking stable state no. " + indexState + ":");

						float matchSum = 0;
						foundObservations = 0;

						for (int k = 0; k < response.size(); k++) {
							String node = response.get(k).split(":")[0].trim();
							String obs = response.get(k).split(":")[1].trim();

							int indexNode = getIndexOfEquation(node);

							// Find matches between computed stable state and observed steady state
							if (indexNode >= 0) {

								// Check if there is a number reported 
								if ((obs.length() > 0) & (Character.isDigit(obs.charAt(0)))) {
									foundObservations++;
									float match = 1 - Math.abs(
											Integer.parseInt(stableStates[indexState][indexNode]) - Float.parseFloat(obs));
									logger.outputStringMessage(2, "Match for observation on node " + node + ": " + match
											+ " (1 - |" + stableStates[indexState][indexNode] + "-" + obs + "|)");
									matchSum += match;
								}
							}
						}
						logger.outputStringMessage(2,
								"From " + foundObservations + " observations, found " + matchSum + " matches");
						matches.add(matchSum);
					}

					for (int index = 0; index < matches.size(); index++) {
						averageMatch += matches.get(index);
					}
					averageMatch /= matches.size();
					logger.outputStringMessage(2, "Average match value through all stable states: " + averageMatch);
					conditionfitness += averageMatch;

					if (foundObservations > 0) {
						// +1 to account for the fact there is also a stable state, which gives a
						// fitness of 1 itself
						conditionfitness /= (foundObservations + 1);
					}

					// further penalize models that have more than one stable states
					// notice that matches.size() is equal to the number of stable states
					conditionfitness /= matches.size();
				}
			}

			logger.outputStringMessage(3, "Scaled fitness [0..1] for model [" + temp.modelName + "] condition "
					+ conditionNumber + " " + "(weight: " + weight + "): " + conditionfitness);

			// compute fitness and scale to ratio of weight to weights of all conditions
			fitness += conditionfitness * weight / data.getWeightSum();
		}

		logger.outputStringMessage(3, "Scaled fitness [0..1] for model [" + modelName + "] across all (" + data.size()
				+ ") conditions: " + fitness);
	}

	@Override
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