package gitsbe;

import java.io.File;

public class Launcher {

	public Launcher() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

		if (args.length == 0)
		{
			System.out.print("The  correct  usage would be: Gitsbe [Options] <Interaction file> [Complex file] <Steady state file> [Generations] [Output file]\n") ;
			System.out.print("Try 'gitsbe --help' for more information\n") ;
			
		}
	
		// 20151007 - 85 cell lines. Steady state from PARADIGM with 0.3x SD cutoff. Effectors set in growth promoting state
		// Model2 used for simulations
		// Missing cell lines (Not in F. Iorio dataset): MDA-MB-175-VII and NCI-H1437
		
		String filenameNetwork = "20150918_DREAM10_model2.sif" ;
		String filenameConfig = "20151007_DREAM10_Model2_85_cell_lines_config.tab" ;
		String filenameSteadyState = "" ;
		
		String[] filenameSteadyStates = {
				"22RV1.tab",
				"647-V.tab",
				"A549.tab",
				"BFTC-905.tab",
				"BT-20.tab",
				"BT-474.tab",
				"BT-549.tab",
				"C32.tab",
				"CAL-51.tab",
				"CAL-120.tab",
				"CAL-148.tab",
				"Calu-3.tab",
				"Calu-6.tab",
				"CAMA-1.tab",
				"COLO-205.tab",
				"DMS-114.tab",
				"DU-4475.tab",
				"EVSA-T.tab",
				"HCC38.tab",
				"HCC70.tab",
				"HCC1143.tab",
				"HCC1187.tab",
				"HCC1395.tab",
				"HCC1419.tab",
				"HCC1428.tab",
				"HCC1500.tab",
				"HCC1569.tab",
				"HCC1806.tab",
				"HCC1937.tab",
				"HCC1954.tab",
				"HCT-116.tab",
				"Hs-578-T.tab",
				"HT-29.tab",
				"HT-1197.tab",
				"HT-1376.tab",
				"J82.tab",
				"KATOIII.tab",
				"KMS-11.tab",
				"KU-19-19.tab",
				"LS-513.tab",
				"M14.tab",
				"MCF7.tab",
				"MDA-MB-157.tab",
				"MDA-MB-231.tab",
				"MDA-MB-415.tab",
				"MDA-MB-436.tab",
				"MDA-MB-453.tab",
				"MDA-MB-468.tab",
				"MFM-223.tab",
				"NCI-H23.tab",
				"NCI-H226.tab",
				"NCI-H358.tab",
				"NCI-H520.tab",
				"NCI-H522.tab",
				"NCI-H747.tab",
				"NCI-H838.tab",
				"NCI-H1299.tab",
				"NCI-H1563.tab",
				"NCI-H1703.tab",
				"NCI-H1793.tab",
				"NCI-H1975.tab",
				"NCI-H2085.tab",
				"NCI-H2170.tab",
				"NCI-H2228.tab",
				"NCI-H2291.tab",
				"NCI-H3122.tab",
				"NCI-SNU-16.tab",
				"RKO.tab",
				"RT4.tab",
				"SW48.tab",
				"SW620.tab",
				"SW780.tab",
				"SW837.tab",
				"SW900.tab",
				"SW948.tab",
				"T47D.tab",
				"T-24.tab",
				"TCCSUP.tab",
				"UACC-812.tab",
				"UM-UC-3.tab",
				"VCaP.tab",
				"VM-CUB-1.tab",
				"MDA-MB-175-VII.tab",
				"MDA-MB-361.tab",
				"NCI-H1437.tab"
		} ;
		
		
		
//		filenameNetwork = "" ;
//		filenameConfig = "" ;
//		String[] filenameSteadyStates = {""} ;
		
		Thread t ;
		
		
		// Repeat process 100 times to obtain confidence intervals for each evolutionary run - to be used for publication
		for (int j = 0; j < 100; j++)
		{
			for (int i = 0; i < filenameSteadyStates.length; i++)
			{
				filenameSteadyState = filenameSteadyStates[i] ;
				t = new Thread (new Gitsbe (filenameNetwork, filenameSteadyState, filenameConfig)) ;
				
				t.start();
				try {
					t.join ();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		filenameSteadyState = "" ;
		
		
	}

}
