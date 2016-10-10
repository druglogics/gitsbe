package gitsbe;

import java.util.ArrayList;
import java.util.Arrays;

public class BooleanEquation {

	// String target name
	private String target;

	// Strings regulators
	private ArrayList<String> activatingRegulators;
	private ArrayList<String> inhibitoryRegulators;

	// Strings operators
	private ArrayList<String> operatorsActivatingRegulators;
	private ArrayList<String> operatorsInhibitoryRegulators;

	// String link activators and inhibitors ("and not"/"or not")
	private String link;

	private BooleanEquation() {

		// Initialize variables

		activatingRegulators = new ArrayList<String>();
		inhibitoryRegulators = new ArrayList<String>();

		operatorsActivatingRegulators = new ArrayList<String>();
		operatorsInhibitoryRegulators = new ArrayList<String>();

	}

	/**
	 * Copy constructor
	 * 
	 * @param originalEquation
	 */
	public BooleanEquation(BooleanEquation originalEquation) {
		// public static List<Dog> cloneList(List<Dog> dogList) {
		// List<Dog> clonedList = new ArrayList<Dog>(dogList.size());
		// for (Dog dog : dogList) {
		// clonedList.add(new Dog(dog));
		// }
		// return clonedList;
		// }

		this();

		for (int i = 0; i < originalEquation.operatorsActivatingRegulators
				.size(); i++) {
			this.operatorsActivatingRegulators.add(new String(
					originalEquation.operatorsActivatingRegulators.get(i)));
		}

		for (int i = 0; i < originalEquation.inhibitoryRegulators.size(); i++) {
			this.inhibitoryRegulators.add(new String(
					originalEquation.inhibitoryRegulators.get(i)));
		}

		for (int i = 0; i < originalEquation.activatingRegulators.size(); i++) {
			this.activatingRegulators.add(new String(
					originalEquation.activatingRegulators.get(i)));
		}

		for (int i = 0; i < originalEquation.operatorsInhibitoryRegulators
				.size(); i++) {
			this.operatorsInhibitoryRegulators.add(new String(
					originalEquation.operatorsInhibitoryRegulators.get(i)));
		}

		this.link = new String(originalEquation.link);

		this.target = new String(originalEquation.target);
	}

	public BooleanEquation(MultipleInteraction multipleInteraction) {
		this();

		// Build expression
		target = multipleInteraction.getTarget();

		ArrayList<String> tempActivatingRegulators = multipleInteraction
				.getActivatingRegulators();

		for (int i = 0; i < tempActivatingRegulators.size(); i++) {
			activatingRegulators.add(tempActivatingRegulators.get(i));

			if (i < tempActivatingRegulators.size() - 1) {
				operatorsActivatingRegulators.add("or");
			}
		}

		ArrayList<String> tempInhibitoryRegulators = multipleInteraction
				.getInhibitoryRegulators();

		for (int i = 0; i < tempInhibitoryRegulators.size(); i++) {
			inhibitoryRegulators.add(tempInhibitoryRegulators.get(i));
			if (i < tempInhibitoryRegulators.size() - 1) {
				operatorsInhibitoryRegulators.add("or");
			}
		}

		link = "and";
	}

