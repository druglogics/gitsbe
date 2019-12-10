package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.drug.DrugPanel;
import eu.druglogics.gitsbe.input.Config;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.input.TrainingData;
import eu.druglogics.gitsbe.util.Logger;
import org.apache.commons.lang3.StringUtils;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.druglogics.gitsbe.util.RandomManager.randInt;
import static java.lang.Math.abs;
import static java.lang.Math.min;

public class MutatedBooleanModel extends BooleanModel {

	private float fitness;

	MutatedBooleanModel(BooleanModel booleanModel, Logger logger) {
		super(booleanModel, logger);
	}

	// Constructor for creating a mutated model offspring from two parents, using
	// crossover
	MutatedBooleanModel(MutatedBooleanModel parent1, MutatedBooleanModel parent2, String modelName,
			Logger logger) {

		super(logger);

		// Copy Boolean equations from parents
		crossoverCopy(parent1, parent2, logger);

		// Copy nodeNameToVariableMap
		this.nodeNameToVariableMap = new LinkedHashMap<>();
		this.nodeNameToVariableMap.putAll(parent1.nodeNameToVariableMap);

		// Define stable states
		this.stableStates = new ArrayList<>();

		// Assign modelName
		this.modelName = modelName;
	}

	private void crossoverCopy(MutatedBooleanModel parent1, MutatedBooleanModel parent2, Logger logger) {
		booleanEquations = new ArrayList<>();

		int crossovers = Config.getInstance().getCrossovers();
		int numberOfBooleanEquations = parent1.booleanEquations.size();
		logger.debug("Crossovers: " + crossovers + "\nNumber of boolean equations: "
				+ numberOfBooleanEquations);

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

	void introduceRandomMutation(int numberOfMutations) {
		for (int i = 0; i < numberOfMutations; i++) {
			// Find random equation to mutate.
			int randomEquation = randInt(0, booleanEquations.size() - 1);

			booleanEquations.get(randomEquation).mutateRandomOperator();
		}
	}

	void shuffleRandomRegulatorPriorities(int numberOfShuffles) {
		for (int i = 0; i < numberOfShuffles; i++) {
			this.shuffleRandomRegulatorPriority();
		}
	}

	private void shuffleRandomRegulatorPriority() {
		// Find random equation to mutate
		int randomEquation = randInt(0, booleanEquations.size() - 1);

		booleanEquations.get(randomEquation).shuffleRandomRegulatorPriority();

	}

	/**
	 * Introduce mutations to topology, removing regulators of nodes (but not all
	 * regulators for any node)
	 *
	 */
	void topologyMutations(int numberOfMutations) {
		for (int i = 0; i < numberOfMutations; i++) {
			// Find random equation to mutate.
			int randomEquationIndex = randInt(0, booleanEquations.size() - 1);
			String orig = booleanEquations.get(randomEquationIndex).getBooleanEquation();

			booleanEquations.get(randomEquationIndex).mutateRegulator();

			if (!booleanEquations.get(randomEquationIndex).getBooleanEquation().equals(orig))
				logger.outputStringMessage(2, "Exchanging equation " + randomEquationIndex
						+ "\n\t" + orig + "\n\t"
						+ booleanEquations.get(randomEquationIndex).getBooleanEquation() + "\n");
		}
	}

	void introduceBalanceMutation(int numberOfMutations) {
		for (int i = 0; i < numberOfMutations; i++) {

			// Find random equation to mutate.
			int randomEquationIndex = randInt(0, booleanEquations.size() - 1);

			booleanEquations.get(randomEquationIndex).mutateLinkOperator();
		}
	}

	/**
	 * Calculate fitness of model by going through all the observations defined in the
	 * training data and computing individual fitness values for each one of them.
	 * Supported observations include unperturbed conditions with a steady state response
	 * vector, simple or multiple node knockout conditions and single drug perturbations
	 * with expected globaloutput response values and double drug perturbation conditions
	 * (HSA or Bliss) where the globaloutput response given is the relative observed response
	 * that is compared with the predicted HSA or Bliss excess.
	 *
	 * @throws Exception
	 */
	void calculateFitness(TrainingData data, String directoryOutput) throws Exception {

		// reset fitness
		fitness = 0;

		// iterate through each data observation
		for (int conditionNumber = 0; conditionNumber < data.size(); conditionNumber++) {
			float conditionfitness = 0;
			float weight = data.getObservations().get(conditionNumber).getWeight();

			logger.outputHeader(3, "Defining model for training data: " + this.modelName);
			ArrayList<String> condition = data.getObservations().get(conditionNumber).getCondition();
			ArrayList<String> response = data.getObservations().get(conditionNumber).getResponse();

			MutatedBooleanModel mutatedBooleanModel = new MutatedBooleanModel(this, logger);
			mutatedBooleanModel.modelName = this.modelName + "_condition_" + conditionNumber;

			// Set up model compliant with condition
			String firstCondition = condition.get(0);
			boolean isDoubleDrugPerturbation = false;

			if (firstCondition.equals("-")) { // unperturbed state
				logger.outputStringMessage(3, "Defining condition: Unperturbed (weight: " + weight + ")");

			} else if (firstCondition.startsWith("Drug")) {
				int count = StringUtils.countMatches(firstCondition, "Drug");

				if (count == 1) { // single drug perturbation
					Pattern singleDrugPattern = Pattern.compile("^Drug\\((.*)\\)$");
					Matcher singleDrugMatcher = singleDrugPattern.matcher(firstCondition);

					String drug;
					if (singleDrugMatcher.find()) {
						drug = singleDrugMatcher.group(1);
					} else {
						logger.error("Wrong format on first condition: `" + firstCondition + "` - "
							+ "skipping this observation, continuing with the next");
						continue;
					}

					ArrayList<String> targets = DrugPanel.getInstance().getDrugTargets(drug);
					boolean effect = DrugPanel.getInstance().getDrugEffect(drug);

					logger.outputStringMessage(3, "Condition is a single drug perturbation: `"
						+ firstCondition + "`");

					for (String target : targets) {
						String equation = target + " *= " + effect;
						logger.outputStringMessage(3, "Defining condition: " + equation);
						mutatedBooleanModel.modifyEquation(equation);
					}
				} else if (count == 3) { // Two drug perturbation condition (HSA or Bliss)
					String firstResponse = response.get(0);
					try {
						conditionfitness = getConditionFitnessForTwoDrugPerturbation(directoryOutput,
							firstCondition, firstResponse);
					} catch (ConfigurationException e) {
						continue;
					}
					isDoubleDrugPerturbation = true;
				} else {
					logger.error("Wrong format on first condition: `" + firstCondition + "` - "
						+ "skipping this observation, continuing with the next");
					continue;
				}
			} else { // single to multiple knockout/over-expression conditions
				for (String conditionStr : condition) {
					String node = conditionStr.split(":")[0];
					String state = conditionStr.split(":")[1];

					String equation = node + " *= ";

					if (state.equals("1"))
						equation += "true";
					else if (state.equals("0"))
						equation += "false";
					else {
						logger.error("Training data with incorrectly formatted condition: " + conditionStr);
						continue;
					}

					logger.outputStringMessage(3, "Defining condition: " + equation);
					mutatedBooleanModel.modifyEquation(equation);
				}
			}

			if (!isDoubleDrugPerturbation) {
				// compute stable state(s) for condition
				mutatedBooleanModel.calculateStableStatesVC(directoryOutput, Config.getInstance().getAttractorTool());

				// check computed stable state(s) with training data observation
				String[][] stableStates = mutatedBooleanModel.getStableStates();

				// go through each element (output) of a response
				if (mutatedBooleanModel.hasStableStates()) {
					// check if globaloutput observation
					if (response.get(0).split(":")[0].equals("globaloutput")) {
						// compute a global output of the model by using specified model outputs
						// scaled output to value <0..1] (exclude 0 since ratios then are difficult)
						float observedGlobalOutput = Float.parseFloat(response.get(0).split(":")[1]);
						float predictedGlobalOutput = ModelOutputs.getInstance()
							.calculateGlobalOutput(mutatedBooleanModel.stableStates, this);

						logger.outputStringMessage(3, "Observed globalOutput: " + observedGlobalOutput);
						logger.outputStringMessage(3, "Predicted globalOutput: " + predictedGlobalOutput);

						conditionfitness = 1 - abs(predictedGlobalOutput - observedGlobalOutput);
					} else {
						// if not globaloutput then go through all specified states in observation and
						// contrast with stable state(s)

						// A model with an existing stable state will get higher fitness than models
						// without stable states
						conditionfitness += 1;

						float averageMatch = 0;
						int foundObservations = 0;
						ArrayList<Float> matches = new ArrayList<>();

						for (int indexState = 1; indexState < stableStates.length; indexState++) {
							logger.outputStringMessage(2, "Checking stable state no. " + indexState + ":");

							float matchSum = 0;
							foundObservations = 0;

							for (String responseStr : response) {
								String node = responseStr.split(":")[0].trim();
								String obs = responseStr.split(":")[1].trim();

								int indexNode = getIndexOfEquation(node);

								if (indexNode >= 0) {
									foundObservations++;
									float match = 1 - abs(
										Integer.parseInt(stableStates[indexState][indexNode]) -
											Float.parseFloat(obs)
									);
									logger.outputStringMessage(2, "Match for observation on node "
										+ node + ": " + match + " (1 - |"
										+ stableStates[indexState][indexNode] + "-" + obs + "|)");
									matchSum += match;
								}
							}
							logger.outputStringMessage(2, "From " + foundObservations
								+ " observations, found " + matchSum + " matches");
							matches.add(matchSum);
						}

						for (Float match : matches) {
							averageMatch += match;
						}
						averageMatch /= matches.size();
						logger.outputStringMessage(2, "Average match value through all stable states: "
							+ averageMatch);
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
			}

			logger.outputStringMessage(3, "Scaled fitness [0..1] for model [" + mutatedBooleanModel.modelName
					+ "] condition " + conditionNumber + " " + "(weight: " + weight + "): "
					+ conditionfitness);

			// compute fitness and scale to ratio of weight to weights of all conditions
			fitness += conditionfitness * weight / data.getWeightSum();
		}

		logger.outputStringMessage(3, "Scaled fitness [0..1] for model [" + modelName
				+ "] across all (" + data.size() + ") conditions: " + fitness);
	}

	@Override
	public void exportModelToGitsbeFile(String directoryOutput) throws IOException {

		String filename = this.modelName + ".gitsbe";
		PrintWriter writer = new PrintWriter(
				new File(directoryOutput, filename).getAbsolutePath(), "UTF-8"
		);

		// Write header with '#'
		writer.println("#Boolean model file in gitsbe format");

		// Write model name
		writer.println("modelname: " + this.modelName);

		// Write fitness
		writer.println("fitness: " + this.fitness);

		// Write stable state(s)
		for (String stableState : this.stableStates) {
			writer.println("stablestate: " + stableState);
		}

		// Write Boolean equations
		for (BooleanEquation booleanEquation : booleanEquations) {
			writer.println("equation: " + booleanEquation.getBooleanEquation());
		}

		// Write alternative names for Veliz-Cuba
		for (Map.Entry<String, String> entry : nodeNameToVariableMap.entrySet()) {
			writer.println("mapping: " + entry.getKey() + " = " + entry.getValue());
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

	/**
	 * Use this function to get the fitness value for a specified condition in the form:
	 * `Drug(A+B) < min(Drug(A),Drug(B))` (HSA) or `Drug(A+B) < product(Drug(A),Drug(B))` (Bliss)
	 *
	 * @param directoryOutput name of directory to pass to the function that calculates the stable states
	 * @param condition the two drug specified condition. String format is: `Drug(A+B) < min(Drug(A),Drug(B))` or `Drug(A+B) < product(Drug(A),Drug(B))`
	 * @param response the relative observed response. String format is: `globaloutput:value` (`value` can be from -1 to 1)
	 * @return a fitness score for the given condition scaled from 0 to 1
	 *
	 * @throws Exception
	 * @throws ConfigurationException whenever a wrong format is encountered in the input training
	 * data (the given condition or the response), signaling thus to stop processing this observation
	 *
	 */
	public float getConditionFitnessForTwoDrugPerturbation(
		String directoryOutput, String condition, String response) throws Exception {
		float conditionFitness = 0;

		if (condition.contains("< min(Drug")) { // HSA

			Pattern twoDrugsHSAPattern = Pattern.compile("^Drug\\((.*)\\+(.*)\\) < min\\(Drug\\((.*)\\),Drug\\((.*)\\)\\)$");
			Matcher twoDrugsHSAMatcher = twoDrugsHSAPattern.matcher(condition);

			String firstDrug;
			String secondDrug;
			if (twoDrugsHSAMatcher.find()) {
				firstDrug = twoDrugsHSAMatcher.group(1);
				secondDrug = twoDrugsHSAMatcher.group(2);
			} else {
				logger.error("Wrong format on first condition: `" + condition + "` - "
					+ "skipping this observation, continuing with the next");
				throw new ConfigurationException();
			}

			ArrayList<String> firstDrugTargets = DrugPanel.getInstance().getDrugTargets(firstDrug);
			ArrayList<String> secondDrugTargets = DrugPanel.getInstance().getDrugTargets(secondDrug);

			boolean firstDrugEffect = DrugPanel.getInstance().getDrugEffect(firstDrug);
			boolean secondDrugEffect = DrugPanel.getInstance().getDrugEffect(secondDrug);

			logger.outputStringMessage(3, "Condition is a double drug perturbation: `"
				+ condition + "`");
			MutatedBooleanModel mutatedBooleanModelBothDrugs = new MutatedBooleanModel(this, logger);

			logger.outputStringMessage(3, "Perturbing model with the first drug: `"
				+ firstDrug + "`");
			MutatedBooleanModel mutatedBooleanModelFirstDrug = new MutatedBooleanModel(this, logger);

			for (String target : firstDrugTargets) {
				String equation = target + " *= " + firstDrugEffect;
				logger.outputStringMessage(3, "Defining condition: " + equation);
				mutatedBooleanModelFirstDrug.modifyEquation(equation);
				mutatedBooleanModelBothDrugs.modifyEquation(equation);
			}

			mutatedBooleanModelFirstDrug.calculateStableStatesVC(directoryOutput, Config.getInstance().getAttractorTool());

			logger.outputStringMessage(3, "Perturbing model with the second drug: `"
				+ secondDrug + "`");

			MutatedBooleanModel mutatedBooleanModelSecondDrug =
				new MutatedBooleanModel(this, logger);

			for (String target : secondDrugTargets) {
				String equation = target + " *= " + secondDrugEffect;
				logger.outputStringMessage(3, "Defining condition: " + equation);
				mutatedBooleanModelSecondDrug.modifyEquation(equation);
				mutatedBooleanModelBothDrugs.modifyEquation(equation);
			}

			mutatedBooleanModelSecondDrug.calculateStableStatesVC(directoryOutput, Config.getInstance().getAttractorTool());

			logger.outputStringMessage(3, "Perturbing model with both drugs");
			mutatedBooleanModelBothDrugs.calculateStableStatesVC(directoryOutput, Config.getInstance().getAttractorTool());

			if (mutatedBooleanModelFirstDrug.hasStableStates()
				&& mutatedBooleanModelSecondDrug.hasStableStates()
				&& mutatedBooleanModelBothDrugs.hasStableStates()) {
				if (response.split(":")[0].equals("globaloutput")) {

					float relObsGL = Float.parseFloat(response.split(":")[1]);
					logger.outputStringMessage(3, "Relative Observed globalOutput: " + relObsGL);

					float firstDrugGL = ModelOutputs.getInstance()
						.calculateGlobalOutput(mutatedBooleanModelFirstDrug.stableStates, this);
					logger.outputStringMessage(3, "Predicted globalOutput for the model perturbed with drug `"
						+ firstDrug + "`: " + firstDrugGL);

					float secondDrugGL = ModelOutputs.getInstance()
						.calculateGlobalOutput(mutatedBooleanModelSecondDrug.stableStates, this);
					logger.outputStringMessage(3, "Predicted globalOutput for the model perturbed with drug `"
						+ secondDrug + "`: " + secondDrugGL);

					float minGL = min(firstDrugGL, secondDrugGL);
					logger.outputStringMessage(3, "Minimum predicted globalOutput value " +
						"of the two single-drug perturbed models: " + minGL);

					float bothDrugsGL = ModelOutputs.getInstance()
						.calculateGlobalOutput(mutatedBooleanModelBothDrugs.stableStates, this);
					logger.outputStringMessage(3, "Predicted globalOutput for the model perturbed with both drugs `"
						+ firstDrug + "` and `" + secondDrug + "`: " + bothDrugsGL);

					float HSAexcess = bothDrugsGL - minGL; // [-1..1]
					logger.outputStringMessage(3, "HSA excess: " + HSAexcess);

					float diff = abs(HSAexcess - relObsGL); // [0..2]
					logger.outputStringMessage(3, "Absolute difference of HSA excess and " +
						"relative observed globalOutput: " + diff);
					conditionFitness = 1 - (diff / 2); // [0..1]
				} else {
					logger.error("Wrong format on first response (expected `globaloutput` but got): `"
						+ response.split(":")[0] + "` - "
						+ "skipping this observation, continuing with the next");
					throw new ConfigurationException();
				}
			} else {
				logger.outputStringMessage(3, "One of the single-drug perturbed models " +
					"or the double-perturbed model didn't have stable states");
			}

		} else if (condition.contains("< product(Drug")) { // Bliss

			Pattern twoDrugsBlissPattern = Pattern.compile("^Drug\\((.*)\\+(.*)\\) < product\\(Drug\\((.*)\\),Drug\\((.*)\\)\\)$");
			Matcher twoDrugsBlissMatcher = twoDrugsBlissPattern.matcher(condition);

			String firstDrug;
			String secondDrug;
			if (twoDrugsBlissMatcher.find()) {
				firstDrug = twoDrugsBlissMatcher.group(1);
				secondDrug = twoDrugsBlissMatcher.group(2);
			} else {
				logger.error("Wrong format on first condition: `" + condition + "` - "
					+ "skipping this observation, continuing with the next");
				throw new ConfigurationException();
			}

			ArrayList<String> firstDrugTargets = DrugPanel.getInstance().getDrugTargets(firstDrug);
			ArrayList<String> secondDrugTargets = DrugPanel.getInstance().getDrugTargets(secondDrug);

			boolean firstDrugEffect = DrugPanel.getInstance().getDrugEffect(firstDrug);
			boolean secondDrugEffect = DrugPanel.getInstance().getDrugEffect(secondDrug);

			logger.outputStringMessage(3, "Condition is a double drug perturbation: `"
				+ condition + "`");
			MutatedBooleanModel mutatedBooleanModelBothDrugs = new MutatedBooleanModel(this, logger);

			logger.outputStringMessage(3, "Perturbing model with the first drug: `"
				+ firstDrug + "`");
			MutatedBooleanModel mutatedBooleanModelFirstDrug = new MutatedBooleanModel(this, logger);

			for (String target : firstDrugTargets) {
				String equation = target + " *= " + firstDrugEffect;
				logger.outputStringMessage(3, "Defining condition: " + equation);
				mutatedBooleanModelFirstDrug.modifyEquation(equation);
				mutatedBooleanModelBothDrugs.modifyEquation(equation);
			}

			mutatedBooleanModelFirstDrug.calculateStableStatesVC(directoryOutput, Config.getInstance().getAttractorTool());

			logger.outputStringMessage(3, "Perturbing model with the second drug: `"
				+ secondDrug + "`");

			MutatedBooleanModel mutatedBooleanModelSecondDrug =
				new MutatedBooleanModel(this, logger);

			for (String target : secondDrugTargets) {
				String equation = target + " *= " + secondDrugEffect;
				logger.outputStringMessage(3, "Defining condition: " + equation);
				mutatedBooleanModelSecondDrug.modifyEquation(equation);
				mutatedBooleanModelBothDrugs.modifyEquation(equation);
			}

			mutatedBooleanModelSecondDrug.calculateStableStatesVC(directoryOutput, Config.getInstance().getAttractorTool());

			logger.outputStringMessage(3, "Perturbing model with both drugs");
			mutatedBooleanModelBothDrugs.calculateStableStatesVC(directoryOutput, Config.getInstance().getAttractorTool());

			if (mutatedBooleanModelFirstDrug.hasStableStates()
				&& mutatedBooleanModelSecondDrug.hasStableStates()
				&& mutatedBooleanModelBothDrugs.hasStableStates()) {
				if (response.split(":")[0].equals("globaloutput")) {

					float relObsGL = Float.parseFloat(response.split(":")[1]);
					logger.outputStringMessage(3, "Relative Observed globalOutput: " + relObsGL);

					float firstDrugGL = ModelOutputs.getInstance()
						.calculateGlobalOutput(mutatedBooleanModelFirstDrug.stableStates, this);
					logger.outputStringMessage(3, "Predicted globalOutput for the model perturbed with drug `"
						+ firstDrug + "`: " + firstDrugGL);

					float secondDrugGL = ModelOutputs.getInstance()
						.calculateGlobalOutput(mutatedBooleanModelSecondDrug.stableStates, this);
					logger.outputStringMessage(3, "Predicted globalOutput for the model perturbed with drug `"
						+ secondDrug + "`: " + secondDrugGL);

					float productGL = firstDrugGL * secondDrugGL;
					logger.outputStringMessage(3, "Product predicted globalOutput value " +
						"of the two single-drug perturbed models: " + productGL);

					float bothDrugsGL = ModelOutputs.getInstance()
						.calculateGlobalOutput(mutatedBooleanModelBothDrugs.stableStates, this);
					logger.outputStringMessage(3, "Predicted globalOutput for the model perturbed with both drugs `"
						+ firstDrug + "` and `" + secondDrug + "`: " + bothDrugsGL);

					float blissExcess = bothDrugsGL - productGL; // [-1..1]
					logger.outputStringMessage(3, "Bliss excess: " + blissExcess);

					float diff = abs(blissExcess - relObsGL); // [0..2]
					logger.outputStringMessage(3, "Absolute difference of Bliss excess and " +
						"relative observed globalOutput: " + diff);
					conditionFitness = 1 - (diff / 2); // [0..1]
				} else {
					logger.error("Wrong format on first response (expected `globaloutput` but got): `"
						+ response.split(":")[0] + "` - "
						+ "skipping this observation, continuing with the next");
					throw new ConfigurationException();
				}
			} else {
				logger.outputStringMessage(3, "One of the single-drug perturbed models " +
					"or the double-perturbed model didn't have stable states");
			}

		} else {
			logger.error("Wrong format on first condition: `" + condition + "` - "
				+ "skipping this observation, continuing with the next");
			throw new ConfigurationException();
		}

		return conditionFitness;
	}

}
