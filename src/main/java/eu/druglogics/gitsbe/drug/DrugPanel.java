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
 * Simplified version of Drabme same-name Class
 */
public class DrugPanel {

	// Panel of single drugs to be used
	private ArrayList<Drug> drugs;
	private Logger logger;

	public DrugPanel(String filename, Logger logger) throws IOException, ConfigurationException {
		this.logger = logger;
		this.drugs = new ArrayList<>();
		loadDrugPanelFile(filename);
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

	public int getDrugPanelSize() {
		return getDrugs().size();
	}

	/**
	 * Adds warnings to the log if there are drug targets
	 * not defined in the given boolean model
	 *
	 * @param booleanModel
	 */
	void checkDrugTargets(BooleanModel booleanModel) {
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
}
