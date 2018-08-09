package eu.druglogics.gitsbe.output;

import eu.druglogics.gitsbe.input.Config;
import eu.druglogics.gitsbe.model.MutatedBooleanModel;
import eu.druglogics.gitsbe.util.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * 
 * @author Asmund Flobak, email: asmund.flobak@ntnu.no
 *
 */

public class Summary {

	// All best models from all the simulations
	private List<ArrayList<MutatedBooleanModel>> bestModels;
	// All fitnesses observed in all the simulations
	private List<ArrayList<float[]>> fitness;
	private String summaryFilename;
	private Logger logger;
	
	@SuppressWarnings("unchecked")
	public Summary(String filename, Logger logger, Config config) throws IOException {
		
		this.bestModels = Arrays.asList (Stream.generate (ArrayList::new).limit (config.getSimulations()).toArray (ArrayList[]::new));
		this.fitness = Arrays.asList (Stream.generate (ArrayList::new).limit (config.getSimulations()).toArray (ArrayList[]::new));
		this.setSummaryFilename(filename);
		this.logger = logger;
	}

	public void getSummary() {
		
		logger.outputHeaderToFile(summaryFilename, "Summary");

		// Write columns with model-defined node names for Veliz-Cuba's algorithm
		for (int i = 0; i < bestModels.size(); i++) {
			for (int j = 0; j < bestModels.get(i).size(); j++) {
				logger.outputStringMessageToFile(summaryFilename, bestModels.get(i).get(j).getFilename());
			}
		}

	}

	public void generateFitnessesReport() {

		logger.outputHeaderToFile(summaryFilename, "Fitness evolution");

		for (int i = 0; i < fitness.size(); i++) {

			logger.outputStringMessageToFile(summaryFilename, "\nSimulation " + (i + 1));
			ArrayList<float[]> simulationfit = fitness.get(i);

			for (int row = 0; row < simulationfit.size(); row++) {
				StringBuilder builder = new StringBuilder();
				String prefix = "";
				for (int col = 0; col < simulationfit.get(row).length; col++) {
					builder.append(prefix).append(simulationfit.get(row)[col]);
					prefix = "\t";
				}
				logger.outputStringMessageToFile(summaryFilename, builder.toString());
			}

		}
	}

	public void addSimulationFitnesses(ArrayList<float[]> fitness, int simulation) {
		this.fitness.set(simulation, fitness);
	}

	public void addModel(int simulation, MutatedBooleanModel model) {
		bestModels.get(simulation).add(model);
	}

	public void saveModelsIndexFile(String filename) throws IOException {
		
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		// Write header with '#'
		writer.println("# Each line contains a filename pointing to model to include in analysis");
		
		// Write columns with model-defined node names for Veliz-Cuba's algorithm
		for (int i = 0; i < bestModels.size(); i++) {
			for (int j = 0; j < bestModels.get(i).size(); j++) {
				writer.println(bestModels.get(i).get(j).getFilename());
			}
		}

		writer.close();
	}

	public String getSummaryFilename() {
		return summaryFilename;
	}

	public void setSummaryFilename(String summaryFilename) {
		this.summaryFilename = summaryFilename;
	}
	
}
