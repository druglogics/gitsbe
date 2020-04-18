package eu.druglogics.gitsbe.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;

import static eu.druglogics.gitsbe.util.Util.isStringAllDashes;

public class MPBNPythonScriptExecuter {

	private static String directoryMPBN = System.getenv("MPBN_HOME");
	public static BufferedReader pythonToJavaReader;
	public static PrintWriter javaToPythonWriter;
	public static Process process;

	public MPBNPythonScriptExecuter() throws Exception {
		String MPBNScriptFile = new File(directoryMPBN, "mpbn-attractors.py").getAbsolutePath();
		ProcessBuilder pb = new ProcessBuilder("python", MPBNScriptFile);
		pb.redirectErrorStream(true);

		// Start mpbn-attractors.py process
		process = pb.start();

		//pythonToJavaReader = getOutputReader(process); // from python to java
		//javaToPythonWriter = getInputWriter(process); // from java to python
	}

	 // Python => Java
	public static BufferedReader getOutputReader(Process process) {
		return new BufferedReader(new InputStreamReader(process.getInputStream()));
	}

	// Java => Python
	public static PrintWriter getInputWriter(Process process) {
		return new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
	}

	synchronized public static ArrayList<String> getAttractors(String boolNetFilename, Logger logger) throws IOException {
		// send .boolnet file argument to python script
		javaToPythonWriter.println(boolNetFilename + '\n');
		//javaToPythonWriter.flush();
		System.out.println("HIIIII");

		// get attractors back one by one
		ArrayList<String> attractors = new ArrayList<>();

		int count = 0;
		String line;
		while (!(line = pythonToJavaReader.readLine()).equals("END")) {
			System.out.println("Nigga you in?");
			String trapSpace = StringUtils.replace(line, "*", "-");
			if (isStringAllDashes(trapSpace)) {
				logger.outputStringMessage(2, "Found trivial trapspace (all dashes) which will be ignored");
			} else {
				attractors.add(trapSpace);
				logger.outputStringMessage(2, "Trapspace " + (++count) + ": " + attractors.get(count-1));
			}
		}

		if (attractors.size() > 0) {
			logger.outputStringMessage(1, "MPBN found " + attractors.size() + " trapspaces.");
		} else {
			logger.outputStringMessage(1, "MPBN found no trapspaces.");
		}

		return attractors;
	}
}
