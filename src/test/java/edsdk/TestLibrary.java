package edsdk;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.VerRsrc.VS_FIXEDFILEINFO;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import edsdk.CmdLine.StreamResult;
import edsdk.api.BaseCanonCamera;

/**
 * test whether the Library is available
 * @author wf
 *
 */
public class TestLibrary  {

//  @Ignore("")
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
  
  @Test 
  public void testLoadversion(){
	  Assume.assumeTrue(Platform.isWindows());
	  String filePath = "EDSDK_64\\DLL\\EDSDK.dll";

      IntByReference dwDummy = new IntByReference();
      dwDummy.setValue(0);

      int versionlength =
              com.sun.jna.platform.win32.Version.INSTANCE.GetFileVersionInfoSize(
                      filePath, dwDummy);

      byte[] bufferarray = new byte[versionlength];
      Pointer lpData = new Memory(bufferarray.length);
      PointerByReference lplpBuffer = new PointerByReference();
      IntByReference puLen = new IntByReference();

      boolean fileInfoResult =
              com.sun.jna.platform.win32.Version.INSTANCE.GetFileVersionInfo(
                      filePath, 0, versionlength, lpData);

      boolean verQueryVal =
              com.sun.jna.platform.win32.Version.INSTANCE.VerQueryValue(
                      lpData, "\\", lplpBuffer, puLen);

      VS_FIXEDFILEINFO lplpBufStructure = new VS_FIXEDFILEINFO(lplpBuffer.getValue());
      lplpBufStructure.read();

      int v1 = (lplpBufStructure.dwFileVersionMS).intValue() >> 16;
      int v2 = (lplpBufStructure.dwFileVersionMS).intValue() & 0xffff;
      int v3 = (lplpBufStructure.dwFileVersionLS).intValue() >> 16;
      int v4 = (lplpBufStructure.dwFileVersionLS).intValue() & 0xffff;

      String libraryversion = String.valueOf(v1) + "." +
	          String.valueOf(v2) + "." +
	          String.valueOf(v3) + "." +
	          String.valueOf(v4);
	System.out.println(
              libraryversion);
	assertEquals("3.5.0.6404",libraryversion);
  }

}
