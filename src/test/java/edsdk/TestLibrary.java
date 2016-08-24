package edsdk;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import edsdk.CmdLine.StreamResult;
import edsdk.api.BaseCanonCamera;

/**
 * test whether the Library is available
 * @author wf
 *
 */
public class TestLibrary  {

  @Test
  public void testLibrary() throws Exception {
    BaseCanonCamera.initLibrary();
    File libFile = BaseCanonCamera.getEdSdkLibraryFile();
    assertTrue(BaseCanonCamera.libraryInfo.hint+libFile.getAbsolutePath()+" should exist",libFile.exists());
    if (Platform.isWindows()) {
      assertTrue("Crash protection is on by default for Windows ",Native.isProtected());
      // use Powershell to find version
      PowerShellResponse response = PowerShell.executeSingleCommand("(Get-Item "+libFile.getAbsolutePath()+").VersionInfo");
      // get results
      String version=response.getCommandOutput();
      System.out.println("Versions:" + version);
      // check for the DLL version that this has been tested with
      assertTrue("Your EDSDK DLL version is different then the version EDSDK4J has been tested with",version.contains("3.4.20.6404"));
    } else if (Platform.isMac()) {
      System.out.println("Checking "+BaseCanonCamera.libraryInfo.hint+" "+libFile.getAbsolutePath());
      String cmd="otool -L "+libFile.getAbsolutePath();
      StreamResult result=CmdLine.getExecuteResult(cmd, null, CmdLine.ExecuteMode.Wait);
      System.out.println(result.stdoutTxt);
      long binlength = libFile.length();
      System.out.println("The dylib has a size of "+binlength+" bytes");
      assertEquals("Version 3.4 of EDSDK is recommended which is tested by length here",1412960,binlength);
    }
  }

}
