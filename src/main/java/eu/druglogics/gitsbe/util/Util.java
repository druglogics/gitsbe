package eu.druglogics.gitsbe.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * A simple class that has static methods useful for the whole project
 * 
 * @author John
 */

public class Util {
	/**
	 * Checks if the BNET_HOME environment variable is set
	 *
	 * @throws Exception
	 */
	public static void checkBNET() throws Exception {
		if (System.getenv("BNET_HOME") == null) {
			throw new Exception("Set environment variable BNET_HOME to point to location of " +
					"BNReduction.sh (see druglogics_dep installation guidelines)");
		}
	}

	/**
	 * Makes the directory path absolute (if it is not already)
	 * 
	 * @param directory
	 */
	public static String makeDirectoryPathAbsolute(String directory) {
		if ((directory.length() > 0) && !(new File(directory).isAbsolute()))
			directory = new File(System.getProperty("user.dir"), directory).getAbsolutePath();
		return directory;
	}

	/**
	 * Reads lines from a file. It may skip the empty ones and the ones starting
	 * with the character '#' (comments) based on the value of
	 * skipEmptyLinesAndComments
	 * 
	 * @param filename
	 * @param skipEmptyLinesAndComments
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> readLinesFromFile(String filename, boolean skipEmptyLinesAndComments)
			throws IOException {
		ArrayList<String> lines = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			while (true) {
				String line = reader.readLine();
				// no more lines to read
				if (line == null) {
					reader.close();
					break;
				}

				if (skipEmptyLinesAndComments) {
					if ((!line.startsWith("#")) & line.length() > 0) {
						lines.add(line);
					}
				} else { // add any line no matter what
					lines.add(line);
				}
			}
		}

		return lines;
	}

	/**
	 * Create a directory based on given path string and output message in the
	 * logger
	 * 
	 * @param directory
	 * @param logger
	 * @return
	 */
	public static void createDirectory(String directory, Logger logger) throws IOException {
		File directoryFile = new File(directory);

		if (directoryFile.exists()) {
			logger.outputStringMessage(1,"Directory: " + directory +
					" already exists - " + "no need to recreate it!");
			return;
		}

		if (!directoryFile.mkdir()) {
			if (!directoryFile.exists()) {
				throw new IOException("Error in creating directory: " +
						directory + ", exiting.");
			}
		} else {
			logger.outputStringMessage(1, "Created directory: " +
					directory);
		}
	}
	
	/**
	 * Create a directory based on the given path string
	 * 
	 * @param directory
	 */
	public static void createDirectory(String directory) throws IOException {
		File directoryFile = new File(directory);
		
		if (directoryFile.exists()) {
			System.out.println("Directory: " + directory + " already exists - " +
					"no need to recreate it!");
			return;
		}
		
		if (!directoryFile.mkdir()) {
			if (!directoryFile.exists()) {
				throw new IOException("Error in creating directory: " +
						directory + ", exiting.");
			}
		} else {
			System.out.println("Created directory: " + directory);
		}
	}

	public static String inferInputDir(String filename) {
		String inputDirectory;
		String parentDir = new File(filename).getParent();
		if (parentDir != null) {
			inputDirectory = new File(parentDir).getAbsolutePath();
		} else {
			inputDirectory =  new File(System.getProperty("user.dir")).getAbsolutePath();
		}
		return inputDirectory;
	}

	/**
	 * removes extension of string, author: coobird
	 * (http://stackoverflow.com/users/17172/coobird)
	 * 
	 * @param str
	 *            (filename)
	 * 
	 * @return filename without extension
	 */
	public static String removeExtension(String str) {

		String separator = System.getProperty("file.separator");
		String filename;

		// Remove the path up to the filename
		int lastSeparatorIndex = str.lastIndexOf(separator);
		if (lastSeparatorIndex == -1) {
			filename = str;
		} else {
			filename = str.substring(lastSeparatorIndex + 1);
		}

		// Remove the extension
		int extensionIndex = filename.lastIndexOf(".");
		if (extensionIndex == -1)
			return filename;

		return filename.substring(0, extensionIndex);
	}

	/**
	 * Merges all files to the mergedFile file and deletes them
	 * 
	 * @param files
	 * @param mergedFile
	 * @throws IOException
	 */
	public static void mergeFiles(ArrayList<String> files, String mergedFile) throws IOException {
		PrintWriter writerOutput = new PrintWriter(mergedFile);

		for (String file : files) {
			ArrayList<String> lines = readLinesFromFile(file, false);
			for (String line : lines) {
				writerOutput.println(line);
			}
			writerOutput.flush();

			File fileToDelete = new File(file);
			if (!fileToDelete.delete()) {
				throw new IOException("Couldn't delete file: " + file);
			}
		}
		writerOutput.close();
	}

	/**
	 * Sort files based on an arithmetic value they contain as strings
	 * 
	 * @param files
	 */
	public static void sortFiles(ArrayList<String> files) {
		try {
			files.sort(new Comparator<String>() {
				public int compare(String str1, String str2) {
					return extractInt(str1) - extractInt(str2);
				}

				int extractInt(String string) {
					// get the filename after the last "/" in the path and remove all non-digit
					// characters
					String number = string.substring(string.lastIndexOf("/")).replaceAll("\\D", "");
					// return 0 if no digits found
					return number.isEmpty() ? 0 : Integer.parseInt(number);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String removeLastChar(String str) {
		if (str != null && str.length() > 0) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	public static String getRepeatedString(String string, int repeats) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < repeats; i++)
			result.append(string);

		return result.toString();
	}

	public static String getFileExtension(String filename) {
		String extension = "";

		try {
			if (filename != null) {
				extension = filename.substring(filename.lastIndexOf("."));
			}
		} catch (Exception e) {
			extension = "";
		}

		return extension;
	}

	public static String getFileName(String filename) {
		File file = new File(filename);
		return(file.getName());
	}

	public static void abort() {
		System.exit(1);
	}

}
