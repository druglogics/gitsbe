package eu.druglogics.gitsbe.util;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

/**
 * A (very) simple logger
 * 
 * @author Asmund Flobak
 * 
 *         email: asmund.flobak@ntnu.no
 *
 */

public class Logger {

	private PrintWriter writerOutput;

	private boolean consoleOutput;
	private boolean debugMode;
	private int verbosity;

	/**
	 * Initialize logger
	 * 
	 * @param filenameOutput
	 *            name of output file. Verbosity decides amount of output.
	 * @param directory
	 *            is name of directory for output.
	 * @param verbosity
	 *            sets level of verbosity: 0 is nothing at all, 1 = most important
	 *            info, 2 = less important, 3 = everything
	 * @throws IOException
	 */
	public Logger(String filenameOutput, String directory, int verbosity, boolean consoleOutput)
			throws IOException {

		// Initialize variables
		String loggerFilename = new File(directory, filenameOutput).getAbsolutePath();

		// Initialize file writers
		this.writerOutput = new PrintWriter(loggerFilename);

		this.setVerbosity(verbosity);
		this.consoleOutput = consoleOutput;
		this.debugMode = false;

		outputStringMessage(1, "Logger started, logging to directory: " + directory);
	}

	public void finish() {
		writerOutput.close();
	}

	/**
	 * Set level of verbosity, 0 = silent, 1 = some, 2 = more, 3 = all
	 * 
	 * @param verbosity
	 */
	public void setVerbosity(int verbosity) {
		if (verbosity >= 0 && verbosity <= 3) {
			this.verbosity = verbosity;
		} else {
			this.verbosity = 3;
		}
	}

	/**
	 * 
	 * @return current verbosity level
	 */
	public int getVerbosity() {
		return verbosity;
	}

	/**
	 * Output message
	 * 
	 * @param verbosity
	 * @param msg
	 */
	public void outputStringMessage(int verbosity, String msg) {
		if (verbosity <= this.verbosity) {
			writerOutput.println(msg);
			writerOutput.flush();
			if (consoleOutput)
				System.out.println(msg);
		}
	}

	/**
	 * Output several lines
	 * 
	 * @param verbosity
	 * @param msg
	 */
	public void outputLines(int verbosity, String[] msg) {
		if (verbosity <= this.verbosity) {
			for (String line : msg) {
				writerOutput.println(line);
				if (consoleOutput)
					System.out.println(line);
			}
			writerOutput.flush();
		}
	}

	/**
	 * Output lines to specified file. Ignores verbosity-level
	 * 
	 * @param filename
	 * @param msg
	 * @throws FileNotFoundException
	 */
	public void outputLinesToFile(String filename, String[] msg) {
		try {
			FileWriter fw = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter writer = new PrintWriter(bw);

			for (String line : msg) {
				writer.println(line);
				if (consoleOutput)
					System.out.println(line);
			}

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Output line to specified file. Ignores verbosity-level
	 * 
	 * @param filename
	 * @param msg
	 * @throws FileNotFoundException
	 */
	public void outputStringMessageToFile(String filename, String msg) {
		try {
			FileWriter fw = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter writer = new PrintWriter(bw);

			writer.println(msg);
			if (consoleOutput)
				System.out.println(msg);

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Output section header
	 * 
	 * @param verbosity
	 * @param msg
	 */
	public void outputHeader(int verbosity, String msg) {
		if (verbosity <= this.verbosity) {
			writerOutput.println("\n" + msg);
			writerOutput.println(dashes(msg.length()));

			if (consoleOutput) {
				System.out.println("\n" + msg);
				System.out.println(dashes(msg.length()));
			}

		}
	}

	public void outputHeaderToFile(String filename, String msg) {
		try {
			FileWriter fw = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter writer = new PrintWriter(bw);

			writer.println("\n" + msg);
			writer.println(dashes(msg.length()));

			if (consoleOutput)
				System.out.println(msg);

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get string with specified number of dashes
	 * 
	 * @param length
	 * @return
	 */
	private String dashes(int length) {
		char[] dashes = new char[length];
		Arrays.fill(dashes, '-');
		return new String(dashes);
	}

	/**
	 * Extra output to log if debug mode is active
	 * 
	 * @param msg
	 */
	public void debug(String msg) {
		if (debugMode)
			outputStringMessage(1, "DEBUG: " + msg);
	}

	/**
	 * Set debug mode
	 * 
	 * @param mode
	 */
	public void setDebug(boolean mode) {
		debugMode = mode;
	}

	/**
	 * Outputs the specified error message ignoring verbosity level
	 * 
	 * @param message
	 */
	public void error(String message) {
		String errorMessage = "ERROR: " + message;
		writerOutput.println(errorMessage);
		writerOutput.flush();
		if (consoleOutput)
			System.out.println(errorMessage);
	}

	public void writeFirstLoggingMessage(String appName, String version) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar calendarData = Calendar.getInstance();

		this.outputHeader(1, appName + " " + version);
		this.outputStringMessage(1, "Start: " + dateFormat.format(calendarData.getTime()));
	}

	public void writeLastLoggingMessage(Timer timer) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar calendarData = Calendar.getInstance();

		this.outputStringMessage(1, "End: " + dateFormat.format(calendarData.getTime()));
		this.outputStringMessage(1, "Analysis completed in "
				+ timer.getHoursOfDuration() + " hours, "
				+ timer.getMinutesOfDuration() + " minutes, and "
				+ timer.getSecondsOfDuration() + " seconds ");
		this.outputStringMessage(1, "\nWith that we say thank you and good bye!");
		this.finish();
	}
}
