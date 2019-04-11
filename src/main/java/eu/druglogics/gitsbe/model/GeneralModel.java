package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.util.Logger;

import static eu.druglogics.gitsbe.util.Util.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GeneralModel {

	private ArrayList<SingleInteraction> singleInteractions = new ArrayList<>();
	private ArrayList<MultipleInteraction> multipleInteractions = new ArrayList<>();
	private Logger logger;
	private String modelName;
	// ArrayList <Complex> complexes = new ArrayList<>();

	public GeneralModel(Logger logger) {
		this.logger = logger;
	}

	GeneralModel(ArrayList<SingleInteraction> singleInteractions, Logger logger) {
		this.singleInteractions = singleInteractions;
		this.logger = logger;
	}

	public int size() {
		return multipleInteractions.size();
	}

	public void loadInteractionsFile(String filenameInteractions) throws IOException {
		String fileExtension = getFileExtension(filenameInteractions);
		if (fileExtension.equals(".sif")) {
			loadSifFile(filenameInteractions);
		}
		else {
			logger.error("New file extension used to load general model, currently not supported");
			throw new IOException("The extension needs to be .sif (other formats not yet supported)");
		}
	}

	void loadSifFile(String filename) throws IOException {

		this.modelName = removeExtension(filename);

		// Read given file
		logger.outputStringMessage(1, "Reading .sif file: " + new File(filename).getAbsolutePath());
		
		ArrayList<String> lines = readLinesFromFile(filename, true);
		ArrayList<String> interactions = new ArrayList<>(lines);

		for (String interaction : interactions) {
			if (interaction.length() > 0) {
				if (interaction.contains("<->")) {
					String line1 = interaction.replace("<->", "<-");
					String line2 = interaction.replace("<->", "->");
					SingleInteraction singleInteraction1 = new SingleInteraction(line1);
					SingleInteraction singleInteraction2 = new SingleInteraction(line2);
					singleInteractions.add(singleInteraction1);
					singleInteractions.add(singleInteraction2);
				} else if (interaction.contains("|-|")) {
					String line1 = interaction.replace("|-|", "|-");
					String line2 = interaction.replace("|-|", "-|");
					SingleInteraction singleInteraction1 = new SingleInteraction(line1);
					SingleInteraction singleInteraction2 = new SingleInteraction(line2);
					singleInteractions.add(singleInteraction1);
					singleInteractions.add(singleInteraction2);
				} else if (interaction.contains("|->")) {
					String line1 = interaction.replace("|->", "->");
					String line2 = interaction.replace("|->", "|-");
					SingleInteraction singleInteraction1 = new SingleInteraction(line1);
					SingleInteraction singleInteraction2 = new SingleInteraction(line2);
					singleInteractions.add(singleInteraction1);
					singleInteractions.add(singleInteraction2);
				} else if (interaction.contains("<-|")) {
					String line1 = interaction.replace("<-|", "<-");
					String line2 = interaction.replace("<-|", "-|");
					SingleInteraction singleInteraction1 = new SingleInteraction(line1);
					SingleInteraction singleInteraction2 = new SingleInteraction(line2);
					singleInteractions.add(singleInteraction1);
					singleInteractions.add(singleInteraction2);
				} else {
					SingleInteraction singleInteraction = new SingleInteraction(interaction);
					singleInteractions.add(singleInteraction);
				}
			}
		}
	}

	int getIndexOfTargetInListOfMultipleInteractions(String target) {
		for (int i = 0; i < multipleInteractions.size(); i++) {
			if (multipleInteractions.get(i).getTarget().equals(target)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Convert list of single interactions into interactions with multiple
	 * regulators for every single target
	 */
	public void buildMultipleInteractions() {
		for (int i = 0; i < singleInteractions.size(); i++) {
			String target = singleInteractions.get(i).getTarget();
			if (getIndexOfTargetInListOfMultipleInteractions(target) < 0) {
				MultipleInteraction multipleInteraction = new MultipleInteraction(target);
				for (SingleInteraction singleInteraction : singleInteractions) {
					if (singleInteraction.getTarget().equals(target)) {
						if (singleInteraction.getArc() == 1) {
							multipleInteraction.addActivatingRegulator(singleInteraction.getSource());
						} else if (singleInteraction.getArc() == -1) {
							multipleInteraction.addInhibitoryRegulator(singleInteraction.getSource());
						} else {
							logger.error("Interaction effect malformed");
						}
					}
				}
				multipleInteractions.add(multipleInteraction);
			}
		}

		// add the input nodes as self activating regulators: (A *= A)
		for (SingleInteraction singleInteraction : singleInteractions) {
			String probableSourceNode = singleInteraction.getSource();
			if (getIndexOfTargetInListOfMultipleInteractions(probableSourceNode) < 0
					&& isNotATarget(probableSourceNode)) {
				MultipleInteraction multipleInteraction = new MultipleInteraction(probableSourceNode);
				multipleInteraction.addActivatingRegulator(probableSourceNode);
				multipleInteractions.add(multipleInteraction);
			}
		}
	}

	ArrayList<MultipleInteraction> getMultipleInteractions() {
		return multipleInteractions;
	}

	public void removeInputs() {
		
		int interactionsBeforeTrim = singleInteractions.size();
		logger.outputStringMessage(1, "\nRemoving inputs. Interactions before trim: "
				+ interactionsBeforeTrim);

		int trimIteration = 0;

		do {
			trimIteration++;
			interactionsBeforeTrim = singleInteractions.size();

			for (int i = singleInteractions.size() - 1; i >= 0; i--) {
				String source = singleInteractions.get(i).getSource();
				if (isNotATarget(source)) {
					logger.outputStringMessage(3, "Removing interaction (i = " + i + ")  (not target): "
							+ singleInteractions.get(i).getInteraction());
					singleInteractions.remove(i);
				}
			}
		} while (interactionsBeforeTrim > singleInteractions.size());

		logger.outputStringMessage(1, "Interactions after trim (" + trimIteration
				+ " iterations): " + singleInteractions.size() + "\n");
	}

	public void removeOutputs() {
		
		int interactionsBeforeTrim = singleInteractions.size();
		logger.outputStringMessage(1, "\nRemoving outputs. Interactions before trim: "
				+ interactionsBeforeTrim);

		int trimIteration = 0;

		do {
			trimIteration++;
			interactionsBeforeTrim = singleInteractions.size();

			for (int i = singleInteractions.size() - 1; i >= 0; i--) {
				String target = singleInteractions.get(i).getTarget();
				if (isNotASource(target)) {
					logger.outputStringMessage(3, "Removing interaction (not source): "
							+ singleInteractions.get(i).getInteraction());
					singleInteractions.remove(i);
				}
			}
		} while (interactionsBeforeTrim > singleInteractions.size());

		logger.outputStringMessage(1, "Interactions after trim (" + trimIteration
				+ " iterations): " + singleInteractions.size() + "\n");
	}

	public void removeInputsOutputs() {

		int interactionsBeforeTrim = singleInteractions.size();
		logger.outputStringMessage(1, "\nInteractions before trim: " + interactionsBeforeTrim + "\n");
		
		int trimIteration = 0;

		do {
			trimIteration++;

			interactionsBeforeTrim = singleInteractions.size();

			for (int i = singleInteractions.size() - 1; i >= 0; i--) {
				String source = singleInteractions.get(i).getSource();
				if (isNotATarget(source)) {
					logger.outputStringMessage(3,
							"Removing interaction (not target): " + singleInteractions.get(i).toString());
					singleInteractions.remove(i);
				}
			}

			for (int i = singleInteractions.size() - 1; i >= 0; i--) {
				String target = singleInteractions.get(i).getTarget();
				if (isNotASource(target)) {
					logger.outputStringMessage(3, "Removing interaction (not source): "
							+ singleInteractions.get(i).toString());
					singleInteractions.remove(i);
				}
			}

			logger.outputStringMessage(3, "Trimming (iteration " + trimIteration
					+ "): Interactions before iteration: " + interactionsBeforeTrim
					+ " Interactions after iteration: " + singleInteractions.size() + "\n");
		} while (interactionsBeforeTrim > singleInteractions.size());

		logger.outputStringMessage(1, "Interactions after trim (" + trimIteration
				+ " iterations): " + singleInteractions.size() + "\n");
	}

	void removeSelfRegulatedInteractions() {
		for (int i = singleInteractions.size() - 1; i >= 0; i--) {
			if (singleInteractions.get(i).getTarget().trim()
					.equals(singleInteractions.get(i).getSource().trim())) {
				logger.outputStringMessage(2, "Removing self regulation: "
						+ singleInteractions.get(i).getInteraction());
				singleInteractions.remove(i);
			}
		}
	}

	MultipleInteraction getMultipleInteraction(int index) {
		return multipleInteractions.get(index);
	}

	public ArrayList<SingleInteraction> getSingleInteractions() {
		return singleInteractions;
	}

	boolean isNotASource(String nodeName) {
		boolean result = true;

		for (SingleInteraction singleInteraction : singleInteractions) {
			if (nodeName.equals(singleInteraction.getSource())) {
				result = false;
			}
		}

		return result;
	}

	boolean isNotATarget(String nodeName) {
		boolean result = true;

		for (SingleInteraction singleInteraction : singleInteractions) {
			if (nodeName.equals(singleInteraction.getTarget())) {
				result = false;
			}
		}

		return result;
	}

	String getModelName() {
		return this.modelName;
	}

	@Override
	public String toString() {
		StringBuilder multipleInteractionsStr = new StringBuilder();

		for (MultipleInteraction multipleInteraction : multipleInteractions) {
			multipleInteractionsStr.append(multipleInteraction.toString()).append("\n");
		}

		return multipleInteractionsStr.toString();
	}

}
