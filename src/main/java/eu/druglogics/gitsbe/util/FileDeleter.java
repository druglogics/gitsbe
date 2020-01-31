package eu.druglogics.gitsbe.util;

import java.io.File;

public class FileDeleter {
	
	// This is the directory fileDeleter is associated with
	private static String directory;
	private static boolean state;
	
	public FileDeleter(String directory) {
		FileDeleter.directory = directory;
		disable();
	}

	public void activate() {
		setState(true);
	}

	public void disable() {
		setState(false);
	}

	public void setState(boolean state) {
		FileDeleter.state = state;
	}

	public static boolean isActive() {
		return state;
	}

	public static void deleteFilesMatchingPattern(Logger logger, String pattern) {
		if (isActive()) {
			File dir = new File(directory);

			for (File file : dir.listFiles()) {
				String filename = file.getName();
				if (filename.toLowerCase().contains(pattern.toLowerCase())) {
					logger.outputStringMessage(2, "Deleting file: "
							+ file.getName());
					if (!file.delete())
						logger.error("Couldn't delete file: " + filename);
				}
			}
		}
	}

	/**
	 * Deletes the directory associated with the fileDeleter and all its contents
	 * only if the fileDeleter is "activated"
	 * 
	 * @param logger
	 */
	public static void cleanDirectory(Logger logger) {
		if (isActive()) {
			File dir = new File(directory);
			logger.outputStringMessage(2, "\n" +
					"Deleting temporary directory: " + dir.getAbsolutePath());
			deleteFilesFromDirectory(dir, logger);
			if (!dir.delete())
				logger.error("Couldn't delete directory: " + directory);
		}
	}

	/**
	 * Deletes all files (and directories recursively) from the specified directory
	 * 
	 * @param directory
	 */
	private static void deleteFilesFromDirectory(File directory, Logger logger) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory())
				deleteFilesFromDirectory(file, logger);
			if (!file.delete())
				logger.error("Couldn't delete file: " + file);
		}
	}

}