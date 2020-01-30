package eu.druglogics.gitsbe.drug;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static eu.druglogics.gitsbe.util.Util.readLinesFromFile;

/**
 * @author john
 * Simplified version of Drabme same-name Class, now written as a Singleton
 */
public class DrugPanel {

	private static DrugPanel drugPanel = null;

	// Panel of single drugs to be used
	private ArrayList<Drug> drugs;
	private Logger logger;

	private DrugPanel(String filename, Logger logger) throws IOException, ConfigurationException {
		this.logger = logger;
		this.drugs = new ArrayList<>();
		loadDrugPanelFile(filename);
	}

	public static DrugPanel getInstance() {
		// To ensure only one instance is created
		if (drugPanel == null) {
			throw new AssertionError("You have to call init first to initialize the DrugPanel Class");
		}
		return drugPanel;
	}

	public synchronized static void init(String filename, Logger logger) throws Exception {
		if (drugPanel != null) {
			throw new AssertionError("You already initialized me");
		}
		drugPanel = new DrugPanel(filename, logger);
	}

	private void loadDrugPanelFile(String filename) throws IOException, ConfigurationException {
		logger.outputStringMessage(3, "Reading drugpanel file: "
			+ new File(filename).getAbsolutePath());
		ArrayList<String> lines = readLinesFromFile(filename, true);

		for (int i = 0; i < lines.size(); i++) {
			ArrayList<String> lineTabSplited = new ArrayList<>(Arrays.asList(lines.get(i).split("\t")));

			// Add drug name
			String drugName = lineTabSplited.get(0);
			drugs.add(new Drug(drugName));

			// Add perturbation effect
			String effect = lineTabSplited.get(1);
			if (effect.equals("inhibits")) {
				drugs.get(i).addEffect(false);
			} else if (effect.equals("activates")) {
				drugs.get(i).addEffect(true);
			} else {
				throw new ConfigurationException("Drug effect: `" + effect + "` is neither `activates` or `inhibits`");
			}

			// Add drug targets
			drugs.get(i).addTargets(new ArrayList<>(lineTabSplited.subList(2, lineTabSplited.size())));
		}
	}

	public ArrayList<Drug> getDrugs() {
		return drugs;
	}

	public ArrayList<String> getDrugNames() {
		ArrayList<String> drugNameList = new ArrayList<>();
		for (Drug drug : this.drugs) {
			drugNameList.add(drug.getName());
		}

		return drugNameList;
	}

	int getDrugPanelSize() {
		return getDrugs().size();
	}

	/**
	 * Adds warnings to the log if there are drug targets
	 * not defined in the given boolean model
	 *
	 * @param booleanModel
	 */
	public void checkDrugTargets(BooleanModel booleanModel) {
		logger.outputHeader(3, "Checking drug targets");

		ArrayList<String> nodes = booleanModel.getNodeNames();

		for (Drug drug : this.drugs) {
			for (String target : drug.getTargets()) {
				if (!nodes.contains(target)) {
					logger.outputStringMessage(3, "Warning: Target `" + target + "` of Drug `"
						+ drug.getName() + "` is not in the network file/model");
				}
			}
		}
	}

	/**
	 * Given a drug name string, this function finds the drug with the same name
	 * and returns it's targets
	 *
	 * @param drugName the name of the drug
	 * @return a list of targets
	 */
	public ArrayList<String> getDrugTargets(String drugName) throws ConfigurationException {
		ArrayList<String> targets = new ArrayList<>();
		boolean drugDefinedInDrugPanel = false;

		for (Drug drug : this.drugs) {
			if (drug.getName().equals(drugName)) {
				targets = drug.getTargets();
				drugDefinedInDrugPanel = true;
				break;
			}
		}

		if (!drugDefinedInDrugPanel)
			throw new ConfigurationException("There is no drug with name: `" + drugName + "`");

		return targets;
	}

	/**
	 * Given a drug name string, this function returns the effect of the drug on it's
	 * targets: either true (overexpression/activation) or false (inhibition)
	 *
	 * @param drugName the name of the drug
	 * @return the effect
	 */
	public boolean getDrugEffect(String drugName) throws ConfigurationException {
		boolean effect = false;
		boolean drugDefinedInDrugPanel = false;

		for (Drug drug : this.drugs) {
			if (drug.getName().equals(drugName)) {
				effect = drug.getEffect();
				drugDefinedInDrugPanel = true;
				break;
			}
		}

		if (!drugDefinedInDrugPanel)
			throw new ConfigurationException("There is no drug with name: `" + drugName + "`");

		return effect;
	}

}