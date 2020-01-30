package eu.druglogics.gitsbe.util;

import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

/**
 * A simple class that has static methods useful for the whole project
 * 
 * @author John
 */

public class Util {

	public static String replaceOperators(String equation) {
		return (equation
				.replace(" and ", " & ")
				.replace(" or ", " | ")
				.replace(" not ", " ! ")
				.replace(" true ", " 1 ")
				.replace(" false ", " 0 "));
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
	 * Merges all files to the mergedFile file and
	 * deletes them if deleteFiles is set to true
	 *
	 * @throws IOException
	 */
    public static void mergeFiles(ArrayList<String> files, String mergedFile, boolean deleteFiles)
            throws IOException {

	    ArrayList<File> files1 = new ArrayList<>();
	    for (String file: files) {
	        files1.add(new File(file));
        }

        FileWriter fileWriter;
        BufferedWriter out;

        fileWriter = new FileWriter(mergedFile, true);
        out = new BufferedWriter(fileWriter);

        for (File file : files1) {
            FileInputStream fileInputStream;
            fileInputStream = new FileInputStream(file);
            BufferedReader in = new BufferedReader(new InputStreamReader(fileInputStream));

            String line;
            while ((line = in.readLine()) != null) {
                out.write(line);
                out.newLine();
            }

            in.close();

            if (deleteFiles) {
                if (!file.delete()) {
                    throw new IOException("Couldn't delete file: " + file);
                }
            }
        }

        out.close();
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

	private static void deleteDirectory(File directory) throws IOException {
		for (File file : directory.listFiles()) {
			if (file.isDirectory())
				deleteDirectory(file);
			if (!file.delete())
				throw new IOException("Couldn't delete file: " + file);
		}

		if (!directory.delete())
			throw new IOException("Couldn't delete directory: " + directory);
	}

	public static void archive(String directoryLog, String directoryTmp) {
		File sourceLogDir = new File(directoryLog);
		File sourceTmpDir = new File(directoryTmp);
		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);

		// compress *_tmp dir and delete it afterwards
		if (sourceTmpDir.exists()) {
			String archiveNameTmp = getFileName(directoryTmp);
			File destinationTmpDir = sourceTmpDir.getParentFile();

			try {
				archiver.create(archiveNameTmp, destinationTmpDir, sourceTmpDir);
				deleteDirectory(sourceTmpDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// compress *.log files in `log` dir
		for (File logFile : sourceLogDir.listFiles()) {
			String logFileFullPath = logFile.getAbsolutePath();

			if (getFileExtension(logFileFullPath).equals(".log")) {
				String archiveNameLog = removeExtension(logFileFullPath);
				try {
					archiver.create(archiveNameLog, sourceLogDir, logFile);
					logFile.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static boolean isNumericString(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			Double.parseDouble(strNum);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * Converts a byte array to a combined String consisting of
	 * each byte's String representation
	 *
	 * @param binaryByteArray
	 */
	public static String byteArrayToString(byte[] binaryByteArray) {
		StringBuilder str = new StringBuilder();
		for(byte b: binaryByteArray) {
			str.append(String.valueOf(b));
		}

		return String.valueOf(str);
	}

	/**
	 * Returns true if the given string is all dashes: '-'
	 *
	 * @param str
	 */
	public static boolean isStringAllDashes(String str) {
		if (str.isEmpty()) return false;

		int countDashes = 0;

		for (Character c : str.toCharArray()) {
			if (c.equals('-'))
				countDashes++;
		}

		return countDashes == str.length();
	}

	/**
	 * Converts an {@link ArrayList} of {@link Float} values to a <i>float</i> array
	 *
	 * @param floats
	 */
	public static float[] convertFloats(ArrayList<Float> floats) {
		float[] result = new float[floats.size()];
		Iterator<Float> iterator = floats.iterator();
		for (int i = 0; i < result.length; i++) {
			result[i] = iterator.next();
		}
		return result;
	}

	public static void abort() {
		System.exit(1);
	}

}
