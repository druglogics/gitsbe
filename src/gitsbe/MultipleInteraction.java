package gitsbe;

import java.util.ArrayList;


public class MultipleInteraction {

	private String target ;
	
	// List of regulators acting independently
	private ArrayList activatingRegulators ;
	private ArrayList inhibitoryRegulators ;
	
	// List of regulators acting together (complexes)
	private ArrayList<String[]> activatingRegulatorComplexes ;
	private ArrayList<String[]> inhibitoryRegulatorComplexes ;
	
	public MultipleInteraction(String targetName) {
		// Define target upon creation
		target = targetName ;
		
		activatingRegulators = new ArrayList ();
		inhibitoryRegulators = new ArrayList ();
		activatingRegulatorComplexes = (ArrayList<String[]>) new ArrayList () ;
		inhibitoryRegulatorComplexes = (ArrayList<String[]>) new ArrayList ();
	}
	
//	ArrayList<MutatedBooleanModel>[] mutatedModels = (ArrayList<MutatedBooleanModel>[])new ArrayList[numGenerations];
	
	public void addActivatingRegulator (String regulator)
	{
		activatingRegulators.add(regulator);
	}
	
	public void addInhibitoryRegulator (String regulator)
	{
		inhibitoryRegulators.add(regulator);
		
	}
	
	public void addActivatingRegulatorComplex (String[] regulators)
	{
		activatingRegulatorComplexes.add(regulators) ;
	}

	public void addInhibitoryRegulatorComplex (String[] regulators)
	{
		inhibitoryRegulatorComplexes.add(regulators);
		
	}
	
	public String getTarget() {
		return target;
	}

	public ArrayList<String> getActivatingRegulators() {
		return activatingRegulators;
	}

	public ArrayList<String> getInhibitoryRegulators() {
		return inhibitoryRegulators;
	}
	
	public ArrayList <String[]> getActivatingRegulatorComplexes () {
		return activatingRegulatorComplexes ;
	}
	
	public ArrayList <String[]> getInhibitoryRegulatorComplexes () {
		return inhibitoryRegulatorComplexes ;
		
	}
	
	
	public String toString ()
	{
		return target + " <- " + activatingRegulators.toString() + activatingRegulatorComplexes + " ! " + inhibitoryRegulators.toString() + inhibitoryRegulatorComplexes ;		
	
	}
	
}
