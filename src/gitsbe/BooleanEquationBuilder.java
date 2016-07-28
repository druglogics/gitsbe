package gitsbe;

import java.util.ArrayList;


public class BooleanEquationBuilder {

	
	private String target ;
	
	private ArrayList activatingRegulators ;
	
	private ArrayList inhibitoryRegulators ;
	
	private ArrayList operatorsActivatingRegulators ;
	private ArrayList operatorsInhibitoryRegulators ;
	
	private Operators operatorLink ;
	
	// BooleanNet
	private String OR = " or " ;
	private String AND = " and " ;
	private String NOT = "  not " ;
	private String EQUALS = " = " ;
	private String SPACE = " " ;
	private String LINK = " and " ;
	private String PARANTHESISSTART = " ( " ;
	private String PARANTHESISEND = " ) " ;
	private String LINKVC = " & " ;
	
	public BooleanEquationBuilder(MultipleInteraction multipleInteraction) {
		// Build expression
		target = multipleInteraction.getTarget() ;
		
		activatingRegulators = new ArrayList<String> ();
		inhibitoryRegulators = new ArrayList<String> ();
		
		operatorsActivatingRegulators = new ArrayList<String> ();
		operatorsInhibitoryRegulators = new ArrayList<String> () ;
		
		operatorLink = Operators.OR ;
		
		
		ArrayList tempActivatingRegulators = multipleInteraction.getActivatingRegulators() ;
		
		for (int i = 0; i < tempActivatingRegulators.size(); i++)
		{
			activatingRegulators.add(tempActivatingRegulators.get(i)) ;
			
			if (i < tempActivatingRegulators.size() - 1)
			{
				operatorsActivatingRegulators.add(OR);
			}
		}
		
		
		ArrayList tempInhibitoryRegulators = multipleInteraction.getInhibitoryRegulators() ;
		
		for (int i = 0; i < tempInhibitoryRegulators.size(); i++)
		{
			inhibitoryRegulators.add(tempInhibitoryRegulators.get(i)) ;
			if (i < tempInhibitoryRegulators.size() - 1)
			{
				operatorsInhibitoryRegulators.add(OR) ;
			}
		}
		
		
	}
	
	public String getBooleanEquation ()
	{
		return getBooleanEquationBooleannet () ;
	}
	
	public String getBooleanEquationBooleannet ()
	{
		// BooleanNet
		OR = " or " ;
		AND = " and " ;
		NOT = " not " ;
		EQUALS = " = " ;
		SPACE = " " ;
		PARANTHESISSTART = " ( " ;
		PARANTHESISEND = " ) " ;
		
		// Make string of activators
				String activators = PARANTHESISSTART ;
				for (int i = 0 ; i < activatingRegulators.size() ; i++)
				{
					activators = activators + activatingRegulators.get(i) ;
					if (i < activatingRegulators.size() - 1)
					{
						activators = activators + operatorsActivatingRegulators.get(i) ;
					}
				}
				activators = activators + PARANTHESISEND ;
				
				// Make string of inhibitors
				String inhibitors = PARANTHESISSTART ;
				for (int i = 0 ; i < inhibitoryRegulators.size() ; i++)
				{
					inhibitors = inhibitors + inhibitoryRegulators.get(i) ;
					if (i < inhibitoryRegulators.size() - 1)
					{
						inhibitors = inhibitors + operatorsInhibitoryRegulators.get(i) ;
					}
				}
				inhibitors = inhibitors + PARANTHESISEND ;
				
				// Return equation of type: Target = (Activator1 or Activator2) and not (Inhibitor1 or Inhibitor2)
				String equation = target + " *= " ;
				
				
				if (activatingRegulators.size() > 0)
				{
					equation = equation + activators ;
				}
				
				if (inhibitoryRegulators.size() > 0)
				{
					if (activatingRegulators.size() > 0)
					{
						equation = equation + LINK ;
					}
					equation = equation + NOT + inhibitors;
				
				}
				
				
				return " " + equation.trim() + " " ;
	}
	
	public String getBooleanEquationVelizCuba ()
	{
		// BooleanNet
		OR = " | " ;
		AND = " & " ;
		NOT = " ! " ;
		EQUALS = " = " ;
		SPACE = " " ;
		PARANTHESISSTART = " ( " ;
		PARANTHESISEND = " ) " ;
		LINK = " & " ;
		
		
		
		
		
		// Make string of activators
		String activators = PARANTHESISSTART ;
		for (int i = 0 ; i < activatingRegulators.size() ; i++)
		{
			activators = activators + activatingRegulators.get(i) ;
			if (i < activatingRegulators.size() - 1)
			{
				activators = activators + operatorsActivatingRegulators.get(i) ;
			}
		}
		activators = activators + PARANTHESISEND ;
		
		// Make string of inhibitors
		String inhibitors = PARANTHESISSTART ;
		for (int i = 0 ; i < inhibitoryRegulators.size() ; i++)
		{
			inhibitors = inhibitors + inhibitoryRegulators.get(i) ;
			if (i < inhibitoryRegulators.size() - 1)
			{
				inhibitors = inhibitors + operatorsInhibitoryRegulators.get(i) ;
			}
		}
		inhibitors = inhibitors + PARANTHESISEND ;
		
		// Return equation of type: Target = (Activator1 or Activator2) and not (Inhibitor1 or Inhibitor2)
		String equation = "" ;
		
//						String equation = "" ;
		
		if (activatingRegulators.size() > 0)
		{
			equation = equation + activators ;
		}
		
		if (inhibitoryRegulators.size() > 0)
		{
			if (activatingRegulators.size() > 0)
			{
				equation = equation + LINK ;
			}
			equation = equation + NOT + inhibitors;
		
		}
		return equation.trim() ;
	}
}
