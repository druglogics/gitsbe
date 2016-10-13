package gitsbe;

public class Launcher {

	public Launcher() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

//		if (args.length == 0)
//		{
//			System.out.print("The  correct  usage would be: Gitsbe [Options] <Interaction file> [Complex file] <Steady state file> [Generations] [Output file]\n") ;
//			System.out.print("Try 'gitsbe --help' for more information\n") ;
//			
//		}
	
		
		String filenameNetwork = args[0];
		String filenameConfig = args[1];
		String[] filenameSteadyStates = {args[2]} ;
		String outputDir = args[3];
		
		Thread t ;
		
		
		// Repeat process 100 times to obtain confidence intervals for each evolutionary run - to be used for publication
  //		for (int j = 0; j < 100; j++)
  //		{
			for (int i = 0; i < filenameSteadyStates.length; i++)
			{
				String filenameSteadyState = filenameSteadyStates[i] ;
				t = new Thread (new Gitsbe (filenameNetwork, filenameSteadyState, filenameConfig, outputDir)) ;
				
				t.start();
				try {
					t.join ();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//		}
		
		
	}

}
