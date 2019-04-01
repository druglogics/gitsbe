package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.util.Logger;

import static eu.druglogics.gitsbe.util.Util.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GeneralModel {

	private ArrayList<SingleInteraction> singleInteractions = new ArrayList<SingleInteraction>();
	private ArrayList<MultipleInteraction> multipleInteractions = new ArrayList<MultipleInteraction>();
	private Logger logger;
	private String modelName;
	// ArrayList <Complex> complexes = new ArrayList <Copmplex> () ;

	public GeneralModel(Logger logger) {
		this.logger = logger;
	}

	public int size() {
		return multipleInteractions.size();
	}

	public void loadInteractionFile(String filenameInteractions) throws IOException {
		String fileExtension = getFileExtension(filenameInteractions);
		if (fileExtension.equals(".sif"))
			this.loadSifFile(filenameInteractions);
		else
			logger.error("New file extension used to load general model, " +
					"currently not supported");
	}

	private void loadSifFile(String filename) throws IOException {

		ArrayList<String> interactions = new ArrayList<String>();
		this.modelName = new File(filename).getName();

		// Read given file
		logger.outputStringMessage(1, "Reading .sif file: " + filename);
		
		ArrayList<String> lines = readLinesFromFile(filename, true);
		for (int index = 0; index < lines.size(); ++index)
			interactions.add(lines.get(index));

		for (int i = 0; i < interactions.size(); i++) {

			if (interactions.get(i).toString().length() > 0) {

				String line = interactions.get(i);

				if (line.contains("<->")) {
					String line1 = line.replace("<->", "<-");
					String line2 = line.replace("<->", "->");
					SingleInteraction singleInteraction1 = new SingleInteraction(line1);
					SingleInteraction singleInteraction2 = new SingleInteraction(line2);
					singleInteractions.add(singleInteraction1);
					singleInteractions.add(singleInteraction2);
				} else if (line.contains("|-|")) {
					String line1 = line.replace("|-|", "|-");
					String line2 = line.replace("|-|", "-|");
					SingleInteraction singleInteraction1 = new SingleInteraction(line1);
					SingleInteraction singleInteraction2 = new SingleInteraction(line2);
					singleInteractions.add(singleInteraction1);
					singleInteractions.add(singleInteraction2);
				} else if (line.contains("|->")) {
					String line1 = line.replace("|->", "->");
					String line2 = line.replace("|->", "|-");
					SingleInteraction singleInteraction1 = new SingleInteraction(line1);
					SingleInteraction singleInteraction2 = new SingleInteraction(line2);
					singleInteractions.add(singleInteraction1);
					singleInteractions.add(singleInteraction2);
				} else if (line.contains("<-|")) {
					String line1 = line.replace("<-|", "<-");
					String line2 = line.replace("<-|", "-|");
					SingleInteraction singleInteraction1 = new SingleInteraction(line1);
					SingleInteraction singleInteraction2 = new SingleInteraction(line2);
					singleInteractions.add(singleInteraction1);
					singleInteractions.add(singleInteraction2);
				} else {
					SingleInteraction singleInteraction = new SingleInteraction(line);
					singleInteractions.add(singleInteraction);
				}
			}
		}
	}

	public void loadFromSingleInteractions(SingleInteraction[] interactions) {
		for (int i = 0; i < interactions.length; i++) {
			singleInteractions.add(new SingleInteraction(interactions[i]));
		}
	}

	private int getIndexOfTargetInListOfMultipleInteraction(String target) {
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
			
			if (getIndexOfTargetInListOfMultipleInteraction(singleInteractions.get(i).getTarget()) < 0) {
				MultipleInteraction multipleInteraction = new MultipleInteraction(singleInteractions.get(i).getTarget());
				for (int k = 0; k < singleInteractions.size(); k++) {
					SingleInteraction singleInteraction = singleInteractions.get(k);

					if (singleInteraction.getTarget().equals(multipleInteraction.getTarget())) {
						if (singleInteraction.getArc() == 1) {
							multipleInteraction.addActivatingRegulator(singleInteraction.getSource());
						} else if (singleInteraction.getArc() == -1) {
							multipleInteraction.addInhibitoryRegulator(singleInteraction.getSource());
						} else {
							logger.error("Interaction without regulator found: \n");
						}
					}
				}
				multipleInteractions.add(multipleInteraction);
			}
		
		}
	}

	public ArrayList<MultipleInteraction> getMultipleInteractions() {
		return multipleInteractions;
	}

	public void removeInputs() {
		
		int interactionsBeforeTrim = singleInteractions.size();
		logger.outputStringMessage(1, "\nRemoving inputs. Interactions before trim: " + interactionsBeforeTrim);

		int trimIteration = 0;

		do {
			trimIteration++;
			interactionsBeforeTrim = singleInteractions.size();

			for (int i = singleInteractions.size() - 1; i >= 0; i--) {
				if (!this.isAlsoTarget(i)) {
					logger.outputStringMessage(3, "Removing interaction (i = " + i + ")  (not target): "
							+ singleInteractions.get(i).getInteraction());
					singleInteractions.remove(i);

				}
			}
		} while (interactionsBeforeTrim > singleInteractions.size());

		logger.outputStringMessage(1,
				"Interactions after trim (" + trimIteration + " iterations): " + singleInteractions.size() + "\n");

	}

	public void removeOutputs() {
		
		int interactionsBeforeTrim = singleInteractions.size();
		logger.outputStringMessage(1, "\nRemoving outputs. Interactions before trim: " + interactionsBeforeTrim);

		int trimIteration = 0;

		do {
			trimIteration++;
			interactionsBeforeTrim = singleInteractions.size();

			for (int i = singleInteractions.size() - 1; i >= 0; i--) {
				if (!this.isAlsoSource(i)) {
					logger.outputStringMessage(3,
							"Removing interaction (not source): " + singleInteractions.get(i).getInteraction());
					singleInteractions.remove(i);
				}
			}
		} while (interactionsBeforeTrim > singleInteractions.size());

		logger.outputStringMessage(1,
				"Interactions after trim (" + trimIteration + " iterations): " + singleInteractions.size() + "\n");

	}

	public void removeInputsOutputs() {

		int interactionsBeforeTrim = singleInteractions.size();
		logger.outputStringMessage(1, "\nInteractions before trim: " + interactionsBeforeTrim + "\n");
		
		int trimIteration = 0;

		do {
			trimIteration++;

			interactionsBeforeTrim = singleInteractions.size();

			for (int i = singleInteractions.size() - 1; i >= 0; i--) {
				if (!this.isAlsoTarget(i)) {
					logger.outputStringMessage(3,
							"Removing interaction (not target): " + singleInteractions.get(i).toString());
					singleInteractions.remove(i);

				}

			}

			for (int i = singleInteractions.size() - 1; i >= 0; i--) {
				if (!this.isAlsoSource(i)) {
					logger.outputStringMessage(3,
							"Removing interaction (not source): " + singleInteractions.get(i).toString());
					singleInteractions.remove(i);
				}

			}

			logger.outputStringMessage(3, "Trimming (iteration " + trimIteration + "): Interactions before iteration: "
					+ interactionsBeforeTrim + " Interactions after iteration: " + singleInteractions.size() + "\n");

		} while (interactionsBeforeTrim > singleInteractions.size());

		logger.outputStringMessage(1,
				"Interactions after trim (" + trimIteration + " iterations): " + singleInteractions.size() + "\n");
	}

	/**
	 * Required if no inputs or targets are removed, to annotate some nodes as
	 * inputs to system. Inputs must be given a value in training data file (unperturbed steady state).
	 */
	public void removeNone() {
		for (int i = 0; i < singleInteractions.size(); i++) {
			if (!isAlsoTarget(i)) {
				singleInteractions.add(new SingleInteraction("true", "->", singleInteractions.get(i).getSource()));
				logger.outputStringMessage(2,
						"Annotating " + singleInteractions.get(i).getSource() + " as input to model.");
			}
		}

	}

	public void removeSelfRegulation() {
		for (int i = singleInteractions.size() - 1; i >= 0; i--) {
			if (singleInteractions.get(i).getTarget().trim().equals(singleInteractions.get(i).getSource().trim())) {
				logger.outputStringMessage(2,
						"Removing self regulation: " + singleInteractions.get(i).getInteraction());
				singleInteractions.remove(i);
			}

		}
	}

	public void removeSmallFeedbackLoops() {
		logger.outputStringMessage(3, "\nRemoving small feedback loops (positive and negative)");
		for (int i = singleInteractions.size() - 1; i >= 0; i--) {
			int index = getSingleInteractionFromSource(singleInteractions.get(i).getTarget());

			if (index >= 0) {
				if (singleInteractions.get(index).getTarget().equals(singleInteractions.get(i).getSource())) {
					logger.outputStringMessage(3,
							"Small feedback detected: " + singleInteractions.get(i).getInteraction() + " AND "
									+ singleInteractions.get(index).getInteraction());
					if (index > i) {
						singleInteractions.remove(index);
						singleInteractions.remove(i);
					} else {
						singleInteractions.remove(i);
						singleInteractions.remove(index);
					}

				}
			}
		}
	}

	public void removeSmallNegativeFeedbackLoops() {
		logger.outputStringMessage(3, "\nRemoving small negative feedback loops");
		for (int i = singleInteractions.size() - 1; i >= 0; i--) {
			int index = getSingleInteractionFromSource(singleInteractions.get(i).getTarget());

			if (index >= 0) {
				if (singleInteractions.get(index).getTarget().equals(singleInteractions.get(i).getSource())) {
					// Check if arcs have opposite signs
					if (singleInteractions.get(index).getArc() != singleInteractions.get(i).getArc()) {
						logger.outputStringMessage(3,
								"Small negative feedback detected: " + singleInteractions.get(i).getInteraction()
										+ " AND " + singleInteractions.get(index).getInteraction());
						if (index > i) {
							singleInteractions.remove(index);
							singleInteractions.remove(i);
						} else {
							singleInteractions.remove(i);
							singleInteractions.remove(index);
						}
					}
				}
			}
		}
	}

	public void removeSmallPositiveFeedbackLoops() {

	}

	public MultipleInteraction getMultipleInteraction(int index) {
		return multipleInteractions.get(index);
	}

	@SuppressWarnings("unused")
	private int getSingleInteractionFromTarget(String target) {
		int index = -1;

		for (int i = 0; i < singleInteractions.size(); i++) {
			if (singleInteractions.get(i).getTarget().equals(target))
				index = i;
		}

		return index;
	}

	private int getSingleInteractionFromSource(String source) {
		int index = -1;

		for (int i = 0; i < singleInteractions.size(); i++) {
			if (singleInteractions.get(i).getSource().equals(source))
				index = i;
		}

		return index;
	}

	public ArrayList<SingleInteraction> getSingleInteractions() {
		return singleInteractions;
	}

	private boolean isAlsoSource(int indexInteractions) {
		boolean result = false;
		String targetname = singleInteractions.get(indexInteractions).getTarget();

		for (int i = 0; i < singleInteractions.size(); i++) {
			if (targetname.equals(singleInteractions.get(i).getSource())) {
				result = true;
			}
		}

		return result;
	}

	private boolean isAlsoTarget(int indexInteractions) {
		boolean result = false;
		String sourcename = singleInteractions.get(indexInteractions).getSource();

		for (int i = 0; i < singleInteractions.size(); i++) {
			if (sourcename.equals(singleInteractions.get(i).getTarget())) {
				result = true;
			}
		}

		return result;
	}

	public String getModelName() {
		return this.modelName;
	}

	public void setModelName(String path) {
		this.modelName = path;
	}

	@Override
	public String toString() {
		String tostring = new String();

		for (int i = 0; i < multipleInteractions.size(); i++) {
			tostring += multipleInteractions.get(i).toString() + "\n";
		}

		return tostring;

	}

}
