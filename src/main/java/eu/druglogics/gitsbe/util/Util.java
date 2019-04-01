package eu.druglogics.gitsbe.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * A simple class that has static methods useful for the whole project
 * 
 * @author John
 */

public class Util {

	public static boolean environmentalVariableBNETisNULL() {
		if (System.getenv("BNET_HOME") == null) {
			System.out.println("Set environment variable BNET_HOME to point to location of BNReduction.sh");
			System.out.println("BNReduction can be obtained from https://github.com/alanavc/BNReduction");
			return true;
		}
		return false;
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
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(filename));

		try {
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

		finally {
			reader.close();
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
	public static boolean createDirectory(String directory, Logger logger) {
		File directoryFile = new File(directory);
		
		if (!directoryFile.mkdir()) {
			if (!directoryFile.exists()) {
				logger.error("Error in creating directory: " + directory + ", exiting.");
				return false;
			}
		}
		
		logger.outputStringMessage(1, "Created directory: " + directory);
		return true;
	}
	
	/**
	 * Create a directory based on the given path string. 
	 * If it already exists, the function call returns false.
	 * 
	 * @param directory
	 */
	public static boolean createDirectory(String directory) {
		File directoryFile = new File(directory);
		
		if (directoryFile.exists()) {
			System.out.println("ERROR: Directory: " + directory + " already exists!");
			return false;
		}
		
		if (!directoryFile.mkdir()) {
			if (!directoryFile.exists()) {
				 System.out.println("Error in creating directory: " + directory + ", exiting.");
				 return false;
			}
		}
		
		System.out.println("Created directory: " + directory);
		return true;
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
	 * Returns true if file is completely empty
	 * @param filename
	 * @return
	 */
	public static boolean isFileEmpty(String filename) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}     
		try {
			if (br.readLine() == null) {
			    return true;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return false;
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

		// Remove the path up to the filename.
		int lastSeparatorIndex = str.lastIndexOf(separator);
		if (lastSeparatorIndex == -1) {
			filename = str;
		} else {
			filename = str.substring(lastSeparatorIndex + 1);
		}

		// Remove the extension.
		int extensionIndex = filename.lastIndexOf(".");
		if (extensionIndex == -1)
			return filename;

		return filename.substring(0, extensionIndex);
	}

	public static void removeLineFromFile(String file, String lineToRemove) {
		try {
			File inFile = new File(file);

			if (!inFile.isFile()) {
				System.out.println("Parameter is not an existing file");
				return;
			}

			// Construct the new file that will later be renamed to the original filename.
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
			BufferedReader br = new BufferedReader(new FileReader(file));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;

			// Read from the original file and write to the new
			// unless content matches data to be removed.
			while ((line = br.readLine()) != null) {

				if (!line.trim().equals(lineToRemove)) {

					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			br.close();

			// Delete the original file
			if (!inFile.delete()) {
				System.out.println("Could not delete file");
				return;
			}

			// Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
				System.out.println("Could not rename file");

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Copies file to directory (both String variables should be the absolute pathnames)
	 * @param file
	 * @param directory
	 * @throws IOException
	 */
	public static void copyFileToDirectory(String file, String directory) throws IOException {
		String filename = new File(file).getName();
		String outputFile = new File(directory, filename).getAbsolutePath();
		
		PrintWriter writerOutput = new PrintWriter(outputFile);
		ArrayList<String> lines = readLinesFromFile(file, false);
		
		for (String line : lines) {
			writerOutput.println(line);
		}
		writerOutput.flush();
		writerOutput.close();
	}
	
	/**
	 * Duplicates file creating outputFile (both String variables should be the absolute pathnames)
	 * @param file
	 * @param outputFile
	 * @throws IOException
	 */
	public static void duplicateFile(String file, String outputFile) throws IOException {
		PrintWriter writerOutput = new PrintWriter(outputFile);
		ArrayList<String> lines = readLinesFromFile(file, false);
		
		for (String line : lines) {
			writerOutput.println(line);
		}
		writerOutput.flush();
		writerOutput.close();
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
			new File(file).delete();
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
			Collections.sort(files, new Comparator<String>() {
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

}
