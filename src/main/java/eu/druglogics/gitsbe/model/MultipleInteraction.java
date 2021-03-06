package eu.druglogics.gitsbe.model;

import java.util.ArrayList;

public class MultipleInteraction {

	private String target;

	// List of regulators acting independently
	private ArrayList<String> activatingRegulators;
	private ArrayList<String> inhibitoryRegulators;

	// List of regulators acting together (complexes)
	private ArrayList<String[]> activatingRegulatorComplexes;
	private ArrayList<String[]> inhibitoryRegulatorComplexes;

	MultipleInteraction(String targetName) {
		// Define target upon creation
		target = targetName;

		activatingRegulators = new ArrayList<>();
		inhibitoryRegulators = new ArrayList<>();
		activatingRegulatorComplexes = new ArrayList<>();
		inhibitoryRegulatorComplexes = new ArrayList<>();
	}

	void addActivatingRegulator(String regulator) {
		activatingRegulators.add(regulator);
	}

	void addInhibitoryRegulator(String regulator) {
		inhibitoryRegulators.add(regulator);
	}

	public void addActivatingRegulatorComplex(String[] regulators) {
		activatingRegulatorComplexes.add(regulators);
	}

	public void addInhibitoryRegulatorComplex(String[] regulators) {
		inhibitoryRegulatorComplexes.add(regulators);
	}

	public String getTarget() {
		return target;
	}

	ArrayList<String> getActivatingRegulators() {
		return activatingRegulators;
	}

	ArrayList<String> getInhibitoryRegulators() {
		return inhibitoryRegulators;
	}

	public ArrayList<String[]> getActivatingRegulatorComplexes() {
		return activatingRegulatorComplexes;
	}

	public ArrayList<String[]> getInhibitoryRegulatorComplexes() {
		return inhibitoryRegulatorComplexes;
	}

	@Override
	public String toString() {
		return target + " <- " + activatingRegulators.toString()
				+ activatingRegulatorComplexes + " ! "
				+ inhibitoryRegulators.toString()
				+ inhibitoryRegulatorComplexes;
	}

}
