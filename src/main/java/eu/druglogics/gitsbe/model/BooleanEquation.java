package eu.druglogics.gitsbe.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static eu.druglogics.gitsbe.util.RandomManager.randInt;
import static eu.druglogics.gitsbe.util.Util.getRepeatedString;

public class BooleanEquation {

	// String target name
	private String target;

	// Strings regulators
	private ArrayList<String> activatingRegulators;
	private ArrayList<String> inhibitoryRegulators;

	// Strings operators
	private ArrayList<String> operatorsActivatingRegulators;
	private ArrayList<String> operatorsInhibitoryRegulators;

	// whitelist of regulators to include in equation - string of true and false
	// where true means included
	private ArrayList<Boolean> whitelistActivatingRegulators;
	private ArrayList<Boolean> whitelistInhibitoryRegulators;

	// String link activators and inhibitors ("and not"/"or not")
	private String link;

	private BooleanEquation() {

		// Initialize variables

		activatingRegulators = new ArrayList<>();
		inhibitoryRegulators = new ArrayList<>();

		operatorsActivatingRegulators = new ArrayList<>();
		operatorsInhibitoryRegulators = new ArrayList<>();

		whitelistActivatingRegulators = new ArrayList<>();
		whitelistInhibitoryRegulators = new ArrayList<>();
	}

	/**
	 * Copy constructor
	 * 
	 * @param originalEquation
	 */
	BooleanEquation(BooleanEquation originalEquation) {
		this();

		this.operatorsActivatingRegulators.addAll(originalEquation.operatorsActivatingRegulators);

		for (int i = 0; i < originalEquation.inhibitoryRegulators.size(); i++) {
			this.inhibitoryRegulators.add(originalEquation.inhibitoryRegulators.get(i));
			whitelistInhibitoryRegulators.add(true);
		}

		for (int i = 0; i < originalEquation.activatingRegulators.size(); i++) {
			this.activatingRegulators.add((originalEquation.activatingRegulators.get(i)));
			whitelistActivatingRegulators.add(true);
		}

		this.operatorsInhibitoryRegulators.addAll(originalEquation.operatorsInhibitoryRegulators);

		this.link = originalEquation.link;

		this.target = originalEquation.target;
	}

	BooleanEquation(MultipleInteraction multipleInteraction) {
		this();

		// Build expression
		target = multipleInteraction.getTarget();

		ArrayList<String> tempActivatingRegulators = multipleInteraction.getActivatingRegulators();

		for (int i = 0; i < tempActivatingRegulators.size(); i++) {
			activatingRegulators.add(tempActivatingRegulators.get(i));
			whitelistActivatingRegulators.add(true);

			if (i < tempActivatingRegulators.size() - 1) {
				operatorsActivatingRegulators.add("or");
			}
		}

		ArrayList<String> tempInhibitoryRegulators = multipleInteraction.getInhibitoryRegulators();

		for (int i = 0; i < tempInhibitoryRegulators.size(); i++) {
			inhibitoryRegulators.add(tempInhibitoryRegulators.get(i));
			whitelistInhibitoryRegulators.add(true);
			if (i < tempInhibitoryRegulators.size() - 1) {
				operatorsInhibitoryRegulators.add("or");
			}
		}

		link = (tempInhibitoryRegulators.size() == 0 || tempActivatingRegulators.size() == 0)
				? ""
				: "and";
	}

