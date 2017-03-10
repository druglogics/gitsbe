package gitsbe;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * A (very) simple logger
 * 
 * @author Asmund Flobak
 * 
 * email: asmund.flobak@ntnu.no
 *
 */
public class Logger {

	// Filenames of output files
	private String filenameDebug ;
	private String filenameSummary ;
	private String filenameOutput ;
//	private String filenameErrors ;
	private String directory ;
	
	
	
	// File writers
	private PrintWriter writerDebug ;
	private PrintWriter writerSummary ;
	private PrintWriter writerOutput ;
//	private PrintWriter writerErrors ;
	
	
	private boolean consoleOutput = true ;
	private boolean debugMode = false ;
	
	private int verbosity ;
	
	private boolean isInitialized = false ;
	
	
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
	public Logger(String filenameOutput, String filenameSummary, String filenameDebug, String directory, int verbosity, boolean debugMode, boolean consoleOutput) throws IOException
	{
		
		// Initialize variables
		this.directory = directory ;
		this.filenameOutput = new File(directory, filenameOutput).getPath() ;
		this.filenameSummary = new File(directory, filenameSummary).getPath() ;
		this.filenameDebug = new File(directory, filenameDebug).getPath() ;
		
		// Initialize file writers
		this.writerOutput = new PrintWriter(filenameOutput) ;
		this.writerSummary = new PrintWriter(filenameSummary) ;
		this.writerDebug = new PrintWriter(filenameDebug) ;
		
		setVerbosity (verbosity) ;
		this.debugMode = debugMode ;
		this.consoleOutput = consoleOutput ;
		
		this.isInitialized = true ;
	}
	
	public void finish ()
	{
		writerOutput.close();
		writerSummary.close();
		writerDebug.close();
	}
	
	
	/**
	 * Set level of verbosity, 0 = silent, 1 = some, 2 = more, 3 = all
	 * 
	 * @param verbosity
	 */
	public void setVerbosity (int verbosity)
	{
		if (verbosity >= 0 && verbosity <= 3)
		{
			this.verbosity = verbosity ;
		}
		else 
		{
			
		}
	}
	
	/**
	 * 
	 * @return current verbosity level
	 */
	public int getVerbosity ()
	{
		return verbosity ;
	}
	
	/**
	 * Output several lines
	 * 
	 * @param verbosity
	 * @param msg
	 */
	public void output(int verbosity, String[] msg)
	{
		if (verbosity <= verbosity)
		{
			for (int i = 0; i < msg.length; i++)
			{
				writerOutput.println(msg[i]);;
				
				if (consoleOutput) System.out.println (msg[i]) ;
			}
			writerOutput.flush();
			
		}
	}
	
	
	/**
	 * Output message
	 * 
	 * @param verbosity 
	 * @param msg
	 */
	public void output(int verbosity, String msg)
	{
		if (verbosity <= this.verbosity)
		{
			writerOutput.println (msg);
			writerOutput.flush();
			
			if (consoleOutput) System.out.println (msg) ;
		}
	}
	
	/**
	 * Output section header
	 * 
	 * @param verbosity
	 * @param msg
	 */
	public void outputHeader(int verbosity, String msg)
	{
		if (verbosity <= this.verbosity)
		{
			writerOutput.println("\n" + msg);
			writerOutput.println(dashes(msg.length()));
			
			if (consoleOutput) 
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
	private String dashes(int length)
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
	public void summary (String msg)
	{
		writerSummary.println(msg);	
		writerSummary.flush();
		
		if (consoleOutput) System.out.println (msg) ;

	}
	
	
	/**
	 * Write debug info
	 * 
	 * @param msg
	 */
	public void debug (String msg)
	{
		writerDebug.println(msg);
		writerDebug.flush();	
		
		if (consoleOutput) System.out.println (msg) ;

	}
	
	/**
	 * Set debug mode
	 * 
	 * @param debugMode boolean, true = log debug info, false = don't log, default
	 */
	public void setDebugMode (boolean debugMode)
	{
		debugMode = debugMode ;
	}
	
	

}
