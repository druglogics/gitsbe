package eu.druglogics.gitsbe;

import static eu.druglogics.gitsbe.util.Util.*;

public class Launcher {

	public static void main(String[] args) {
		Launcher gitsbeLauncher = new Launcher();
		gitsbeLauncher.start(args);
	}

	public void start(String[] args) {
		if (environmentalVariableBNETisNULL())
			return;
		setupInputAndRun(args);
	}

	private void setupInputAndRun(String[] args) {
		if (args.length == 0) {
			System.out.println("No user arguments supplied");
			System.out.println(
					"Usage: gitsbe <name project> <filename network> <filename training data> <filename config file> <filename model outputs>");
			System.out.println("\nExample Testrun (command line usage): ");

			String[] test_args = new String[] { "example_run_ags", "toy_ags_network.sif", "toy_ags_training_data.tab",
					"toy_ags_config.tab", "toy_ags_modeloutputs.tab" };
			System.out.println("java -cp /pathToBinFolderOfGitsbeProject gitsbe.Launcher " + test_args[0] + " "
					+ test_args[1] + " " + test_args[2] + " " + test_args[3] + " " + test_args[4] + "\n\n");
		} else {
			String nameProject = args[0];
			String filenameNetwork = args[1];
			String[] filenameTrainingData = { args[2] };
			String filenameConfig = args[3];
			String filenameModelOutputs = args[4];

			Thread t;
			
			for (int i = 0; i < filenameTrainingData.length; i++) {
				t = new Thread(new Gitsbe(nameProject, filenameNetwork, filenameTrainingData[i], filenameModelOutputs,
						filenameConfig));
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}
}