	/**
	 * Build equation from Boolean expression. Currently expressions must be of the
	 * following type (the .booleannet format):
	 * A *= ( ( ( B ) or C ) or D ) and not ( ( ( E ) or F ) or G )
	 *
	 * Spaces between parentheses and node names are essential
	 * 
	 * @param equation
	 */
	public BooleanEquation(String equation) {
		this();

		equation = equation.trim();

		// Trim spaces until only single spaces are present, which will be used
		// as split points
		int length;
		do {
			length = equation.length();
			equation = equation.replaceAll(" {2}", " "); // 2 spaces regex
		} while (length > equation.length());

		equation = equation.replaceAll("and not", "andnot");
		equation = equation.replaceAll("or not", "ornot");

		// Split equation to array
		ArrayList<String> splitEquation = new ArrayList<>(Arrays.asList(equation.split(" ")));

		target = splitEquation.get(0);
		link = ""; // in case equation has only activating or inhibiting regulators
		splitEquation.remove(0);
		boolean beforeNot = true;

		// Parse each element of array
		do {
			String element = splitEquation.get(0).trim();
			splitEquation.remove(0);

			switch (element) {
			case "*=":
			case "(":
			case ")":
				break;
			case "andnot":
				beforeNot = false;
				link = "and";
				break;
			case "ornot":
				beforeNot = false;
				link = "or";
				break;
			case "not":
				beforeNot = false;
				break;
			case "or":
			case "and":
				if (beforeNot)
					operatorsActivatingRegulators.add(element);
				else
					operatorsInhibitoryRegulators.add(element);
				break;
			default:
				if (beforeNot) {
					activatingRegulators.add(element);
					whitelistActivatingRegulators.add(true);
				} else {
					inhibitoryRegulators.add(element);
					whitelistInhibitoryRegulators.add(true);
				}
			}
		} while (splitEquation.size() > 0);

	}

	/**
	 * Returns the string of the equation represented in the Booleannet format: <br>
	 * <i>A *=  (  (  B )  or C or ...) and not  (  ( E )  or F or ...)</i>
	 *
	 */
	public String getBooleanEquation() {
		StringBuilder equation = new StringBuilder(target + " *= ");

		// Add activating regulators
		if (Collections.frequency(whitelistActivatingRegulators, true) > 0) {
			equation.append(getRepeatedString(" ( ",
					Collections.frequency(whitelistActivatingRegulators, true)));
			equation.append(" ");

			for (int i = 0; i < activatingRegulators.size(); i++) {

				if (whitelistActivatingRegulators.get(i)) {
					// If not first element then add boolean operator before regulator
					if (Collections.frequency(whitelistActivatingRegulators.subList(0, i), true) > 0) {
						equation.append(" ").append(operatorsActivatingRegulators.get(i - 1)).append(" ");
					}
					equation.append(activatingRegulators.get(i)).append(" ) ");
				}
			}
		}

		// Find correct link for activators and inhibitory regulators
		if ((Collections.frequency(whitelistActivatingRegulators, true) > 0)
				&& (Collections.frequency(whitelistInhibitoryRegulators, true) > 0))
			equation.append(link);

		if (Collections.frequency(whitelistInhibitoryRegulators, true) > 0) {
			equation.append(" not ");

			equation.append(getRepeatedString(" ( ",
					Collections.frequency(whitelistInhibitoryRegulators, true)));

			for (int i = 0; i < inhibitoryRegulators.size(); i++)

			{
				if (whitelistInhibitoryRegulators.get(i)) {
					if (Collections.frequency(whitelistInhibitoryRegulators.subList(0, i), true) > 0)
						equation.append(" ").append(operatorsInhibitoryRegulators.get(i - 1)).append(" ");

					equation.append(inhibitoryRegulators.get(i)).append(" ) ");
				}
			}

		}

		return " " + equation.toString().trim() + " ";
	}

	void mutateRandomOperator() {
		if (randInt(0, 1) > 0.5) {
			mutateRandomActivatoryOperator();
		} else {
			mutateRandomInhibitoryOperator();
		}
	}

	void mutateRegulator() {
		// randomly select activating or inhibiting regulator, but make sure at least
		// one regulator is kept
		if ((Collections.frequency(whitelistActivatingRegulators, true)
				+ Collections.frequency(whitelistInhibitoryRegulators, true)) > 1) {
			if (randInt(0, 1) > 0.5) {
				mutateActivatingRegulator();
			} else {
				mutateInhibitoryRegulator();
			}
		}
	}
	
	private void mutateActivatingRegulator() {
		if (this.activatingRegulators.size() > 0) {
			int index = randInt(0, activatingRegulators.size() - 1);
			this.whitelistActivatingRegulators.set(index, !whitelistActivatingRegulators.get(index));
		}
	}

	private void mutateInhibitoryRegulator() {
		if (this.inhibitoryRegulators.size() > 0) {
			int index = randInt(0, inhibitoryRegulators.size() - 1);
			this.whitelistInhibitoryRegulators.set(index, !whitelistInhibitoryRegulators.get(index));
		}
	}

