/**
 * Copyright (C) 2014 BITPlan GmbH
 * 
 * Pater-Delp-Str. 1
 * D-47877 Willich-Schiefbahn
 *
 * http://www.bitplan.com
 */
package edsdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

/**
 * Handling command line tools
 * 
 * @author wf
 * 
 */
public class CmdLine {

	protected static boolean debug = false;
	protected static Logger LOGGER = Logger.getLogger("edsdk");

	/**
	 * helper class to check the operating system this Java VM runs in
	 * http://stackoverflow
	 * .com/questions/228477/how-do-i-programmatically-determine
	 * -operating-system-in-java compare to
	 * http://svn.terracotta.org/svn/tc/dso/tags
	 * /2.6.4/code/base/common/src/com/tc/util/runtime/Os.java
	 * http://www.docjar.com
	 * /html/api/org/apache/commons/lang/SystemUtils.java.html
	 */
	public static final class OsCheck {
		/**
		 * types of Operating Systems
		 */
		public enum OSType {
			Windows, MacOS, Linux, Other
		};

		public static OsCheck.OSType detectedOS;

		/**
		 * detected the operating system from the os.name System property and cache
		 * the result
		 * 
		 * @returns - the operating system detected
		 */
		public static OsCheck.OSType getOperatingSystemType() {
			if (detectedOS == null) {
				String OS = System.getProperty("os.name", "generic").toLowerCase();
				if (OS.indexOf("win") >= 0) {
					detectedOS = OSType.Windows;
				} else if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
					detectedOS = OSType.MacOS;
				} else if (OS.indexOf("nux") >= 0) {
					detectedOS = OSType.Linux;
				} else {
					detectedOS = OSType.Other;
				}
			}
			return detectedOS;
		}
	}

	/**
	 * get the command for the given tool
	 * 
	 * @param tool
	 *          the tool to get the command for
	 * @return
	 */
	public static String getCmd(String tool) {
		String result = "/usr/bin/" + tool;
		switch (OsCheck.getOperatingSystemType()) {
		case MacOS:
			if (!tool.equals("umask"))
				result = "/opt/local/bin/" + tool;
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * allows a stream result
	 * 
	 * @author wf
	 * 
	 */
	public static class StreamResult {
		public String cmd;
		public InputStream stream;
		public int exitCode;
		public String msg;
		public Process process;
		public File tmpDirectory;
		public String stdoutTxt;
		public String stderrTxt;
		public StreamGobbler errorGobbler;
		public File resultFile;
		public StreamGobbler outputGobbler;

		/**
		 * show me as debug information
		 */
		public void showDebug() {
			String result = "      cmd: '" + cmd + "'\n";
			result += "      msg: '" + msg + "'\n";
			result += "stdoutTxt: '" + stdoutTxt + "'\n";
			result += "stderrTxt: '" + stderrTxt + "'\n";
			LOGGER.log(Level.INFO, result);
		}
	}

	/**
	 * execute the given command
	 * 
	 * @param cmd
	 * @return
	 * @throws IOException
	 */
	public static Process execute(String[] cmd) throws IOException {
		return execute(cmd, null);
	}

	/**
	 * execute the given command
	 * @param shcmd
	 * @param envp
	 * @param dir
	 * @param processToKill
	 * @return
	 * @throws IOException
	 */
	public static Process execute(String[] shcmd, String[] envp, File dir, Process processToKill)
			throws IOException {
		java.lang.Runtime rt = java.lang.Runtime.getRuntime();
		if (processToKill != null)
			processToKill.destroy();
		Process newProcess = rt.exec(shcmd,envp,dir);
		return newProcess;
	}
	
	/**
	 * execute the given command
	 * @param shcmd
	 * @param processToKill
	 * @return
	 * @throws IOException
	 */
	public static Process execute(String[] shcmd,  Process processToKill) throws IOException {
		return execute(shcmd,null,null,processToKill);
	}

	/**
	 * get the Execution result of the given process
	 * 
	 * @param cmd
	 *          - the command to execute
	 * @param processToKill
	 *          - the process to kill (if any)
	 * @param stdout
	 *          - true if stdout should be grabbed as an InputStream
	 * @return - the execution result
	 * @throws IOException
	 * @throws Exception
	 */
	public static StreamResult getExecuteResult(String cmd,
			Process processToKill, ExecuteMode executeMode) throws Exception {
		return getExecuteResult(cmd, processToKill, executeMode, null,null, null);
	}

	public static enum ExecuteMode {
		Stdout, Gobble, Wait
	};

	/**
	 * get the Execution result of the given process
	 * 
	 * @param cmd
	 * @param processToKill
	 * @param stdout
	 *          - true if stdout should be grabbed as an InputStream
	 * @param expectedResultFile
	 *          TODO
	 * @param errorTrigger
	 *          TODO
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static StreamResult getExecuteResult(String cmd,
			Process processToKill, ExecuteMode executeMode, File tmpDirectory, File expectedResultFile,
			String errorTrigger) throws IOException, InterruptedException {
		StreamResult result = new StreamResult();
		result.cmd = cmd;
		result.msg = cmd;
		result.stderrTxt = "";
		result.stdoutTxt = "";
		if (debug)
		  LOGGER.log(Level.INFO, "exec: " + cmd);
		String[] shcmd = { "/bin/bash", "-c", cmd };
		// SystemCommandExecutor sce=SystemCommandExecutor.getExecutor(cmd);
		if (tmpDirectory == null)
			tmpDirectory = new File(System.getProperty("java.io.tmpdir"));
		result.process = execute(shcmd, null,tmpDirectory,processToKill);
		switch (executeMode) {
		case Stdout:
			// sce.executeCommand(false);
			// result.stream=sce.getStdInPipe();
			result.stream = result.process.getInputStream();
			if (debug)
				LOGGER.log(Level.INFO, "input stream for " + cmd + " accepted");
			result.exitCode = 0;
			break;
		case Gobble:
			result.errorGobbler = new 
      StreamGobbler(result.process,result.process.getErrorStream(), "ERROR",tmpDirectory);            
  
			// any output?
			result.outputGobbler = new 
      StreamGobbler(result.process,result.process.getInputStream(), "OUTPUT",tmpDirectory);
      
			// kick them off
			result.errorGobbler.start();
			result.outputGobbler.start();

			break;
		case Wait:
			result.exitCode = result.process.waitFor();
			// result.exitCode=sce.executeCommand(true);
			result.stdoutTxt = StringListToString(IOUtils.readLines(result.process
					.getInputStream(),"utf-8"));
			result.stderrTxt = StringListToString(IOUtils.readLines(result.process
					.getErrorStream(),"utf-8"));
			// String stdoutTxt = sce.getStandardOutputTextFromCommand();
			// String stderrTxt = sce.getStandardErrorTextFromCommand();
			boolean failed = result.exitCode != 0;
			if (errorTrigger != null)
				failed = failed && result.stderrTxt.contains(errorTrigger);
			if (failed) {
				// if stdErr was set and no exitCode ...
				if (result.exitCode == 0)
					result.exitCode = 1;
				result.msg += "\nstdout='" + result.stdoutTxt + "'";
				result.msg += "\nstderr='" + result.stderrTxt + "'";
			} else {
				if (expectedResultFile != null) {
					if (expectedResultFile.exists())
						result.resultFile = expectedResultFile;
					// FIXME - just deliver as File?
					result.stream = new FileInputStream(expectedResultFile);
				}
			}
		} // switch
		if (debug)
			result.showDebug();
		return result;
	}

	/**
	 * create a string from a list of strings
	 * 
	 * @param lines
	 * @return
	 */
	protected static String StringListToString(List<String> lines) {
		String result = "";
		for (String line : lines) {
			result += line + "\n";
		}
		return result;
	}

	protected static interface StreamListener {
		/**
		 * trigger newly read lines
		 * 
		 * @param the
		 *          source Gobbler
		 * @param line
		 */
		public void onRead(StreamGobbler source, String line);
	}

	/**
	 * StreamGobbler
	 * 
	 * @author wf
	 * 
	 */
	protected static class StreamGobbler extends Thread {
		boolean gobbleDebug=false;
		InputStream is;
		String type;
		Process process;
		List<StreamListener> listeners = new ArrayList<StreamListener>();
		private boolean running;
		File execDirectory;

		/**
		 * add a listener
		 * 
		 * @param listener
		 */
		public void addListener(StreamListener listener) {
			listeners.add(listener);
		}

		/**
		 * constructor
		 * @param process
		 * @param is
		 * @param type
		 * @param execDirectory 
		 */
		StreamGobbler(Process process,InputStream is, String type, File execDirectory) {
			this.process=process;
			this.is = is;
			this.type = type;
			this.execDirectory=execDirectory;
		}
		
		/**
		 * kill the process
		 */
		public void kill()  {
			running=false;
			// jdk 7
			// http://stackoverflow.com/questions/6356340/killing-a-process-using-java
			process.destroy();
			process.destroy();
			// FIXME jdk8
			//process.destroyForcibly();
			// LOGGER.log(Level.INFO,"process killed");
		}

		public void run() {
			running=true;
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while (running && ((line = br.readLine()) != null)) {
					if (gobbleDebug)
						System.out.println(type + ">" + line);
					for (StreamListener listener : listeners) {
						listener.onRead(this, line);
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