	/**
	 * Build equation from Boolean expression. Currently expressions must be of
	 * the following type: A *= (((B) or C) or D) and not (((E) or F) or G)
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
		ArrayList<String> splitequation = new ArrayList<String>(
				Arrays.asList(equation.split(" ")));

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
				if (beforeNot)
					activatingRegulators.add(element);
				else
					inhibitoryRegulators.add(element);
			}
		} while (splitequation.size() > 0);

	}

	public String getBooleanEquation() {
		String equation = target + " *= ";

		// Add activating regulators
		if (activatingRegulators.size() > 0) {
			equation += getString(" ( ", activatingRegulators.size());
			equation += " ";

			for (int i = 0; i < activatingRegulators.size(); i++) {
				if (i > 0)
					equation += " " + operatorsActivatingRegulators.get(i - 1)
							+ " ";

				equation += activatingRegulators.get(i) + " ) ";
			}

			// equation += ")" ;
		}

		// Find correct link for activators and inhibitory regulators
		if ((activatingRegulators.size() > 0)
				&& (inhibitoryRegulators.size() > 0))
			equation += link;

		if (inhibitoryRegulators.size() > 0) {
			equation += " not ";

			equation += getString(" ( ", inhibitoryRegulators.size());
			// equation += " " ;

			for (int i = 0; i < inhibitoryRegulators.size(); i++) {
				if (i > 0)
					equation += " " + operatorsInhibitoryRegulators.get(i - 1)
							+ " ";

				equation += inhibitoryRegulators.get(i) + " ) ";
			}

			// equation += ")" ;
		}

		// BclxL *= (((NFKB) or STAT3)) and not ((((BID) or GZMB) or DISC))
		// BclxL *= ((NFKB) or STAT3) and not (((BID) or GZMB) or DISC)

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
		if (Gitsbe.randInt(0, 1) == 1) {
			mutateRandomActivatoryOperator();
		} else {
			mutateRandomInhibitoryOperator();
		}
	}

	public void mutateRandomOperator(int mutations) {
		for (int i = 0; i < mutations; i++) {
			mutateRandomOperator();
		}
	}

	public void mutateRandomActivatoryOperator() {
		if (operatorsActivatingRegulators.size() > 0) {
			int randomIndex = Gitsbe.randInt(0,
					operatorsActivatingRegulators.size() - 1);

			if (operatorsActivatingRegulators.get(randomIndex).trim()
					.equals("or")) {
				operatorsActivatingRegulators.set(randomIndex, " and ");
			} else {
				operatorsActivatingRegulators.set(randomIndex, "or");
			}
		}
	}

	public void mutateRandomInhibitoryOperator() {
		if (operatorsInhibitoryRegulators.size() > 0) {
			int randomIndex = Gitsbe.randInt(0,
					operatorsInhibitoryRegulators.size() - 1);

			if (operatorsInhibitoryRegulators.get(randomIndex).trim()
					.equals("or")) {
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
		if (Gitsbe.randInt(0, 1) == 1) {
			shuffleRandomActivatingRegulatorPriority();
		} else {
			shuffleRandomInhibitoryRegulatorPriority();
		}
	}

	public void shuffleRandomActivatingRegulatorPriority() {
		if (activatingRegulators.size() > 1) {
			int randomIndex = Gitsbe
					.randInt(0, activatingRegulators.size() - 2);

			String temp = activatingRegulators.get(randomIndex);
			activatingRegulators.set(randomIndex,
					activatingRegulators.get(randomIndex + 1));
			activatingRegulators.set(randomIndex + 1, temp);
		}
	}

	public void shuffleRandomInhibitoryRegulatorPriority() {
		if (inhibitoryRegulators.size() > 1) {
			int randomIndex = Gitsbe
					.randInt(0, inhibitoryRegulators.size() - 2);

			String temp = inhibitoryRegulators.get(randomIndex);
			inhibitoryRegulators.set(randomIndex,
					inhibitoryRegulators.get(randomIndex + 1));
			inhibitoryRegulators.set(randomIndex + 1, temp);
		}
	}

	public String[] convertToSifLines() {
		String[] lines = new String[activatingRegulators.size()
				+ inhibitoryRegulators.size()];

		for (int i = 0; i < activatingRegulators.size(); i++)
			lines[i] = activatingRegulators.get(i) + " -> " + target;

		for (int i = 0; i < inhibitoryRegulators.size(); i++)
			lines[i + activatingRegulators.size()] = inhibitoryRegulators
					.get(i) + " -| " + target;

		return lines;
	}

	public SingleInteraction[] getSingleInteractions() {
		int regulators = activatingRegulators.size()
				+ inhibitoryRegulators.size();

		SingleInteraction[] singleInteractions = new SingleInteraction[regulators];

		for (int i = 0; i < activatingRegulators.size(); i++) {
			singleInteractions[i] = new SingleInteraction(
					activatingRegulators.get(i), "->", target);
		}

		for (int i = 0; i < inhibitoryRegulators.size(); i++) {
			singleInteractions[i + activatingRegulators.size()] = new SingleInteraction(
					inhibitoryRegulators.get(i), "-|", target);
		}

		return singleInteractions;
	}

	private String getString(String string, int repeats) {
		String result = "";
		for (int i = 0; i < repeats; i++)
			result += string;

		return result;
	}

	private String getChars(char character, int length) {
		char[] chars = new char[length];
		Arrays.fill(chars, character);
		return new String(chars);
	}
}
