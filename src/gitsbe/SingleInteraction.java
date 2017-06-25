package gitsbe;

/**
 * 
 * @author Asmund Flobak, email: asmund.flobak@ntnu.no
 *
 */

public class SingleInteraction {

	private String source ;
	private String target ;
	private int arc ;
	
	/**
	 * A SingleInteraction can be initialized with a space-separated string of one node, one interaction and one node
	 * 
	 * @param firstNode
	 * @param interaction
	 * @param secondNode
	 */
	public SingleInteraction(String firstNode, String interaction, String secondNode) {
		this.setInteraction(firstNode, interaction, secondNode);
	}
	
	public SingleInteraction (String interaction)
	{
		this.setInteraction(interaction);
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param interaction
	 */
	public SingleInteraction (SingleInteraction interaction)
	{
		this.source = interaction.source ;
		this.target = interaction.target ;
		this.arc = interaction.arc ;
	}
	
	public void setInteraction (String interaction){

		// String split by spaces

//		logger.output(3, interaction);

		String[] temp = interaction.split(" ", 3) ;
		
		this.setInteraction(temp[0], temp[1], temp[2]) ;
				
	}
	
	public void setInteraction(String firstNode, String interaction, String secondNode) {
		
		switch (interaction) {
		case "activate" :
		case "activates" :
		case "->":
			arc = 1;
			source = firstNode ;
			target = secondNode ;
			break;
		case "inhibit":
		case "inhibits":
		case "-|":
			arc = -1;
			source = firstNode ;
			target = secondNode ;
			break;
		case "<-":
			arc = 1 ;
			source = secondNode ;
			target = firstNode ;
			break ;
		case "|-":
			arc = -1 ;
			source = secondNode ;
			target = firstNode ;
			break ;
		case "|->":
		case "<->":
		case "<-|":
		case "|-|":
			// 
//			Logger.debug("\nERROR: double interaction encountered: " + firstNode + interaction + secondNode + "\n");
		}
	}
	public String toString (){
		return source + " " + arc + " " + target ;
	}
	
	public String getInteraction ()
	{
		String link ;
		
		if (arc == 1)
			link = "->" ;
		else
			link = "-|" ;
		
		return source + " " + link + " " + target ;
	}
	
	public String getSource ()
	{
		return source ;
	}
	
	public String getTarget ()
	{
		return target ;
	}
	
	public int getArc ()
	{
		return arc ;
		
	}

}
