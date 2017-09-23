package gitsbe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
	private String filenameSummary ;
	private String filenameOutput ;
	private String filenameErrors ;
	private String filenameWarnings ;
	private String directory ;
	
	
	// File writers
	private PrintWriter writerDebug ;
	private PrintWriter writerSummary ;
	private PrintWriter writerOutput ;
	private PrintWriter writerError ;
	private PrintWriter writerWarning ;
	
	private boolean consoleOutput = true ;
	private boolean debugMode = false;
	
	private int verbosity ;
	
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
	public Logger(String filenameOutput, String filenameSummary, String directory, int verbosity, boolean consoleOutput) throws IOException
	{
		
		// Initialize variables
		this.directory = directory ;
		this.filenameOutput = new File(directory, filenameOutput).getPath() ;
		this.filenameSummary = new File(directory, filenameSummary).getPath() ;
		
		// Initialize file writers
		this.writerOutput = new PrintWriter(this.filenameOutput) ;
		this.writerSummary = new PrintWriter(this.filenameSummary) ;
		this.writerError = new PrintWriter(this.filenameOutput + "_error.log");
		this.writerWarning = new PrintWriter(this.filenameOutput + "_warning.log");
		setVerbosity (verbosity) ;
		this.consoleOutput = consoleOutput ;
		
		output(1, "Logger started, logging to directory: " + this.directory);
	}
	
	public void finish ()
	{
		writerOutput.close();
		writerSummary.close();
		writerDebug.close();
		writerWarning.close();
		writerError.close();
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
	 * Output lines to specified file. Ignores verbosity-level
	 * 
	 * @param filename
	 * @param msg
	 * @throws FileNotFoundException
	 */
	public void output(String filename, String[] msg)
	{
		try {
			FileWriter fw = new FileWriter(filename, true);
		    BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter writer = new PrintWriter(bw);
			
			for (int i = 0; i < msg.length; i++)
			{
				writer.println(msg[i]);;
				writerOutput.println(msg[i]);
				
				if (consoleOutput) System.out.println (msg[i]) ;
			}
			writer.flush();
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	public void output (String filename, String msg) 
	{
		try {
			FileWriter fw = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter writer = new PrintWriter(bw);

			
			writer.println(msg);
			writerOutput.println(msg);
			
			if (consoleOutput) System.out.println (msg) ;
			
			writer.flush();
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * Write warning with prefix WARNING
	 * @param msg
	 */
	public void warning (String msg)
	{
		writerWarning.write("WARNING: " + msg);
		writerWarning.flush();
		writerOutput.write("\tWARNING: " + msg);
		writerOutput.flush();
		
		if (consoleOutput) System.out.println ("\n\tWARNING: " + msg + "\n") ;

	}
	
	/**
	 * Write error with prefix ERROR
	 * 
	 * @param msg
	 */
	public void error (String msg)
	{
		writerError.write("ERROR: " + msg);
		writerError.flush();
		writerOutput.println("\tERROR: " + msg);
		writerOutput.flush();
		
		if (consoleOutput) System.out.println ("\n\tERROR: " + msg + "\n") ;

	}
	
	/**
	 * Extra output to log if debug mode is active
	 * 
	 * @param msg
	 */
	public void debug (String msg)
	{
		if (debugMode)
			output(1, "DEBUG: " + msg);
	}
	
	/**
	 * Set debug mode
	 * 
	 * @param mode
	 */
	public void setDebug (boolean mode)
	{
		debugMode = mode ;
	}
	
}
