package gitsbe;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * A simple logger
 * 
 * @author Asmund Flobak
 *  
 * email: asmund.flobak@ntnu.no
 *
 */
public class Logger {

	// Filenames of output files
	private static String filenameDebug ;
	private static String filenameSummary ;
	private static String filenameOutput ;
//	private static String filenameErrors ;
	private static String directory ;
	
	
	
	// File writers
	private static PrintWriter writerDebug ;
	private static PrintWriter writerSummary ;
	private static PrintWriter writerOutput ;
//	private static PrintWriter writerErrors ;
	
	
	private static boolean consoleOutput = true ;
	private static boolean debugMode = false ;
	
	private static int verbosity ;
	
	private static boolean isInitialized = false ;
	
	
//	public Logger() {
//		// TODO Auto-generated constructor stub
//	}

	
	/**
	 * Initialize logger
	 * 
	 * @param filenameOuput name of output file. Verbosity decides amount of output.
	 * @param filenameSummary name of summary file.
	 * @param filenameDebug name of debug file.
	 * @param directory is name of directory for output.
	 * @param verbosity sets level of verbosity: 0 is nothing at all, 1 = most important info, 2 = less important, 3 = everything
	 * @throws IOException
	 */
	public static void initialize (String filenameOutput, String filenameSummary, String filenameDebug, String directory, int verbosity, boolean debugMode, boolean consoleOutput) throws IOException
	{
		
		// Initialize variables
		Logger.directory = directory ;
		Logger.filenameOutput = new File(directory, filenameOutput).getPath() ;
		Logger.filenameSummary = new File(directory, filenameSummary).getPath() ;
		Logger.filenameDebug = new File(directory,  filenameDebug).getPath() ;
//		Logger.filenameErrors = new File (directory, filenameErrors).getPath() ;
		
		// Initialize file writers
		Logger.writerOutput = new PrintWriter(Logger.filenameOutput) ;
		Logger.writerSummary = new PrintWriter(Logger.filenameSummary) ;
		Logger.writerDebug = new PrintWriter(Logger.filenameDebug) ;
//		Logger.writerErrors = new PrintWriter (filenameErrors) ;
		
		setVerbosity (verbosity) ;
		Logger.debugMode = debugMode ;
		Logger.consoleOutput = consoleOutput ;
		
		Logger.isInitialized = true ;
	}
	
	public static void finish ()
	{
		writerOutput.close();
		writerSummary.close();
		writerDebug.close();
//		writerErrors.close();
	}
	
	
	/**
	 * Set level of verbosity, 0 = silent, 1 = some, 2 = more, 3 = all
	 * 
	 * @param verbosity
	 */
	public static void setVerbosity (int verbosity)
	{
		if (verbosity >= 0 && verbosity <= 3)
		{
			Logger.verbosity = verbosity ;
		}
		else 
		{
			
		}
	}
	
	/**
	 * 
	 * @return current verbosity level
	 */
	public static int getVerbosity ()
	{
		return Logger.verbosity ;
	}
	
	/**
	 * Output several lines
	 * 
	 * @param verbosity
	 * @param msg
	 */
	public static void output(int verbosity, String[] msg)
	{
		if (verbosity <= Logger.verbosity)
		{
			for (int i = 0; i < msg.length; i++)
			{
				Logger.writerOutput.println(msg[i]);;
				
				if (Logger.consoleOutput) System.out.println (msg[i]) ;
			}
			Logger.writerOutput.flush();
			
		}
	}
	
	
	/**
	 * Output message
	 * 
	 * @param verbosity 
	 * @param msg
	 */
	public static void output(int verbosity, String msg)
	{
		if (verbosity <= Logger.verbosity)
		{
			Logger.writerOutput.println (msg);
			Logger.writerOutput.flush();
			
			if (Logger.consoleOutput) System.out.println (msg) ;
		}
	}
	
	/**
	 * Output section header
	 * 
	 * @param verbosity
	 * @param msg
	 */
	public static void outputHeader(int verbosity, String msg)
	{
		if (verbosity <= Logger.verbosity)
		{
			Logger.writerOutput.println("\n" + msg);
			Logger.writerOutput.println(dashes(msg.length()));
			
			if (Logger.consoleOutput) 
			{
				System.out.println ("\n" + msg) ;
				System.out.println (dashes(msg.length())) ;
			}

		}
	}
	
	/**
	 * Get string with specified number of dashes
	 * 
	 * @param length
	 * @return
	 */
	private static String dashes(int length)
	{
		char[] dashes = new char[length];
		Arrays.fill(dashes, '-');
		return new String (dashes);
	}
	
	/**
	 * Write summary
	 * 
	 * @param msg
	 */
	public static void summary (String msg)
	{
		Logger.writerSummary.println(msg);	
		Logger.writerSummary.flush();
		
		if (Logger.consoleOutput) System.out.println (msg) ;

	}
	
	
	/**
	 * Write debug info
	 * 
	 * @param msg
	 */
	public static void debug (String msg)
	{
		Logger.writerDebug.println(msg);
		Logger.writerDebug.flush();	
		
		if (Logger.consoleOutput) System.out.println (msg) ;

	}
	
	
//	/**
//	 * Report error, to log file and stderr
//	 * 
//	 * @param msg
//	 */
//	public static void error (String msg)
//	{
//		Logger.writerErrors.println(msg);
//		Logger.writerErrors.flush();		
//	}
	
	/**
	 * Set debug mode
	 * 
	 * @param debugMode boolean, true = log debug info, false = don't log, default
	 */
	public static void setDebugMode (boolean debugMode)
	{
		Logger.debugMode = debugMode ;
	}
	
	

}