	private void mutateRandomActivatoryOperator() {
		if (operatorsActivatingRegulators.size() > 0) {
			int randomIndex = randInt(0, operatorsActivatingRegulators.size() - 1);

			if (operatorsActivatingRegulators.get(randomIndex).trim().equals("or")) {
				operatorsActivatingRegulators.set(randomIndex, " and ");
			} else {
				operatorsActivatingRegulators.set(randomIndex, "or");
			}
		}
	}

	private void mutateRandomInhibitoryOperator() {
		if (operatorsInhibitoryRegulators.size() > 0) {
			int randomIndex = randInt(0, operatorsInhibitoryRegulators.size() - 1);

			if (operatorsInhibitoryRegulators.get(randomIndex).trim().equals("or")) {
				operatorsInhibitoryRegulators.set(randomIndex, "and");
			} else {
				operatorsInhibitoryRegulators.set(randomIndex, "or");
			}
		}
	}

	public void mutateLinkOperator() {
		if (link.trim().equals("and")) {
			link = "or";
		} else {
			link = "and";
		}
	}

	void shuffleRandomRegulatorPriority() {
		if (randInt(0, 1) == 1) {
			shuffleRandomActivatingRegulatorPriority();
		} else {
			shuffleRandomInhibitoryRegulatorPriority();
		}
	}

	private void shuffleRandomActivatingRegulatorPriority() {
		if (activatingRegulators.size() > 1) {
			int randomIndex = randInt(0, activatingRegulators.size() - 2);

			String temp = activatingRegulators.get(randomIndex);
			activatingRegulators.set(randomIndex, activatingRegulators.get(randomIndex + 1));
			activatingRegulators.set(randomIndex + 1, temp);
		}
	}

	private void shuffleRandomInhibitoryRegulatorPriority() {
		if (inhibitoryRegulators.size() > 1) {
			int randomIndex = randInt(0, inhibitoryRegulators.size() - 2);

			String temp = inhibitoryRegulators.get(randomIndex);
			inhibitoryRegulators.set(randomIndex, inhibitoryRegulators.get(randomIndex + 1));
			inhibitoryRegulators.set(randomIndex + 1, temp);
		}
	}

	ArrayList<String> convertToSifLines(String delimiter) {
		ArrayList<String> lines = new ArrayList<>();

		for (String activatingRegulator : activatingRegulators)
			lines.add(activatingRegulator + delimiter + "->" + delimiter + target);

		for (String inhibitoryRegulator : inhibitoryRegulators)
			lines.add(inhibitoryRegulator + delimiter + "-|" + delimiter + target);

		return lines;
	}

	public ArrayList<SingleInteraction> getSingleInteractions() {
		ArrayList<SingleInteraction> singleInteractions = new ArrayList<>();

		for (String activatingRegulator : activatingRegulators) {
			singleInteractions.add(new SingleInteraction(activatingRegulator, "->", target));
		}

		for (String inhibitoryRegulator : inhibitoryRegulators) {
			singleInteractions.add(new SingleInteraction(inhibitoryRegulator, "-|", target));
		}

		return singleInteractions;
	}

	public int getNumWhitelistedRegulators() {
		return (getNumWhitelistedActivatingRegulators()
				+ getNumWhitelistedInhibitoryRegulators());
	}

	public int getNumBlacklistedRegulators() {
		return (getNumBlacklistedActivatingRegulators()
				+ getNumBlacklistedInhibitoryRegulators());
	}

	public int getNumWhitelistedActivatingRegulators() {
		return Collections.frequency(whitelistActivatingRegulators, true);
	}

	public int getNumWhitelistedInhibitoryRegulators() {
		return Collections.frequency(whitelistInhibitoryRegulators, true);
	}

	public int getNumBlacklistedActivatingRegulators() {
		return Collections.frequency(whitelistActivatingRegulators, false);
	}

	public int getNumBlacklistedInhibitoryRegulators() {
		return Collections.frequency(whitelistInhibitoryRegulators, false);
	}

	public int getNumRegulators() {
		return (activatingRegulators.size() + inhibitoryRegulators.size());
	}

	public ArrayList<String> getActivatingRegulators() {
		return activatingRegulators;
	}

	public ArrayList<String> getInhibitoryRegulators() {
		return inhibitoryRegulators;
	}

	public String getTarget() {
		return target;
	}

	public String getLink() {
		return link;
	}
}
