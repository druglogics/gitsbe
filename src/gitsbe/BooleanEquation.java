package gitsbe;

import static gitsbe.RandomManager.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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

		activatingRegulators = new ArrayList<String>();
		inhibitoryRegulators = new ArrayList<String>();

		operatorsActivatingRegulators = new ArrayList<String>();
		operatorsInhibitoryRegulators = new ArrayList<String>();

		whitelistActivatingRegulators = new ArrayList<Boolean>();
		whitelistInhibitoryRegulators = new ArrayList<Boolean>();
	}

	/**
	 * Copy constructor
	 * 
	 * @param originalEquation
	 */
	public BooleanEquation(BooleanEquation originalEquation) {

		this();

		for (int i = 0; i < originalEquation.operatorsActivatingRegulators.size(); i++) {
			this.operatorsActivatingRegulators.add(new String(originalEquation.operatorsActivatingRegulators.get(i)));
		}

		for (int i = 0; i < originalEquation.inhibitoryRegulators.size(); i++) {
			this.inhibitoryRegulators.add(new String(originalEquation.inhibitoryRegulators.get(i)));
			whitelistInhibitoryRegulators.add(true);
		}

		for (int i = 0; i < originalEquation.activatingRegulators.size(); i++) {
			this.activatingRegulators.add(new String(originalEquation.activatingRegulators.get(i)));
			whitelistActivatingRegulators.add(true);
		}

		for (int i = 0; i < originalEquation.operatorsInhibitoryRegulators.size(); i++) {
			this.operatorsInhibitoryRegulators.add(new String(originalEquation.operatorsInhibitoryRegulators.get(i)));
		}

		this.link = new String(originalEquation.link);

		this.target = new String(originalEquation.target);
	}

	public BooleanEquation(MultipleInteraction multipleInteraction) {
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

		link = "and";
	}

	/**
	 * Build equation from Boolean expression. Currently expressions must be of the
	 * following type: A *= (((B) or C) or D) and not (((E) or F) or G)
	 * 
	 * @param equation
	 */
	public BooleanEquation(String equation) {
		this();

		equation = equation.trim();

		// Trim spaces until only single spaces are present, which will be used
		// as split points
		int length = 0;
		do {
			length = equation.length();
			equation = equation.replaceAll("  ", " ");
		} while (length > equation.length());

		equation = equation.replaceAll("and not", "andnot");
		equation = equation.replaceAll("or not", "ornot");

		// Split equation to array
		ArrayList<String> splitequation = new ArrayList<String>(Arrays.asList(equation.split(" ")));

		target = splitequation.get(0);
		splitequation.remove(0);
		boolean beforeNot = true;

		// Parse each element of array
		do {
			String element = splitequation.get(0).trim();
			splitequation.remove(0);

			switch (element) {
			case "*=":
				break;
			case "(":
				break;
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
		} while (splitequation.size() > 0);

	}

	public String getBooleanEquation() {
		String equation = target + " *= ";

		// Add activating regulators
		if (Collections.frequency(whitelistActivatingRegulators, true) > 0) {
			equation += getRepeatedString(" ( ", Collections.frequency(whitelistActivatingRegulators, true));
			equation += " ";

			for (int i = 0; i < activatingRegulators.size(); i++) {

				if (whitelistActivatingRegulators.get(i) == true) {
					// If not first element then add boolean operator before regulator
					if (Collections.frequency(whitelistActivatingRegulators.subList(0, i), true) > 0) {
						equation += " " + operatorsActivatingRegulators.get(i - 1) + " ";
					}
					equation += activatingRegulators.get(i) + " ) ";
				}
			}

			// equation += ")" ;
		}

		// Find correct link for activators and inhibitory regulators
		if ((Collections.frequency(whitelistActivatingRegulators, true) > 0)
				&& (Collections.frequency(whitelistInhibitoryRegulators, true) > 0))
			equation += link;

		if (Collections.frequency(whitelistInhibitoryRegulators, true) > 0) {
			equation += " not ";

			equation += getRepeatedString(" ( ", Collections.frequency(whitelistInhibitoryRegulators, true));
			// equation += " " ;

			for (int i = 0; i < inhibitoryRegulators.size(); i++)

			{
				if (whitelistInhibitoryRegulators.get(i) == true) {
					if (Collections.frequency(whitelistInhibitoryRegulators.subList(0, i), true) > 0)
						equation += " " + operatorsInhibitoryRegulators.get(i - 1) + " ";

					equation += inhibitoryRegulators.get(i) + " ) ";
				}
			}

		}

		return " " + equation.trim() + " ";
	}

	public String getBooleanEquationVC() {
		String equation = getBooleanEquation();

		// Replace operators
		equation = equation.replace(" and ", " & ");
		equation = equation.replace(" or ", " | ");
		equation = equation.replace(" not ", " ! ");
		equation = equation.replace(" true ", " 1 ");
		equation = equation.replace(" false ", " 0 ");

		return equation;
	}

	public void mutateRandomOperator() {
		if (randInt(0, 1) > 0.5) {
			mutateRandomActivatoryOperator();
		} else {
			mutateRandomInhibitoryOperator();
		}
	}

	public void mutateRegulator() {
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
	
	public void mutateActivatingRegulator() {
		if (this.activatingRegulators.size() > 0) {
			int index = randInt(0, activatingRegulators.size() - 1);
			this.whitelistActivatingRegulators.set(index, !whitelistActivatingRegulators.get(index));
		}
	}

	public void mutateInhibitoryRegulator() {
		if (this.inhibitoryRegulators.size() > 0) {
			int index = randInt(0, inhibitoryRegulators.size() - 1);
			this.whitelistInhibitoryRegulators.set(index, !whitelistInhibitoryRegulators.get(index));
		}
	}

	public void mutateRandomOperator(int mutations) {
		for (int i = 0; i < mutations; i++) {
			mutateRandomOperator();
		}
	}

	public void mutateRandomActivatoryOperator() {
		if (operatorsActivatingRegulators.size() > 0) {
			int randomIndex = randInt(0, operatorsActivatingRegulators.size() - 1);

			if (operatorsActivatingRegulators.get(randomIndex).trim().equals("or")) {
				operatorsActivatingRegulators.set(randomIndex, " and ");
			} else {
				operatorsActivatingRegulators.set(randomIndex, "or");
			}
		}
	}

	public void mutateRandomInhibitoryOperator() {
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

	public void shuffleRandomRegulatorPriority() {
		if (randInt(0, 1) == 1) {
			shuffleRandomActivatingRegulatorPriority();
		} else {
			shuffleRandomInhibitoryRegulatorPriority();
		}
	}

	public void shuffleRandomActivatingRegulatorPriority() {
		if (activatingRegulators.size() > 1) {
			int randomIndex = randInt(0, activatingRegulators.size() - 2);

			String temp = activatingRegulators.get(randomIndex);
			activatingRegulators.set(randomIndex, activatingRegulators.get(randomIndex + 1));
			activatingRegulators.set(randomIndex + 1, temp);
		}
	}

	public void shuffleRandomInhibitoryRegulatorPriority() {
		if (inhibitoryRegulators.size() > 1) {
			int randomIndex = randInt(0, inhibitoryRegulators.size() - 2);

			String temp = inhibitoryRegulators.get(randomIndex);
			inhibitoryRegulators.set(randomIndex, inhibitoryRegulators.get(randomIndex + 1));
			inhibitoryRegulators.set(randomIndex + 1, temp);
		}
	}

	public String[] convertToSifLines() {
		String[] lines = new String[activatingRegulators.size() + inhibitoryRegulators.size()];

		for (int i = 0; i < activatingRegulators.size(); i++)
			lines[i] = activatingRegulators.get(i) + " -> " + target;

		for (int i = 0; i < inhibitoryRegulators.size(); i++)
			lines[i + activatingRegulators.size()] = inhibitoryRegulators.get(i) + " -| " + target;

		return lines;
	}

	public SingleInteraction[] getSingleInteractions() {
		int regulators = activatingRegulators.size() + inhibitoryRegulators.size();

		SingleInteraction[] singleInteractions = new SingleInteraction[regulators];

		for (int i = 0; i < activatingRegulators.size(); i++) {
			singleInteractions[i] = new SingleInteraction(activatingRegulators.get(i), "->", target);
		}

		for (int i = 0; i < inhibitoryRegulators.size(); i++) {
			singleInteractions[i + activatingRegulators.size()] = new SingleInteraction(inhibitoryRegulators.get(i),
					"-|", target);
		}

		return singleInteractions;
	}

	private String getRepeatedString(String string, int repeats) {
		String result = "";
		for (int i = 0; i < repeats; i++)
			result += string;

		return result;
	}

	public int getNumWhitelistedRegulators() {
		return (Collections.frequency(whitelistActivatingRegulators, true)
				+ Collections.frequency(whitelistInhibitoryRegulators, true));
	}

	public int getNumBlacklistedRegulators() {
		return (Collections.frequency(whitelistActivatingRegulators, false)
				+ Collections.frequency(whitelistInhibitoryRegulators, false));
	}

	public int getNumRegulators() {
		return (activatingRegulators.size() + inhibitoryRegulators.size());
	}
	
}
