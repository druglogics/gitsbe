package gitsbe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
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

	public static void deleteFilesFromDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				deleteFilesFromDirectory(file);
			file.delete();
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

	public static boolean createDirectory(String directory, Logger logger) {
		if (!new File(directory).mkdir()) {
			if (!new File(directory).exists()) {
				logger.outputStringMessage(1, "ERROR creating folder: " + directory + ", exiting.");
				return false;
			}
		}
		return true;
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

	public static void copy(File sourceLocation, File targetLocation) throws IOException {
		if (sourceLocation.isDirectory()) {
			copyDirectory(sourceLocation, targetLocation);
		} else {
			copyFile(sourceLocation, targetLocation);
		}
	}

	public static void copyDirectory(File source, File target) throws IOException {
		if (!target.exists()) {
			target.mkdir();
		}

		for (String f : source.list()) {
			copy(new File(source, f), new File(target, f));
		}
	}

	public static void copyFile(File source, File target) throws IOException {
		try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(target)) {
			byte[] buf = new byte[1024];
			int length;
			while ((length = in.read(buf)) > 0) {
				out.write(buf, 0, length);
			}
		}
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

	// May not be working...
	public static void compressDirectory(String filenameArchive, String directory, Logger logger) {
		// tar cvfz tmp.tar.gz tmp

		try {
			ProcessBuilder pb = new ProcessBuilder("tar", "cvfz", filenameArchive, "-C",
					new File(directory).getParent(), new File(directory).getName());

			if (logger.getVerbosity() >= 3) {
				pb.redirectErrorStream(true);
				pb.redirectOutput();
			}

			logger.outputStringMessage(3, "Compressing temporary models: " + filenameArchive);

			Process p;
			p = pb.start();

			try {
				p.waitFor();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while (r.ready()) {
				logger.outputStringMessage(3, r.readLine());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}